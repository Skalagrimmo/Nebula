import random
from core.events import emit

def trigger_random_event(state):
    """Випадкова подія, як у рогаликах"""
    
    events = [
        {
            "name": "💀 Plague Outbreak",
            "effect": lambda s: plague_effect(s),
            "chance": 0.02,
            "message": "A deadly plague sweeps through the galaxy!"
        },
        {
            "name": "💰 Trade Boom",
            "effect": lambda s: trade_boom_effect(s),
            "chance": 0.03,
            "message": "Galactic trade boom increases everyone's wealth!"
        },
        {
            "name": "🛸 Ancient Relic Found",
            "effect": lambda s: relic_effect(s),
            "chance": 0.01,
            "message": "An ancient relic has been discovered!"
        },
        {
            "name": "☄️ Solar Storm",
            "effect": lambda s: solar_storm_effect(s),
            "chance": 0.02,
            "message": "A massive solar storm damages fleets!"
        },
        {
            "name": "🤝 Peace Conference",
            "effect": lambda s: peace_effect(s),
            "chance": 0.015,
            "message": "All factions are invited to a peace conference!"
        }
    ]
    
    for event in events:
        if random.random() < event["chance"]:
            event["effect"](state)
            emit(state, "RANDOM_EVENT", {
                "name": event["name"],
                "message": event["message"]
            })
            return event["message"]
    
    return None


def plague_effect(state):
    for system in state["systems"].values():
        if "population" in system:
            system["population"] = max(50, system["population"] - random.randint(10, 30))


def trade_boom_effect(state):
    for faction in state["factions"].values():
        faction["credits"] += random.randint(20, 50)


def relic_effect(state):
    factions = list(state["factions"].keys())
    if factions:
        winner = random.choice(factions)
        state["factions"][winner]["credits"] += 100
        state["factions"][winner]["military"] *= 1.2


def solar_storm_effect(state):
    for fleet in state["fleets"].values():
        if random.random() < 0.3:
            fleet["strength"] *= 0.7


def peace_effect(state):
    diplomacy = state.get("diplomacy", {})
    for a in diplomacy:
        for b in diplomacy[a]:
            if "relation" in diplomacy[a][b]:
                diplomacy[a][b]["relation"] = min(100, diplomacy[a][b]["relation"] + 20)