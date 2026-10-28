package com.example.engine

import com.example.model.*
import kotlin.random.Random

object NebulaSimulationEngine {

    // Monotonic sequence for unique AI fleet IDs
    private var aiFleetSeq = 0

    // Advance simulation by 1 turn
    fun step(state: GameState) {
        state.ticks += 1
        state.stardate += 0.1f

        // 1. Economy Loop
        processEconomy(state)

        // 2. AI Faction Actions (Fleet construction, Tech research, Diplomacy)
        processAIActions(state)

        // 3. Fleet Movement & Tactical Clashes
        processFleetMovement(state)

        // 4. Random Galactic Events
        processRandomEvents(state)

        // 5. Dynamic Market Price Fluctuations
        processMarketPrices(state)

        // 6. Check Achievements
        checkAchievements(state)

        // 7. Check Victory Conditions
        if (!state.victoryAchieved && !state.sandboxMode) {
            checkVictory(state)
        }
    }

    private fun processEconomy(state: GameState) {
        for ((_, system) in state.systems) {
            val owner = system.owner ?: continue
            val faction = state.factions[owner] ?: continue

            val econMult = if (faction.tech.contains("economy")) 1.25f else 1.0f
            val income = ((system.economy * 0.15f + system.population * 0.05f) * econMult).toInt().coerceAtLeast(1)
            faction.credits += income

            // Fleet upkeep
            val fleetCount = state.fleets.count { it.owner == owner }
            faction.credits = (faction.credits - (fleetCount * 3)).coerceAtLeast(0)
        }
    }

    private fun processAIActions(state: GameState) {
        for ((factionId, faction) in state.factions) {
            if (factionId == state.playerFactionId && state.mode == "strategy") continue

            // 1. Build fleets if rich enough
            if (faction.credits >= 120 && faction.systems.isNotEmpty()) {
                val spawnSystem = faction.capital ?: faction.systems.first()
                val type = when {
                    faction.personality.aggression > 0.6f -> FleetType.ASSAULT
                    faction.personality.defenseFocus > 0.6f -> FleetType.BATTLESHIP
                    else -> FleetType.FIGHTER
                }
                faction.credits -= type.cost
                state.fleets.add(
                    Fleet(
                        id = "ai_fleet_${state.ticks}_${++aiFleetSeq}",
                        owner = factionId,
                        location = spawnSystem,
                        strength = type.strength.toFloat(),
                        speed = type.speed,
                        type = type,
                        role = if (faction.personality.aggression > 0.5f) "attack" else "defense"
                    )
                )
            }

            // 2. Research tech if credits permit
            if (faction.credits >= 200) {
                val availableTech = TechCatalog.TECHS.map { it.id }.filterNot { faction.tech.contains(it) }
                if (availableTech.isNotEmpty()) {
                    val pick = availableTech.random()
                    val node = TechCatalog.get(pick)
                    if (node != null && faction.credits >= node.cost) {
                        faction.credits -= node.cost
                        faction.tech.add(pick)
                        if (pick == "laser") faction.military += 0.2f
                    }
                }
            }
        }
    }

    private fun processFleetMovement(state: GameState) {
        val fleetsToMove = state.fleets.filter { it.role == "attack" && it.owner != state.playerFactionId }
        for (fleet in fleetsToMove) {
            if (Random.nextFloat() > 0.4f) continue // movement interval
            val currentSys = state.systems[fleet.location] ?: continue
            val neighbors = currentSys.neighbors
            if (neighbors.isEmpty()) continue

            // Pick unowned or enemy neighbor
            val targets = neighbors.filter { nId ->
                val nSys = state.systems[nId]
                nSys != null && nSys.owner != fleet.owner
            }
            val target = if (targets.isNotEmpty()) targets.random() else neighbors.random()
            fleet.location = target

            // Resolve conquest if system is empty or rival
            val targetSys = state.systems[target]
            if (targetSys != null) {
                val defenderFleets = state.fleets.filter { it.location == target && it.owner != fleet.owner }
                if (defenderFleets.isEmpty() && targetSys.owner != fleet.owner) {
                    // System conquered
                    val oldOwner = targetSys.owner
                    if (oldOwner != null) {
                        state.factions[oldOwner]?.systems?.remove(target)
                    }
                    targetSys.owner = fleet.owner
                    state.factions[fleet.owner]?.systems?.add(target)
                    state.events.add(
                        0,
                        GalacticEvent(
                            id = "conq_${state.ticks}_${fleet.id}",
                            tick = state.ticks,
                            titleKey = "System Annexed",
                            messageKey = "Forces of ${state.factions[fleet.owner]?.name ?: fleet.owner} seized control of ${targetSys.id}!",
                            systemId = target,
                            icon = "🚩"
                        )
                    )
                } else if (defenderFleets.isNotEmpty()) {
                    // Battle clash
                    resolveFleetClash(state, fleet, defenderFleets.first())
                }
            }
        }
    }

