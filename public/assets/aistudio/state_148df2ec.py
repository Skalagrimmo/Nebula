from world.galaxy import generate_galaxy
from world.factions import generate_factions
from world.capitals import assign_capitals
from world.diplomacy_init import init_diplomacy


def create_state():
    state = {
        "version": "3.2",
        "time": 0,
        "ticks": 0,
        "systems": generate_galaxy(),
        "factions": generate_factions(),
        "fleets": {},
        "armies": {},
        "frontlines": set(),
        "events": [],
        "epochs": [],
        "history": [],
        "faction_history": {},
        "diplomacy": {}
    }
    
    assign_capitals(state)
    init_diplomacy(state)
    
    return state


def get_army_power(state, army):
    power = 0
    for fid in army["fleets"]:
        fleet = state["fleets"].get(fid)
        if fleet:
            power += fleet["strength"]
    return power


def choose_army_target(state, army):
    systems = state["systems"]
    owner = army["owner"]
    
    enemies = [
        sid for sid, s in systems.items()
        if s.get("owner") and s["owner"] != owner
    ]
    
    if enemies:
        return enemies[0]
    
    return None