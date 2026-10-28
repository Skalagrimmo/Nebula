from ui.colors import C
from world.galaxy import SYSTEMS


def map_view(ship):
    """Показує карту галактики"""
    print(C.BOLD + "\n" + "=" * 50 + C.RESET)
    print(C.CYAN + "🗺️ GALACTIC MAP" + C.RESET)
    print(C.BOLD + "=" * 50 + C.RESET)
    
    for s, info in SYSTEMS.items():
        mark = C.YELLOW + "▶ " + C.RESET if s == ship["location"] else "  "
        faction = f"[{info.get('faction', 'Neutral')}]"
        print(f"{mark} {s:<10} {faction:<15} {info.get('desc', '')}")