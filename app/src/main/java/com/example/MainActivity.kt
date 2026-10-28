package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.localization.GameLanguage
import com.example.localization.NebulaStrings
import com.example.model.FleetType
import com.example.model.StarSystem
import com.example.ui.components.*
import com.example.ui.rpg.RpgCockpitView
import com.example.ui.theme.*
import com.example.viewmodel.NebulaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NebulaTheme {
                NebulaGameApp()
            }
        }
    }
}

@Composable
fun NebulaGameApp(
    viewModel: NebulaViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val tickSpeedMs by viewModel.tickSpeedMs.collectAsStateWithLifecycle()
    val currentLang by viewModel.currentLang.collectAsStateWithLifecycle()
    val selectedSystem by viewModel.selectedSystem.collectAsStateWithLifecycle()
    val activeDialog by viewModel.activeDialog.collectAsStateWithLifecycle()
    val saveFeedback by viewModel.saveFeedback.collectAsStateWithLifecycle()
    val rpgMessage by viewModel.rpgMessage.collectAsStateWithLifecycle()

    LaunchedEffect(context, viewModel) {
        viewModel.attachContext(context.applicationContext)
    }

    DisposableEffect(lifecycleOwner, context, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                // Persist a final checkpoint when the app leaves the foreground.
                viewModel.saveGame(context.applicationContext, showFeedback = false, force = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var showMainMenu by remember { mutableStateOf(false) }
    var showFactionSelect by remember { mutableStateOf(false) }
    var showHowToPlay by remember { mutableStateOf(false) }
    var selectedBuildSystem by remember { mutableStateOf<StarSystem?>(null) }

    // Intercept back button to return to 4X map or open menu
    BackHandler {
        when {
            activeDialog != null -> viewModel.dismissDialog()
            showMainMenu -> showMainMenu = false
            showFactionSelect -> showFactionSelect = false
            showHowToPlay -> showHowToPlay = false
            gameState.mode == "rpg" -> viewModel.switchMode("strategy")
            else -> showMainMenu = true
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack),
        topBar = {
            TacticalTopBar(
                gameState = gameState,
                isPlaying = isPlaying,
                tickSpeedMs = tickSpeedMs,
                currentLang = currentLang,
                onTogglePlay = { viewModel.togglePlay(context) },
                onStepTick = { viewModel.stepTick(context) },
                onCycleSpeed = { viewModel.cycleSpeed() },
                onSwitchMode = { newMode -> viewModel.switchMode(newMode) },
                onToggleLanguage = { viewModel.toggleLanguage() },
                onSaveGame = { viewModel.saveGame(context) },
                onOpenMenu = { showMainMenu = true },
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        bottomBar = {
            if (gameState.mode == "strategy") {
                StrategyCommandDock(
                    lang = currentLang,
                    onOpenInspector = {
                        val sys = selectedSystem ?: gameState.systems.values.firstOrNull()
                        if (sys != null) {
                            viewModel.selectSystem(sys)
                            viewModel.openDialog("inspector")
                        }
                    },
                    onOpenBuild = {
                        val capitalSys = gameState.systems[gameState.getPlayerFaction()?.capital ?: "Sol"]
                            ?: gameState.systems.values.firstOrNull { it.owner == gameState.playerFactionId }
                        if (capitalSys != null) {
                            selectedBuildSystem = capitalSys
                            viewModel.openDialog("build")
                        }
                    },
                    onOpenTech = { viewModel.openDialog("tech") },
                    onOpenDiplomacy = { viewModel.openDialog("diplomacy") },
                    onOpenEvents = { viewModel.openDialog("events") },
                    onOpenAchievements = { viewModel.openDialog("achievements") },
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                )
            }
        },
        snackbarHost = {
            if (saveFeedback != null) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .testTag("save_snackbar"),
                    action = {
                        TextButton(onClick = { viewModel.clearFeedback() }) {
                            Text("OK", color = TacticalCyan)
                        }
                    },
                    containerColor = SpaceCardBg,
                    contentColor = TextPrimary
                ) {
                    Text(saveFeedback ?: "")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Mode Viewport
            if (gameState.mode == "strategy") {
                TacticalStarMap(
                    gameState = gameState,
                    selectedSystem = selectedSystem,
                    onSystemSelected = { sys ->
                        viewModel.selectSystem(sys)
                        viewModel.openDialog("inspector")
                    }
                )
            } else {
                RpgCockpitView(
                    gameState = gameState,
                    lang = currentLang,
                    rpgMessage = rpgMessage,
                    onTravel = { destId -> viewModel.rpgTravel(destId) },
                    onWormhole = { viewModel.rpgWormhole() },
                    onBuyItem = { item, amt -> viewModel.rpgBuyItem(item, amt) },
                    onSellItem = { item, amt -> viewModel.rpgSellItem(item, amt) },
                    onUpgrade = { type -> viewModel.rpgUpgrade(type) },
                    onRepair = { viewModel.rpgRepair() },
                    onStartCombat = { viewModel.rpgStartCombat() },
                    onCombatAttack = { viewModel.rpgCombatAttack() },
                    onCombatFlee = { viewModel.rpgCombatFlee() },
                    onClearMessage = { viewModel.clearRpgMessage() },
                    onOpenStarMap = { viewModel.switchMode("strategy") }
                )
            }
        }
    }

    // --- Dialogs & Overlays ---

    // 1. System Inspector
    val inspectorSystem = selectedSystem
    if (activeDialog == "inspector" && inspectorSystem != null) {
        SystemInspectorDialog(
            system = inspectorSystem,
            gameState = gameState,
            lang = currentLang,
            onDismiss = { viewModel.dismissDialog() },
            onOpenBuild = { sys ->
                selectedBuildSystem = sys
                viewModel.openDialog("build")
            },
            onMoveFleetTo = { fleetId, destId ->
                viewModel.moveFleet(fleetId, destId)
            },
            onRpgTravelTo = { destId ->
                viewModel.rpgTravel(destId)
            }
        )
    }

    // 2. Fleet Builder
    if (activeDialog == "build") {
        selectedBuildSystem?.let { buildSystem ->
            FleetBuilderDialog(
                system = buildSystem,
                gameState = gameState,
                lang = currentLang,
                onDismiss = { viewModel.dismissDialog() },
                onBuild = { type ->
                    viewModel.buildFleet(buildSystem.id, type)
                }
            )
        }
    }

    // 3. Tech Tree
    if (activeDialog == "tech") {
        TechTreeDialog(
            gameState = gameState,
            lang = currentLang,
            onDismiss = { viewModel.dismissDialog() },
            onResearchTech = { techId ->
                viewModel.researchTech(techId)
            }
        )
    }

    // 4. Diplomacy
    if (activeDialog == "diplomacy") {
        DiplomacyDialog(
            gameState = gameState,
            lang = currentLang,
            onDismiss = { viewModel.dismissDialog() },
            onProposePeace = { targetId ->
                viewModel.proposePeace(targetId)
            },
            onSpyAction = { targetId ->
                viewModel.spyAction(targetId)
            }
        )
    }

    // 5. Wars & Events
    if (activeDialog == "events") {
        WarsAndEventsDialog(
            gameState = gameState,
            lang = currentLang,
            onDismiss = { viewModel.dismissDialog() }
        )
    }

    // 6. Achievements
    if (activeDialog == "achievements") {
        AchievementsDialog(
            gameState = gameState,
            lang = currentLang,
            onDismiss = { viewModel.dismissDialog() }
        )
    }

    // 7. Victory Dialog
    if (gameState.victoryAchieved && !gameState.sandboxMode) {
        VictoryDialog(
            gameState = gameState,
            lang = currentLang,
            onContinueSandbox = { viewModel.continueInSandbox() },
            onReturnToMenu = {
                viewModel.continueInSandbox()
                showMainMenu = true
            }
        )
    }

    // 8. Main Menu Modal
    if (showMainMenu) {
        AlertDialog(
            onDismissRequest = { showMainMenu = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMainMenu = false }) {
                    Text(NebulaStrings.get("menu_back", currentLang), color = TacticalCyan)
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "🌌 ${NebulaStrings.get("app_name", currentLang)}",
                        color = StellarGold,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = NebulaStrings.get("app_subtitle", currentLang),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().testTag("main_menu_modal"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            showMainMenu = false
                            showFactionSelect = true
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TacticalCyan, contentColor = SpaceBlack),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(NebulaStrings.get("menu_new_4x", currentLang), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showMainMenu = false
                            viewModel.startNewGame(mode = "rpg", factionId = "solarian")
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StellarGold, contentColor = SpaceBlack),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(NebulaStrings.get("menu_new_rpg", currentLang), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.loadGame(context)
                            showMainMenu = false
                        },
                        enabled = viewModel.hasSaveGame(context),
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SpaceCardBg, contentColor = TextPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(NebulaStrings.get("menu_load", currentLang))
                    }

                    Button(
                        onClick = {
                            showMainMenu = false
                            showHowToPlay = true
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SpaceCardBg, contentColor = TextSecondary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(NebulaStrings.get("menu_how_to_play", currentLang))
                    }

                    OutlinedButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SpaceCardBorder)
                    ) {
                        Text(
                            "${NebulaStrings.get("menu_language", currentLang)}: ${currentLang.flag} ${currentLang.displayName}",
                            color = TacticalCyan
                        )
                    }
                }
            },
            containerColor = SpaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 9. Faction Selection Dialog (for New 4X Game)
    if (showFactionSelect) {
        AlertDialog(
            onDismissRequest = { showFactionSelect = false },
            confirmButton = {
                TextButton(onClick = { showFactionSelect = false }) {
                    Text(NebulaStrings.get("menu_back", currentLang), color = TacticalCyan)
                }
            },
            title = {
                Text("👑 ${NebulaStrings.get("select_faction", currentLang)}", color = StellarGold, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val factionsList = listOf(
                        Triple("solarian", "👑 Solarian Empire", FactionABlue),
                        Triple("zergon", "🐝 Zergon Hive", FactionBRed),
                        Triple("federation", "🌌 Celestial Federation", FactionCGreen)
                    )

                    factionsList.forEach { (fid, fname, fcolor) ->
                        Button(
                            onClick = {
                                showFactionSelect = false
                                viewModel.startNewGame(mode = "strategy", factionId = fid)
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SpaceCardBg, contentColor = fcolor),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, fcolor.copy(alpha = 0.5f))
                        ) {
                            Text(fname, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            },
            containerColor = SpaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 10. How To Play Modal
    if (showHowToPlay) {
        AlertDialog(
            onDismissRequest = { showHowToPlay = false },
            confirmButton = {
                Button(
                    onClick = { showHowToPlay = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TacticalCyan, contentColor = SpaceBlack)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text("📖 ${NebulaStrings.get("guide_title", currentLang)}", color = StellarGold, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = NebulaStrings.get("guide_text", currentLang),
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            },
            containerColor = SpaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