    fun resolveFleetClash(state: GameState, attacker: Fleet, defender: Fleet): String {
        val attBonus = if (state.factions[attacker.owner]?.tech?.contains("laser") == true) 1.2f else 1.0f
        val defBonus = if (state.factions[defender.owner]?.tech?.contains("shield") == true) 1.3f else 1.0f

        val attRoll = (attacker.strength * attBonus * Random.nextFloat() * 1.5f).toInt()
        val defRoll = (defender.strength * defBonus * Random.nextFloat() * 1.5f).toInt()

        val attackerWon = attRoll >= defRoll
        val winner = if (attackerWon) attacker else defender
        val loser = if (attackerWon) defender else attacker

        loser.strength = (loser.strength - (if (attackerWon) defRoll * 0.4f else attRoll * 0.4f)).coerceAtLeast(0f)
        if (loser.strength <= 5f) {
            state.fleets.remove(loser)
        }

        winner.strength = (winner.strength - 3f).coerceAtLeast(5f)

        state.factions[winner.owner]?.stats?.let { it.wins += 1; it.battles += 1 }
        state.factions[loser.owner]?.stats?.let { it.losses += 1; it.battles += 1 }

        if (winner.owner == state.playerFactionId) {
            unlockAchievement(state, "first_blood")
        }

        val resultMsg = "Naval Clash at ${winner.location}: ${state.factions[winner.owner]?.name} defeated ${state.factions[loser.owner]?.name} ($attRoll vs $defRoll)!"
        state.events.add(
            0,
            GalacticEvent(
                id = "clash_${state.ticks}",
                tick = state.ticks,
                titleKey = "Space Battle",
                messageKey = resultMsg,
                systemId = winner.location,
                icon = "💥"
            )
        )
        return resultMsg
    }

    private fun processRandomEvents(state: GameState) {
        if (Random.nextFloat() > 0.12f) return // 12% chance per tick

        val randomSystem = state.systems.values.randomOrNull() ?: return
        val eventTypes = listOf(
            "supernova" to ("Supernova Pulse" to "A massive solar eruption at ${randomSystem.id} reduced local infrastructure."),
            "artifact" to ("Ancient Relic Unearthed" to "Archaeologists on ${randomSystem.id} discovered precursor archives (+100 credits)."),
            "alien" to ("First Contact" to "Unidentified alien envoys arrived in ${randomSystem.id} opening technological exchange."),
            "storm" to ("Ion Storm" to "Hyperspace navigation surrounding ${randomSystem.id} disrupted by electromagnetic turbulence."),
            "pirates" to ("Pirate Incursion" to "Corsair raiders emerged near ${randomSystem.id} demanding protection tithes!"),
            "breakthrough" to ("Scientific Breakthrough" to "Galactic researchers made a propulsion breakthrough (+10% fleet speed).")
        )

        val (type, info) = eventTypes.random()
        val (title, msg) = info

        when (type) {
            "artifact" -> {
                randomSystem.owner?.let { owner ->
                    state.factions[owner]?.let { it.credits += 100 }
                }
            }
            "pirates" -> {
                state.fleets.add(
                    Fleet(
                        id = "pirate_${state.ticks}",
                        owner = "pirates",
                        location = randomSystem.id,
                        strength = 15f,
                        speed = 1.5f,
                        type = FleetType.PIRATE,
                        role = "attack"
                    )
                )
            }
            "supernova" -> {
                randomSystem.population = (randomSystem.population * 0.85f).toInt()
            }
        }

        state.events.add(
            0,
            GalacticEvent(
                id = "rand_ev_${state.ticks}",
                tick = state.ticks,
                titleKey = title,
                messageKey = msg,
                systemId = randomSystem.id,
                icon = when (type) {
                    "supernova" -> "☀️"
                    "artifact" -> "🔮"
                    "pirates" -> "☠️"
                    "breakthrough" -> "🔬"
                    else -> "📡"
                }
            )
        )
    }

