def print_debug(state):

    print()
    print("=" * 60)
    print("DEBUG")
    print("=" * 60)

    print("Systems:", len(state["systems"]))
    print("Factions:", len(state["factions"]))
    print("Fleets:", len(state["fleets"]))
    print("Armies:", len(state.get("armies", {})))

    total_strength = 0

    for fleet in state["fleets"].values():
        total_strength += fleet.get("strength", 0)

    print("Fleet strength:", total_strength)

    print()

    for fid, faction in state["factions"].items():

        print(
            fid,
            "| systems:", len(faction.get("systems", [])),
            "| credits:", faction.get("credits", 0),
            "| military:", round(faction.get("military", 1), 2),
            "| strategy:", faction.get("strategy", "none")
        )