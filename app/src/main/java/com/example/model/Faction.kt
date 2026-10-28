package com.example.model

data class FactionPersonality(
    var aggression: Float = 0.5f,
    var economyFocus: Float = 0.5f,
    var expansionFocus: Float = 0.5f,
    var defenseFocus: Float = 0.5f,
    var risk: Float = 0.5f
)

data class FactionStats(
    var wins: Int = 0,
    var losses: Int = 0,
    var battles: Int = 0
)

data class Faction(
    val id: String,
    var name: String,
    var displayName: String,
    var capital: String? = null,
    var credits: Int = 100,
    var military: Float = 1.0f,
    val systems: MutableList<String> = mutableListOf(),
    val tech: MutableList<String> = mutableListOf(),
    val stats: FactionStats = FactionStats(),
    val personality: FactionPersonality = FactionPersonality(),
    var strategy: String = "balanced",
    /** Simulation tick at which this faction last annexed an AI-controlled system. */
    var lastExpansionTick: Int = 0
)
