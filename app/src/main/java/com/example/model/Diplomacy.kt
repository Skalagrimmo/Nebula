package com.example.model

enum class RelationStatus(val code: String, val icon: String) {
    ALLY("ally", "🛡️"),
    FRIENDLY("friendly", "🤝"),
    NEUTRAL("neutral", "⚖️"),
    SUSPICIOUS("suspicious", "👀"),
    HOSTILE("hostile", "⚔️");

    companion object {
        fun fromRelation(relation: Int): RelationStatus {
            return when {
                relation >= 60 -> ALLY
                relation >= 20 -> FRIENDLY
                relation >= -20 -> NEUTRAL
                relation >= -60 -> SUSPICIOUS
                else -> HOSTILE
            }
        }
    }
}

data class DiplomacyRelation(
    var relation: Int = 0,
    var trust: Int = 50,
    var wars: Int = 0,
    var status: RelationStatus = RelationStatus.NEUTRAL
)
