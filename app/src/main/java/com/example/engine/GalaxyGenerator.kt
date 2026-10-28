package com.example.engine

import com.example.model.*
import kotlin.random.Random

object GalaxyGenerator {

    val SYSTEM_SPECS = listOf(
        Triple("Sol", 15f, 5f) to SystemType.CAPITAL,
        Triple("Vega", 26f, 6f) to SystemType.RICH,
        Triple("Sirius", 5f, 11f) to SystemType.INDUSTRIAL,
        Triple("Altair", 6f, 17f) to SystemType.VOLCANIC,
        Triple("Proxima", 17f, 19f) to SystemType.DESERT,
        Triple("Nebula", 8f, 21f) to SystemType.VOLCANIC,
        Triple("Alpha", 36f, 9f) to SystemType.FOREST,
        Triple("Beta", 24f, 14f) to SystemType.OCEAN,
        Triple("Gamma", 34f, 18f) to SystemType.AGRICULTURAL,
        Triple("Delta", 18f, 15f) to SystemType.INDUSTRIAL,
        Triple("Epsilon", 18f, 8f) to SystemType.ICE,
        Triple("Zeta", 38f, 21f) to SystemType.PIRATE,
        Triple("Orion", 28f, 22f) to SystemType.RICH,
        Triple("Cassiopeia", 40f, 4f) to SystemType.FOREST
    )

    val CONNECTIONS = mapOf(
        "Sol" to listOf("Vega", "Sirius", "Proxima", "Alpha"),
        "Vega" to listOf("Sol", "Altair", "Beta"),
        "Sirius" to listOf("Sol", "Gamma", "Epsilon"),
        "Altair" to listOf("Vega", "Delta"),
        "Proxima" to listOf("Sol", "Nebula", "Epsilon"),
        "Nebula" to listOf("Proxima", "Beta"),
        "Alpha" to listOf("Sol", "Gamma", "Cassiopeia"),
        "Beta" to listOf("Vega", "Nebula", "Delta", "Orion"),
        "Gamma" to listOf("Sirius", "Alpha", "Zeta"),
        "Delta" to listOf("Altair", "Epsilon", "Beta"),
        "Epsilon" to listOf("Sirius", "Proxima", "Delta"),
        "Zeta" to listOf("Gamma", "Orion"),
        "Orion" to listOf("Zeta", "Beta"),
        "Cassiopeia" to listOf("Alpha", "Vega")
    )

    val WORMHOLES = mapOf(
        "Sol" to "Altair",
        "Altair" to "Sol",
        "Vega" to "Sirius",
        "Sirius" to "Vega",
        "Proxima" to "Nebula",
        "Nebula" to "Proxima",
        "Alpha" to "Delta",
        "Delta" to "Alpha"
    )

