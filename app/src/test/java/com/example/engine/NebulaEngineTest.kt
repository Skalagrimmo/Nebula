package com.example.engine

import com.example.model.FleetType
import com.example.model.QuestStatus
import com.example.model.QuestType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the core simulation engine.
 * These tests are pure-JVM (no Android framework classes needed).
 */
class NebulaEngineTest {

    private fun newState(mode: String = "strategy") = GalaxyGenerator.createInitialState(mode = mode)

    @Test
    fun `step advances tick counter and stardate`() {
        val state = newState()
        val ticksBefore = state.ticks
        val stardateBefore = state.stardate
        NebulaSimulationEngine.step(state)
        assertEquals(ticksBefore + 1, state.ticks)
        assertEquals(stardateBefore + 0.1f, state.stardate, 0.001f)
    }

    @Test
    fun `buildFleet deducts credits and spawns fleet at owned system`() {
        val state = newState()
        val faction = state.getPlayerFaction()!!
        faction.credits = 500
        val fleetCountBefore = state.fleets.count { it.owner == faction.id }

        val built = NebulaSimulationEngine.buildFleet(state, "Sol", FleetType.BATTLESHIP)

        assertTrue(built)
        assertEquals(300, faction.credits)
        assertEquals(fleetCountBefore + 1, state.fleets.count { it.owner == faction.id })
    }

    @Test
    fun `buildFleet is rejected at an unowned system`() {
        val state = newState()
        state.getPlayerFaction()!!.credits = 500
        assertFalse(NebulaSimulationEngine.buildFleet(state, "Vega", FleetType.SCOUT))
    }

    @Test
    fun `researchTech deducts cost and unlocks the technology`() {
        val state = newState()
        val faction = state.getPlayerFaction()!!
        faction.credits = 200

        val researched = NebulaSimulationEngine.researchTech(state, "economy")

        assertTrue(researched)
        assertEquals(80, faction.credits)
        assertTrue(faction.tech.contains("economy"))
    }

    @Test
    fun `hunt quest is not completed by arrival alone`() {
        val state = newState(mode = "rpg")
        val ship = state.playerShip
        ship.weapon = 9999 // guarantee a one-shot kill every fight
        ship.hull = 100
        ship.location = "Vega" // HUNT quest destination

        val huntQuest = state.quests.first { it.type == QuestType.HUNT }

        // Two kills while standing at the destination must NOT complete the quest.
        repeat(2) { NebulaSimulationEngine.rpgFightPirate(state) }
        assertEquals(QuestStatus.ACTIVE, huntQuest.status)

        // The third kill reaches killsNeeded and completes it.
        NebulaSimulationEngine.rpgFightPirate(state)
        assertEquals(QuestStatus.COMPLETED, huntQuest.status)
    }
}
