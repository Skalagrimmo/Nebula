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
