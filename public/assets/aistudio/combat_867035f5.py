import random
import sys
from ui.colors import C


def fight(ship):
    """Бій з піратами в RPG режимі"""
    enemy_hull = 40 + random.randint(-10, 20)
    enemy_weapon = random.randint(1, 3)
    enemy_name = random.choice(["Pirate", "Raider", "Marauder", "Corsair"])
    
    print(C.BOLD + "\n" + "=" * 50 + C.RESET)
    print(C.RED + "☠️ COMBAT!" + C.RESET)
    print(C.BOLD + "=" * 50 + C.RESET)
    print(f"\n  {C.RED}{enemy_name}{C.RESET} attacks!")
    print(f"  Enemy hull: {enemy_hull}")
    print()
    
    while enemy_hull > 0 and ship["hull"] > 0:
        action = input("  [attack/flee] > ").strip().lower()
        
        if action == "attack":
            damage = random.randint(8, 15) * ship["weapon"]
            enemy_hull -= damage
            ship["hull"] -= random.randint(3, 10) * max(1, enemy_weapon - int(ship["shield"]))
            
            print(f"  {C.GREEN}Hit!{C.RESET} {damage} damage")
            print(f"  Enemy hull: {enemy_hull}  Your hull: {ship['hull']}")
            
            if enemy_hull <= 0:
                print(C.GREEN + "  🏆 Enemy destroyed!" + C.RESET)
                ship["kills"] += 1
                ship["credits"] += 50 + random.randint(0, 30)
                ship["reputation"] = min(100, ship["reputation"] + 2)
                return True
        
        elif action == "flee":
            if random.random() < 0.5 + ship["shield"] * 0.2:
                print(C.YELLOW + "  Escaped!" + C.RESET)
                return False
            else:
                ship["hull"] -= 10
                print(C.RED + "  Failed to escape!" + C.RESET)
    
    if ship["hull"] <= 0:
        print(C.RED + "\n💀 Ship destroyed! Game over." + C.RESET)
        sys.exit()
    
    return True