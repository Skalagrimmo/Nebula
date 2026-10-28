package com.example.engine

import com.example.model.FleetType
import com.example.model.QuestStatus
import com.example.model.QuestType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    fun `hunt quest tracks kills in the target system`() {
        val state = newState(mode = "rpg")
        val ship = state.playerShip
        ship.weapon = 9999 // guarantee a one-shot kill every fight
        ship.hull = 100
        ship.location = "Vega" // HUNT quest destination

        val huntQuest = state.quests.first { it.type == QuestType.HUNT }

        // Two kills while standing at the destination must NOT complete the quest.
        repeat(2) {
            NebulaSimulationEngine.rpgStartCombat(state)
            NebulaSimulationEngine.rpgCombatAttack(state)
        }
        assertEquals(QuestStatus.ACTIVE, huntQuest.status)
        assertEquals(2, huntQuest.progress)

        // The third kill reaches killsNeeded and completes it.
        NebulaSimulationEngine.rpgStartCombat(state)
        NebulaSimulationEngine.rpgCombatAttack(state)
        assertEquals(QuestStatus.COMPLETED, huntQuest.status)
    }

    @Test
    fun `combat victory grants bounty, xp and a kill`() {
        val state = newState(mode = "rpg")
        val ship = state.playerShip
        ship.weapon = 9999
        ship.hull = 100
        val creditsBefore = ship.credits

        val (started, _) = NebulaSimulationEngine.rpgStartCombat(state)
        assertTrue(started)
        NebulaSimulationEngine.rpgCombatAttack(state)

        assertEquals(1, ship.kills)
        assertTrue(ship.credits > creditsBefore)
        assertTrue(ship.xp >= 25)
        assertNull(state.activeEncounter)
    }

    @Test
    fun `flee attempt always ends the encounter`() {
        val state = newState(mode = "rpg")
        NebulaSimulationEngine.rpgStartCombat(state)
        assertNotNull(state.activeEncounter)

        NebulaSimulationEngine.rpgCombatFlee(state)
        assertNull(state.activeEncounter)
    }

    @Test
    fun `delivery quest accumulates partial sales and completes on the final one`() {
        val state = newState(mode = "rpg")
        val ship = state.playerShip
        ship.location = "Sirius" // initial delivery contract destination
        ship.cargo["food"] = 5

        val delivery = state.quests.first { it.type == QuestType.DELIVERY }

        NebulaSimulationEngine.rpgSellItem(state, "food", 2)
        assertEquals(QuestStatus.ACTIVE, delivery.status)
        assertEquals(2, delivery.progress)

        NebulaSimulationEngine.rpgSellItem(state, "food", 3)
        assertEquals(QuestStatus.COMPLETED, delivery.status)
        assertEquals(0, ship.cargo["food"])
    }

    @Test
    fun `high reputation grants a purchase discount`() {
        val state = newState(mode = "rpg")
        val ship = state.playerShip
        ship.location = "Sol"
        ship.credits = 100
        ship.reputation = 100 // +20% trade terms

        val price = state.systems["Sol"]!!.prices["ore"] ?: 10
        val bought = NebulaSimulationEngine.rpgBuyItem(state, "ore", 1)

        assertTrue(bought)
        assertEquals(1, ship.cargo["ore"])
        assertEquals(100 - (price * 0.8f).toInt(), ship.credits)
    }

    @Test
    fun `repair restores the hull and charges two credits per point`() {
        val state = newState(mode = "rpg")
        val ship = state.playerShip
        ship.hull = 50
        ship.credits = 200

        val (ok, _) = NebulaSimulationEngine.rpgRepair(state)

        assertTrue(ok)
        assertEquals(100, ship.hull)
        assertEquals(100, ship.credits)
    }

    @Test
    fun `repair is rejected when the hull is already intact`() {
        val state = newState(mode = "rpg")
        val (ok, _) = NebulaSimulationEngine.rpgRepair(state)
        assertFalse(ok)
    }

    @Test
    fun `xp levels up the captain and improves the ship`() {
        val state = newState(mode = "rpg")
        val ship = state.playerShip
        ship.weapon = 9999
        ship.hull = 100
        ship.xp = 95
        ship.level = 1

        NebulaSimulationEngine.rpgStartCombat(state)
        NebulaSimulationEngine.rpgCombatAttack(state) // +25 xp crosses the 100 xp threshold

        assertEquals(2, ship.level)
        assertEquals(20, ship.xp) // 95 + 25 - 100
        assertEquals(110, ship.hullMax) // +10 max hull per level
    }

    @Test
    fun `quest board refreshes procedurally while capping active contracts`() {
        val state = newState(mode = "rpg")
        repeat(400) { NebulaSimulationEngine.step(state) }

        val active = state.quests.count { it.status == QuestStatus.ACTIVE }
        assertTrue("expected at least the starter contracts", active >= 2)
        assertTrue("active contracts must stay under the cap", active <= 5)
        assertTrue("the board should offer procedural contracts over time", state.quests.size > 2)
    }
}
