import random
from ui.colors import C
from world.galaxy import SYSTEMS, CONNECTIONS, WORMHOLES


def travel(ship):
    """Подорож між системами"""
    cur = ship["location"]
    
    print(C.BOLD + "\n" + "=" * 50 + C.RESET)
    print(C.CYAN + "🚀 TRAVEL" + C.RESET)
    print(C.BOLD + "=" * 50 + C.RESET)
    
    print(f"\n  Current: {C.YELLOW}{cur}{C.RESET}")
    print(f"  Routes: {', '.join(CONNECTIONS.get(cur, []))}")
    print(f"  Wormhole: {'Yes' if cur in WORMHOLES else 'No'}")
    print()
    
    mode = input("  [route/wormhole/back] > ").strip().lower()
    
    if mode == "route":
        dest = input("  Destination: ").strip()
        if dest in CONNECTIONS.get(cur, []):
            ship["location"] = dest
            print(C.GREEN + f"  Jumped to {dest}" + C.RESET)
            
            # Випадкові події
            event = random.random()
            if event < 0.3:
                print(C.RED + "  ☠️ Pirates attack!" + C.RESET)
                # Тут можна викликати бій
            elif event < 0.4:
                found = random.randint(20, 60)
                ship["credits"] += found
                print(C.GOLD + f"  Found {found} credits floating in space!" + C.RESET)
        else:
            print(C.RED + "  No route!" + C.RESET)
    
    elif mode == "wormhole":
        if cur in WORMHOLES:
            dest = WORMHOLES[cur]
            ship["location"] = dest
            ship["hull"] -= random.randint(5, 20)
            print(C.MAGENTA + f"  🌠 Wormhole jump to {dest}! (Hull: {ship['hull']})" + C.RESET)
        else:
            print(C.RED + "  No wormhole here!" + C.RESET)