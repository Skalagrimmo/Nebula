def update_empire_strategy(state):

    for fid, faction in state["factions"].items():

        systems = faction["systems"]

        if not systems:
            continue

        # 📊 оцінка загрози
        threat = 0
        border_systems = 0

        for sid in systems:
            system = state["systems"][sid]

            for n in system.get("neighbors", []):

                owner = state["systems"][n].get("owner")

                if owner and owner != fid:
                    threat += 1
                    border_systems += 1

        # 🧠 проста стратегія

        if threat > 3:
            faction["strategy"] = "defensive"
        elif border_systems > 0:
            faction["strategy"] = "balanced"
        else:
            faction["strategy"] = "expansion"