    private fun processMarketPrices(state: GameState) {
        for ((_, sys) in state.systems) {
            sys.prices["ore"] = ((sys.prices["ore"] ?: 10) + Random.nextInt(-1, 2)).coerceIn(4, 25)
            sys.prices["food"] = ((sys.prices["food"] ?: 8) + Random.nextInt(-1, 2)).coerceIn(3, 20)
            sys.prices["tech"] = ((sys.prices["tech"] ?: 25) + Random.nextInt(-2, 3)).coerceIn(12, 45)
        }
    }

    private fun checkAchievements(state: GameState) {
        val playerFaction = state.getPlayerFaction()

        if (playerFaction != null) {
            if (playerFaction.systems.size >= 10) unlockAchievement(state, "empire_builder")
            if (playerFaction.credits >= 5000) unlockAchievement(state, "tycoon")
            if (playerFaction.tech.size >= 3) unlockAchievement(state, "technologist")
            if (playerFaction.stats.wins >= 5) unlockAchievement(state, "conqueror")
        }

        if (state.playerShip.kills >= 5) unlockAchievement(state, "pirate_hunter")
        if (state.ticks >= 100) unlockAchievement(state, "survivor")
    }

    private fun checkVictory(state: GameState) {
        val totalSystems = state.systems.size

        for ((id, faction) in state.factions) {
            // 1. Domination (>= 75% of systems)
            if (faction.systems.size.toFloat() / totalSystems >= 0.75f) {
                state.victoryAchieved = true
                state.winnerFactionId = id
                state.victoryType = "domination"
                if (id == state.playerFactionId) unlockAchievement(state, "victor")
                return
            }

            // 2. Economic (>= 20,000 credits)
            if (faction.credits >= 20000) {
                state.victoryAchieved = true
                state.winnerFactionId = id
                state.victoryType = "economic"
                if (id == state.playerFactionId) unlockAchievement(state, "victor")
                return
            }
        }

        // 3. Military (All other factions eliminated)
        val activeFactions = state.factions.filter { (_, f) -> f.systems.isNotEmpty() || state.fleets.any { fl -> fl.owner == f.id } }
        if (activeFactions.size == 1) {
            val soleWinner = activeFactions.keys.first()
            state.victoryAchieved = true
            state.winnerFactionId = soleWinner
            state.victoryType = "military"
            if (soleWinner == state.playerFactionId) unlockAchievement(state, "victor")
        }
    }

    fun unlockAchievement(state: GameState, id: String) {
        val ach = state.achievements.firstOrNull { it.id == id }
        if (ach != null && !ach.unlocked) {
            ach.unlocked = true
            ach.unlockedAt = System.currentTimeMillis()
            state.events.add(
                0,
                GalacticEvent(
                    id = "ach_${state.ticks}_$id",
                    tick = state.ticks,
                    titleKey = "Achievement Unlocked!",
                    messageKey = "${ach.icon} ${ach.id.replace('_', ' ').uppercase()}",
                    icon = "🏆"
                )
            )
        }
    }

    // --- 4X Player Actions ---

    fun buildFleet(state: GameState, systemId: String, type: FleetType): Boolean {
        val faction = state.getPlayerFaction() ?: return false
        val system = state.systems[systemId] ?: return false

        if (system.owner != faction.id) return false
        if (faction.credits < type.cost) return false

        faction.credits -= type.cost
        val fleet = Fleet(
            id = "player_fleet_${state.ticks}_${Random.nextInt(100, 999)}",
            owner = faction.id,
            location = systemId,
            strength = type.strength.toFloat(),
            speed = type.speed,
            type = type,
            role = "defense"
        )
        state.fleets.add(fleet)
        return true
    }

