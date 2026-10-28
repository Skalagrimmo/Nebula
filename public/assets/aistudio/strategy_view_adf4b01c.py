from world.strategy import analyze_faction_strategy, predict_winner
from ui.colors import C

def print_strategy_dashboard(state):

    print()
    print(C.BOLD + "=" * 60 + C.RESET)
    print(C.CYAN + "FACTION STRATEGY DASHBOARD" + C.RESET)
    print(C.BOLD + "=" * 60 + C.RESET)

    winner, scores = predict_winner(state)

    print()
    print("🏆 Predicted dominant faction:", winner)

    print()

    for fid in state["factions"]:

        s = analyze_faction_strategy(state, fid)

        print(C.YELLOW + fid + C.RESET)
        print(" mode:", s["mode"])
        print(" power:", s["power"])
        print(" systems:", s["systems"])
        print(" credits:", s["credits"])
        print()