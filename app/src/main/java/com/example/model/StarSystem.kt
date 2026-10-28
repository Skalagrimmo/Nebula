package com.example.model

data class StarSystem(
    val id: String,
    var owner: String? = null,
    var capital: Boolean = false,
    var population: Int = 100,
    var economy: Int = 50,
    var resources: Int = 100,
    var stock: Int = 50,
    var demand: Int = 50,
    var value: Float = 0f,
    val posX: Float = 0f, // 0..45 grid space
    val posY: Float = 0f, // 0..25 grid space
    val neighbors: MutableList<String> = mutableListOf(),
    var type: SystemType = SystemType.FOREST,
    var prices: MutableMap<String, Int> = mutableMapOf("ore" to 10, "food" to 5, "tech" to 20),
    var description: String = ""
)
