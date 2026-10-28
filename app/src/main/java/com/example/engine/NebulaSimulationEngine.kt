package com.example.engine

import com.example.model.*
import kotlin.random.Random

object NebulaSimulationEngine {

    // Monotonic sequence for unique AI fleet IDs
    private var aiFleetSeq = 0

    // Monotonic sequence for unique procedural contract IDs
    private var questSeq = 0

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

        // 7. RPG contract board refresh
        processRpgContracts(state)

        // 8. Check Victory Conditions
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

    private val pirateNames = listOf("Corsair", "Raider", "Marauder", "Reaver", "Buccaneer")
    private val tradeGoods = listOf("ore", "food", "tech")

    /**
     * Reputation above 50 grants up to +20% better trade terms and contract
     * payouts (cheaper buys, richer sells, larger quest rewards).
     */
    private fun reputationBonus(ship: PlayerShip): Float =
        ((ship.reputation - 50).coerceIn(0, 50)) / 250f

    /** Adds captain experience; returns a message when a new level is reached. */
    private fun addXp(state: GameState, amount: Int): String? {
        val ship = state.playerShip
        ship.xp += amount
        var levelMsg: String? = null
        while (ship.xp >= ship.level * 100) {
            ship.xp -= ship.level * 100
            ship.level += 1
            ship.hullMax += 10
            ship.hull = ship.hullMax
            ship.shield = (ship.shield + 0.02f).coerceAtMost(0.9f)
            levelMsg = "LEVEL UP! Captain rank ${ship.level} reached: +10 max hull, +2% shield deflection."
        }
        return levelMsg
    }

    /** Marks a contract fulfilled, pays out the reward scaled by reputation, awards XP. */
    private fun completeQuest(state: GameState, q: Quest, repBonus: Int) {
        if (q.status != QuestStatus.ACTIVE) return
        q.status = QuestStatus.COMPLETED
        val ship = state.playerShip
        val payout = (q.reward * (1f + reputationBonus(ship))).toInt() + q.bonus
        ship.credits += payout
        ship.reputation = (ship.reputation + repBonus).coerceAtMost(100)
        val levelMsg = addXp(state, 30 + q.reward / 10)
        state.events.add(
            0,
            GalacticEvent(
                id = "q_comp_${state.ticks}",
                tick = state.ticks,
                titleKey = "Contract Complete",
                messageKey = "\"${q.name}\" fulfilled! Payout: $payout cr.${levelMsg?.let { " $it" } ?: ""}",
                icon = "✅"
            )
        )
    }

    /** Periodically offers a new procedural contract while the active count stays under the cap. */
    private fun processRpgContracts(state: GameState) {
        if (state.mode != "rpg") return
        if (state.ticks % 8 != 0) return
        if (state.quests.count { it.status == QuestStatus.ACTIVE } >= 5) return
        if (Random.nextFloat() > 0.6f) return
        rpgGenerateQuest(state)
    }

    /** Generates one procedural contract from the current system's board. */
    fun rpgGenerateQuest(state: GameState): Quest? {
        val ship = state.playerShip
        val source = ship.location
        val candidates = state.systems.keys.filter { it != source }
        if (candidates.isEmpty()) return null
        val dest = candidates.random()
        val id = "q_${state.ticks}_${++questSeq}"

        val quest = when (Random.nextInt(5)) {
            0 -> {
                val item = tradeGoods.random()
                val amount = Random.nextInt(3, 7)
                Quest(
                    id = id,
                    type = QuestType.DELIVERY,
                    name = "Supply Run to $dest",
                    desc = "Deliver $amount units of ${item.replaceFirstChar { it.uppercase() }} to the $dest station.",
                    destination = dest,
                    item = item,
                    amount = amount,
                    reward = 40 + amount * 12,
                    bonus = 20,
                    source = source
                )
            }
            1 -> {
                val kills = Random.nextInt(2, 5)
                Quest(
                    id = id,
                    type = QuestType.HUNT,
                    name = "Bounty Contract: $dest",
                    desc = "Destroy $kills pirate vessels operating in the $dest system.",
                    destination = dest,
                    reward = 80 + kills * 30,
                    bonus = 30,
                    source = source,
                    killsNeeded = kills
                )
            }
            2 -> Quest(
                id = id,
                type = QuestType.EXPLORE,
                name = "Survey: $dest",
                desc = "Chart the $dest system and transmit stellar cartography data.",
                destination = dest,
                reward = 70,
                bonus = 25,
                source = source
            )
            3 -> Quest(
                id = id,
                type = QuestType.ESCORT,
                name = "Convoy Escort to $dest",
                desc = "Escort a merchant convoy safely to $dest. Expect hostile interference en route.",
                destination = dest,
                reward = 130,
                bonus = 40,
                source = source
            )
            else -> Quest(
                id = id,
                type = QuestType.RESCUE,
                name = "Distress Call: $dest",
                desc = "A stranded crew in the $dest system is under corsair threat. Rescue the survivors.",
                destination = dest,
                reward = 150,
                bonus = 50,
                source = source
            )
        }
        state.quests.add(quest)
        state.events.add(
            0,
            GalacticEvent(
                id = "q_new_${state.ticks}",
                tick = state.ticks,
                titleKey = "New Contract",
                messageKey = "Contract board update: \"${quest.name}\" is now available.",
                icon = quest.type.icon
            )
        )
        return quest
    }

