package com.example.engine

import com.example.model.Faction
import com.example.model.Fleet
import com.example.model.FleetType
import com.example.model.GameState
import com.example.model.StarSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Longer-run strategy regression tests.
 * Random events are tolerated; economy and ownership invariants are not.
 */
class NebulaBalanceTest {

    @Test
    fun `100 ticks preserve budget and faction ownership invariants`() {
        val state = GalaxyGenerator.createInitialState()

        repeat(100) { NebulaSimulationEngine.step(state) }

        assertEquals(101, state.ticks)
        assertTrue("stardate should advance with the simulation", state.stardate > 3009f)

        for ((factionId, faction) in state.factions) {
            assertTrue("$factionId must not have negative credits", faction.credits >= 0)
            assertEquals(
                "$factionId must not list duplicate systems",
                faction.systems.size,
                faction.systems.toSet().size
            )
            for (systemId in faction.systems) {
                assertEquals(
                    "the system owner must match its faction list",
                    factionId,
                    state.systems[systemId]?.owner
                )
            }
        }

        for ((systemId, system) in state.systems) {
            val ownerId = system.owner ?: continue
            assertTrue("claimed system must have a faction owner", state.factions.containsKey(ownerId))
            assertTrue(
                "owner must list the system",
                state.factions.getValue(ownerId).systems.contains(systemId)
            )
        }
    }

    @Test
    fun `starting factions use different AI fleet doctrines`() {
        val state = GalaxyGenerator.createInitialState()
        val zergon = state.factions.getValue("zergon")
        val federation = state.factions.getValue("federation")

        assertTrue("Zergon should prioritize aggression", zergon.personality.aggression > 0.6f)
        assertTrue("Zergon should prioritize expansion", zergon.personality.expansionFocus > 0.6f)
        assertTrue("Federation should prioritize its economy", federation.personality.economyFocus > 0.6f)
        assertTrue("Federation should prioritize defense", federation.personality.defenseFocus > 0.6f)

        state.fleets.clear()
        zergon.credits = 300
        federation.credits = 300
        NebulaSimulationEngine.processAIActions(state)

        val zergonFleet = state.fleets.single { it.owner == "zergon" }
        assertEquals(FleetType.ASSAULT, zergonFleet.type)
        assertEquals("attack", zergonFleet.role)

        val federationFleet = state.fleets.single { it.owner == "federation" }
        assertEquals(FleetType.BATTLESHIP, federationFleet.type)
        assertEquals("defense", federationFleet.role)
    }

    @Test
    fun `AI fleet production stops at territory-scaled limit and can research`() {
        val state = GalaxyGenerator.createInitialState()
        val zergon = state.factions.getValue("zergon")
        zergon.credits = 10_000

        // The starting force has two ships; with one system, the minimum AI cap is three.
        repeat(10) { NebulaSimulationEngine.processAIActions(state) }

        assertEquals(3, state.fleets.count { it.owner == "zergon" })
        assertTrue("AI should still have enough credits to invest in technology", zergon.credits > 0)
        assertTrue("AI research should progress while fleet production is capped", zergon.tech.isNotEmpty())
    }

    @Test
    fun `AI fleet waits for its faction expansion cooldown before annexing`() {
        val state = GameState(
            ticks = 120,
            playerFactionId = "solarian",
            factions = mutableMapOf(
                "zergon" to Faction(
                    id = "zergon",
                    name = "Zergon Hive",
                    displayName = "Zergon Hive",
                    systems = mutableListOf("A"),
                    lastExpansionTick = 1
                )
            ),
            systems = mutableMapOf(
                "A" to StarSystem(
                    id = "A",
                    owner = "zergon",
                    neighbors = mutableListOf("B")
                ),
                "B" to StarSystem(
                    id = "B",
                    owner = null,
                    neighbors = mutableListOf("A")
                )
            ),
            fleets = mutableListOf(
                Fleet(
                    id = "zergon-raider",
                    owner = "zergon",
                    location = "B",
                    strength = 20f,
                    type = FleetType.ASSAULT,
                    role = "attack"
                )
            )
        )

        NebulaSimulationEngine.processFleetMovement(state)
        assertNull("AI should hold an occupied system until its cooldown expires", state.systems.getValue("B").owner)
        assertEquals("B", state.fleets.single().location)

        state.ticks = 121
        NebulaSimulationEngine.processFleetMovement(state)

        assertEquals("zergon", state.systems.getValue("B").owner)
        assertTrue(state.factions.getValue("zergon").systems.contains("B"))
        assertEquals(121, state.factions.getValue("zergon").lastExpansionTick)
    }

