import random
from world.system_types import SYSTEM_TYPES, get_random_system_type, apply_system_bonuses

# ============================================================
# НАЗВИ СИСТЕМ
# ============================================================

SYSTEM_NAMES = [
    "Sol", "Vega", "Sirius", "Altair", "Proxima", "Nebula",
    "Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta",
    "Eta", "Theta", "Iota", "Kappa", "Lambda", "Mu",
    "Nu", "Xi", "Omicron", "Pi", "Rho", "Sigma",
    "Tau", "Upsilon", "Phi", "Chi", "Psi", "Omega",
    "Andromeda", "Cassiopeia", "Orion", "Pegasus", "Hercules",
    "Lyra", "Cygnus", "Draco", "Phoenix", "Aquila",
    "Aurora", "Nova", "Pulsar", "Quasar", "Void"
]

# ============================================================
# СИСТЕМИ ДЛЯ RPG
# ============================================================

SYSTEMS = {
    "Sol": {"type": "capital", "faction": "Federation", "prices": {"ore": 10, "food": 5, "tech": 20}, "desc": "🏛️ Capital of the Federation"},
    "Vega": {"type": "trade", "faction": "Trade Guild", "prices": {"ore": 7, "food": 8, "tech": 15}, "desc": "💰 Trading hub"},
    "Sirius": {"type": "pirate", "faction": "Pirates", "prices": {"ore": 15, "food": 3, "tech": 25}, "desc": "☠️ Pirate stronghold"},
    "Altair": {"type": "rich", "faction": "Empire", "prices": {"ore": 20, "food": 10, "tech": 30}, "desc": "👑 Imperial core world"},
    "Proxima": {"type": "desert", "faction": "None", "prices": {"ore": 5, "food": 12, "tech": 10}, "desc": "🏜️ Desert outpost"},
    "Nebula": {"type": "volcanic", "faction": "None", "prices": {"ore": 25, "food": 2, "tech": 40}, "desc": "🌋 Volcanic ruins"},
    "Alpha": {"type": "forest", "faction": "None", "prices": {"ore": 12, "food": 7, "tech": 18}, "desc": "🌲 Forest world"},
    "Beta": {"type": "ocean", "faction": "None", "prices": {"ore": 8, "food": 9, "tech": 12}, "desc": "🌊 Ocean world"},
    "Gamma": {"type": "agricultural", "faction": "None", "prices": {"ore": 6, "food": 11, "tech": 8}, "desc": "🌾 Agricultural world"},
    "Delta": {"type": "industrial", "faction": "None", "prices": {"ore": 22, "food": 6, "tech": 28}, "desc": "🏭 Industrial world"},
    "Epsilon": {"type": "ice", "faction": "Pirates", "prices": {"ore": 18, "food": 4, "tech": 22}, "desc": "❄️ Ice world"}
}

CONNECTIONS = {
    "Sol": ["Vega", "Sirius", "Proxima", "Alpha"],
    "Vega": ["Sol", "Altair", "Beta"],
    "Sirius": ["Sol", "Gamma", "Epsilon"],
    "Altair": ["Vega", "Delta"],
    "Proxima": ["Sol", "Nebula", "Epsilon"],
    "Nebula": ["Proxima", "Beta"],
    "Alpha": ["Sol", "Gamma"],
    "Beta": ["Vega", "Nebula"],
    "Gamma": ["Sirius", "Alpha"],
    "Delta": ["Altair", "Epsilon"],
    "Epsilon": ["Sirius", "Proxima", "Delta"]
}

WORMHOLES = {
    "Sol": "Altair",
    "Vega": "Sirius",
    "Proxima": "Nebula",
    "Alpha": "Delta"
}


# ============================================================
# ГЕНЕРАЦІЯ ГАЛАКТИКИ (ДЛЯ 4X)
# ============================================================

def generate_galaxy():
    """Генерує галактику з різними типами систем"""
    systems = {}
    
    names = random.sample(SYSTEM_NAMES, min(20, len(SYSTEM_NAMES)))
    
    for name in names:
        system_type = get_random_system_type()
        
        system = {
            "id": name,
            "owner": None,
            "capital": False,
            "population": random.randint(50, 500),
            "economy": random.randint(10, 100),
            "resources": random.randint(50, 200),
            "stock": 0,
            "demand": random.randint(20, 100),
            "value": 0,
            "pos": (random.randint(0, 39), random.randint(0, 19)),
            "neighbors": []
        }
        
        system = apply_system_bonuses(system, system_type)
        systems[name] = system
    
    keys = list(systems.keys())
    
    for i in range(len(keys) - 1):
        a = keys[i]
        b = keys[i + 1]
        systems[a]["neighbors"].append(b)
        systems[b]["neighbors"].append(a)
    
    for _ in range(len(keys)):
        a = random.choice(keys)
        b = random.choice(keys)
        if a == b:
            continue
        if b not in systems[a]["neighbors"]:
            systems[a]["neighbors"].append(b)
        if a not in systems[b]["neighbors"]:
            systems[b]["neighbors"].append(a)
    
    return systems