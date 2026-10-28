import random
from ui.colors import C
from core.events import emit

# ============================================================
# БАЗА ВИПАДКОВИХ ПОДІЙ
# ============================================================

EVENT_TYPES = {
    "diplomatic_incident": {
        "name": "💥 Diplomatic Incident",
        "chance": 0.025,
        "effect": "diplomatic_incident_effect",
        "message": "A diplomatic incident sparks tensions between {a} and {b}!"
    },
    "alliance_formed": {
        "name": "🤝 Alliance Formed",
        "chance": 0.02,
        "effect": "alliance_formed_effect",
        "message": "{a} and {b} have formed an alliance!"
    },
    "plague": {
        "name": "💀 Plague Outbreak",
        "chance": 0.015,
        "effect": "plague_effect",
        "message": "A deadly plague sweeps through {system}!"
    },
    "tech_breakthrough": {
        "name": "🚀 Technological Breakthrough",
        "chance": 0.02,
        "effect": "tech_breakthrough_effect",
        "message": "{faction} has made a technological breakthrough!"
    },
    "golden_age": {
        "name": "💰 Golden Age",
        "chance": 0.015,
        "effect": "golden_age_effect",
        "message": "{faction} enters a golden age of prosperity!"
    },
    "discovery": {
        "name": "🌠 New Discovery",
        "chance": 0.01,
        "effect": "discovery_effect",
        "message": "A new system has been discovered!"
    },
    "leadership_change": {
        "name": "👑 Leadership Change",
        "chance": 0.015,
        "effect": "leadership_change_effect",
        "message": "{faction} has a new leader!"
    },
    "rebellion": {
        "name": "⚔️ Rebellion",
        "chance": 0.015,
        "effect": "rebellion_effect",
        "message": "A rebellion has broken out in {system}!"
    },
    "trade_boom": {
        "name": "📈 Trade Boom",
        "chance": 0.025,
        "effect": "trade_boom_effect",
        "message": "Trade routes are booming across the galaxy!"
    },
    # 🆕 НОВІ ПОДІЇ
    "supernova": {
        "name": "🌟 Supernova",
        "chance": 0.008,
        "effect": "supernova_effect",
        "message": "A supernova has destroyed {system}!"
    },
    "alien_encounter": {
        "name": "👽 Alien Encounter",
        "chance": 0.01,
        "effect": "alien_encounter_effect",
        "message": "Aliens have been encountered near {system}!"
    },
    "sos_signal": {
        "name": "📡 SOS Signal",
        "chance": 0.015,
        "effect": "sos_effect",
        "message": "An SOS signal is coming from {system}!"
    },
    "artifact": {
        "name": "💎 Ancient Artifact",
        "chance": 0.01,
        "effect": "artifact_effect",
        "message": "An ancient artifact has been found on {system}!"
    },
    "storm": {
        "name": "🌊 Space Storm",
        "chance": 0.02,
        "effect": "storm_effect",
        "message": "A massive space storm damages fleets near {system}!"
    },
    "pirate_raid": {
        "name": "🏴‍☠️ Pirate Raid",
        "chance": 0.02,
        "effect": "pirate_raid_effect",
        "message": "Pirates are raiding {system}!"
    },
    "refugees": {
        "name": "🆘 Refugees",
        "chance": 0.015,
        "effect": "refugee_effect",
        "message": "Refugees from {system} are fleeing the war!"
    },
    "solar_flare": {
        "name": "☀️ Solar Flare",
        "chance": 0.018,
        "effect": "solar_flare_effect",
        "message": "A solar flare has hit {system}!"
    },
    "cultural_exchange": {
        "name": "🎭 Cultural Exchange",
        "chance": 0.015,
        "effect": "cultural_exchange_effect",
        "message": "Cultural exchange between {a} and {b} improves relations!"
    },
    "economic_crisis": {
        "name": "📉 Economic Crisis",
        "chance": 0.015,
        "effect": "economic_crisis_effect",
        "message": "{faction} is experiencing an economic crisis!"
    }
}


# ============================================================
# ЕФЕКТИ ПОДІЙ
# ============================================================