    @Test
    fun `economic victory requires the raised threshold and minimum campaign length`() {
        val state = GalaxyGenerator.createInitialState()
        val solarian = state.factions.getValue("solarian")
        solarian.credits = 20_000
        state.ticks = 2_399

        NebulaSimulationEngine.checkVictory(state)
        assertTrue("no victory should be possible before turn 2,400", !state.victoryAchieved)

        state.ticks = 2_400
        NebulaSimulationEngine.checkVictory(state)
        assertTrue("the old 20,000-credit threshold should no longer end a campaign", !state.victoryAchieved)

        solarian.credits = 150_000
        NebulaSimulationEngine.checkVictory(state)
        assertTrue(state.victoryAchieved)
        assertEquals("solarian", state.winnerFactionId)
        assertEquals("economic", state.victoryType)
    }

    @Test
    fun `RPG mode does not trigger 4X victory conditions`() {
        val state = GalaxyGenerator.createInitialState(mode = "rpg")
        state.ticks = 2_400
        state.factions.getValue("solarian").credits = 150_000

        NebulaSimulationEngine.checkVictory(state)

        assertTrue("RPG mode should not show a strategic victory screen", !state.victoryAchieved)
    }

    @Test
    fun `domination victory requires at least 80 percent and minimum campaign length`() {
        val state = GalaxyGenerator.createInitialState()
        val solarian = state.factions.getValue("solarian")
        state.factions.values.forEach { faction ->
            faction.systems.clear()
            faction.credits = 0
        }
        solarian.systems.addAll(state.systems.keys.take(12))
        state.fleets.clear()
        state.ticks = 2_399

        NebulaSimulationEngine.checkVictory(state)
        assertTrue("even a dominant empire must not end a new campaign early", !state.victoryAchieved)

        state.ticks = 2_400
        NebulaSimulationEngine.checkVictory(state)
        assertTrue(state.victoryAchieved)
        assertEquals("solarian", state.winnerFactionId)
        assertEquals("domination", state.victoryType)
    }

    @Test
    fun `military victory requires minimum campaign length`() {
        val state = GalaxyGenerator.createInitialState()
        val solarian = state.factions.getValue("solarian")
        state.factions.values.forEach { faction ->
            faction.systems.clear()
            faction.credits = 0
        }
        solarian.systems.add("Sol")
        state.fleets.removeAll { it.owner != "solarian" }
        state.ticks = 2_399

        NebulaSimulationEngine.checkVictory(state)
        assertTrue("military elimination must not end a new campaign early", !state.victoryAchieved)

        state.ticks = 2_400
        NebulaSimulationEngine.checkVictory(state)
        assertTrue(state.victoryAchieved)
        assertEquals("solarian", state.winnerFactionId)
        assertEquals("military", state.victoryType)
    }

    @Test
    fun `declaring war creates a hostile active war record for both factions`() {
        val state = GalaxyGenerator.createInitialState()
        val initialWars = state.factions.getValue("solarian").let {
            state.diplomacy.getValue(it.id).getValue("zergon").wars
        }

        val message = NebulaSimulationEngine.declareWar(state, "zergon")

        assertTrue(message.contains("War declared"))
        assertEquals(1, state.activeWars.size)
        assertTrue(NebulaSimulationEngine.isAtWar(state, "solarian", "zergon"))
        val solarianRelation = state.diplomacy.getValue("solarian").getValue("zergon")
        val zergonRelation = state.diplomacy.getValue("zergon").getValue("solarian")
        assertEquals(-65, solarianRelation.relation)
        assertEquals(solarianRelation.relation, zergonRelation.relation)
        assertEquals(com.example.model.RelationStatus.HOSTILE, solarianRelation.status)
        assertEquals(initialWars + 1, solarianRelation.wars)
        assertEquals(1, zergonRelation.wars)
    }

