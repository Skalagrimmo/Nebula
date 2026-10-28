def analyze_faction_strategy(state, faction_id):

    faction = state["factions"][faction_id]

    aggression = faction["personality"]["aggression"]
    economy = faction["personality"]["economy_focus"]

    systems_owned = len(faction["systems"])
    credits = faction["credits"]

    # 🧠 базова стратегічна модель
    if aggression > 0.6:
        mode = "EXPANSIONIST WARFARE"
    elif economy > 0.6:
        mode = "ECONOMIC GROWTH"
    else:
        mode = "BALANCED CONTROL"

    # 📊 оцінка сили
    power = systems_owned * 10 + credits * 0.01 + aggression * 50

    return {
        "mode": mode,
        "power": round(power, 2),
        "systems": systems_owned,
        "credits": credits
    }


def predict_winner(state):

    scores = {}

    for fid in state["factions"]:

        s = analyze_faction_strategy(state, fid)
        scores[fid] = s["power"]

    winner = max(scores, key=scores.get)

    return winner, scores