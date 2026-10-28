package com.example.model

enum class FleetType(
    val code: String,
    val typeName: String,
    val strength: Int,
    val speed: Float,
    val cost: Int,
    val icon: String
) {
    SCOUT("scout", "Scout", 5, 3f, 50, "🔍"),
    FIGHTER("fighter", "Fighter", 15, 1f, 100, "⚔️"),
    BATTLESHIP("battleship", "Battleship", 25, 0.5f, 200, "🛡️"),
    ASSAULT("assault", "Assault", 20, 1.5f, 150, "💀"),
    PIRATE("pirate", "Pirate", 12, 2f, 0, "🏴‍☠️"),
    FLAGSHIP("flagship", "Flagship", 40, 0.5f, 400, "🏛️");

    companion object {
        fun fromCode(code: String): FleetType {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: FIGHTER
        }
    }
}

data class Fleet(
    val id: String,
    var owner: String,
    var location: String,
    var strength: Float,
    var speed: Float = 1.0f,
    var type: FleetType = FleetType.FIGHTER,
    var role: String = "attack" // "attack" or "defense"
)
