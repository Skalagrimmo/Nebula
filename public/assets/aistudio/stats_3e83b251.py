from ui.colors import C


def stats_full(ship):
    """Повна статистика корабля"""
    print(C.BOLD + "\n" + "=" * 50 + C.RESET)
    print(C.CYAN + "📊 FULL STATS" + C.RESET)
    print(C.BOLD + "=" * 50 + C.RESET)
    
    print(f"\n  Credits: {ship['credits']}")
    print(f"  Cargo: {ship['cargo']}")
    print(f"  Cargo Limit: {ship['cargo_limit']}")
    print(f"  Hull: {ship['hull']}/{ship['hull_max']}")
    print(f"  Weapon: {ship['weapon']}")
    print(f"  Shield: {ship['shield']:.2f}")
    print(f"  Reputation: {ship['reputation']}/100")
    print(f"  Kills: {ship['kills']}")