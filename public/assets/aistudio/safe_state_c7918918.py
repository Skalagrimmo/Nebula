def ensure_state(state):

    if "diplomacy" not in state:
        state["diplomacy"] = {}

    if "ai_wars" not in state:
        state["ai_wars"] = []

    if "strategic_plans" not in state:
        state["strategic_plans"] = []

    if "events" not in state:
        state["events"] = []

    if "alliances" not in state:
        state["alliances"] = {}

    return state


def ensure_faction(f):

    f.setdefault("credits", 100)
    f.setdefault("military", 1)
    f.setdefault("systems", [])

    f.setdefault("stats", {
        "wins": 0,
        "losses": 0,
        "battles": 0
    })

    f.setdefault("personality", {
        "aggression": 0.5,
        "economy_focus": 0.5,
        "expansion_focus": 0.5,
        "defense_focus": 0.5,
        "risk": 0.5
    })


def ensure_system(s):
    s.setdefault("owner", None)
    s.setdefault("resources", 100)
    s.setdefault("stock", 50)
    s.setdefault("demand", 50)
    s.setdefault("log", [])  # ← ДОДАТИ ЦЕЙ РЯДОК
    s.setdefault("population", 100)  # ← теж додати для економіки
    s.setdefault("economy", 50)      # ← теж додати
    
    if "neighbors" not in s:
        s["neighbors"] = []