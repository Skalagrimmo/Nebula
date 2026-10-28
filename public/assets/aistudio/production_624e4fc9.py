import random
from world.fleets import create_fleet
from world.fleet_types import FLEET_TYPES


def produce_fleets(state):
    """Виробництво флотів з урахуванням типу системи"""
    for fid, faction in state["factions"].items():
        capital = faction.get("capital")
        if not capital:
            continue
        
        credits = faction.get("credits", 0)
        
        # Вартість залежить від типу системи
        cost_modifier = 1.0
        if capital in state["systems"]:
            sys_type = state["systems"][capital].get("type", "forest")
            if sys_type == "industrial":
                cost_modifier = 0.7
            elif sys_type == "capital":
                cost_modifier = 0.8
            elif sys_type == "rich":
                cost_modifier = 0.9
        
        cost = int(200 * cost_modifier)
        
        if credits < cost:
            continue
        
        system_count = len(faction.get("systems", []))
        fleet_count = sum(1 for f in state["fleets"].values() if f["owner"] == fid)
        max_fleets = max(1, system_count * 2)
        
        if fleet_count >= max_fleets:
            continue
        
        faction["credits"] -= cost
        
        # Вибір типу флоту залежно від стратегії
        strategy = faction.get("strategy", "balanced")
        fleet_types = ["fighter", "assault"]
        
        if strategy == "defensive":
            fleet_types = ["battleship", "battleship", "fighter"]
        elif strategy == "aggressive":
            fleet_types = ["assault", "assault", "fighter"]
        elif strategy == "expansion":
            fleet_types = ["scout", "fighter", "assault"]
        elif strategy == "economic":
            fleet_types = ["fighter", "fighter", "battleship"]
        
        fleet_type = random.choice(fleet_types)
        
        create_fleet(state, fid, capital, fleet_type)
        
        from core.events import emit
        emit(state, "fleet_built", {
            "faction": fid,
            "fleet_type": FLEET_TYPES[fleet_type]["name"]
        })