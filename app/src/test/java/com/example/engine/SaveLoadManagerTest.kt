package com.example.engine

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ActiveWar
import com.example.model.DiplomacyRelation
import com.example.model.GalacticEvent
import com.example.model.PirateEncounter
import com.example.model.RelationStatus
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SaveLoadManagerTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private fun clearSaveFiles() {
        listOf("savegame.json", "savegame.json.bak", "savegame.json.new").forEach {
            File(context.filesDir, it).delete()
        }
    }

    @After
    fun tearDown() {
        clearSaveFiles()
    }

    @Test
    fun `round trip preserves strategic state and AI personality`() {
        clearSaveFiles()
        val state = GalaxyGenerator.createInitialState(mode = "strategy")
        state.ticks = 29
        state.stardate = 3002.9f

        val sol = state.systems.getValue("Sol")
        sol.value = 12.75f
        sol.description = "Industrial capital"

        val faction = state.factions.getValue("solarian")
        faction.lastExpansionTick = 17
        faction.personality.aggression = 0.91f
        faction.personality.economyFocus = 0.73f
        faction.personality.expansionFocus = 0.64f
        faction.personality.defenseFocus = 0.28f
        faction.personality.risk = 0.37f

        state.diplomacy.clear()
        state.diplomacy["solarian"] = mutableMapOf(
            "federation" to DiplomacyRelation(
                relation = 72,
                trust = 91,
                wars = 2,
                status = RelationStatus.ALLY
            )
        )
        state.activeWars.clear()
        state.activeWars.add(
            ActiveWar(
                factionA = "solarian",
                factionB = "zergon",
                startTick = 17,
                scoreA = 8,
                scoreB = 3,
                exhaustionA = 33,
                exhaustionB = 47
            )
        )
        state.events.clear()
        state.events.add(
            GalacticEvent(
                id = "test-supernova",
                tick = 28,
                titleKey = "event.supernova.title",
                messageKey = "event.supernova.message",
                systemId = "Sol",
                factionId = "solarian",
                icon = "☄️",
                effectDesc = "Production reduced"
            )
        )
        state.activeEncounter = PirateEncounter(
            id = "encounter-9",
            enemyName = "Corsair Raider",
            enemyHp = 27,
            enemyHpMax = 45,
            enemyDamage = 8,
            bounty = 120,
            salvageItem = "ore",
            salvageAmount = 2,
            questId = "quest-test"
        )

        assertTrue("saveGame should succeed", SaveLoadManager.saveGame(context, state))
        assertTrue("save file should be present", SaveLoadManager.hasSaveFile(context))

        val loaded = SaveLoadManager.loadGame(context)
        assertNotNull("loadGame should return a state", loaded)
        val restored = requireNotNull(loaded)

        assertEquals(29, restored.ticks)
        assertEquals(3002.9f, restored.stardate, 0.001f)
        assertEquals(12.75f, restored.systems.getValue("Sol").value, 0.001f)
        assertEquals("Industrial capital", restored.systems.getValue("Sol").description)

        assertEquals(17, restored.factions.getValue("solarian").lastExpansionTick)

        val restoredPersonality = restored.factions.getValue("solarian").personality
        assertEquals(0.91f, restoredPersonality.aggression, 0.001f)
        assertEquals(0.73f, restoredPersonality.economyFocus, 0.001f)
        assertEquals(0.64f, restoredPersonality.expansionFocus, 0.001f)
        assertEquals(0.28f, restoredPersonality.defenseFocus, 0.001f)
        assertEquals(0.37f, restoredPersonality.risk, 0.001f)

        val relation = restored.diplomacy.getValue("solarian").getValue("federation")
        assertEquals(72, relation.relation)
        assertEquals(91, relation.trust)
        assertEquals(2, relation.wars)
        assertEquals(RelationStatus.ALLY, relation.status)

        assertEquals(1, restored.activeWars.size)
        assertEquals("solarian", restored.activeWars.single().factionA)
        assertEquals("zergon", restored.activeWars.single().factionB)
        assertEquals(17, restored.activeWars.single().startTick)
        assertEquals(8, restored.activeWars.single().scoreA)
        assertEquals(3, restored.activeWars.single().scoreB)
        assertEquals(33, restored.activeWars.single().exhaustionA)
        assertEquals(47, restored.activeWars.single().exhaustionB)

        val event = restored.events.single()
        assertEquals("test-supernova", event.id)
        assertEquals(28, event.tick)
        assertEquals("event.supernova.title", event.titleKey)
        assertEquals("event.supernova.message", event.messageKey)
        assertEquals("Sol", event.systemId)
        assertEquals("solarian", event.factionId)
        assertEquals("☄️", event.icon)
        assertEquals("Production reduced", event.effectDesc)

        val encounter = requireNotNull(restored.activeEncounter)
        assertEquals("encounter-9", encounter.id)
        assertEquals("Corsair Raider", encounter.enemyName)
        assertEquals(27, encounter.enemyHp)
        assertEquals(45, encounter.enemyHpMax)
        assertEquals(8, encounter.enemyDamage)
        assertEquals(120, encounter.bounty)
        assertEquals("ore", encounter.salvageItem)
        assertEquals(2, encounter.salvageAmount)
        assertEquals("quest-test", encounter.questId)
    }

    @Test
    fun `older active war entries without exhaustion fields still load`() {
        clearSaveFiles()
        val state = GalaxyGenerator.createInitialState(mode = "strategy")
        state.activeWars.add(
            ActiveWar(
                factionA = "solarian",
                factionB = "zergon",
                startTick = 14,
                scoreA = 2,
                scoreB = 1
            )
        )
        assertTrue(SaveLoadManager.saveGame(context, state))

        val file = File(context.filesDir, "savegame.json")
        val root = JSONObject(file.readText(Charsets.UTF_8))
        val war = root.getJSONArray("activeWars").getJSONObject(0)
        war.remove("exhaustionA")
        war.remove("exhaustionB")
        file.writeText(root.toString(), Charsets.UTF_8)

        val restored = requireNotNull(SaveLoadManager.loadGame(context))
        val warAfterLoad = restored.activeWars.single()
        assertEquals(2, warAfterLoad.scoreA)
        assertEquals(1, warAfterLoad.scoreB)
        assertEquals(0, warAfterLoad.exhaustionA)
        assertEquals(0, warAfterLoad.exhaustionB)
    }

    @Test
    fun `older saves without newly persisted fields still load`() {
        clearSaveFiles()
        val state = GalaxyGenerator.createInitialState(mode = "strategy")
        assertTrue(SaveLoadManager.saveGame(context, state))

        val file = File(context.filesDir, "savegame.json")
        val root = JSONObject(file.readText(Charsets.UTF_8))
        root.remove("diplomacy")
        root.remove("activeWars")
        root.remove("events")
        root.remove("activeEncounter")

        val systems = root.getJSONObject("systems")
        val systemKeys = systems.keys()
        while (systemKeys.hasNext()) {
            val system = systems.getJSONObject(systemKeys.next())
            system.remove("value")
            system.remove("description")
        }

        val factions = root.getJSONObject("factions")
        val factionKeys = factions.keys()
        while (factionKeys.hasNext()) {
            val faction = factions.getJSONObject(factionKeys.next())
            faction.remove("personality")
            faction.remove("lastExpansionTick")
        }
        file.writeText(root.toString(), Charsets.UTF_8)

        val loaded = SaveLoadManager.loadGame(context)
        assertNotNull("legacy save should remain readable", loaded)
        val restored = requireNotNull(loaded)
        assertTrue(restored.diplomacy.isEmpty())
        assertTrue(restored.activeWars.isEmpty())
        assertTrue(restored.events.isEmpty())
        assertEquals(0f, restored.systems.getValue("Sol").value, 0f)
        assertEquals("", restored.systems.getValue("Sol").description)
        assertEquals(0.5f, restored.factions.getValue("solarian").personality.aggression, 0f)
        assertEquals(0, restored.factions.getValue("solarian").lastExpansionTick)
    }
}