    @Test
    fun `peace proposal ends an active war but does not instantly create an ally`() {
        val state = GalaxyGenerator.createInitialState()
        NebulaSimulationEngine.declareWar(state, "zergon")

        val message = NebulaSimulationEngine.proposePeace(state, "zergon")

        assertTrue(message.contains("Armistice"))
        assertTrue(state.activeWars.isEmpty())
        assertTrue(!NebulaSimulationEngine.isAtWar(state, "solarian", "zergon"))
        assertEquals(-40, state.diplomacy.getValue("solarian").getValue("zergon").relation)
        assertEquals(-40, state.diplomacy.getValue("zergon").getValue("solarian").relation)
        assertTrue(state.events.any { it.titleKey == "Armistice Signed" })
    }

    @Test
    fun `player cannot annex a rival system until war is declared`() {
        val state = GalaxyGenerator.createInitialState()
        state.fleets.removeAll { it.owner == "federation" }

        val peacefulMove = NebulaSimulationEngine.moveFleet(state, "fleet_sol_2", "Alpha")
        assertTrue(!peacefulMove)
        assertEquals("Sol", state.fleets.first { it.id == "fleet_sol_2" }.location)
        assertEquals("federation", state.systems.getValue("Alpha").owner)

        NebulaSimulationEngine.declareWar(state, "federation")
        val wartimeMove = NebulaSimulationEngine.moveFleet(state, "fleet_sol_2", "Alpha")

        assertTrue(wartimeMove)
        assertEquals("solarian", state.systems.getValue("Alpha").owner)
        assertTrue(state.factions.getValue("solarian").systems.contains("Alpha"))
        assertTrue(!state.factions.getValue("federation").systems.contains("Alpha"))
    }

    @Test
    fun `aggressive AI declares war when diplomatic relations become hostile`() {
        val state = GalaxyGenerator.createInitialState()
        val zergonRelation = state.diplomacy.getValue("zergon").getValue("federation")
        zergonRelation.relation = -75
        zergonRelation.status = com.example.model.RelationStatus.HOSTILE
        val federationRelation = state.diplomacy.getValue("federation").getValue("zergon")
        federationRelation.relation = -75
        federationRelation.status = com.example.model.RelationStatus.HOSTILE
        state.ticks = 20

        NebulaSimulationEngine.processAIDiplomacy(state)

        assertTrue(NebulaSimulationEngine.isAtWar(state, "zergon", "federation"))
        assertEquals(1, state.activeWars.size)
        assertTrue(state.events.any { it.titleKey == "War Declared" })
    }

    @Test
    fun `war exhaustion rises with time and battle outcomes`() {
        val state = GalaxyGenerator.createInitialState()
        NebulaSimulationEngine.declareWar(state, "zergon")
        val war = state.activeWars.single()

        state.ticks = 60
        NebulaSimulationEngine.processWarExhaustion(state)
        assertEquals(1, war.exhaustionA)
        assertEquals(1, war.exhaustionB)

        val playerFleet = state.fleets.first { it.owner == "solarian" }
        val zergonFleet = state.fleets.first { it.owner == "zergon" }
        NebulaSimulationEngine.resolveFleetClash(state, playerFleet, zergonFleet)

        assertEquals(1, war.scoreA + war.scoreB)
        assertTrue("battle winner and loser must both gain war exhaustion",
            war.exhaustionA >= 5 && war.exhaustionB >= 5)
        assertTrue("battle losses should create substantial exhaustion",
            war.exhaustionA + war.exhaustionB >= 23)
    }

    @Test
    fun `AI automatically seeks armistice when its war exhaustion is high`() {
        val state = GalaxyGenerator.createInitialState()
        NebulaSimulationEngine.declareWar(state, "zergon")
        val war = state.activeWars.single()
        war.exhaustionB = 80
        state.ticks = 20

        NebulaSimulationEngine.processAIDiplomacy(state)

        assertTrue(state.activeWars.isEmpty())
        assertTrue(state.events.any { it.titleKey == "War Exhaustion" })
        assertTrue(!NebulaSimulationEngine.isAtWar(state, "solarian", "zergon"))
    }

