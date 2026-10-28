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

    init {
        _selectedSystem.value = _gameState.value.systems["Sol"]
    }

    fun startNewGame(mode: String = "strategy", factionId: String = "solarian") {
        stopTicker()
        val newState = GalaxyGenerator.createInitialState(playerFactionId = factionId, mode = mode)
        _gameState.value = newState
        _selectedSystem.value = newState.systems[newState.getPlayerFaction()?.capital ?: "Sol"] ?: newState.systems.values.firstOrNull()
        _activeDialog.value = null
        _rpgMessage.value = null
    }

    fun loadGame(context: Context) {
        viewModelScope.launch {
            // File I/O stays off the main thread; applicationContext avoids leaking the Activity.
            val appContext = context.applicationContext
            val loaded = withContext(Dispatchers.IO) { SaveLoadManager.loadGame(appContext) }
            if (loaded != null) {
                stopTicker()
                _gameState.value = loaded
                _selectedSystem.value = loaded.systems[loaded.playerShip.location] ?: loaded.systems.values.firstOrNull()
                _activeDialog.value = null
                _rpgMessage.value = null
                _saveFeedback.value = "Game loaded successfully."
            } else {
                _saveFeedback.value = "No saved game found."
            }
        }
    }

    fun saveGame(context: Context) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                SaveLoadManager.saveGame(context.applicationContext, _gameState.value)
            }
            _saveFeedback.value = if (ok) "Game saved (Sector Stardate ${String.format("%.1f", _gameState.value.stardate)})" else "Save failed."
        }
    }

    fun hasSaveGame(context: Context): Boolean {
        return SaveLoadManager.hasSaveFile(context)
    }

    fun togglePlay(context: Context? = null) {
        if (_isPlaying.value) {
            stopTicker()
        } else {
            startTicker(context?.applicationContext)
        }
    }

    fun stepTick(context: Context? = null) {
        viewModelScope.launch { executeTick(context?.applicationContext) }
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
        _gameState.value = curr.copy()
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
        _gameState.value = curr.copy()
    }

    // --- 4X Player Actions ---

    fun buildFleet(systemId: String, type: FleetType) {
        val curr = _gameState.value
        if (NebulaSimulationEngine.buildFleet(curr, systemId, type)) {
            _gameState.value = curr.copy()
        }
    }

    fun moveFleet(fleetId: String, destId: String) {
        val curr = _gameState.value
        if (NebulaSimulationEngine.moveFleet(curr, fleetId, destId)) {
            _gameState.value = curr.copy()
        }
    }

    fun researchTech(techId: String) {
        val curr = _gameState.value
        if (NebulaSimulationEngine.researchTech(curr, techId)) {
            _gameState.value = curr.copy()
        }
    }

    fun proposePeace(targetFactionId: String): String {
        val curr = _gameState.value
        val res = NebulaSimulationEngine.proposePeace(curr, targetFactionId)
        _gameState.value = curr.copy()
        return res
    }

    fun spyAction(targetFactionId: String): String {
        val curr = _gameState.value
        val res = NebulaSimulationEngine.spyAction(curr, targetFactionId)
        _gameState.value = curr.copy()
        return res
    }

    // --- RPG Player Actions ---

    fun rpgBuyItem(item: String, amount: Int) {
        val curr = _gameState.value
        if (NebulaSimulationEngine.rpgBuyItem(curr, item, amount)) {
            _gameState.value = curr.copy()
        }
    }

    fun rpgSellItem(item: String, amount: Int) {
        val curr = _gameState.value
        if (NebulaSimulationEngine.rpgSellItem(curr, item, amount)) {
            _gameState.value = curr.copy()
        }
    }

    fun rpgTravel(destinationId: String) {
        val curr = _gameState.value
        val (ok, msg) = NebulaSimulationEngine.rpgTravel(curr, destinationId)
        _rpgMessage.value = msg
        if (ok) {
            _selectedSystem.value = curr.systems[curr.playerShip.location]
        }
        _gameState.value = curr.copy()
    }

    fun rpgWormhole() {
        val curr = _gameState.value
        val (ok, msg) = NebulaSimulationEngine.rpgWormhole(curr)
        _rpgMessage.value = msg
        if (ok) {
            _selectedSystem.value = curr.systems[curr.playerShip.location]
        }
        _gameState.value = curr.copy()
    }

    fun rpgUpgrade(type: String) {
        val curr = _gameState.value
        val (_, msg) = NebulaSimulationEngine.rpgUpgrade(curr, type)
        _rpgMessage.value = msg
        _gameState.value = curr.copy()
    }

    fun rpgRepair() {
        val curr = _gameState.value
        val (_, msg) = NebulaSimulationEngine.rpgRepair(curr)
        _rpgMessage.value = msg
        _gameState.value = curr.copy()
    }

    fun rpgStartCombat() {
        val curr = _gameState.value
        val (_, msg) = NebulaSimulationEngine.rpgStartCombat(curr)
        _rpgMessage.value = msg
        _gameState.value = curr.copy()
    }

    fun rpgCombatAttack() {
        val curr = _gameState.value
        _rpgMessage.value = NebulaSimulationEngine.rpgCombatAttack(curr)
        _selectedSystem.value = curr.systems[curr.playerShip.location]
        _gameState.value = curr.copy()
    }

    fun rpgCombatFlee() {
        val curr = _gameState.value
        _rpgMessage.value = NebulaSimulationEngine.rpgCombatFlee(curr)
        _selectedSystem.value = curr.systems[curr.playerShip.location]
        _gameState.value = curr.copy()
    }

    private fun startTicker(context: Context?) {
        _isPlaying.value = true
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive && _isPlaying.value) {
                delay(_tickSpeedMs.value)
                executeTick(context)
            }
        }
    }

    private fun stopTicker() {
        _isPlaying.value = false
        tickerJob?.cancel()
        tickerJob = null
    }

    /**
     * One simulation turn. Engine mutations stay confined to the main dispatcher
     * (single-threaded state ownership); only blocking file I/O hops to Dispatchers.IO.
     */
    private suspend fun executeTick(context: Context?) {
        val curr = _gameState.value
        NebulaSimulationEngine.step(curr)
        if (context != null && curr.ticks % 50 == 0) {
            withContext(Dispatchers.IO) {
                SaveLoadManager.saveGame(context, curr)
            }
        }
        _gameState.value = curr.copy()
    }

    override fun onCleared() {
        super.onCleared()
        stopTicker()
    }
}
