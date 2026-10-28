import random


def build_strategic_plan(state):

    factions = state["factions"]
    diplomacy = state["diplomacy"]

    plans = []

    for a in factions:

        best_target = None
        best_score = -999999

        for b in factions:

            if a == b:
                continue

            rel = diplomacy[a][b]["relation"]
            trust = diplomacy[a][b]["trust"]

            enemy = factions[b]

            score = 0

            # 🔴 ненависть = бажання війни
            score += -rel * 2

            # 🟡 низька довіра = нестабільність
            score += (50 - trust)

            # ⚔️ сила ворога (слабші привабливіші)
            score -= enemy.get("military", 1) * 20

            # 💰 багатство = ціль
            score += enemy.get("credits", 100) / 10

            # 🧠 агресія фракції
            score += factions[a]["personality"]["aggression"] * 50

            # 📉 ризик (високий ризик = менше війни)
            score -= factions[a]["personality"]["risk"] * 30

            if score > best_score:
                best_score = score
                best_target = b

        # рішення
        action = "neutral"

        if best_score > 80:
            action = "war"
        elif best_score > 40:
            action = "pressure"
        else:
            action = "ignore"

        plans.append({
            "attacker": a,
            "target": best_target,
            "action": action,
            "score": best_score
        })

    return plans