    fun createInitialState(playerFactionId: String = "solarian", mode: String = "strategy"): GameState {
        val state = GameState(
            version = "1.0",
            ticks = 1,
            stardate = 3000.0f,
            mode = mode,
            playerFactionId = playerFactionId
        )

        // 1. Generate Star Systems
        SYSTEM_SPECS.forEach { (coords, type) ->
            val (name, x, y) = coords
            val basePop = Random.nextInt(80, 250)
            val baseEcon = Random.nextInt(40, 95)
            val baseRes = Random.nextInt(50, 150)
            val pop = (basePop * type.bonusPopulation).toInt()
            val econ = (baseEcon * type.bonusEconomy).toInt()
            val res = (baseRes * type.bonusResources).toInt()

            val prices = mutableMapOf(
                "ore" to when (type) {
                    SystemType.RICH, SystemType.VOLCANIC -> Random.nextInt(5, 10)
                    SystemType.INDUSTRIAL -> Random.nextInt(12, 18)
                    else -> Random.nextInt(8, 14)
                },
                "food" to when (type) {
                    SystemType.AGRICULTURAL, SystemType.OCEAN -> Random.nextInt(3, 7)
                    SystemType.DESERT, SystemType.ICE -> Random.nextInt(12, 20)
                    else -> Random.nextInt(6, 12)
                },
                "tech" to when (type) {
                    SystemType.INDUSTRIAL, SystemType.CAPITAL -> Random.nextInt(15, 22)
                    SystemType.PIRATE, SystemType.DESERT -> Random.nextInt(26, 38)
                    else -> Random.nextInt(20, 30)
                }
            )

            state.systems[name] = StarSystem(
                id = name,
                owner = null,
                capital = false,
                population = pop,
                economy = econ,
                resources = res,
                stock = Random.nextInt(30, 80),
                demand = Random.nextInt(30, 70),
                posX = x,
                posY = y,
                neighbors = CONNECTIONS[name]?.toMutableList() ?: mutableListOf(),
                type = type,
                prices = prices,
                description = "Sector star system: ${type.icon} ${type.name.lowercase().replaceFirstChar { it.uppercase() }}"
            )
        }

        // 2. Generate 3 core Factions
        val solarian = Faction(
            id = "solarian",
            name = "Solarian Empire",
            displayName = "👑 Solarian Empire",
            capital = "Sol",
            credits = 250,
            military = 1.2f,
            systems = mutableListOf("Sol"),
            personality = FactionPersonality(
                aggression = 0.45f,
                economyFocus = 0.65f,
                expansionFocus = 0.45f,
                defenseFocus = 0.70f,
                risk = 0.35f
            ),
            strategy = "balanced"
        )
        val zergon = Faction(
            id = "zergon",
            name = "Zergon Hive",
            displayName = "🐝 Zergon Hive",
            capital = "Epsilon",
            credits = 200,
            military = 1.3f,
            systems = mutableListOf("Epsilon"),
            personality = FactionPersonality(
                aggression = 0.85f,
                economyFocus = 0.25f,
                expansionFocus = 0.85f,
                defenseFocus = 0.35f,
                risk = 0.80f
            ),
            strategy = "aggressive"
        )
        val federation = Faction(
            id = "federation",
            name = "Celestial Federation",
            displayName = "🌌 Celestial Federation",
            capital = "Alpha",
            credits = 300,
            military = 1.0f,
            systems = mutableListOf("Alpha"),
            personality = FactionPersonality(
                aggression = 0.35f,
                economyFocus = 0.85f,
                expansionFocus = 0.40f,
                defenseFocus = 0.70f,
                risk = 0.25f
            ),
            strategy = "economic"
        )

        state.factions[solarian.id] = solarian
        state.factions[zergon.id] = zergon
        state.factions[federation.id] = federation

        // Setup capitals on systems
        state.systems["Sol"]?.apply { owner = "solarian"; capital = true; type = SystemType.CAPITAL }
        state.systems["Epsilon"]?.apply { owner = "zergon"; capital = true; type = SystemType.CAPITAL }
        state.systems["Alpha"]?.apply { owner = "federation"; capital = true; type = SystemType.CAPITAL }

        // Setup initial fleets
        state.fleets.add(Fleet("fleet_sol_1", "solarian", "Sol", 30f, 1f, FleetType.FIGHTER, "defense"))
        state.fleets.add(Fleet("fleet_sol_2", "solarian", "Sol", 20f, 1.5f, FleetType.SCOUT, "attack"))
        state.fleets.add(Fleet("fleet_zerg_1", "zergon", "Epsilon", 35f, 1.2f, FleetType.ASSAULT, "attack"))
        state.fleets.add(Fleet("fleet_zerg_2", "zergon", "Epsilon", 25f, 1f, FleetType.FIGHTER, "defense"))
        state.fleets.add(Fleet("fleet_fed_1", "federation", "Alpha", 25f, 1f, FleetType.FIGHTER, "defense"))
        state.fleets.add(Fleet("fleet_fed_2", "federation", "Alpha", 30f, 0.8f, FleetType.BATTLESHIP, "attack"))

        // 3. Initialize Diplomacy
        val factionIds = state.factions.keys.toList()
        for (a in factionIds) {
            state.diplomacy[a] = mutableMapOf()
            for (b in factionIds) {
                if (a != b) {
                    val initialRelation = if ((a == "solarian" && b == "zergon") || (a == "zergon" && b == "solarian")) -25 else 10
                    state.diplomacy[a]?.put(
                        b,
                        DiplomacyRelation(
                            relation = initialRelation,
                            trust = 50,
                            wars = 0,
                            status = RelationStatus.fromRelation(initialRelation)
                        )
                    )
                }
            }
        }

        // 4. Initialize Player Ship for RPG
        state.playerShip = PlayerShip(
            location = state.systems[state.factions[playerFactionId]?.capital ?: "Sol"]?.id ?: "Sol",
            credits = 150,
            cargoLimit = 15,
            hull = 100,
            hullMax = 100,
            weapon = 1,
            shield = 0.1f,
            reputation = 50,
            kills = 0
        )

        // 5. Initial Quests
        state.quests.add(
            Quest(
                id = "q_sol_delivery",
                type = QuestType.DELIVERY,
                name = "Provisions to Sirius",
                desc = "Deliver 5 Food rations to Sirius industrial colony.",
                destination = "Sirius",
                item = "food",
                amount = 5,
                reward = 80,
                bonus = 25,
                status = QuestStatus.ACTIVE,
                source = "Sol"
            )
        )
        state.quests.add(
            Quest(
                id = "q_alpha_hunt",
                type = QuestType.HUNT,
                name = "Corsair Suppression",
                desc = "Neutralize 3 pirate vessels threatening Vega shipping lanes.",
                destination = "Vega",
                reward = 120,
                bonus = 40,
                status = QuestStatus.ACTIVE,
                source = "Sol",
                killsNeeded = 3
            )
        )

        // 6. Initial Welcome Event
        state.events.add(
            GalacticEvent(
                id = "ev_0",
                tick = 1,
                titleKey = "Galactic Era Initiated",
                messageKey = "Stardate 3000.0: The galactic sector stabilizes under three rival spacefaring empires.",
                icon = "🚀"
            )
        )

        return state
    }
}
