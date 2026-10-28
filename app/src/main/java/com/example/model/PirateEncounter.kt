package com.example.model

/**
 * A persistent hostile contact engaged by the player ship.
 * The full encounter state is serialized so an in-progress fight can resume after loading.
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
