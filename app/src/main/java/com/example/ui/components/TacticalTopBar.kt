package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun TacticalTopBar(
    gameState: GameState,
    isPlaying: Boolean,
    tickSpeedMs: Long,
    currentLang: GameLanguage,
    onTogglePlay: () -> Unit,
    onStepTick: () -> Unit,
    onCycleSpeed: () -> Unit,
    onSwitchMode: (String) -> Unit,
    onToggleLanguage: () -> Unit,
    onSaveGame: () -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tactical_top_bar"),
        color = SpaceDark,
        tonalElevation = 4.dp,
        border = null
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp)) {
            // Row 1: Mode Switcher & Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Mode Toggle Pill
                Row(
                    modifier = Modifier
                        .background(SpaceCardBg, RoundedCornerShape(20.dp))
                        .border(1.dp, SpaceCardBorder, RoundedCornerShape(20.dp))
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = gameState.mode == "strategy",
                        onClick = { onSwitchMode("strategy") },
                        label = {
                            Text(
                                "🏛️ 4X",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TacticalCyan,
                            selectedLabelColor = SpaceBlack,
                            containerColor = Color.Transparent,
                            labelColor = TextSecondary
                        ),
                        border = null
                    )
                    FilterChip(
                        selected = gameState.mode == "rpg",
                        onClick = { onSwitchMode("rpg") },
                        label = {
                            Text(
                                "🚀 RPG",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StellarGold,
                            selectedLabelColor = SpaceBlack,
                            containerColor = Color.Transparent,
                            labelColor = TextSecondary
                        ),
                        border = null
                    )
                }

                // Stardate & Turn Counter
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "T${gameState.ticks} | ${String.format("%.1f", gameState.stardate)}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TacticalCyan
                    )
                }

                // Quick Tools (Save, Lang, Menu)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleLanguage,
                        modifier = Modifier.size(36.dp).testTag("lang_toggle_button")
                    ) {
                        Text(currentLang.flag, fontSize = 16.sp)
                    }

                    IconButton(
                        onClick = onSaveGame,
                        modifier = Modifier.size(36.dp).testTag("save_button")
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Save",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenMenu,
                        modifier = Modifier.size(36.dp).testTag("menu_button")
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2: Status Telemetry & Simulation Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Telemetry (Faction stats or Ship stats)
                if (gameState.mode == "strategy") {
                    val playerFaction = gameState.getPlayerFaction()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "💰 ${playerFaction?.credits ?: 0}",
                            color = StellarGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "🪐 ${playerFaction?.systems?.size ?: 0}",
                            color = TacticalCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "⚔️ ${String.format("%.1f", playerFaction?.military ?: 1f)}",
                            color = LaserRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    val ship = gameState.playerShip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "💰 ${ship.credits}",
                            color = StellarGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "🛡️ ${ship.hull}/${ship.hullMax}",
                            color = if (ship.hull > 40) ShieldGreen else LaserRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "📦 ${ship.totalCargo}/${ship.cargoLimit}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Turn Ticking Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Play/Pause button
                    FilledTonalIconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier.size(32.dp).testTag("play_pause_button"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (isPlaying) LaserRed.copy(alpha = 0.25f) else TacticalCyan.copy(alpha = 0.25f),
                            contentColor = if (isPlaying) LaserRed else TacticalCyan
                        )
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Single Step Button
                    FilledTonalIconButton(
                        onClick = onStepTick,
                        modifier = Modifier.size(32.dp).testTag("step_button"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = SpaceCardBg,
                            contentColor = TextPrimary
                        )
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Step",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Speed Toggle Button
                    TextButton(
                        onClick = onCycleSpeed,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Text(
                            text = when (tickSpeedMs) {
                                500L -> "2x"
                                250L -> "4x"
                                else -> "1x"
                            },
                            color = StellarGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
