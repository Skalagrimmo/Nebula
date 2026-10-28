package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.GameLanguage
import com.example.localization.NebulaStrings
import com.example.model.GameState
import com.example.model.RelationStatus
import com.example.ui.theme.*

@Composable
fun DiplomacyDialog(
    gameState: GameState,
    lang: GameLanguage,
    onDismiss: () -> Unit,
    onProposePeace: (String) -> String,
    onDemandConcession: (String, String) -> String,
    onDeclareWar: (String) -> String,
    onSpyAction: (String) -> String
) {
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var confirmWarAgainst by remember { mutableStateOf<String?>(null) }
    var demandConcessionAgainst by remember { mutableStateOf<String?>(null) }
    val playerFactionId = gameState.playerFactionId
    val otherFactions = gameState.factions.values.filter { it.id != playerFactionId }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(NebulaStrings.get("menu_back", lang), color = TacticalCyan)
            }
        },
        title = {
            Column {
                Text(
                    text = "🤝 ${NebulaStrings.get("diplomacy_title", lang)}",
                    color = StellarGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${NebulaStrings.get("player", lang)}: ${gameState.getPlayerFaction()?.name ?: playerFactionId}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().testTag("diplomacy_dialog")) {
                if (feedbackMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        color = TacticalCyan.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, TacticalCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = feedbackMessage!!,
                            color = TacticalCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(otherFactions) { faction ->
                        val rel = gameState.diplomacy[playerFactionId]?.get(faction.id)
                        val activeWar = gameState.activeWars.firstOrNull {
                            (it.factionA == playerFactionId && it.factionB == faction.id) ||
                                (it.factionA == faction.id && it.factionB == playerFactionId)
                        }
                        val status = rel?.status ?: RelationStatus.NEUTRAL
                        val statusColor = when (status) {
                            RelationStatus.ALLY -> ShieldGreen
                            RelationStatus.FRIENDLY -> TacticalCyan
                            RelationStatus.NEUTRAL -> TextSecondary
                            RelationStatus.SUSPICIOUS -> StellarGold
                            RelationStatus.HOSTILE -> LaserRed
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SpaceCardBorder, RoundedCornerShape(8.dp)),
                            color = SpaceCardBg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = faction.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Surface(
                                        color = statusColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "${status.icon} ${status.name}",
                                            color = statusColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${NebulaStrings.get("relation", lang)}: ${rel?.relation ?: 0}",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "${NebulaStrings.get("trust", lang)}: ${rel?.trust ?: 50}%",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Wars: ${rel?.wars ?: 0}",
                                        fontSize = 11.sp,
                                        color = if ((rel?.wars ?: 0) > 0) LaserRed else TextSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { feedbackMessage = onProposePeace(faction.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ShieldGreen, contentColor = SpaceBlack),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.weight(1f).height(32.dp)
                                    ) {
                                        Text(NebulaStrings.get("propose_peace", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { feedbackMessage = onSpyAction(faction.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SpaceDark, contentColor = TacticalCyan),
                                        border = BorderStroke(1.dp, TacticalCyan.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.weight(1f).height(32.dp)
                                    ) {
                                        Text(NebulaStrings.get("spy_action", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                if (activeWar == null) {
                                    OutlinedButton(
                                        onClick = { confirmWarAgainst = faction.id },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LaserRed),
                                        border = BorderStroke(1.dp, LaserRed.copy(alpha = 0.7f)),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.fillMaxWidth().height(32.dp)
                                    ) {
                                        Text(NebulaStrings.get("declare_war", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    val playerIsA = activeWar.factionA == playerFactionId
                                    val playerScore = if (playerIsA) activeWar.scoreA else activeWar.scoreB
                                    val opponentScore = if (playerIsA) activeWar.scoreB else activeWar.scoreA
                                    val opponentExhaustion = if (playerIsA) activeWar.exhaustionB else activeWar.exhaustionA
                                    val hasLeverage = playerScore >= opponentScore + 2 || opponentExhaustion >= 60
                                    val concessionSystems = if (faction.systems.size > 1) {
                                        gameState.systems.values.filter { it.owner == faction.id && !it.capital }
                                    } else emptyList()

                                    Text(
                                        text = "${NebulaStrings.get("war_active", lang)} · ${activeWar.scoreA} : ${activeWar.scoreB}",
                                        color = LaserRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${NebulaStrings.get("war_exhaustion", lang)}: ${activeWar.exhaustionA}% / ${activeWar.exhaustionB}%",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (hasLeverage && concessionSystems.isNotEmpty()) {
                                        OutlinedButton(
                                            onClick = { demandConcessionAgainst = faction.id },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StellarGold),
                                            border = BorderStroke(1.dp, StellarGold.copy(alpha = 0.7f)),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                            modifier = Modifier.fillMaxWidth().height(32.dp)
                                        ) {
                                            Text(NebulaStrings.get("demand_terms", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else if (!hasLeverage) {
                                        Text(
                                            NebulaStrings.get("concession_need_leverage", lang),
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = SpaceDark,
        shape = RoundedCornerShape(14.dp)
    )

    val targetId = confirmWarAgainst
    val targetFaction = targetId?.let { gameState.factions[it] }
    if (targetId != null && targetFaction != null) {
        AlertDialog(
            onDismissRequest = { confirmWarAgainst = null },
            title = {
                Text(NebulaStrings.get("confirm_declare_war", lang), color = LaserRed, fontWeight = FontWeight.Bold)
            },
            text = { Text("${NebulaStrings.get("confirm_war", lang)} ${targetFaction.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    feedbackMessage = onDeclareWar(targetId)
                    confirmWarAgainst = null
                }) {
                    Text(NebulaStrings.get("declare_war", lang), color = LaserRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmWarAgainst = null }) {
                    Text(NebulaStrings.get("menu_back", lang), color = TacticalCyan)
                }
            },
            containerColor = SpaceDark,
            shape = RoundedCornerShape(14.dp)
        )
    }

    val concessionTargetId = demandConcessionAgainst
    val concessionTarget = concessionTargetId?.let { gameState.factions[it] }
    val concessionSystems = if (concessionTargetId != null && (concessionTarget?.systems?.size ?: 0) > 1) {
        gameState.systems.values.filter { it.owner == concessionTargetId && !it.capital }
    } else emptyList()
    if (concessionTargetId != null && concessionTarget != null) {
        AlertDialog(
            onDismissRequest = { demandConcessionAgainst = null },
            title = {
                Text(NebulaStrings.get("demand_terms", lang), color = StellarGold, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "${NebulaStrings.get("choose_concession", lang)} ${concessionTarget.name}:",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(concessionSystems, key = { it.id }) { system ->
                            OutlinedButton(
                                onClick = {
                                    feedbackMessage = onDemandConcession(concessionTargetId, system.id)
                                    demandConcessionAgainst = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(system.id, color = StellarGold, fontWeight = FontWeight.Bold)
                                    Text(
                                        system.description,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                    if (concessionSystems.isEmpty()) {
                        Text(NebulaStrings.get("no_concession_systems", lang), color = TextSecondary, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { demandConcessionAgainst = null }) {
                    Text(NebulaStrings.get("menu_back", lang), color = TacticalCyan)
                }
            },
            containerColor = SpaceDark,
            shape = RoundedCornerShape(14.dp)
        )
    }
}
