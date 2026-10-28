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
import com.example.model.TechCatalog
import com.example.ui.theme.*

@Composable
fun TechTreeDialog(
    gameState: GameState,
    lang: GameLanguage,
    onDismiss: () -> Unit,
    onResearchTech: (String) -> Unit
) {
    val playerFaction = gameState.getPlayerFaction()
    val credits = playerFaction?.credits ?: 0
    val researched = playerFaction?.tech ?: emptyList()

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
                    text = "🔬 ${NebulaStrings.get("tech_title", lang)}",
                    color = StellarGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Treasury: $credits cr. | Active Researches: ${researched.size}/${TechCatalog.TECHS.size}",
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
                    .testTag("tech_tree_dialog"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TechCatalog.TECHS) { tech ->
                    val isResearched = researched.contains(tech.id)
                    val canAfford = credits >= tech.cost

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isResearched) ShieldGreen.copy(alpha = 0.6f) else SpaceCardBorder,
                                RoundedCornerShape(8.dp)
                            ),
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
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(tech.icon, fontSize = 20.sp)
                                Column {
                                    Text(
                                        text = NebulaStrings.get(tech.nameKey, lang),
                                        color = if (isResearched) ShieldGreen else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "${NebulaStrings.get("cost", lang)}: ${tech.cost} cr.",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            if (isResearched) {
                                Text(
                                    text = "✓ ${NebulaStrings.get("tech_researched", lang)}",
                                    color = ShieldGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Button(
                                    onClick = { onResearchTech(tech.id) },
                                    enabled = canAfford,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TacticalCyan,
                                        contentColor = SpaceBlack,
                                        disabledContainerColor = SpaceCardBorder.copy(alpha = 0.5f),
                                        disabledContentColor = TextMuted
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        NebulaStrings.get("tech_research", lang),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
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
