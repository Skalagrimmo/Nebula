package com.example.model

data class GalacticEvent(
    val id: String,
    val tick: Int,
    val titleKey: String,
    val messageKey: String,
    val systemId: String? = null,
    val factionId: String? = null,
    val icon: String = "📡",
    val effectDesc: String = ""
)
