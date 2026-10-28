def choose_fleet_target(state, fleet):

    systems = state["systems"]
    owner = fleet["owner"]

    # шукаємо ворогів
    enemy_systems = [
        sid for sid, s in systems.items()
        if s.get("owner") and s["owner"] != owner
    ]

    # шукаємо свої системи (для захисту)
    own_systems = [
        sid for sid, s in systems.items()
        if s.get("owner") == owner
    ]

    role = fleet.get("role", "attack")

    # 🛡️ ЗАХИСТ
    if role == "defense":
        if own_systems:
            return own_systems[0]
        return fleet["location"]

    # ⚔️ АТАКА
    if enemy_systems:
        return enemy_systems[0]

    return fleet["location"]