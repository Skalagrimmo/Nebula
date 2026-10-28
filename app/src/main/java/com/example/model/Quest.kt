package com.example.model

enum class QuestType(val code: String, val icon: String) {
    DELIVERY("delivery", "📦"),
    HUNT("hunt", "🎯"),
    EXPLORE("explore", "🧭"),
    ESCORT("escort", "🛡️"),
    RESCUE("rescue", "🆘")
}

enum class QuestStatus {
    ACTIVE,
    COMPLETED,
    FAILED
}

data class Quest(
    val id: String,
    val type: QuestType,
    val name: String,
    val desc: String,
    val destination: String,
    val item: String? = null,
    val amount: Int = 0,
    val reward: Int,
    val bonus: Int = 0,
    var status: QuestStatus = QuestStatus.ACTIVE,
    val source: String,
    val killsNeeded: Int = 3,
    var progress: Int = 0
)
