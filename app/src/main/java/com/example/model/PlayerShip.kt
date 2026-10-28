package com.example.model

data class PlayerShip(
    var location: String = "Sol",
    var credits: Int = 100,
    val cargo: MutableMap<String, Int> = mutableMapOf("ore" to 0, "food" to 0, "tech" to 0),
    var cargoLimit: Int = 10,
    var hull: Int = 100,
    var hullMax: Int = 100,
    var weapon: Int = 1,
    var shield: Float = 0.0f,
    var reputation: Int = 50,
    var kills: Int = 0
) {
    val totalCargo: Int
        get() = cargo.values.sum()

    val availableCargo: Int
        get() = (cargoLimit - totalCargo).coerceAtLeast(0)
}
