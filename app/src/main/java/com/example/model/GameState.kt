package com.example.model

data class ActiveWar(
    val factionA: String,
    val factionB: String,
    val startTick: Int,
    var scoreA: Int = 0,
    var scoreB: Int = 0
)

data class GameState(
    var version: String = "1.0",
    var ticks: Int = 1,
    var stardate: Float = 3000.0f,
    val systems: MutableMap<String, StarSystem> = mutableMapOf(),
    val factions: MutableMap<String, Faction> = mutableMapOf(),
    val fleets: MutableList<Fleet> = mutableListOf(),
    var playerShip: PlayerShip = PlayerShip(),
    val quests: MutableList<Quest> = mutableListOf(),
    val achievements: MutableList<Achievement> = AchievementCatalog.ACHIEVEMENTS.map { it.copy() }.toMutableList(),
    val diplomacy: MutableMap<String, MutableMap<String, DiplomacyRelation>> = mutableMapOf(),
    val activeWars: MutableList<ActiveWar> = mutableListOf(),
    val events: MutableList<GalacticEvent> = mutableListOf(),
    var mode: String = "strategy", // "strategy" or "rpg"
    var playerFactionId: String = "solarian",
    var victoryAchieved: Boolean = false,
    var winnerFactionId: String? = null,
    var victoryType: String? = null,
    var sandboxMode: Boolean = false
) {
    fun getPlayerFaction(): Faction? = factions[playerFactionId]
}