    fun moveFleet(state: GameState, fleetId: String, destinationId: String): Boolean {
        val fleet = state.fleets.firstOrNull { it.id == fleetId && it.owner == state.playerFactionId } ?: return false
        val currentSys = state.systems[fleet.location] ?: return false

        if (!currentSys.neighbors.contains(destinationId)) return false
        fleet.location = destinationId

        // Check if landing on unowned or enemy system
        val destSys = state.systems[destinationId]
        if (destSys != null && destSys.owner != fleet.owner) {
            val enemyFleets = state.fleets.filter { it.location == destinationId && it.owner != fleet.owner }
            if (enemyFleets.isEmpty()) {
                val oldOwner = destSys.owner
                if (oldOwner != null) state.factions[oldOwner]?.systems?.remove(destinationId)
                destSys.owner = fleet.owner
                state.factions[fleet.owner]?.systems?.add(destinationId)
                state.events.add(
                    0,
                    GalacticEvent(
                        id = "annex_${state.ticks}",
                        tick = state.ticks,
                        titleKey = "Territory Secured",
                        messageKey = "Your armada took control of ${destSys.id}.",
                        systemId = destinationId,
                        icon = "🚩"
                    )
                )
            } else {
                resolveFleetClash(state, fleet, enemyFleets.first())
            }
        }
        return true
    }

    fun researchTech(state: GameState, techId: String): Boolean {
        val faction = state.getPlayerFaction() ?: return false
        val node = TechCatalog.get(techId) ?: return false

        if (faction.tech.contains(techId)) return false
        if (faction.credits < node.cost) return false

        faction.credits -= node.cost
        faction.tech.add(techId)
        if (techId == "laser") faction.military += 0.2f
        checkAchievements(state)
        return true
    }

    fun proposePeace(state: GameState, targetFactionId: String): String {
        val rel = state.diplomacy[state.playerFactionId]?.get(targetFactionId) ?: return "No diplomatic channel."
        rel.relation = (rel.relation + 30).coerceAtMost(100)
        rel.trust = (rel.trust + 20).coerceAtMost(100)
        rel.status = RelationStatus.fromRelation(rel.relation)

        state.diplomacy[targetFactionId]?.get(state.playerFactionId)?.let {
            it.relation = rel.relation
            it.trust = rel.trust
            it.status = rel.status
        }

        if (rel.status == RelationStatus.ALLY) {
            unlockAchievement(state, "diplomat")
        }
        return "Peace treaty signed with ${state.factions[targetFactionId]?.name}! Relations normalized."
    }

    fun spyAction(state: GameState, targetFactionId: String): String {
        val player = state.getPlayerFaction() ?: return "Error"
        if (player.credits < 10) return "Not enough credits for espionage!"
        player.credits -= 10

        val target = state.factions[targetFactionId] ?: return "Target faction unavailable."
        val fleets = state.fleets.count { it.owner == targetFactionId }
        val systems = target.systems.size

        return "INTEL REPORT: ${target.name}\nTreasury: ${target.credits} cr. | Fleets: $fleets | Controlled Systems: $systems | Military Rating: ${String.format("%.1f", target.military)}"
    }

    // --- RPG Player Starship Actions ---

    fun rpgBuyItem(state: GameState, item: String, amount: Int): Boolean {
        val ship = state.playerShip
        val currentSys = state.systems[ship.location] ?: return false
        val price = currentSys.prices[item] ?: return false
        val totalCost = price * amount

        if (ship.credits < totalCost) return false
        if (ship.availableCargo < amount) return false

        ship.credits -= totalCost
        ship.cargo[item] = (ship.cargo[item] ?: 0) + amount
        return true
    }

    fun rpgSellItem(state: GameState, item: String, amount: Int): Boolean {
        val ship = state.playerShip
        val currentSys = state.systems[ship.location] ?: return false
        val currentCargo = ship.cargo[item] ?: 0
        if (currentCargo < amount) return false

        val price = currentSys.prices[item] ?: return false
        val totalRevenue = price * amount

        ship.credits += totalRevenue
        ship.cargo[item] = currentCargo - amount

        // Check if fulfilling delivery quest
        for (q in state.quests.filter { it.status == QuestStatus.ACTIVE && it.type == QuestType.DELIVERY }) {
            if (q.destination == ship.location && q.item == item) {
                q.status = QuestStatus.COMPLETED
                ship.credits += q.reward + q.bonus
                ship.reputation += 10
                state.events.add(
                    0,
                    GalacticEvent(
                        id = "q_comp_${state.ticks}",
                        tick = state.ticks,
                        titleKey = "Contract Complete",
                        messageKey = "Delivery to ${q.destination} fulfilled! Earned ${q.reward + q.bonus} credits.",
                        icon = "📦"
                    )
                )
            }
        }
        return true
    }

