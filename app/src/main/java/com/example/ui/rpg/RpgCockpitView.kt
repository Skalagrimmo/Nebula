package com.example.ui.rpg

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GalaxyGenerator
import com.example.localization.GameLanguage
import com.example.localization.NebulaStrings
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun RpgCockpitView(
    gameState: GameState,
    lang: GameLanguage,
    onTravel: (String) -> Unit,
    onWormhole: () -> Unit,
    onBuyItem: (String, Int) -> Unit,
    onSellItem: (String, Int) -> Unit,
    onUpgrade: (String) -> Unit,
    onFightPirate: () -> Unit,
    onOpenStarMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ship = gameState.playerShip
    val currentSystem = gameState.systems[ship.location]
    var activeSubModal by remember { mutableStateOf<String?>(null) } // "market", "travel", "upgrade", "quests", "combat"
    var combatFeedback by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .padding(12.dp)
            .testTag("rpg_cockpit_view"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Current Star System Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SpaceDark,
            border = BorderStroke(1.dp, SpaceCardBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(currentSystem?.type?.icon ?: "🪐", fontSize = 28.sp)
                        Column {
                            Text(
                                text = "Sector: ${currentSystem?.id ?: "Unknown"}",
                                color = StellarGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentSystem?.description ?: "",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = onOpenStarMap,
                        colors = ButtonDefaults.buttonColors(containerColor = SpaceCardBg, contentColor = TacticalCyan),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("🗺️ Radar", fontSize = 11.sp)
                    }
                }
            }
        }

        // 2. Starship HUD Telemetry
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SpaceCardBg,
            border = BorderStroke(1.dp, TacticalCyan.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "🚀 ${NebulaStrings.get("ship_hud", lang)}",
                    color = TacticalCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                // Hull Bar
                val hullRatio = (ship.hull.toFloat() / ship.hullMax).coerceIn(0f, 1f)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🛡️ ${NebulaStrings.get("hull", lang)}", fontSize = 11.sp, color = TextSecondary)
                        Text("${ship.hull} / ${ship.hullMax} HP", fontSize = 11.sp, color = if (hullRatio > 0.4f) ShieldGreen else LaserRed, fontFamily = FontFamily.Monospace)
                    }
                    LinearProgressIndicator(
                        progress = { hullRatio },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = if (hullRatio > 0.4f) ShieldGreen else LaserRed,
                        trackColor = SpaceDark
                    )
                }

                // Gauges Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    GaugeItem("⚡ ${NebulaStrings.get("weapon", lang)}", "T${ship.weapon}")
                    GaugeItem("🛡️ ${NebulaStrings.get("shield", lang)}", "${(ship.shield * 100).toInt()}%")
                    GaugeItem("📦 ${NebulaStrings.get("cargo", lang)}", "${ship.totalCargo}/${ship.cargoLimit}")
                    GaugeItem("💰 ${NebulaStrings.get("credits", lang)}", "${ship.credits}")
                }
            }
        }

        // 3. Cockpit Operations Grid
        Text(
            text = "Station Controls",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CockpitStationCard(
                title = NebulaStrings.get("market", lang),
                icon = "💰",
                subtitle = "Buy / Sell",
                onClick = { activeSubModal = "market" },
                modifier = Modifier.weight(1f)
            )
            CockpitStationCard(
                title = NebulaStrings.get("travel", lang),
                icon = "🌌",
                subtitle = "Jump / Wormhole",
                onClick = { activeSubModal = "travel" },
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CockpitStationCard(
                title = NebulaStrings.get("upgrade", lang),
                icon = "🛠️",
                subtitle = "Shipyard",
                onClick = { activeSubModal = "upgrade" },
                modifier = Modifier.weight(1f)
            )
            CockpitStationCard(
                title = NebulaStrings.get("quests", lang),
                icon = "📜",
                subtitle = "Contracts (${gameState.quests.count { it.status == QuestStatus.ACTIVE }})",
                onClick = { activeSubModal = "quests" },
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CockpitStationCard(
                title = NebulaStrings.get("combat_title", lang),
                icon = "⚔️",
                subtitle = "Hunt Corsairs",
                onClick = { activeSubModal = "combat" },
                modifier = Modifier.weight(1f),
                accentColor = LaserRed
            )
        }
    }

    // --- Sub-Modals ---

    // 1. Market Dialog
    if (activeSubModal == "market" && currentSystem != null) {
        AlertDialog(
            onDismissRequest = { activeSubModal = null },
            confirmButton = {
                TextButton(onClick = { activeSubModal = null }) {
                    Text(NebulaStrings.get("menu_back", lang), color = TacticalCyan)
                }
            },
            title = {
                Column {
                    Text("💰 ${NebulaStrings.get("market", lang)}: ${currentSystem.id}", color = StellarGold, fontWeight = FontWeight.Bold)
                    Text("Cargo: ${ship.totalCargo}/${ship.cargoLimit} | Credits: ${ship.credits} cr", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("ore", "food", "tech").forEach { item ->
                        val price = currentSystem.prices[item] ?: 10
                        val owned = ship.cargo[item] ?: 0
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = SpaceCardBg,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, SpaceCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(NebulaStrings.get("item_$item", lang), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Price: $price cr. | Hold: $owned", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { onBuyItem(item, 1) },
                                        enabled = ship.credits >= price && ship.availableCargo > 0,
                                        colors = ButtonDefaults.buttonColors(containerColor = TacticalCyan, contentColor = SpaceBlack),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("+1 Buy", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { onSellItem(item, 1) },
                                        enabled = owned > 0,
                                        colors = ButtonDefaults.buttonColors(containerColor = StellarGold, contentColor = SpaceBlack),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("-1 Sell", fontSize = 10.sp, fontWeight = FontWeight.Bold)
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

    // 2. Travel Dialog
    if (activeSubModal == "travel" && currentSystem != null) {
        AlertDialog(
            onDismissRequest = { activeSubModal = null },
            confirmButton = {
                TextButton(onClick = { activeSubModal = null }) {
                    Text(NebulaStrings.get("menu_back", lang), color = TacticalCyan)
                }
            },
            title = {
                Text("🌌 ${NebulaStrings.get("travel", lang)}", color = StellarGold, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Hyperlane Connections:", color = TextSecondary, fontSize = 12.sp)
                    currentSystem.neighbors.forEach { nId ->
                        Button(
                            onClick = {
                                onTravel(nId)
                                activeSubModal = null
                            },
                            modifier = Modifier.fillMaxWidth().height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SpaceCardBg, contentColor = TacticalCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🚀 Jump to $nId", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Wormhole
                    val hasWormhole = GalaxyGenerator.WORMHOLES.containsKey(currentSystem.id)
                    if (hasWormhole) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val dest = GalaxyGenerator.WORMHOLES[currentSystem.id]
                        Button(
                            onClick = {
                                onWormhole()
                                activeSubModal = null
                            },
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple, contentColor = TextPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🌀 Wormhole to $dest", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            containerColor = SpaceDark,
            shape = RoundedCornerShape(14.dp)
        )
    }

    // 3. Upgrades Dialog
    if (activeSubModal == "upgrade") {
        AlertDialog(
            onDismissRequest = { activeSubModal = null },
            confirmButton = {
                TextButton(onClick = { activeSubModal = null }) {
                    Text(NebulaStrings.get("menu_back", lang), color = TacticalCyan)
                }
            },
            title = {
                Text("🛠️ ${NebulaStrings.get("upgrade", lang)} | ${ship.credits} cr.", color = StellarGold, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UpgradeItem(NebulaStrings.get("up_cargo", lang), ship.credits >= 50) { onUpgrade("cargo") }
                    UpgradeItem(NebulaStrings.get("up_hull", lang), ship.credits >= 70) { onUpgrade("hull") }
                    UpgradeItem(NebulaStrings.get("up_weapon", lang), ship.credits >= 100) { onUpgrade("weapon") }
                    UpgradeItem(NebulaStrings.get("up_shield", lang), ship.credits >= 120) { onUpgrade("shield") }
                }
            },
            containerColor = SpaceDark,
            shape = RoundedCornerShape(14.dp)
        )
    }

    // 4. Quests Dialog
    if (activeSubModal == "quests") {
        AlertDialog(
            onDismissRequest = { activeSubModal = null },
            confirmButton = {
                TextButton(onClick = { activeSubModal = null }) {
                    Text(NebulaStrings.get("menu_back", lang), color = TacticalCyan)
                }
            },
            title = {
                Text("📜 ${NebulaStrings.get("quests", lang)}", color = StellarGold, fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 350.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(gameState.quests) { q ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = SpaceCardBg,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (q.status == QuestStatus.COMPLETED) ShieldGreen else SpaceCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${q.type.icon} ${q.name}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        text = when (q.status) {
                                            QuestStatus.ACTIVE -> "Active"
                                            QuestStatus.COMPLETED -> "✓ Done"
                                            QuestStatus.FAILED -> "Failed"
                                        },
                                        color = if (q.status == QuestStatus.COMPLETED) ShieldGreen else StellarGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(q.desc, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                                Text("Reward: +${q.reward} cr.", color = StellarGold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            },
            containerColor = SpaceDark,
            shape = RoundedCornerShape(14.dp)
        )
    }

    // 5. Combat Dialog
    if (activeSubModal == "combat") {
        AlertDialog(
            onDismissRequest = { activeSubModal = null },
            confirmButton = {
                TextButton(onClick = { activeSubModal = null }) {
                    Text(NebulaStrings.get("menu_back", lang), color = TacticalCyan)
                }
            },
            title = {
                Text("☠️ Corsair Engagement", color = LaserRed, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Hostile pirate raiders detected in sector scan. Weapons online.", color = TextSecondary, fontSize = 12.sp)

                    if (combatFeedback != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = SpaceBlack,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, TacticalCyan.copy(alpha = 0.5f))
                        ) {
                            Text(combatFeedback!!, color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(8.dp))
                        }
                    }

                    Button(
                        onClick = {
                            onFightPirate()
                            combatFeedback = "Volley fired! Sensors update telemetry above."
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LaserRed, contentColor = TextPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("⚔️ Open Fire! (Laser T${ship.weapon})", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            activeSubModal = null
                            combatFeedback = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SpaceCardBg, contentColor = TextSecondary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Emergency Warp Away", fontSize = 12.sp)
                    }
                }
            },
            containerColor = SpaceDark,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun GaugeItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = TextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun CockpitStationCard(
    title: String,
    icon: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = TacticalCyan
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        color = SpaceCardBg,
        border = BorderStroke(1.dp, SpaceCardBorder),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(icon, fontSize = 24.sp)
            Column {
                Text(title, color = accentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(subtitle, color = TextSecondary, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun UpgradeItem(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(38.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TacticalCyan,
            contentColor = SpaceBlack,
            disabledContainerColor = SpaceCardBg,
            disabledContentColor = TextMuted
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