    fun rpgBuyItem(state: GameState, item: String, amount: Int): Boolean {
        val ship = state.playerShip
        val currentSys = state.systems[ship.location] ?: return false
        val price = currentSys.prices[item] ?: return false
        val totalCost = (price * amount * (1f - reputationBonus(ship))).toInt()

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
        val totalRevenue = (price * amount * (1f + reputationBonus(ship))).toInt()

        ship.credits += totalRevenue
        ship.cargo[item] = currentCargo - amount

        // Accumulate partial deliveries for matching delivery contracts at this station
        for (q in state.quests.filter { it.status == QuestStatus.ACTIVE && it.type == QuestType.DELIVERY }) {
            if (q.destination == ship.location && q.item == item) {
                q.progress += amount
                if (q.progress >= q.amount) {
                    completeQuest(state, q, 10)
                }
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

        var message = "Hyperspace jump to $destinationId successful."

        // Contract progress on arrival
        for (q in state.quests.filter { it.status == QuestStatus.ACTIVE && q.destination == destinationId }) {
            when (q.type) {
                QuestType.EXPLORE -> {
                    completeQuest(state, q, 15)
                    message = "Stellar survey of $destinationId complete. Contract fulfilled!"
                }
                QuestType.ESCORT -> {
                    if (Random.nextFloat() < 0.35f) {
                        rpgStartCombat(state)
                        message = "Convoy ambushed en route to $destinationId! The freighter made it — drive off the raiders!"
                    } else {
                        completeQuest(state, q, 12)
                        message = "Merchant convoy delivered safely to $destinationId. Contract fulfilled!"
                    }
                }
                QuestType.RESCUE -> {
                    if (q.progress == 0) {
                        q.progress = 1
                        rpgStartCombat(state, questId = q.id)
                        message = "Distress beacon located at $destinationId — corsairs are stripping the wreck. Engage to rescue the crew!"
                    } else {
                        completeQuest(state, q, 15)
                        message = "Survivors recovered at $destinationId. Contract fulfilled!"
                    }
                }
                else -> {}
            }
        }

        // Random deep-space pirate ambush on the lane
        if (state.activeEncounter == null && Random.nextFloat() < 0.22f) {
            rpgStartCombat(state)
            message = "Hyperspace jump complete. WARNING: pirate raider on radar!"
        }

        return true to message
    }

    fun rpgWormhole(state: GameState): Pair<Boolean, String> {
        val ship = state.playerShip
        val wormholeTarget = GalaxyGenerator.WORMHOLES[ship.location]
            ?: return false to "No wormhole singularity detected in ${ship.location}."

        ship.location = wormholeTarget
        step(state)

        return if (Random.nextFloat() < 0.15f) {
            rpgStartCombat(state)
            true to "Sub-space anomaly traversed! Emerge at $wormholeTarget — the turbulence attracted a hostile contact!"
        } else {
            true to "Sub-space anomaly traversed! Emerge at $wormholeTarget."
        }
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

    /** Station repair service: 2 credits per missing hull point. */
    fun rpgRepair(state: GameState): Pair<Boolean, String> {
        val ship = state.playerShip
        val missing = ship.hullMax - ship.hull
        if (missing <= 0) return false to "Hull integrity already at maximum."
        val cost = missing * 2
        if (ship.credits < cost) return false to "Full repair costs $cost cr. — insufficient funds."
        ship.credits -= cost
        ship.hull = ship.hullMax
        return true to "Dock crews restore hull plating (+$missing HP) for $cost cr."
    }

    /**
     * Begins a multi-round engagement. The hostile is persistent until it is
     * destroyed, the player flees, or the player's ship goes down.
     */
    fun rpgStartCombat(state: GameState, questId: String? = null): Pair<Boolean, String> {
        val ship = state.playerShip
        if (state.activeEncounter != null) {
            return false to "Weapons already locked on an active hostile!"
        }
        val name = pirateNames.random()
        val hp = Random.nextInt(30, 45) + ship.weapon * 12
        state.activeEncounter = PirateEncounter(
            id = "enc_${state.ticks}",
            enemyName = name,
            enemyHp = hp,
            enemyHpMax = hp,
            enemyDamage = Random.nextInt(12, 20) + ship.weapon * 2,
            bounty = Random.nextInt(30, 60) + ship.weapon * 10,
            salvageItem = tradeGoods.random(),
            salvageAmount = Random.nextInt(1, 3),
            questId = questId
        )
        return true to "Hostile $name detected (${hp} HP)! Weapons online."
    }

    private fun shipDestroyed(state: GameState): String {
        val ship = state.playerShip
        val capital = state.getPlayerFaction()?.capital ?: "Sol"
        ship.location = capital
        ship.hull = 60
        ship.credits = (ship.credits * 0.7f).toInt()
        state.activeEncounter = null
        return "Ship destroyed! Emergency pod recovered at $capital (-30% credits)."
    }

    /** One volley of the ongoing engagement. */
    fun rpgCombatAttack(state: GameState): String {
        val enc = state.activeEncounter ?: return "No hostile contact on scanners."
        val ship = state.playerShip

        val damage = (ship.weapon * 18 * (1f + Random.nextFloat() * 0.5f)).toInt() + ship.level * 3
        enc.enemyHp -= damage

        if (enc.enemyHp <= 0) {
            state.activeEncounter = null
            ship.kills += 1
            ship.credits += enc.bounty
            ship.reputation = (ship.reputation + 5).coerceAtMost(100)
            unlockAchievement(state, "first_blood")
            if (ship.kills >= 5) unlockAchievement(state, "pirate_hunter")

            var salvageMsg = ""
            if (ship.availableCargo > 0) {
                val salvaged = minOf(enc.salvageAmount, ship.availableCargo)
                ship.cargo[enc.salvageItem] = (ship.cargo[enc.salvageItem] ?: 0) + salvaged
                salvageMsg = " Salvage: +$salvaged ${enc.salvageItem}."
            }

            // Hunt contract progress (kills count in the contract's system)
            for (q in state.quests.filter { it.status == QuestStatus.ACTIVE && it.type == QuestType.HUNT }) {
                if (q.destination == ship.location) {
                    q.progress += 1
                    if (q.progress >= q.killsNeeded) completeQuest(state, q, 8)
                }
            }

            // Rescue contract: the corsair holding the survivors is destroyed
            if (enc.questId != null) {
                val rescue = state.quests.firstOrNull { it.id == enc.questId && it.status == QuestStatus.ACTIVE }
                if (rescue != null) completeQuest(state, rescue, 15)
            }

            val levelMsg = addXp(state, 25)
            return "Target destroyed! Bounty: +${enc.bounty} cr.$salvageMsg${levelMsg?.let { " $it" } ?: ""} (Hull: ${ship.hull}/${ship.hullMax})"
        }

        // Enemy counter-fire
        val incoming = (enc.enemyDamage * (1f - ship.shield)).toInt().coerceAtLeast(4)
        ship.hull -= incoming
        if (ship.hull <= 0) return shipDestroyed(state)

        return "Volley hits for -$damage. ${enc.enemyName} returns fire: -$incoming. (Enemy: ${enc.enemyHp}/${enc.enemyHpMax} HP | Hull: ${ship.hull}/${ship.hullMax})"
    }

    /** Attempt to disengage. Higher captain level and shields improve the odds. */
    fun rpgCombatFlee(state: GameState): String {
        val enc = state.activeEncounter ?: return "No hostile contact on scanners."
        val ship = state.playerShip
        val escapeChance = (0.5f + ship.level * 0.05f + ship.shield * 0.2f).coerceAtMost(0.95f)
        state.activeEncounter = null

        if (Random.nextFloat() < escapeChance) {
            return "Emergency warp successful — the ${enc.enemyName} lost your trail. (Hull: ${ship.hull}/${ship.hullMax})"
        }

        val incoming = (enc.enemyDamage * (1f - ship.shield)).toInt().coerceAtLeast(4)
        ship.hull -= incoming
        if (ship.hull <= 0) return shipDestroyed(state)
        return "Escaped the ${enc.enemyName}, but the parting volley cost -$incoming hull. (Hull: ${ship.hull}/${ship.hullMax})"
    }
}
