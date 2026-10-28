import random


def choose_targets(state):

    factions = state["factions"]
    diplomacy = state["diplomacy"]

    decisions = []

    for a in factions:

        best_target = None
        best_score = 9999  # чим менше — тим краще ворог

        for b in factions:

            if a == b:
                continue

            data = diplomacy[a][b]

            relation = data["relation"]
            trust = data["trust"]

            enemy_score = 0

            # -------------------------
            # ВОРОЖІСТЬ
            # -------------------------
            enemy_score += -relation  # чим гірші відносини — тим привабливіша ціль

            # -------------------------
            # ДОВІРА (менше довіри = більший ризик)
            # -------------------------
            enemy_score += (50 - trust)

            # -------------------------
            # ВІЙСЬКОВА ПОТУЖНІСТЬ
            # -------------------------
            enemy_power = state["factions"][b]["military"]
            enemy_score += enemy_power * 10

            # -------------------------
            # ВИГІДНІСТЬ (економіка)
            # -------------------------
            enemy_score -= state["factions"][b]["credits"] / 50

            if enemy_score < best_score:
                best_score = enemy_score
                best_target = b

        if best_target:

            decisions.append({
                "attacker": a,
                "target": best_target,
                "score": best_score
            })

    return decisions