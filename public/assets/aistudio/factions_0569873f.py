import random
from world.faction_names import generate_all_faction_names, get_faction_emoji

# ============================================================
# НАЗВИ ФРАКЦІЙ (ДЛЯ ІМПОРТУ)
# ============================================================

FACTION_NAMES = {
    "FACTION_A": "Solarian Empire",
    "FACTION_B": "Zergon Hive",
    "FACTION_C": "Celestial Federation"
}


# ============================================================
# ГЕНЕРАЦІЯ ФРАКЦІЙ
# ============================================================

def generate_factions():
    """Генерує фракції з випадковими назвами"""
    factions = {}
    
    # Генеруємо назви для 3 фракцій
    names = generate_all_faction_names(3)
    
    # ID фракцій (залишаємо FACTION_A, FACTION_B, FACTION_C для сумісності)
    ids = ["FACTION_A", "FACTION_B", "FACTION_C"]
    
    for i, fid in enumerate(ids):
        name = names[i] if i < len(names) else f"Faction {i+1}"
        
        factions[fid] = {
            "id": fid,
            "name": name,
            "display_name": f"{get_faction_emoji('empire')} {name}",
            "capital": None,
            "credits": 100,
            "military": 1,
            "systems": [],
            "tech": [],
            "stats": {
                "wins": 0,
                "losses": 0,
                "battles": 0
            },
            "personality": {
                "aggression": random.uniform(0.2, 0.8),
                "economy_focus": random.uniform(0.2, 0.8),
                "expansion_focus": random.uniform(0.2, 0.8),
                "defense_focus": random.uniform(0.2, 0.8),
                "risk": random.uniform(0.2, 0.8)
            },
            "war_score": {},
            "strategy": "balanced"
        }
    
    return factions


def get_faction_display(faction):
    """Повертає відображення фракції з емодзі та назвою"""
    return faction.get("display_name", faction.get("name", faction.get("id")))


def get_faction_name(faction_id):
    """Повертає назву фракції за ID"""
    return FACTION_NAMES.get(faction_id, faction_id)