def init_diplomacy(state):
    """
    Ініціалізує дипломатію для всіх фракцій
    """
    factions = list(state["factions"].keys())
    diplomacy = {}
    
    for a in factions:
        diplomacy[a] = {}
        for b in factions:
            if a == b:
                continue
            diplomacy[a][b] = {
                "relation": 0,
                "trust": 50,
                "wars": 0,
                "status": "Neutral"
            }
    
    state["diplomacy"] = diplomacy