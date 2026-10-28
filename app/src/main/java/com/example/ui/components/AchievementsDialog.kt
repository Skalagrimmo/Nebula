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
import com.example.ui.theme.*

@Composable
fun AchievementsDialog(
    gameState: GameState,
    lang: GameLanguage,
    onDismiss: () -> Unit
) {
    val unlockedCount = gameState.achievements.count { it.unlocked }
    val totalCount = gameState.achievements.size

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
                    text = "🏆 ${NebulaStrings.get("ach_title", lang)}",
                    color = StellarGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Progress: $unlockedCount / $totalCount unlocked",
                    color = ShieldGreen,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .testTag("achievements_dialog"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(gameState.achievements) { ach ->
                    val isUnlocked = ach.unlocked
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isUnlocked) StellarGold.copy(alpha = 0.5f) else SpaceCardBorder,
                                RoundedCornerShape(8.dp)
                            ),
                        color = if (isUnlocked) SpaceCardBg else SpaceDark,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(ach.icon, fontSize = 24.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = NebulaStrings.get(ach.nameKey, lang),
                                    color = if (isUnlocked) StellarGold else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = NebulaStrings.get(ach.descKey, lang),
                                    color = if (isUnlocked) TextPrimary else TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = if (isUnlocked) "✅" else "⬜",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = SpaceDark,
        shape = RoundedCornerShape(14.dp)
    )
}
