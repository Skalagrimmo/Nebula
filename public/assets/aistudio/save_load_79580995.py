import json
import os
from datetime import datetime
from world.achievements import init_achievements


def save_game(state, filename="savegame.json"):
    try:
        save_data = {
            "version": state.get("version", "3.2"),
            "time": state["time"],
            "ticks": state.get("ticks", 0),
            "systems": state["systems"],
            "factions": state["factions"],
            "fleets": state["fleets"],
            "armies": state.get("armies", {}),
            "frontlines": list(state.get("frontlines", set())),
            "events": state.get("events", []),
            "epochs": state.get("epochs", []),
            "history": state.get("history", []),
            "faction_history": state.get("faction_history", {}),
            "diplomacy": state.get("diplomacy", {}),
            "mode": state.get("mode", "strategy"),
            "ship": state.get("ship", {}),
            "quests": state.get("quests", []),
            "achievements": state.get("achievements", {}),  # 🆕
            "saved_at": datetime.now().isoformat()
        }
        with open(filename, 'w', encoding='utf-8') as f:
            json.dump(save_data, f, indent=2, ensure_ascii=False)
        return True, f"Game saved to {filename}"
    except Exception as e:
        return False, f"Save failed: {e}"


def load_game(filename="savegame.json"):
    try:
        if not os.path.exists(filename):
            return None, f"File {filename} not found"
        with open(filename, 'r', encoding='utf-8') as f:
            data = json.load(f)
        if "frontlines" in data:
            data["frontlines"] = set(data["frontlines"])
        if "mode" not in data:
            data["mode"] = "strategy"
        if "ship" not in data:
            from rpg.ship import create_ship
            data["ship"] = create_ship()
        if "quests" not in data:
            data["quests"] = []
        if "achievements" not in data:
            init_achievements(data)
        return data, f"Game loaded from {filename}"
    except Exception as e:
        return None, f"Load failed: {e}"