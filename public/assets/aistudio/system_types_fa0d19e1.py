import random

SYSTEM_TYPES = {
    "capital": {
        "name": "🏛️ Capital",
        "bonus_population": 1.5,
        "bonus_economy": 1.5,
        "bonus_resources": 1.2,
        "bonus_defense": 2.0,
        "desc": "Capital world, center of civilization"
    },
    "rich": {
        "name": "💰 Rich",
        "bonus_population": 1.0,
        "bonus_economy": 1.5,
        "bonus_resources": 2.0,
        "bonus_defense": 1.0,
        "desc": "Resource-rich world"
    },
    "industrial": {
        "name": "🏭 Industrial",
        "bonus_population": 1.0,
        "bonus_economy": 1.2,
        "bonus_resources": 1.0,
        "bonus_defense": 1.0,
        "desc": "Heavy industry, fast production"
    },
    "agricultural": {
        "name": "🌾 Agricultural",
        "bonus_population": 1.8,
        "bonus_economy": 0.8,
        "bonus_resources": 1.0,
        "bonus_defense": 1.0,
        "desc": "Breadbasket world, high population growth"
    },
    "desert": {
        "name": "🏜️ Desert",
        "bonus_population": 0.5,
        "bonus_economy": 1.0,
        "bonus_resources": 1.5,
        "bonus_defense": 0.8,
        "desc": "Harsh desert, rich in minerals"
    },
    "ocean": {
        "name": "🌊 Ocean",
        "bonus_population": 1.5,
        "bonus_economy": 0.7,
        "bonus_resources": 0.5,
        "bonus_defense": 1.2,
        "desc": "Water world, dense population"
    },
    "forest": {
        "name": "🌲 Forest",
        "bonus_population": 1.2,
        "bonus_economy": 1.0,
        "bonus_resources": 1.2,
        "bonus_defense": 1.0,
        "desc": "Lush world, balanced resources"
    },
    "pirate": {
        "name": "☠️ Pirate",
        "bonus_population": 0.5,
        "bonus_economy": 0.3,
        "bonus_resources": 0.5,
        "bonus_defense": 0.5,
        "desc": "Pirate stronghold, constant danger"
    },
    "volcanic": {
        "name": "🌋 Volcanic",
        "bonus_population": 0.3,
        "bonus_economy": 1.0,
        "bonus_resources": 2.0,
        "bonus_defense": 0.5,
        "desc": "Volcanic world, extremely rich"
    },
    "ice": {
        "name": "❄️ Ice",
        "bonus_population": 0.3,
        "bonus_economy": 0.8,
        "bonus_resources": 1.2,
        "bonus_defense": 0.5,
        "desc": "Frozen world, sparse population"
    }
}

def get_random_system_type():
    types = list(SYSTEM_TYPES.keys())
    weights = [2 if t == "capital" else 1 for t in types]
    return random.choices(types, weights=weights, k=1)[0]

def apply_system_bonuses(system, system_type):
    type_data = SYSTEM_TYPES.get(system_type, SYSTEM_TYPES["forest"])
    
    system["type"] = system_type
    system["type_name"] = type_data["name"]
    system["type_desc"] = type_data["desc"]
    
    system["population"] = int(system["population"] * type_data["bonus_population"])
    system["economy"] = int(system["economy"] * type_data["bonus_economy"])
    system["resources"] = int(system["resources"] * type_data["bonus_resources"])
    
    return system