    @Test
    fun `territorial peace requires leverage and transfers a non-capital system`() {
        val state = GalaxyGenerator.createInitialState()
        val zergon = state.factions.getValue("zergon")
        val player = state.factions.getValue("solarian")
        state.systems.getValue("Vega").owner = "zergon"
        zergon.systems.add("Vega")
        NebulaSimulationEngine.declareWar(state, "zergon")
        val war = state.activeWars.single()
        assertTrue(!NebulaSimulationEngine.canDemandConcession(state, "zergon"))

        val rejected = NebulaSimulationEngine.proposePeace(state, "zergon", "Vega")
        assertTrue(rejected.contains("Insufficient leverage"))
        assertEquals("zergon", state.systems.getValue("Vega").owner)

        war.scoreA = 2
        assertTrue(NebulaSimulationEngine.canDemandConcession(state, "zergon"))
        val accepted = NebulaSimulationEngine.proposePeace(state, "zergon", "Vega")

        assertTrue(accepted.contains("Vega"))
        assertEquals("solarian", state.systems.getValue("Vega").owner)
        assertTrue(player.systems.contains("Vega"))
        assertTrue(!zergon.systems.contains("Vega"))
        assertTrue(state.activeWars.isEmpty())
        assertTrue(state.events.any { it.titleKey == "Territorial Concession" })
    }

    @Test
    fun `territorial peace cannot take a capital or the final enemy system`() {
        val state = GalaxyGenerator.createInitialState()
        NebulaSimulationEngine.declareWar(state, "zergon")
        val capitalResult = NebulaSimulationEngine.proposePeace(state, "zergon", "Epsilon")
        assertTrue(capitalResult.contains("capital"))
        assertTrue(NebulaSimulationEngine.isAtWar(state, "solarian", "zergon"))

        state.systems.getValue("Vega").owner = "zergon"
        state.factions.getValue("zergon").systems.add("Vega")
        state.factions.getValue("zergon").systems.remove("Epsilon")
        state.systems.getValue("Epsilon").owner = null
        val lastSystemResult = NebulaSimulationEngine.proposePeace(state, "zergon", "Vega")
        assertTrue(lastSystemResult.contains("last system"))
        assertTrue(NebulaSimulationEngine.isAtWar(state, "solarian", "zergon"))
    }

    @Test
    fun `pirate fleets can still fight rival fleets outside formal diplomacy`() {
        val state = GameState(
            ticks = 1,
            playerFactionId = "solarian",
            factions = mutableMapOf(
                "solarian" to Faction(
                    id = "solarian",
                    name = "Solarian Empire",
                    displayName = "Solarian Empire",
                    systems = mutableListOf("A")
                )
            ),
            systems = mutableMapOf(
                "A" to StarSystem(
                    id = "A",
                    owner = "solarian",
                    neighbors = mutableListOf("B")
                ),
                "B" to StarSystem(
                    id = "B",
                    owner = null,
                    neighbors = mutableListOf("A")
                )
            ),
            fleets = mutableListOf(
                Fleet(
                    id = "pirate-raider",
                    owner = "pirates",
                    location = "A",
                    strength = 15f,
                    type = FleetType.PIRATE,
                    role = "attack"
                ),
                Fleet(
                    id = "solar-defender",
                    owner = "solarian",
                    location = "B",
                    strength = 25f,
                    type = FleetType.FIGHTER,
                    role = "defense"
                )
            )
        )

        repeat(200) {
            if (state.events.any { event -> event.titleKey == "Space Battle" }) return@repeat
            NebulaSimulationEngine.processFleetMovement(state)
        }

        assertTrue("pirates should still be able to initiate a clash without a war record",
            state.events.any { it.titleKey == "Space Battle" })
        assertTrue("pirates must never annex systems", state.systems.values.none { it.owner == "pirates" })
    }

    @Test
    fun `pirate fleets cannot annex systems during repeated movement`() {
        val state = GameState(
            factions = mutableMapOf(
                "solarian" to Faction(
                    id = "solarian",
                    name = "Solarian Empire",
                    displayName = "Solarian Empire",
                    systems = mutableListOf("A")
                )
            ),
            systems = mutableMapOf(
                "A" to StarSystem(
                    id = "A",
                    owner = "solarian",
                    neighbors = mutableListOf("B")
                ),
                "B" to StarSystem(
                    id = "B",
                    owner = null,
                    neighbors = mutableListOf("A")
                )
            ),
            fleets = mutableListOf(
                Fleet(
                    id = "pirate-raider",
                    owner = "pirates",
                    location = "A",
                    strength = 15f,
                    type = FleetType.PIRATE,
                    role = "attack"
                )
            )
        )

        repeat(100) { NebulaSimulationEngine.processFleetMovement(state) }

        assertEquals("solarian", state.systems.getValue("A").owner)
        assertNull(state.systems.getValue("B").owner)
        assertTrue(state.systems.values.none { it.owner == "pirates" })
    }
}
