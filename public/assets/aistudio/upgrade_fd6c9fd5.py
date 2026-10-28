from ui.colors import C


def upgrade(ship):
    """Прокачка корабля"""
    print(C.BOLD + "\n" + "=" * 50 + C.RESET)
    print(C.CYAN + "🛠️ UPGRADES" + C.RESET)
    print(C.BOLD + "=" * 50 + C.RESET)
    
    print(f"\n  Credits: {C.GREEN}{ship['credits']}{C.RESET}")
    print()
    print("  1. Cargo (+5)     - 50 credits")
    print("  2. Hull (+20)     - 70 credits")
    print("  3. Weapon (+1)    - 100 credits")
    print("  4. Shield (+0.05) - 120 credits")
    print()
    
    choice = input("  > ").strip()
    
    if choice == "1" and ship["credits"] >= 50:
        ship["cargo_limit"] += 5
        ship["credits"] -= 50
        print(C.GREEN + "  Cargo upgraded!" + C.RESET)
    elif choice == "2" and ship["credits"] >= 70:
        ship["hull_max"] += 20
        ship["hull"] += 20
        ship["credits"] -= 70
        print(C.GREEN + "  Hull upgraded!" + C.RESET)
    elif choice == "3" and ship["credits"] >= 100:
        ship["weapon"] += 1
        ship["credits"] -= 100
        print(C.GREEN + "  Weapon upgraded!" + C.RESET)
    elif choice == "4" and ship["credits"] >= 120:
        ship["shield"] += 0.05
        ship["credits"] -= 120
        print(C.GREEN + "  Shield upgraded!" + C.RESET)
    else:
        print(C.RED + "  Not enough credits or invalid choice!" + C.RESET)