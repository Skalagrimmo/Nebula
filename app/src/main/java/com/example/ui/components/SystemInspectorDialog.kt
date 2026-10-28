package com.example.ui.components

import androidx.compose.foundation.background
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
import com.example.model.StarSystem
import com.example.ui.theme.*

@Composable
fun SystemInspectorDialog(
    system: StarSystem,
    gameState: GameState,
    lang: GameLanguage,
    onDismiss: () -> Unit,
    onOpenBuild: (StarSystem) -> Unit,
    onMoveFleetTo: (String, String) -> Unit, // fleetId, targetSystemId
    onRpgTravelTo: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(NebulaStrings.get("menu_back", lang), color = TacticalCyan)
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(system.type.icon, fontSize = 20.sp)
                Column {
                    Text(
                        text = if (system.capital) "★ ${system.id} (Capital)" else system.id,
                        color = StellarGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = NebulaStrings.get("systype_${system.type.code}", lang),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("system_inspector_dialog")
            ) {
                // Owner & Status
                val ownerFaction = system.owner?.let { gameState.factions[it] }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SpaceCardBg, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${NebulaStrings.get("owner", lang)}:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = ownerFaction?.displayName ?: NebulaStrings.get("neutral", lang),
                        color = when (system.owner) {
                            "solarian" -> FactionABlue
                            "zergon" -> FactionBRed
                            "federation" -> FactionCGreen
                            else -> FactionNeutral
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricCard("👥 ${NebulaStrings.get("population", lang)}", "${system.population}")
                    MetricCard("📈 ${NebulaStrings.get("economy", lang)}", "${system.economy}")
                    MetricCard("⛏️ ${NebulaStrings.get("resources", lang)}", "${system.resources}")
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Commodity Prices
                Text(
                    text = "📊 ${NebulaStrings.get("market", lang)} (cr):",
                    color = TacticalCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ore: ${system.prices["ore"]} cr", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Food: ${system.prices["food"]} cr", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Tech: ${system.prices["tech"]} cr", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stationed Fleets
                val fleetsHere = gameState.fleets.filter { it.location == system.id }
                Text(
                    text = "⚓ Fleets (${fleetsHere.size}):",
                    color = TacticalCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (fleetsHere.isEmpty()) {
                    Text(
                        text = NebulaStrings.get("no_fleets", lang),
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    fleetsHere.take(3).forEach { fl ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${fl.type.icon} ${fl.type.typeName} (${gameState.factions[fl.owner]?.name ?: fl.owner})",
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                            Text(
                                "⚔️ ${fl.strength.toInt()}",
                                fontSize = 11.sp,
                                color = LaserRed,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                val isPlayerOwner = system.owner == gameState.playerFactionId
                if (gameState.mode == "strategy") {
                    if (isPlayerOwner) {
                        Button(
                            onClick = {
                                onDismiss()
                                onOpenBuild(system)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = TacticalCyan, contentColor = SpaceBlack),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🚀 ${NebulaStrings.get("cmd_build_fleet", lang)}")
                        }
                    } else {
                        // Check if player has adjacent fleet to attack/move
                        val adjacentPlayerFleets = gameState.fleets.filter {
                            it.owner == gameState.playerFactionId && system.neighbors.contains(it.location)
                        }
                        if (adjacentPlayerFleets.isNotEmpty()) {
                            Button(
                                onClick = {
                                    val fl = adjacentPlayerFleets.first()
                                    onMoveFleetTo(fl.id, system.id)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = LaserRed, contentColor = TextPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("⚔️ ${NebulaStrings.get("cmd_attack", lang)} / Advance Fleet")
                            }
                        }
                    }
                } else {
                    // RPG Mode
                    val isAdjacent = gameState.systems[gameState.playerShip.location]?.neighbors?.contains(system.id) == true
                    if (isAdjacent && gameState.playerShip.location != system.id) {
                        Button(
                            onClick = {
                                onRpgTravelTo(system.id)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = StellarGold, contentColor = SpaceBlack),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🚀 ${NebulaStrings.get("travel", lang)} to ${system.id}")
                        }
                    }
                }
            }
        },
        containerColor = SpaceDark,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun MetricCard(label: String, value: String) {
    Column(
        modifier = Modifier
            .background(SpaceCardBg, RoundedCornerShape(6.dp))
            .border(1.dp, SpaceCardBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 10.sp, color = TextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = FontFamily.Monospace)
    }
}
