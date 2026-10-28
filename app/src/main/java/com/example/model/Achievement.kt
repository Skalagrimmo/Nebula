package com.example.model

data class Achievement(
    val id: String,
    val nameKey: String,
    val descKey: String,
    val icon: String,
    var unlocked: Boolean = false,
    var unlockedAt: Long? = null
)

object AchievementCatalog {
    val ACHIEVEMENTS = listOf(
        Achievement("first_blood", "ach_first_blood", "ach_first_blood_desc", "⚔️"),
        Achievement("empire_builder", "ach_empire_builder", "ach_empire_builder_desc", "🏛️"),
        Achievement("tycoon", "ach_tycoon", "ach_tycoon_desc", "💰"),
        Achievement("explorer", "ach_explorer", "ach_explorer_desc", "🧭"),
        Achievement("technologist", "ach_technologist", "ach_technologist_desc", "🔬"),
        Achievement("conqueror", "ach_conqueror", "ach_conqueror_desc", "👑"),
        Achievement("pirate_hunter", "ach_pirate_hunter", "ach_pirate_hunter_desc", "☠️"),
        Achievement("diplomat", "ach_diplomat", "ach_diplomat_desc", "🤝"),
        Achievement("survivor", "ach_survivor", "ach_survivor_desc", "🛡️"),
        Achievement("victor", "ach_victor", "ach_victor_desc", "🏆")
    )
}
