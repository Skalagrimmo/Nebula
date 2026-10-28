import random
from world.fleet_types import FLEET_TYPES, create_fleet_with_type, get_fleet_ai_type

def create_fleet(state, owner_id, location, fleet_type=None):
    """Створює флот із вказаним типом"""
    if fleet_type is None:
        fleet_type = get_fleet_ai_type()
    return create_fleet_with_type(state, owner_id, location, fleet_type)

def get_fleet_stats(state, fleet_id):
    """Повертає статистику флоту"""
    fleet = state["fleets"].get(fleet_id)
    if not fleet:
        return None
    
    return {
        "strength": fleet["strength"],
        "speed": fleet.get("speed", 1),
        "type": fleet.get("type_name", "Unknown"),
        "role": fleet.get("role", "Unknown")
    }

def get_fleet_type_name(state, fleet_id):
    """Повертає назву типу флоту"""
    fleet = state["fleets"].get(fleet_id)
    if not fleet:
        return "Unknown"
    return fleet.get("type_name", "Unknown")

def get_fleet_strength(state, fleet_id):
    """Повертає силу флоту з урахуванням типу"""
    fleet = state["fleets"].get(fleet_id)
    if not fleet:
        return 0
    return fleet.get("strength", 0)