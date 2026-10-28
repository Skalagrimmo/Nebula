package com.example.engine

import android.content.Context
import android.util.Log
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object SaveLoadManager {

    private const val SAVE_FILE_NAME = "savegame.json"
    private const val TAG = "SaveLoadManager"

    fun saveGame(context: Context, state: GameState): Boolean {
        return try {
            val root = JSONObject()
            root.put("version", state.version)
            root.put("ticks", state.ticks)
            root.put("stardate", state.stardate.toDouble())
            root.put("mode", state.mode)
            root.put("playerFactionId", state.playerFactionId)
            root.put("victoryAchieved", state.victoryAchieved)
            root.put("winnerFactionId", state.winnerFactionId ?: JSONObject.NULL)
            root.put("victoryType", state.victoryType ?: JSONObject.NULL)
            root.put("sandboxMode", state.sandboxMode)

            // Systems
            val systemsObj = JSONObject()
            for ((id, sys) in state.systems) {
                val sObj = JSONObject().apply {
                    put("id", sys.id)
                    put("owner", sys.owner ?: JSONObject.NULL)
                    put("capital", sys.capital)
                    put("population", sys.population)
                    put("economy", sys.economy)
                    put("resources", sys.resources)
                    put("stock", sys.stock)
                    put("demand", sys.demand)
                    put("posX", sys.posX.toDouble())
                    put("posY", sys.posY.toDouble())
                    put("type", sys.type.code)
                    put("neighbors", JSONArray(sys.neighbors))
                    val pricesObj = JSONObject()
                    sys.prices.forEach { (k, v) -> pricesObj.put(k, v) }
                    put("prices", pricesObj)
                }
                systemsObj.put(id, sObj)
            }
            root.put("systems", systemsObj)

            // Factions
            val factionsObj = JSONObject()
            for ((id, fac) in state.factions) {
                val fObj = JSONObject().apply {
                    put("id", fac.id)
                    put("name", fac.name)
                    put("displayName", fac.displayName)
                    put("capital", fac.capital ?: JSONObject.NULL)
                    put("credits", fac.credits)
                    put("military", fac.military.toDouble())
                    put("systems", JSONArray(fac.systems))
                    put("tech", JSONArray(fac.tech))
                    put("strategy", fac.strategy)
                    val statsObj = JSONObject().apply {
                        put("wins", fac.stats.wins)
                        put("losses", fac.stats.losses)
                        put("battles", fac.stats.battles)
                    }
                    put("stats", statsObj)
                }
                factionsObj.put(id, fObj)
            }
            root.put("factions", factionsObj)

            // Fleets
            val fleetsArr = JSONArray()
            for (fl in state.fleets) {
                val flObj = JSONObject().apply {
                    put("id", fl.id)
                    put("owner", fl.owner)
                    put("location", fl.location)
                    put("strength", fl.strength.toDouble())
                    put("speed", fl.speed.toDouble())
                    put("type", fl.type.code)
                    put("role", fl.role)
                }
                fleetsArr.put(flObj)
            }
            root.put("fleets", fleetsArr)

            // Player Ship
            val shipObj = JSONObject().apply {
                val ship = state.playerShip
                put("location", ship.location)
                put("credits", ship.credits)
                put("cargoLimit", ship.cargoLimit)
                put("hull", ship.hull)
                put("hullMax", ship.hullMax)
                put("weapon", ship.weapon)
                put("shield", ship.shield.toDouble())
                put("reputation", ship.reputation)
                put("kills", ship.kills)
                put("xp", ship.xp)
                put("level", ship.level)
                val cargoObj = JSONObject()
                ship.cargo.forEach { (k, v) -> cargoObj.put(k, v) }
                put("cargo", cargoObj)
            }
            root.put("playerShip", shipObj)

            // Quests
            val questsArr = JSONArray()
            for (q in state.quests) {
                val qObj = JSONObject().apply {
                    put("id", q.id)
                    put("type", q.type.code)
                    put("name", q.name)
                    put("desc", q.desc)
                    put("destination", q.destination)
                    put("item", q.item ?: JSONObject.NULL)
                    put("amount", q.amount)
                    put("reward", q.reward)
                    put("bonus", q.bonus)
                    put("status", q.status.name)
                    put("source", q.source)
                    put("killsNeeded", q.killsNeeded)
                    put("progress", q.progress)
                }
                questsArr.put(qObj)
            }
            root.put("quests", questsArr)

            // Achievements
            val achArr = JSONArray()
            for (ach in state.achievements) {
                val aObj = JSONObject().apply {
                    put("id", ach.id)
                    put("unlocked", ach.unlocked)
                    put("unlockedAt", ach.unlockedAt ?: JSONObject.NULL)
                }
                achArr.put(aObj)
            }
            root.put("achievements", achArr)

            val file = File(context.filesDir, SAVE_FILE_NAME)
            file.writeText(root.toString(2), Charsets.UTF_8)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save game", e)
            false
        }
    }

    fun hasSaveFile(context: Context): Boolean {
        return File(context.filesDir, SAVE_FILE_NAME).exists()
    }

    fun loadGame(context: Context): GameState? {
        return try {
            val file = File(context.filesDir, SAVE_FILE_NAME)
            if (!file.exists()) return null
            val jsonStr = file.readText(Charsets.UTF_8)
            val root = JSONObject(jsonStr)

            val state = GameState(
                version = root.optString("version", "1.0"),
                ticks = root.optInt("ticks", 1),
                stardate = root.optDouble("stardate", 3000.0).toFloat(),
                mode = root.optString("mode", "strategy"),
                playerFactionId = root.optString("playerFactionId", "solarian"),
                victoryAchieved = root.optBoolean("victoryAchieved", false),
                winnerFactionId = if (root.isNull("winnerFactionId")) null else root.optString("winnerFactionId"),
                victoryType = if (root.isNull("victoryType")) null else root.optString("victoryType"),
                sandboxMode = root.optBoolean("sandboxMode", false)
            )

            // Systems
            val sysObj = root.optJSONObject("systems")
            if (sysObj != null) {
                for (key in sysObj.keys()) {
                    val sObj = sysObj.getJSONObject(key)
                    val neighborsList = mutableListOf<String>()
                    val nArr = sObj.optJSONArray("neighbors")
                    if (nArr != null) {
                        for (i in 0 until nArr.length()) {
                            neighborsList.add(nArr.getString(i))
                        }
                    }
                    val pricesMap = mutableMapOf<String, Int>()
                    val pObj = sObj.optJSONObject("prices")
                    if (pObj != null) {
                        for (pk in pObj.keys()) {
                            pricesMap[pk] = pObj.getInt(pk)
                        }
                    }
                    state.systems[key] = StarSystem(
                        id = sObj.getString("id"),
                        owner = if (sObj.isNull("owner")) null else sObj.getString("owner"),
                        capital = sObj.optBoolean("capital", false),
                        population = sObj.optInt("population", 100),
                        economy = sObj.optInt("economy", 50),
                        resources = sObj.optInt("resources", 100),
                        stock = sObj.optInt("stock", 50),
                        demand = sObj.optInt("demand", 50),
                        posX = sObj.optDouble("posX", 10.0).toFloat(),
                        posY = sObj.optDouble("posY", 10.0).toFloat(),
                        neighbors = neighborsList,
                        type = SystemType.fromCode(sObj.optString("type", "forest")),
                        prices = pricesMap
                    )
                }
            }

            // Factions
            val facObj = root.optJSONObject("factions")
            if (facObj != null) {
                for (key in facObj.keys()) {
                    val fObj = facObj.getJSONObject(key)
                    val systemsList = mutableListOf<String>()
                    val sArr = fObj.optJSONArray("systems")
                    if (sArr != null) {
                        for (i in 0 until sArr.length()) systemsList.add(sArr.getString(i))
                    }
                    val techList = mutableListOf<String>()
                    val tArr = fObj.optJSONArray("tech")
                    if (tArr != null) {
                        for (i in 0 until tArr.length()) techList.add(tArr.getString(i))
                    }
                    val stObj = fObj.optJSONObject("stats")
                    val stats = FactionStats(
                        wins = stObj?.optInt("wins", 0) ?: 0,
                        losses = stObj?.optInt("losses", 0) ?: 0,
                        battles = stObj?.optInt("battles", 0) ?: 0
                    )
                    state.factions[key] = Faction(
                        id = fObj.getString("id"),
                        name = fObj.getString("name"),
                        displayName = fObj.optString("displayName", fObj.getString("name")),
                        capital = if (fObj.isNull("capital")) null else fObj.getString("capital"),
                        credits = fObj.optInt("credits", 100),
                        military = fObj.optDouble("military", 1.0).toFloat(),
                        systems = systemsList,
                        tech = techList,
                        stats = stats,
                        strategy = fObj.optString("strategy", "balanced")
                    )
                }
            }

            // Fleets
            val fleetsArr = root.optJSONArray("fleets")
            if (fleetsArr != null) {
                for (i in 0 until fleetsArr.length()) {
                    val flObj = fleetsArr.getJSONObject(i)
                    state.fleets.add(
                        Fleet(
                            id = flObj.getString("id"),
                            owner = flObj.getString("owner"),
                            location = flObj.getString("location"),
                            strength = flObj.optDouble("strength", 10.0).toFloat(),
                            speed = flObj.optDouble("speed", 1.0).toFloat(),
                            type = FleetType.fromCode(flObj.optString("type", "fighter")),
                            role = flObj.optString("role", "attack")
                        )
                    )
                }
            }

            // Player Ship
            val shipObj = root.optJSONObject("playerShip")
            if (shipObj != null) {
                val cargoMap = mutableMapOf<String, Int>()
                val cObj = shipObj.optJSONObject("cargo")
                if (cObj != null) {
                    for (ck in cObj.keys()) {
                        cargoMap[ck] = cObj.getInt(ck)
                    }
                }
                state.playerShip = PlayerShip(
                    location = shipObj.optString("location", "Sol"),
                    credits = shipObj.optInt("credits", 100),
                    cargo = cargoMap,
                    cargoLimit = shipObj.optInt("cargoLimit", 10),
                    hull = shipObj.optInt("hull", 100),
                    hullMax = shipObj.optInt("hullMax", 100),
                    weapon = shipObj.optInt("weapon", 1),
                    shield = shipObj.optDouble("shield", 0.0).toFloat(),
                    reputation = shipObj.optInt("reputation", 50),
                    kills = shipObj.optInt("kills", 0),
                    xp = shipObj.optInt("xp", 0),
                    level = shipObj.optInt("level", 1)
                )
            }

            // Quests
            val questsArr = root.optJSONArray("quests")
            if (questsArr != null) {
                for (i in 0 until questsArr.length()) {
                    val qObj = questsArr.getJSONObject(i)
                    val type = QuestType.values().firstOrNull { it.code == qObj.optString("type", "delivery") }
                        ?: QuestType.DELIVERY
                    val status = when (qObj.optString("status", "ACTIVE")) {
                        "COMPLETED" -> QuestStatus.COMPLETED
                        "FAILED" -> QuestStatus.FAILED
                        else -> QuestStatus.ACTIVE
                    }
                    state.quests.add(
                        Quest(
                            id = qObj.getString("id"),
                            type = type,
                            name = qObj.getString("name"),
                            desc = qObj.getString("desc"),
                            destination = qObj.getString("destination"),
                            item = if (qObj.isNull("item")) null else qObj.optString("item"),
                            amount = qObj.optInt("amount", 0),
                            reward = qObj.optInt("reward", 0),
                            bonus = qObj.optInt("bonus", 0),
                            status = status,
                            source = qObj.optString("source", "Sol"),
                            killsNeeded = qObj.optInt("killsNeeded", 3),
                            progress = qObj.optInt("progress", 0)
                        )
                    )
                }
            }

            // Achievements
            val achArr = root.optJSONArray("achievements")
            if (achArr != null) {
                for (i in 0 until achArr.length()) {
                    val aObj = achArr.getJSONObject(i)
                    val aId = aObj.getString("id")
                    val existing = state.achievements.firstOrNull { it.id == aId }
                    if (existing != null) {
                        existing.unlocked = aObj.optBoolean("unlocked", false)
                        if (!aObj.isNull("unlockedAt")) {
                            existing.unlockedAt = aObj.optLong("unlockedAt")
                        }
                    }
                }
            }

            state
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load game", e)
            null
        }
    }
}
