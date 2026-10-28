package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
fun VictoryDialog(
    gameState: GameState,
    lang: GameLanguage,
    onContinueSandbox: () -> Unit,
    onReturnToMenu: () -> Unit
) {
    val winner = gameState.factions[gameState.winnerFactionId] ?: gameState.getPlayerFaction()
    val isPlayer = gameState.winnerFactionId == gameState.playerFactionId
    val vTypeKey = when (gameState.victoryType) {
        "domination" -> "victory_domination"
        "economic" -> "victory_economic"
        "military" -> "victory_military"
        else -> "victory_domination"
    }

    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            Button(
                onClick = onContinueSandbox,
                colors = ButtonDefaults.buttonColors(containerColor = StellarGold, contentColor = SpaceBlack),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_continue_sandbox")
            ) {
                Text(
                    text = NebulaStrings.get("btn_continue_sandbox", lang),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onReturnToMenu,
                modifier = Modifier.testTag("btn_victory_menu")
            ) {
                Text(
                    text = NebulaStrings.get("btn_to_menu", lang),
                    color = TextSecondary
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("👑", fontSize = 42.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = NebulaStrings.get("victory_title", lang),
                    color = StellarGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("victory_dialog"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${winner?.name ?: "Unknown"} ${if (isPlayer) "🏆 VICTORY!" else "Triumphed"}",
                    color = if (isPlayer) ShieldGreen else LaserRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = NebulaStrings.get(vTypeKey, lang),
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Sector statistics
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SpaceCardBorder, RoundedCornerShape(8.dp)),
                    color = SpaceCardBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Galactic Summary: T${gameState.ticks} | ${gameState.systems.size} Systems",
                            color = TacticalCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        gameState.factions.values.forEach { f ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = f.displayName,
                                    fontSize = 11.sp,
                                    color = if (f.id == winner?.id) StellarGold else TextSecondary
                                )
                                Text(
                                    text = "🪐 ${f.systems.size} | 💰 ${f.credits} cr",
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
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
