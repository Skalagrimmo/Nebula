from world.galaxy import SYSTEMS


def create_ship():
    """Створює стан корабля для RPG режиму"""
    return {
        "location": "Sol",
        "credits": 100,
        "cargo": {"ore": 0, "food": 0, "tech": 0},
        "cargo_limit": 10,
        "hull": 100,
        "hull_max": 100,
        "weapon": 1,
        "shield": 0.0,
        "reputation": 50,
        "kills": 0
    }


def ship_status(ship):
    """Показує статус корабля"""
    from ui.colors import C
    
    print(C.BOLD + "\n" + "=" * 50 + C.RESET)
    print(C.CYAN + "🚀 SHIP STATUS" + C.RESET)
    print(C.BOLD + "=" * 50 + C.RESET)
    
    sys_info = SYSTEMS.get(ship["location"], {})
    desc = sys_info.get("desc", "Unknown")
    
    print(f"  Location: {C.YELLOW}{ship['location']}{C.RESET} ({desc})")
    print(f"  Credits: {C.GREEN}{ship['credits']}{C.RESET}")
    print(f"  Reputation: {ship['reputation']}/100")
    
    # Hull бар
    hull_pct = int(ship["hull"] / ship["hull_max"] * 20)
    hull_color = C.GREEN if hull_pct > 10 else C.RED
    print(f"  Hull: [{hull_color}{'█' * hull_pct}{'░' * (20 - hull_pct)}{C.RESET}] {ship['hull']}/{ship['hull_max']}")
    
    print(f"  Weapon: {ship['weapon']}  Shield: {ship['shield']:.2f}")
    print(f"  Cargo: {sum(ship['cargo'].values())}/{ship['cargo_limit']}")
    print(f"  Kills: {ship['kills']}")