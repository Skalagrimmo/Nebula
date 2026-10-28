package com.example.model

enum class SystemType(
    val code: String,
    val icon: String,
    val bonusPopulation: Float,
    val bonusEconomy: Float,
    val bonusResources: Float,
    val bonusDefense: Float
) {
    CAPITAL("capital", "🏛️", 1.5f, 1.5f, 1.2f, 2.0f),
    RICH("rich", "💰", 1.0f, 1.5f, 2.0f, 1.0f),
    INDUSTRIAL("industrial", "🏭", 1.0f, 1.2f, 1.0f, 1.0f),
    AGRICULTURAL("agricultural", "🌾", 1.8f, 0.8f, 1.0f, 1.0f),
    DESERT("desert", "🏜️", 0.5f, 1.0f, 1.5f, 0.8f),
    OCEAN("ocean", "🌊", 1.5f, 0.7f, 0.5f, 1.2f),
    FOREST("forest", "🌲", 1.2f, 1.0f, 1.2f, 1.0f),
    PIRATE("pirate", "☠️", 0.5f, 0.3f, 0.5f, 0.5f),
    VOLCANIC("volcanic", "🌋", 0.3f, 1.0f, 2.0f, 0.5f),
    ICE("ice", "❄️", 0.3f, 0.8f, 1.2f, 0.5f);

    companion object {
        fun fromCode(code: String?): SystemType {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: FOREST
        }
    }
}
