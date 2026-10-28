package com.example.model

/**
 * A persistent hostile contact engaged by the player ship.
 * Lives in GameState while the fight is ongoing; intentionally not serialized
 * (an encounter never survives save/load).
 */
data class PirateEncounter(
    val id: String,
    val enemyName: String,
    var enemyHp: Int,
    val enemyHpMax: Int,
    val enemyDamage: Int,
    val bounty: Int,
    val salvageItem: String,
    val salvageAmount: Int,
    val questId: String? = null
)
