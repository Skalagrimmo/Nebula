package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.*

@Composable
fun WarsAndEventsDialog(
    gameState: GameState,
    lang: GameLanguage,
    onDismiss: () -> Unit
) {
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
                    text = "📜 ${NebulaStrings.get("cmd_events", lang)} & Wars",
                    color = StellarGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sector Chronicle | Turn ${gameState.ticks}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .testTag("wars_and_events_dialog"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "${NebulaStrings.get("active_wars", lang)} (${gameState.activeWars.size})",
                        color = LaserRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (gameState.activeWars.isEmpty()) {
                    item {
                        Text(
                            text = NebulaStrings.get("no_wars", lang),
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    items(gameState.activeWars) { war ->
                        val factionA = gameState.factions[war.factionA]?.name ?: war.factionA
                        val factionB = gameState.factions[war.factionB]?.name ?: war.factionB
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, LaserRed.copy(alpha = 0.65f), RoundedCornerShape(8.dp)),
                            color = SpaceCardBg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "⚔️ $factionA  vs  $factionB",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${NebulaStrings.get("war_score", lang)}: ${war.scoreA} – ${war.scoreB}",
                                    color = LaserRed,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Started T${war.startTick} · ${(gameState.ticks - war.startTick).coerceAtLeast(0)} turns",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "${factionA}: ${war.exhaustionA}% · ${factionB}: ${war.exhaustionB}%",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = NebulaStrings.get("recent_events", lang),
                        color = StellarGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (gameState.events.isEmpty()) {
                    item {
                        Text(
                            text = NebulaStrings.get("no_events", lang),
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    items(gameState.events) { ev ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SpaceCardBorder, RoundedCornerShape(8.dp)),
                            color = SpaceCardBg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(ev.icon, fontSize = 18.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = ev.titleKey,
                                            color = TacticalCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "T${ev.tick}",
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = ev.messageKey,
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
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
}
