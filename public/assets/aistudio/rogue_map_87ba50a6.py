from ui.colors import C
import random

def render_rogue_map(state):
    """Рендерить карту в стилі старих рогаликів"""
    
    size_x = 50
    size_y = 25
    
    # Створюємо порожню карту
    grid = [[" " for _ in range(size_x)] for _ in range(size_y)]
    
    # Отримуємо позиції систем
    for sid, system in state["systems"].items():
        x, y = system["pos"]
        
        # Переконуємось, що координати в межах
        if 0 <= x < size_x and 0 <= y < size_y:
            owner = system.get("owner")
            capital = system.get("capital", False)
            
            # Визначаємо символ
            if capital:
                char = "★"  # Столиця
            else:
                char = "●"  # Звичайна система
            
            # Визначаємо колір
            if owner == "FACTION_A":
                color = C.BLUE
            elif owner == "FACTION_B":
                color = C.RED
            elif owner == "FACTION_C":
                color = C.GREEN
            else:
                color = C.GRAY
                char = "·"  # Нейтральна
            
            grid[y][x] = color + char + C.RESET
    
    # Додаємо флоти (як маленькі трикутники)
    for fid, fleet in state["fleets"].items():
        loc = fleet["location"]
        if loc in state["systems"]:
            x, y = state["systems"][loc]["pos"]
            if 0 <= x < size_x and 0 <= y < size_y:
                # Флот позначаємо як ^ (стрілка вгору)
                grid[y][x] = C.YELLOW + "▲" + C.RESET
    
    # Друкуємо карту з рамкою
    print()
    print(C.BOLD + "╔" + "═" * (size_x + 2) + "╗" + C.RESET)
    for row in grid:
        print(C.BOLD + "║" + C.RESET + "".join(row) + C.BOLD + "║" + C.RESET)
    print(C.BOLD + "╚" + "═" * (size_x + 2) + "╝" + C.RESET)
    
    # Легенда
    print()
    print(C.GRAY + "  ·" + C.RESET + " Neutral  " + 
          C.BLUE + "●" + C.RESET + " Faction A  " +
          C.RED + "●" + C.RESET + " Faction B  " +
          C.GREEN + "●" + C.RESET + " Faction C  " +
          C.YELLOW + "▲" + C.RESET + " Fleet  " +
          C.GOLD + "★" + C.RESET + " Capital")