def trigger_random_event(state):
    """Випадкова подія з багатьох типів"""
    
    event_keys = list(EVENT_TYPES.keys())
    weights = [EVENT_TYPES[k]["chance"] for k in event_keys]
    
    total = sum(weights)
    if total == 0:
        return None
    
    chosen = random.choices(event_keys, weights=weights, k=1)[0]
    event = EVENT_TYPES[chosen]
    
    effect_func = globals().get(event["effect"])
    if effect_func:
        result = effect_func(state)
        if result:
            emit(state, "RANDOM_EVENT", {
                "name": event["name"],
                "message": event["message"].format(**result) if result else event["message"],
                "data": result
            })
            return event["message"].format(**result) if result else event["message"]
    
    return None


# ============================================================
# ІСНУЮЧІ ПОДІЇ
# ============================================================

def diplomatic_incident_effect(state):
    factions = list(state["factions"].keys())
    if len(factions) < 2:
        return None
    
    a, b = random.sample(factions, 2)
    diplomacy = state["diplomacy"]
    
    if a in diplomacy and b in diplomacy[a]:
        diplomacy[a][b]["relation"] = max(-100, diplomacy[a][b]["relation"] - 30)
        diplomacy[b][a]["relation"] = max(-100, diplomacy[b][a]["relation"] - 30)
        
        if diplomacy[a][b]["relation"] < -70:
            diplomacy[a][b]["status"] = "Hostile"
            diplomacy[b][a]["status"] = "Hostile"
    
    return {"a": state["factions"][a].get("name", a), "b": state["factions"][b].get("name", b)}


def alliance_formed_effect(state):
    factions = list(state["factions"].keys())
    if len(factions) < 2:
        return None
    
    a, b = random.sample(factions, 2)
    diplomacy = state["diplomacy"]
    
    if a in diplomacy and b in diplomacy[a]:
        diplomacy[a][b]["relation"] = min(100, diplomacy[a][b]["relation"] + 40)
        diplomacy[b][a]["relation"] = min(100, diplomacy[b][a]["relation"] + 40)
        diplomacy[a][b]["status"] = "Ally"
        diplomacy[b][a]["status"] = "Ally"
    
    return {"a": state["factions"][a].get("name", a), "b": state["factions"][b].get("name", b)}


def plague_effect(state):
    systems = list(state["systems"].keys())
    if not systems:
        return None
    
    system = random.choice(systems)
    if system in state["systems"]:
        pop = state["systems"][system].get("population", 100)
        state["systems"][system]["population"] = max(20, pop - random.randint(15, 40))
        
        # Якщо населення занадто мале, система стає нейтральною
        if state["systems"][system]["population"] < 30:
            owner = state["systems"][system].get("owner")
            if owner:
                state["systems"][system]["owner"] = None
                if system in state["factions"][owner]["systems"]:
                    state["factions"][owner]["systems"].remove(system)
    
    return {"system": system}


def tech_breakthrough_effect(state):
    factions = list(state["factions"].keys())
    if not factions:
        return None
    
    faction = random.choice(factions)
    if faction in state["factions"]:
        state["factions"][faction]["military"] *= 1.25
        
        from world.technology import TECH_TREE
        techs = list(TECH_TREE.keys())
        if techs:
            tech = random.choice(techs)
            if tech not in state["factions"][faction].get("tech", []):
                state["factions"][faction]["tech"].append(tech)
    
    return {"faction": state["factions"][faction].get("name", faction)}


def golden_age_effect(state):
    factions = list(state["factions"].keys())
    if not factions:
        return None
    
    faction = random.choice(factions)
    if faction in state["factions"]:
        state["factions"][faction]["credits"] += 300
    
    return {"faction": state["factions"][faction].get("name", faction)}


