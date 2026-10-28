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
import com.example.model.FleetType
import com.example.model.GameState
import com.example.model.StarSystem
import com.example.ui.theme.*

@Composable
fun FleetBuilderDialog(
    system: StarSystem,
    gameState: GameState,
    lang: GameLanguage,
    onDismiss: () -> Unit,
    onBuild: (FleetType) -> Unit
) {
    val playerFaction = gameState.getPlayerFaction()
    val credits = playerFaction?.credits ?: 0

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
                    text = "🛠️ ${NebulaStrings.get("cmd_build_fleet", lang)}",
                    color = StellarGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${system.id} Shipyards | Treasury: $credits cr.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fleet_builder_dialog"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val availableTypes = listOf(
                    FleetType.SCOUT,
                    FleetType.FIGHTER,
                    FleetType.ASSAULT,
                    FleetType.BATTLESHIP,
                    FleetType.FLAGSHIP
                )

                items(availableTypes) { type ->
                    val canAfford = credits >= type.cost
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (canAfford) SpaceCardBorder else LaserRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        color = SpaceCardBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(type.icon, fontSize = 20.sp)
                                Column {
                                    Text(
                                        text = type.typeName,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "⚔️ Str: ${type.strength} | ⚡ Spd: ${type.speed}",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    onBuild(type)
                                    onDismiss()
                                },
                                enabled = canAfford,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TacticalCyan,
                                    contentColor = SpaceBlack,
                                    disabledContainerColor = SpaceCardBorder.copy(alpha = 0.5f),
                                    disabledContentColor = TextMuted
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = "${type.cost} cr.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
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
