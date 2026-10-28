def explain_decision(state, faction_id, action):

    faction = state["factions"][faction_id]

    reasons = []

    aggression = faction["personality"]["aggression"]
    credits = faction["credits"]
    systems = len(faction["systems"])

    # базові причини

    if aggression > 0.7:
        reasons.append("High aggression → expansion priority")

    if credits > 200:
        reasons.append("Strong economy → supports military action")

    if systems < 3:
        reasons.append("Low territory → expansion pressure")

    if action["type"] == "MOVE":
        reasons.append("Fleet repositioning for strategic control")

    if action["type"] == "ATTACK":
        reasons.append("Opportunity detected in target system")

    return reasons