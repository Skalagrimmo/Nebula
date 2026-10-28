package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.GalaxyGenerator
import com.example.engine.NebulaSimulationEngine
import com.example.engine.SaveLoadManager
import com.example.localization.GameLanguage
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class NebulaViewModel : ViewModel() {

    private val _gameState = MutableStateFlow(GalaxyGenerator.createInitialState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _tickSpeedMs = MutableStateFlow(1000L)
    val tickSpeedMs: StateFlow<Long> = _tickSpeedMs.asStateFlow()

    private val _currentLang = MutableStateFlow(GameLanguage.detectSystem())
    val currentLang: StateFlow<GameLanguage> = _currentLang.asStateFlow()

    private val _selectedSystem = MutableStateFlow<StarSystem?>(null)
    val selectedSystem: StateFlow<StarSystem?> = _selectedSystem.asStateFlow()

    private val _activeDialog = MutableStateFlow<String?>(null)
    val activeDialog: StateFlow<String?> = _activeDialog.asStateFlow()

    private val _saveFeedback = MutableStateFlow<String?>(null)
    val saveFeedback: StateFlow<String?> = _saveFeedback.asStateFlow()

    private val _rpgMessage = MutableStateFlow<String?>(null)
    val rpgMessage: StateFlow<String?> = _rpgMessage.asStateFlow()

    private var tickerJob: Job? = null
    private var autosaveJob: Job? = null
    private var applicationContext: Context? = null
    private val saveMutex = Mutex()
    private var stateRevision: Long = 0L
    private var isDirty: Boolean = false

    init {
        _selectedSystem.value = _gameState.value.systems["Sol"]
    }

    fun startNewGame(mode: String = "strategy", factionId: String = "solarian") {
        stopTicker()
        val newState = GalaxyGenerator.createInitialState(playerFactionId = factionId, mode = mode)
        publishState(newState)
        _selectedSystem.value = newState.systems[newState.getPlayerFaction()?.capital ?: "Sol"] ?: newState.systems.values.firstOrNull()
        _activeDialog.value = null
        _rpgMessage.value = null
    }

    fun loadGame(context: Context) {
        attachContext(context.applicationContext)
        viewModelScope.launch {
            val appContext = context.applicationContext
            val loaded = withContext(Dispatchers.IO) { SaveLoadManager.loadGame(appContext) }
            if (loaded != null) {
                stopTicker()
                publishState(loaded, markDirty = false)
                _selectedSystem.value = loaded.systems[loaded.playerShip.location] ?: loaded.systems.values.firstOrNull()
                _activeDialog.value = null
                _rpgMessage.value = null
                _saveFeedback.value = "Game loaded successfully."
            } else {
                _saveFeedback.value = "No saved game found."
            }
        }
    }

    /**
     * Attach the application context and start a low-frequency autosave loop.
     * Autosaving is dirty-state based, so idle games do not repeatedly write to disk.
     */
    fun attachContext(context: Context) {
        applicationContext = context.applicationContext
        if (autosaveJob?.isActive == true) return

        autosaveJob = viewModelScope.launch {
            while (isActive) {
                delay(AUTOSAVE_INTERVAL_MS)
                val appContext = applicationContext
                if (appContext != null && isDirty) {
                    saveSnapshot(appContext, showFeedback = false, force = false)
                }
            }
        }
    }

    fun saveGame(
        context: Context,
        showFeedback: Boolean = true,
        force: Boolean = true
    ) {
        attachContext(context.applicationContext)
        viewModelScope.launch {
            saveSnapshot(context.applicationContext, showFeedback, force)
        }
    }

    private suspend fun saveSnapshot(
        context: Context,
        showFeedback: Boolean,
        force: Boolean
    ) {
        if (!force && !isDirty) return

        // Encode while running on the state-owning dispatcher. Only the immutable
        // JSON snapshot crosses to IO, avoiding concurrent reads of mutable models.
        val snapshotState = _gameState.value
        val snapshotRevision = stateRevision
        val serialized = try {
            SaveLoadManager.serializeGame(snapshotState)
        } catch (e: Exception) {
            if (showFeedback) _saveFeedback.value = "Save failed."
            return
        }

        val ok = withContext(Dispatchers.IO) {
            saveMutex.withLock {
                SaveLoadManager.writeSerializedGame(context.applicationContext, serialized)
            }
        }
        if (ok && stateRevision == snapshotRevision) isDirty = false

        if (showFeedback) {
            _saveFeedback.value = if (ok) {
                "Game saved (Sector Stardate ${String.format("%.1f", snapshotState.stardate)})"
            } else {
                "Save failed."
            }
        }
    }

    fun hasSaveGame(context: Context): Boolean {
        return SaveLoadManager.hasSaveFile(context)
    }

    fun togglePlay(context: Context? = null) {
        context?.let { attachContext(it.applicationContext) }
        if (_isPlaying.value) {
            stopTicker()
        } else {
            startTicker()
        }
    }

    fun stepTick(context: Context? = null) {
        context?.let { attachContext(it.applicationContext) }
        viewModelScope.launch { executeTick() }
    }

    fun cycleSpeed() {
        _tickSpeedMs.value = when (_tickSpeedMs.value) {
            1000L -> 500L
            500L -> 250L
            else -> 1000L
        }
    }

    fun toggleLanguage() {
        _currentLang.value = if (_currentLang.value == GameLanguage.UK) GameLanguage.EN else GameLanguage.UK
    }

    fun switchMode(newMode: String) {
        val curr = _gameState.value
        curr.mode = newMode
        publishState(curr)
    }

    fun selectSystem(system: StarSystem?) {
        _selectedSystem.value = system
    }

    fun openDialog(dialogId: String?) {
        _activeDialog.value = dialogId
    }

    fun dismissDialog() {
        _activeDialog.value = null
    }

    fun clearFeedback() {
        _saveFeedback.value = null
    }

    fun clearRpgMessage() {
        _rpgMessage.value = null
    }

    fun continueInSandbox() {
        val curr = _gameState.value
        curr.sandboxMode = true
        curr.victoryAchieved = false
        publishState(curr)
    }

    // --- 4X Player Actions ---

    fun buildFleet(systemId: String, type: FleetType) {
        val curr = _gameState.value
        if (NebulaSimulationEngine.buildFleet(curr, systemId, type)) {
            publishState(curr)
        }
    }

    fun moveFleet(fleetId: String, destId: String) {
        val curr = _gameState.value
        if (NebulaSimulationEngine.moveFleet(curr, fleetId, destId)) {
            publishState(curr)
        }
    }

    fun researchTech(techId: String) {
        val curr = _gameState.value
        if (NebulaSimulationEngine.researchTech(curr, techId)) {
            publishState(curr)
        }
    }

    fun proposePeace(targetFactionId: String): String {
        val curr = _gameState.value
        val res = NebulaSimulationEngine.proposePeace(curr, targetFactionId)
        publishState(curr)
        return res
    }

    fun spyAction(targetFactionId: String): String {
        val curr = _gameState.value
        val res = NebulaSimulationEngine.spyAction(curr, targetFactionId)
        publishState(curr)
        return res
    }

    // --- RPG Player Actions ---

    fun rpgBuyItem(item: String, amount: Int) {
        val curr = _gameState.value
        if (NebulaSimulationEngine.rpgBuyItem(curr, item, amount)) {
            publishState(curr)
        }
    }

    fun rpgSellItem(item: String, amount: Int) {
        val curr = _gameState.value
        if (NebulaSimulationEngine.rpgSellItem(curr, item, amount)) {
            publishState(curr)
        }
    }

    fun rpgTravel(destinationId: String) {
        val curr = _gameState.value
        val (ok, msg) = NebulaSimulationEngine.rpgTravel(curr, destinationId)
        _rpgMessage.value = msg
        if (ok) {
            _selectedSystem.value = curr.systems[curr.playerShip.location]
        }
        publishState(curr)
    }

    fun rpgWormhole() {
        val curr = _gameState.value
        val (ok, msg) = NebulaSimulationEngine.rpgWormhole(curr)
        _rpgMessage.value = msg
        if (ok) {
            _selectedSystem.value = curr.systems[curr.playerShip.location]
        }
        publishState(curr)
    }

    fun rpgUpgrade(type: String) {
        val curr = _gameState.value
        val (_, msg) = NebulaSimulationEngine.rpgUpgrade(curr, type)
        _rpgMessage.value = msg
        publishState(curr)
    }

    fun rpgRepair() {
        val curr = _gameState.value
        val (_, msg) = NebulaSimulationEngine.rpgRepair(curr)
        _rpgMessage.value = msg
        publishState(curr)
    }

    fun rpgStartCombat() {
        val curr = _gameState.value
        val (_, msg) = NebulaSimulationEngine.rpgStartCombat(curr)
        _rpgMessage.value = msg
        publishState(curr)
    }

    fun rpgCombatAttack() {
        val curr = _gameState.value
        _rpgMessage.value = NebulaSimulationEngine.rpgCombatAttack(curr)
        _selectedSystem.value = curr.systems[curr.playerShip.location]
        publishState(curr)
    }

    fun rpgCombatFlee() {
        val curr = _gameState.value
        _rpgMessage.value = NebulaSimulationEngine.rpgCombatFlee(curr)
        _selectedSystem.value = curr.systems[curr.playerShip.location]
        publishState(curr)
    }

    private fun startTicker() {
        _isPlaying.value = true
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive && _isPlaying.value) {
                delay(_tickSpeedMs.value)
                executeTick()
            }
        }
    }

    private fun stopTicker() {
        _isPlaying.value = false
        tickerJob?.cancel()
        tickerJob = null
    }

    /**
     * One simulation turn. Engine mutations stay confined to the main dispatcher.
     * Periodic autosaves are handled independently from the tick loop.
     */
    private suspend fun executeTick() {
        val curr = _gameState.value
        NebulaSimulationEngine.step(curr)
        publishState(curr)
    }

    private fun publishState(state: GameState, markDirty: Boolean = true) {
        _gameState.value = state.copy()
        stateRevision += 1
        isDirty = markDirty
    }

    override fun onCleared() {
        stopTicker()
        autosaveJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val AUTOSAVE_INTERVAL_MS = 30_000L
    }
}
