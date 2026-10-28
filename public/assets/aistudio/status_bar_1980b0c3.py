from ui.colors import C

def print_status_bar(state, player_faction=None):
    """Друкує статус-бар внизу екрану"""
    
    width = 60
    bar = "─" * width
    
    # Інформація про гравця
    if player_faction and player_faction in state["factions"]:
        p = state["factions"][player_faction]
        player_info = f"{C.GREEN}{player_faction}{C.RESET} 💰{p['credits']} 🪐{len(p['systems'])}"
    else:
        player_info = f"{C.GRAY}SPECTATOR{C.RESET}"
    
    # Час і ходи
    time_info = f"T{state['time']} #{state.get('ticks', 0)}"
    
    # Системи
    total = len(state["systems"])
    owned = sum(1 for s in state["systems"].values() if s.get("owner"))
    
    # Формуємо рядок
    left = f" {player_info} "
    center = f" {time_info} "
    right = f" 🪐{owned}/{total} "
    
    # Розраховуємо відступи
    left_len = len(left) - 10  # приблизно
    center_len = len(center) - 4
    right_len = len(right) - 4
    
    # Друкуємо
    print()
    print(C.GRAY + "╔" + "═" * width + "╗" + C.RESET)
    
    # Складаємо рядок
    line = "║"
    line += left
    line += " " * (width - left_len - center_len - right_len - 4)
    line += center
    line += " " * (width - left_len - center_len - right_len - 4)
    line += right
    line += "║"
    
    print(line)
    print(C.GRAY + "╚" + "═" * width + "╝" + C.RESET)