    fun rpgTravel(state: GameState, destinationId: String): Pair<Boolean, String> {
        val ship = state.playerShip
        val currentSys = state.systems[ship.location] ?: return false to "Current location invalid"

        if (!currentSys.neighbors.contains(destinationId)) {
            return false to "No direct hyperlane to $destinationId"
        }

        ship.location = destinationId
        step(state) // travel takes 1 game turn

        // Check exploration quest
        for (q in state.quests.filter { it.status == QuestStatus.ACTIVE && it.type == QuestType.EXPLORE }) {
            if (q.destination == destinationId) {
                q.status = QuestStatus.COMPLETED
                ship.credits += q.reward
                ship.reputation += 15
            }
        }

        // Random chance of pirate encounter in deep space
        val encounter = Random.nextFloat() < 0.25f
        return true to if (encounter) "Hyperspace jump complete. WARNING: Pirate raider on radar!" else "Hyperspace jump to $destinationId successful."
    }

    fun rpgWormhole(state: GameState): Pair<Boolean, String> {
        val ship = state.playerShip
        val wormholeTarget = GalaxyGenerator.WORMHOLES[ship.location]
            ?: return false to "No wormhole singularity detected in ${ship.location}."

        ship.location = wormholeTarget
        step(state)

        return true to "Sub-space anomaly traversed! Emerge at $wormholeTarget."
    }

    fun rpgUpgrade(state: GameState, type: String): Pair<Boolean, String> {
        val ship = state.playerShip
        return when (type) {
            "cargo" -> {
                if (ship.credits < 50) return false to "Need 50 credits"
                ship.credits -= 50
                ship.cargoLimit += 5
                true to "Cargo hold expanded (+5 slots)"
            }
            "hull" -> {
                if (ship.credits < 70) return false to "Need 70 credits"
                ship.credits -= 70
                ship.hullMax += 20
                ship.hull = ship.hullMax
                true to "Hull reinforced (+20 HP & fully repaired)"
            }
            "weapon" -> {
                if (ship.credits < 100) return false to "Need 100 credits"
                ship.credits -= 100
                ship.weapon += 1
                true to "Laser cannon upgraded to Tier ${ship.weapon}"
            }
            "shield" -> {
                if (ship.credits < 120) return false to "Need 120 credits"
                ship.credits -= 120
                ship.shield += 0.05f
                true to "Deflector shields boosted (+5% deflection)"
            }
            else -> false to "Unknown upgrade"
        }
    }

    fun rpgFightPirate(state: GameState): Triple<Boolean, Boolean, String> {
        // Returns: (Victory, Escaped, Details)
        val ship = state.playerShip
        val pirateHp = Random.nextInt(35, 70)
        val shipDamage = (ship.weapon * 18 * (1f + Random.nextFloat() * 0.5f)).toInt()
        val pirateDamage = ((Random.nextInt(15, 30) * (1f - ship.shield)).toInt()).coerceAtLeast(5)

        ship.hull = (ship.hull - pirateDamage).coerceAtLeast(0)

        if (ship.hull <= 0) {
            // Respawn at capital with fee
            val capital = state.getPlayerFaction()?.capital ?: "Sol"
            ship.location = capital
            ship.hull = 60
            ship.credits = (ship.credits * 0.7f).toInt()
            return Triple(false, false, "Ship destroyed! Emergency pod recovered at $capital (-30% credits).")
        }

        if (shipDamage >= pirateHp) {
            ship.kills += 1
            val bounty = Random.nextInt(35, 80)
            ship.credits += bounty
            ship.reputation += 5
            unlockAchievement(state, "first_blood")
            if (ship.kills >= 5) unlockAchievement(state, "pirate_hunter")

            // Check hunt quests
            for (q in state.quests.filter { it.status == QuestStatus.ACTIVE && it.type == QuestType.HUNT }) {
                if (ship.kills >= q.killsNeeded) {
                    q.status = QuestStatus.COMPLETED
                    ship.credits += q.reward
                }
            }

            return Triple(true, false, "Target neutralized! Bounty collected: +$bounty cr. (Hull: ${ship.hull}/${ship.hullMax})")
        }

        return Triple(false, false, "Target damaged (-$shipDamage HP), took -$pirateDamage damage. (Hull: ${ship.hull}/${ship.hullMax})")
    }
}