def discovery_effect(state):
    from world.galaxy import SYSTEM_NAMES
    
    used_names = list(state["systems"].keys())
    available = [n for n in SYSTEM_NAMES if n not in used_names]
    
    if not available:
        return None
    
    new_system = random.choice(available)
    
    state["systems"][new_system] = {
        "id": new_system,
        "owner": None,
        "capital": False,
        "population": random.randint(50, 300),
        "economy": random.randint(10, 80),
        "resources": random.randint(50, 150),
        "stock": 0,
        "demand": random.randint(20, 80),
        "value": 0,
        "pos": (random.randint(0, 39), random.randint(0, 19)),
        "neighbors": []
    }
    
    target = random.choice(used_names)
    state["systems"][new_system]["neighbors"].append(target)
    state["systems"][target]["neighbors"].append(new_system)
    
    return {"system": new_system}


def leadership_change_effect(state):
    factions = list(state["factions"].keys())
    if not factions:
        return None
    
    faction = random.choice(factions)
    if faction in state["factions"]:
        strategies = ["aggressive", "defensive", "expansion", "economic", "balanced"]
        state["factions"][faction]["strategy"] = random.choice(strategies)
        
        p = state["factions"][faction]["personality"]
        p["aggression"] = random.uniform(0.2, 0.8)
        p["economy_focus"] = random.uniform(0.2, 0.8)
        p["expansion_focus"] = random.uniform(0.2, 0.8)
        p["defense_focus"] = random.uniform(0.2, 0.8)
        p["risk"] = random.uniform(0.2, 0.8)
    
    return {"faction": state["factions"][faction].get("name", faction)}


def rebellion_effect(state):
    systems = list(state["systems"].keys())
    if not systems:
        return None
    
    system = random.choice(systems)
    if system in state["systems"]:
        owner = state["systems"][system].get("owner")
        if owner:
            state["systems"][system]["owner"] = None
            if system in state["factions"][owner]["systems"]:
                state["factions"][owner]["systems"].remove(system)
    
    return {"system": system}


def trade_boom_effect(state):
    for faction in state["factions"].values():
        faction["credits"] += random.randint(40, 100)
    return {}


# ============================================================
# 🆕 НОВІ ПОДІЇ
# ============================================================

def supernova_effect(state):
    """Наднова знищує систему"""
    systems = list(state["systems"].keys())
    if len(systems) < 3:
        return None
    
    system = random.choice(systems)
    if system in state["systems"]:
        # Перевіряємо, чи є флоти в системі
        fleets_in_system = [f for f in state["fleets"].values() if f["location"] == system]
        for fleet in fleets_in_system:
            fleet["strength"] *= 0.3  # Флоти сильно пошкоджені
        
        # Видаляємо систему
        owner = state["systems"][system].get("owner")
        if owner and system in state["factions"][owner]["systems"]:
            state["factions"][owner]["systems"].remove(system)
        
        # Видаляємо з'єднання
        neighbors = state["systems"][system].get("neighbors", [])
        for n in neighbors:
            if n in state["systems"] and system in state["systems"][n]["neighbors"]:
                state["systems"][n]["neighbors"].remove(system)
        
        del state["systems"][system]
        
        return {"system": system}
    
    return None


def alien_encounter_effect(state):
    """Приліт чужинців — випадкова фракція отримує бонус"""
    factions = list(state["factions"].keys())
    if not factions:
        return None
    
    faction = random.choice(factions)
    if faction in state["factions"]:
        # Чужинці дають технологію
        state["factions"][faction]["military"] *= 1.3
        state["factions"][faction]["credits"] += 200
        
        # Додаємо випадкову технологію
        from world.technology import TECH_TREE
        techs = list(TECH_TREE.keys())
        if techs:
            tech = random.choice(techs)
            if tech not in state["factions"][faction].get("tech", []):
                state["factions"][faction]["tech"].append(tech)
    
    return {"system": random.choice(list(state["systems"].keys()))}


def sos_effect(state):
    """SOS сигнал — гравець отримує нагороду за допомогу"""
    factions = list(state["factions"].keys())
    if not factions:
        return None
    
    system = random.choice(list(state["systems"].keys()))
    
    # Нагорода для випадкової фракції
    faction = random.choice(factions)
    if faction in state["factions"]:
        state["factions"][faction]["credits"] += 150
        state["factions"][faction]["reputation"] = min(100, state["factions"][faction].get("reputation", 50) + 10)
    
    return {"system": system}


