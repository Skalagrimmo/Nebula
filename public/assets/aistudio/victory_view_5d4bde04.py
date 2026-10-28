from world.victory import get_victory_progress, get_victory_stats
from ui.colors import C


def print_victory_progress(state):
    """Показує прогрес до перемоги"""
    
    progress = get_victory_progress(state)
    
    print()
    print(C.BOLD + "=" * 60 + C.RESET)
    print(C.CYAN + "🏆 VICTORY PROGRESS" + C.RESET)
    print(C.BOLD + "=" * 60 + C.RESET)
    
    # Якщо вже є перемога, показуємо статус
    if state.get("victory_achieved", False):
        winner = state.get("winner", "Unknown")
        vtype = state.get("victory_type", "Unknown")
        print(C.GOLD + f"  👑 {winner} has already won by {vtype}!" + C.RESET)
        print(C.GRAY + "  Continue playing for more challenges." + C.RESET)
        print()
    
    for faction_id, data in progress.items():
        percent = data["systems"]
        credits = data["credits"]
        fleets = data["fleets"]
        
        bars = min(int(percent / 5), 20)
        bar = "█" * bars + "░" * (20 - bars)
        
        if faction_id == "FACTION_A":
            color = C.BLUE
        elif faction_id == "FACTION_B":
            color = C.RED
        elif faction_id == "FACTION_C":
            color = C.GREEN
        else:
            color = C.WHITE
        
        # Відмітка про переможця
        winner_mark = C.GOLD + "👑 " + C.RESET if state.get("winner") == faction_id else ""
        
        print(f"{winner_mark}{color}{faction_id:<12}{C.RESET} [{bar}] {percent}%")
        print(f"  💰 {credits} credits  🚀 {fleets} fleets")
        print()