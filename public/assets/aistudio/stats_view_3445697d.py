from ui.colors import C


def print_stats(state, engine):
    print()
    print(C.BOLD + "=" * 60 + C.RESET)
    print(C.CYAN + "📊 GAME STATISTICS" + C.RESET)
    print(C.BOLD + "=" * 60 + C.RESET)

    total_systems = len(state["systems"])
    total_fleets = len(state["fleets"])
    total_armies = len(state.get("armies", {}))
    total_events = len(state.get("events", []))

    print(f"  ⏱️  Ticks: {engine.ticks}")
    print(f"  🪐 Systems: {total_systems}")
    print(f"  🚀 Fleets: {total_fleets}")
    print(f"  ⚔️  Armies: {total_armies}")
    print(f"  📜 Events: {total_events}")

    print()
    print(C.YELLOW + "FACTION STATS:" + C.RESET)

    for fid, faction in state["factions"].items():
        stats = faction.get("stats", {"wins": 0, "losses": 0, "battles": 0})
        winrate = round(stats["wins"] / max(1, stats["battles"]) * 100, 1)
        print(f"  {fid}:")
        print(f"    ✅ Wins: {stats['wins']}")
        print(f"    ❌ Losses: {stats['losses']}")
        print(f"    ⚔️  Battles: {stats['battles']}")
        print(f"    📈 Winrate: {winrate}%")