def artifact_effect(state):
    """Артефакт — великий бонус для фракції"""
    factions = list(state["factions"].keys())
    if not factions:
        return None
    
    system = random.choice(list(state["systems"].keys()))
    faction = random.choice(factions)
    
    if faction in state["factions"]:
        state["factions"][faction]["military"] *= 1.4
        state["factions"][faction]["credits"] += 400
        
        from world.technology import TECH_TREE
        techs = list(TECH_TREE.keys())
        if techs:
            tech = random.choice(techs)
            if tech not in state["factions"][faction].get("tech", []):
                state["factions"][faction]["tech"].append(tech)
    
    return {"system": system}


def storm_effect(state):
    """Космічний шторм пошкоджує флоти в регіоні"""
    systems = list(state["systems"].keys())
    if not systems:
        return None
    
    system = random.choice(systems)
    
    # Пошкоджуємо флоти в системі та сусідніх
    affected = [system] + state["systems"][system].get("neighbors", [])
    
    for fid, fleet in state["fleets"].items():
        if fleet["location"] in affected:
            fleet["strength"] *= 0.7
    
    return {"system": system}


def pirate_raid_effect(state):
    """Піратський рейд — грабують систему"""
    systems = list(state["systems"].keys())
    if not systems:
        return None
    
    system = random.choice(systems)
    if system in state["systems"]:
        owner = state["systems"][system].get("owner")
        if owner and owner in state["factions"]:
            # Втрата ресурсів
            state["systems"][system]["stock"] = max(0, state["systems"][system]["stock"] - 50)
            state["factions"][owner]["credits"] = max(0, state["factions"][owner]["credits"] - 50)
    
    return {"system": system}


def refugee_effect(state):
    """Біженці — збільшують населення сусідньої системи"""
    systems = list(state["systems"].keys())
    if len(systems) < 2:
        return None
    
    source = random.choice(systems)
    
    # Шукаємо сусідню систему
    neighbors = state["systems"][source].get("neighbors", [])
    if not neighbors:
        return None
    
    target = random.choice(neighbors)
    if target in state["systems"]:
        # Біженці збільшують населення
        state["systems"][target]["population"] += random.randint(20, 60)
        
        # Якщо система належить фракції, вона отримує бонус
        owner = state["systems"][target].get("owner")
        if owner and owner in state["factions"]:
            state["factions"][owner]["credits"] += 30
    
    return {"system": source}


def solar_flare_effect(state):
    """Сонячний спалах — пошкоджує систему"""
    systems = list(state["systems"].keys())
    if not systems:
        return None
    
    system = random.choice(systems)
    if system in state["systems"]:
        state["systems"][system]["population"] = max(30, state["systems"][system]["population"] - 20)
        state["systems"][system]["economy"] = max(5, state["systems"][system]["economy"] - 10)
    
    return {"system": system}


def cultural_exchange_effect(state):
    """Культурний обмін — покращує відносини між двома фракціями"""
    factions = list(state["factions"].keys())
    if len(factions) < 2:
        return None
    
    a, b = random.sample(factions, 2)
    diplomacy = state["diplomacy"]
    
    if a in diplomacy and b in diplomacy[a]:
        diplomacy[a][b]["relation"] = min(100, diplomacy[a][b]["relation"] + 25)
        diplomacy[b][a]["relation"] = min(100, diplomacy[b][a]["relation"] + 25)
        
        if diplomacy[a][b]["relation"] > 60:
            diplomacy[a][b]["status"] = "Friendly"
            diplomacy[b][a]["status"] = "Friendly"
    
    return {"a": state["factions"][a].get("name", a), "b": state["factions"][b].get("name", b)}


def economic_crisis_effect(state):
    """Економічна криза — фракція втрачає гроші"""
    factions = list(state["factions"].keys())
    if not factions:
        return None
    
    faction = random.choice(factions)
    if faction in state["factions"]:
        state["factions"][faction]["credits"] = int(state["factions"][faction]["credits"] * 0.7)
    
    return {"faction": state["factions"][faction].get("name", faction)}