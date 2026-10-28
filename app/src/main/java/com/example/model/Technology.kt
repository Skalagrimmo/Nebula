package com.example.model

data class TechNode(
    val id: String,
    val nameKey: String,
    val cost: Int,
    val bonus: Float,
    val icon: String
)

object TechCatalog {
    val TECHS = listOf(
        TechNode("laser", "tech_laser", 100, 0.20f, "⚡"),
        TechNode("shield", "tech_shield", 150, 0.30f, "🛡️"),
        TechNode("warp", "tech_warp", 200, 0.50f, "🌀"),
        TechNode("economy", "tech_economy", 120, 0.25f, "💰"),
        TechNode("colony", "tech_colony", 180, 0.40f, "🏛️")
    )

    fun get(id: String): TechNode? = TECHS.firstOrNull { it.id == id }
}
