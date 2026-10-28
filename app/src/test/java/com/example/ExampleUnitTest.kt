package com.example

import com.example.engine.GalaxyGenerator
import com.example.engine.NebulaSimulationEngine
import com.example.localization.GameLanguage
import com.example.localization.NebulaStrings
import com.example.model.FleetType
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testUiRevisionMakesMutableStateUpdatesObservable() {
        val state = GalaxyGenerator.createInitialState()
        val previous = state
        state.ticks += 1

        val published = state.copy(uiRevision = 1L)

        assertNotEquals("UI revision must distinguish published snapshots", previous, published)
        assertEquals(1L, published.uiRevision)
    }

    @Test
    fun testGalaxyGeneration() {
        val state = GalaxyGenerator.createInitialState(playerFactionId = "solarian", mode = "strategy")
        assertTrue(state.systems.isNotEmpty())
        assertTrue(state.systems.containsKey("Sol"))
        assertTrue(state.factions.containsKey("solarian"))
        assertEquals("solarian", state.playerFactionId)
        assertTrue(state.fleets.isNotEmpty())
    }

    @Test
    fun testSimulationStep() {
        val state = GalaxyGenerator.createInitialState()
        val initialTicks = state.ticks
        NebulaSimulationEngine.step(state)
        assertEquals(initialTicks + 1, state.ticks)
    }

    @Test
    fun testFleetBuilding() {
        val state = GalaxyGenerator.createInitialState(playerFactionId = "solarian")
        val initialCredits = state.getPlayerFaction()?.credits ?: 0
        val initialFleets = state.fleets.size

        val built = NebulaSimulationEngine.buildFleet(state, "Sol", FleetType.SCOUT)
        assertTrue(built)
        assertEquals(initialCredits - FleetType.SCOUT.cost, state.getPlayerFaction()?.credits)
        assertEquals(initialFleets + 1, state.fleets.size)
    }

    @Test
    fun testLocalization() {
        val appNameUk = NebulaStrings.get("app_name", GameLanguage.UK)
        val appNameEn = NebulaStrings.get("app_name", GameLanguage.EN)
        assertEquals("Небула 4X", appNameUk)
        assertEquals("Nebula 4X", appNameEn)
    }

    @Test
    fun testRpgTrading() {
        val state = GalaxyGenerator.createInitialState(mode = "rpg")
        val ship = state.playerShip
        val startCredits = ship.credits

        val buySuccess = NebulaSimulationEngine.rpgBuyItem(state, "food", 2)
        assertTrue(buySuccess)
        assertEquals(2, ship.cargo["food"])
        assertTrue(ship.credits < startCredits)

        val sellSuccess = NebulaSimulationEngine.rpgSellItem(state, "food", 2)
        assertTrue(sellSuccess)
        assertEquals(0, ship.cargo["food"])
    }
}
