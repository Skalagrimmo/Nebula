def assign_fleets_to_armies(state):

    armies = state["armies"]
    fleets = state["fleets"]

    # очищаємо
    for army in armies.values():
        army["fleets"] = []

    for fid, fleet in fleets.items():

        owner = fleet["owner"]

        # шукаємо армію власника
        army = None

        for a in armies.values():
            if a["owner"] == owner:
                army = a
                break

        # якщо нема — створюємо
        if not army:
            from world.armies import create_army
            army_id = create_army(state, owner)
            army = state["armies"][army_id]

        army["fleets"].append(fid)

def choose_army_target(state, army):

    systems = state["systems"]
    owner = army["owner"]

    # 🔴 вороги
    enemy_systems = [
        sid for sid, s in systems.items()
        if s.get("owner") and s["owner"] != owner
    ]

    # 🟢 свої системи (для захисту)
    own_systems = [
        sid for sid, s in systems.items()
        if s.get("owner") == owner
    ]

    # ⚔️ пріоритет: атакувати слабкого ворога
    if enemy_systems:

        def score(sid):
            s = systems[sid]
            return (s.get("population", 100) + s.get("economy", 50)) - s.get("resources", 0)

        return min(enemy_systems, key=score)

    # 🛡️ якщо нема ворогів — захищаємося
    if own_systems:
        return own_systems[0]

    return None
    
def update_army_orders(state):

    for army in state["armies"].values():

        target = choose_army_target(state, army)

        army["target"] = target