import random

FLEET_TYPES = {
    "scout": {
        "name": "🔍 Scout",
        "strength": 5,
        "speed": 3,
        "cost": 50,
        "desc": "Fast scout ship, weak but quick"
    },
    "fighter": {
        "name": "⚔️ Fighter",
        "strength": 15,
        "speed": 1,
        "cost": 100,
        "desc": "Standard combat ship"
    },
    "battleship": {
        "name": "🛡️ Battleship",
        "strength": 25,
        "speed": 0.5,
        "cost": 200,
        "desc": "Heavy ship, slow but powerful"
    },
    "assault": {
        "name": "💀 Assault",
        "strength": 20,
        "speed": 1.5,
        "cost": 150,
        "desc": "Fast and powerful assault ship"
    },
    "pirate": {
        "name": "🏴‍☠️ Pirate",
        "strength": 12,
        "speed": 2,
        "cost": 0,
        "desc": "Pirate ship, only for pirates"
    },
    "flagship": {
        "name": "🏛️ Flagship",
        "strength": 40,
        "speed": 0.5,
        "cost": 400,
        "desc": "Mighty flagship, extremely expensive"
    }
}

def get_random_fleet_type():
    types = list(FLEET_TYPES.keys())
    weights = [3, 5, 2, 2, 1, 0.5]
    return random.choices(types, weights=weights, k=1)[0]

def get_fleet_ai_type():
    types = ["fighter", "battleship", "assault"]
    return random.choice(types)

def create_fleet_with_type(state, owner_id, location, fleet_type=None):
    if fleet_type is None:
        fleet_type = get_fleet_ai_type()
    
    type_data = FLEET_TYPES.get(fleet_type, FLEET_TYPES["fighter"])
    
    fleet_id = f"FLEET_{len(state['fleets'])}"
    
    state["fleets"][fleet_id] = {
        "id": fleet_id,
        "owner": owner_id,
        "location": location,
        "strength": type_data["strength"],
        "speed": type_data["speed"],
        "type": fleet_type,
        "type_name": type_data["name"],
        "role": "attack" if fleet_type in ["fighter", "assault", "flagship"] else "defense"
    }
    
    return fleet_id