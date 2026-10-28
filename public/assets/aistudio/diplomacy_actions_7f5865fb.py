def apply_diplomacy_actions(state):

    diplomacy = state["diplomacy"]
    factions = list(state["factions"].keys())

    for a in factions:

        for b in factions:

            if a == b:
                continue

            data = diplomacy[a][b]

            relation = data["relation"]
            trust = data["trust"]

            # -------------------------
            # АЛЬЯНС
            # -------------------------
            if data["status"] == "Ally":

                state["events"].append({
                    "type": "alliance_formed",
                    "a": a,
                    "b": b
                })

            # -------------------------
            # ВІЙНА
            # -------------------------
            elif data["status"] == "Hostile":

                # перевірка щоб не спамило війною
                if data.get("wars", 0) % 5 == 0:

                    state["events"].append({
                        "type": "war_declared",
                        "attacker": a,
                        "defender": b
                    })

            # -------------------------
            # НЕЙТРАЛІТЕТ
            # -------------------------
            elif data["status"] == "Neutral":
                pass