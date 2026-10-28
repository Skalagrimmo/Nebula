package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.GameLanguage
import com.example.localization.NebulaStrings
import com.example.ui.theme.*

@Composable
fun StrategyCommandDock(
    lang: GameLanguage,
    onOpenInspector: () -> Unit,
    onOpenBuild: () -> Unit,
    onOpenTech: () -> Unit,
    onOpenDiplomacy: () -> Unit,
    onOpenEvents: () -> Unit,
    onOpenAchievements: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("strategy_command_dock"),
        color = SpaceDark,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CommandButton(
                label = NebulaStrings.get("cmd_inspect", lang),
                icon = "📡",
                onClick = onOpenInspector,
                tag = "btn_inspect"
            )

            CommandButton(
                label = NebulaStrings.get("cmd_build_fleet", lang),
                icon = "🚀",
                onClick = onOpenBuild,
                tag = "btn_build_fleet",
                isAccent = true
            )

            CommandButton(
                label = NebulaStrings.get("cmd_tech", lang),
                icon = "🔬",
                onClick = onOpenTech,
                tag = "btn_tech"
            )

            CommandButton(
                label = NebulaStrings.get("cmd_diplomacy", lang),
                icon = "🤝",
                onClick = onOpenDiplomacy,
                tag = "btn_diplomacy"
            )

            CommandButton(
                label = NebulaStrings.get("cmd_events", lang),
                icon = "📜",
                onClick = onOpenEvents,
                tag = "btn_events"
            )

            CommandButton(
                label = NebulaStrings.get("cmd_achievements", lang),
                icon = "🏆",
                onClick = onOpenAchievements,
                tag = "btn_achievements"
            )
        }
    }
}

@Composable
private fun CommandButton(
    label: String,
    icon: String,
    onClick: () -> Unit,
    tag: String,
    isAccent: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(44.dp)
            .testTag(tag),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isAccent) TacticalCyan else SpaceCardBg,
            contentColor = if (isAccent) SpaceBlack else TextPrimary
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(icon, fontSize = 14.sp)
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
