import time
import random
from ui.colors import C


def animate_battle(fleet1, fleet2, strength1, strength2, system_name):
    """Епічна анімація бою в стилі 90-х"""
    
    # Очищуємо екран
    print("\033[2J\033[H")
    
    # Заголовок
    print(C.BOLD + C.RED + "⚔️" * 30 + C.RESET)
    print(C.BOLD + C.YELLOW + f"  BATTLE FOR {system_name}  " + C.RESET)
    print(C.BOLD + C.RED + "⚔️" * 30 + C.RESET)
    print()
    
    # Показуємо флоти
    print(f"  {C.RED}🔥 {fleet1}{C.RESET}  vs  {C.BLUE}🌊 {fleet2}{C.RESET}")
    print()
    
    # Початкові HP
    hp1 = strength1
    hp2 = strength2
    max_hp = max(strength1, strength2, 100)
    
    # Бойовий цикл
    round_num = 1
    while hp1 > 0 and hp2 > 0:
        # Показуємо раунд
        print(C.BOLD + f"Round {round_num}" + C.RESET)
        
        # Атака 1
        damage1 = random.randint(5, 20)
        hp2 -= damage1
        print(f"  {C.RED}{fleet1}{C.RESET} attacks {C.BLUE}{fleet2}{C.RESET} for {damage1} damage!")
        
        # HP бар 2
        bar2 = int((hp2 / max_hp) * 20)
        print(f"  {C.BLUE}HP: {C.RESET}[{'█' * max(0, bar2)}{'░' * (20 - max(0, bar2))}] {max(0, hp2)}")
        
        # Перевірка смерті
        if hp2 <= 0:
            print()
            print(C.RED + "💀 " + C.BOLD + f"{fleet2} DESTROYED!" + C.RESET)
            time.sleep(1)
            break
        
        # Атака 2
        damage2 = random.randint(5, 20)
        hp1 -= damage2
        print(f"  {C.BLUE}{fleet2}{C.RESET} attacks {C.RED}{fleet1}{C.RESET} for {damage2} damage!")
        
        # HP бар 1
        bar1 = int((hp1 / max_hp) * 20)
        print(f"  {C.RED}HP: {C.RESET}[{'█' * max(0, bar1)}{'░' * (20 - max(0, bar1))}] {max(0, hp1)}")
        
        # Ефект удару
        print("  " + C.YELLOW + "💥 *CRASH*" + C.RESET)
        print()
        
        # Перевірка смерті
        if hp1 <= 0:
            print()
            print(C.RED + "💀 " + C.BOLD + f"{fleet1} DESTROYED!" + C.RESET)
            time.sleep(1)
            break
        
        round_num += 1
        time.sleep(1.5)
    
    # Результат
    print()
    print(C.BOLD + "═" * 50 + C.RESET)
    if hp1 > hp2:
        print(C.GREEN + "🏆 " + C.BOLD + f"{fleet1} WINS THE BATTLE!" + C.RESET)
    else:
        print(C.GREEN + "🏆 " + C.BOLD + f"{fleet2} WINS THE BATTLE!" + C.RESET)
    print(C.BOLD + "═" * 50 + C.RESET)
    
    # Затримка перед продовженням
    time.sleep(2)