from ui.colors import C
from world.fleets import create_fleet

def print_human_controls():
    """Показує доступні команди для гравця"""
    print()
    print(C.BOLD + C.YELLOW + "YOUR COMMANDS:" + C.RESET)
    print("  [b] Build  [m] Move  [a] Attack  [i] Inspect  [f] Stats")
    print("  [w] Wars  [r] Events  [peace] Peace  [spy] Spy  [tech] Tech")
    print("  [p] Pause  [s] Step  [S] Save  [q] Menu")

def handle_human_command(state, cmd, player_faction):
    """Обробляє команди гравця"""
    faction = state["factions"][player_faction]

    if cmd == "b":
        # Побудувати флот
        capital = faction.get("capital")

        if not capital:
            print(C.RED + "  No capital system! Cannot build fleet." + C.RESET)
            return

        if faction["credits"] < 100:
            print(C.RED + f"  Not enough credits! Need 100, have {faction['credits']}" + C.RESET)
            return

        faction["credits"] -= 100
        fleet_id = create_fleet(state, player_faction, capital)

        print(C.GREEN + f"  Fleet {fleet_id} built at {capital}! (Cost: 100 credits)" + C.RESET)

        state["events"].append({
            "type": "fleet_built",
            "faction": player_faction,
            "fleet": fleet_id
        })

    elif cmd == "m":
        # Перемістити флот
        fleets = [fid for fid, f in state["fleets"].items() if f["owner"] == player_faction]

        if not fleets:
            print(C.RED + "  You have no fleets to move!" + C.RESET)
            return

        print(C.YELLOW + "  Your fleets:" + C.RESET)
        for i, fid in enumerate(fleets, 1):
            fleet = state["fleets"][fid]
            print(f"    {i}. {fid} (at {fleet['location']}, strength: {fleet['strength']})")

        try:
            idx = int(input("  Select fleet: ")) - 1
            if idx < 0 or idx >= len(fleets):
                print(C.RED + "  Invalid fleet!" + C.RESET)
                return

            fleet_id = fleets[idx]
            fleet = state["fleets"][fleet_id]

            current = fleet["location"]
            neighbors = state["systems"][current].get("neighbors", [])

            if not neighbors:
                print(C.RED + "  No neighboring systems!" + C.RESET)
                return

            print(C.YELLOW + "  Neighbor systems:" + C.RESET)
            for i, nid in enumerate(neighbors, 1):
                owner = state["systems"][nid].get("owner", "None")
                print(f"    {i}. {nid} (owner: {owner})")

            idx2 = int(input("  Select target: ")) - 1
            if idx2 < 0 or idx2 >= len(neighbors):
                print(C.RED + "  Invalid target!" + C.RESET)
                return

            target = neighbors[idx2]
            fleet["location"] = target
            print(C.GREEN + f"  Fleet {fleet_id} moved to {target}" + C.RESET)

        except ValueError:
            print(C.RED + "  Invalid input! Please enter a number." + C.RESET)
        except:
            print(C.RED + "  Invalid input!" + C.RESET)

    elif cmd == "a":
        # Атака
        fleets = [fid for fid, f in state["fleets"].items() if f["owner"] == player_faction]

        if not fleets:
            print(C.RED + "  You have no fleets to attack with!" + C.RESET)
            return

        print(C.YELLOW + "  Your fleets:" + C.RESET)
        for i, fid in enumerate(fleets, 1):
            fleet = state["fleets"][fid]
            print(f"    {i}. {fid} (at {fleet['location']}, strength: {fleet['strength']})")

        try:
            idx = int(input("  Select fleet: ")) - 1
            if idx < 0 or idx >= len(fleets):
                print(C.RED + "  Invalid fleet!" + C.RESET)
                return

            fleet_id = fleets[idx]
            fleet = state["fleets"][fleet_id]

            current = fleet["location"]
            neighbors = state["systems"][current].get("neighbors", [])

            enemies = [
                nid for nid in neighbors
                if state["systems"][nid].get("owner") and
                   state["systems"][nid]["owner"] != player_faction
            ]

            if not enemies:
                print(C.RED + "  No enemy systems nearby!" + C.RESET)
                return

            print(C.YELLOW + "  Enemy systems nearby:" + C.RESET)
            for i, nid in enumerate(enemies, 1):
                owner = state["systems"][nid]["owner"]
                print(f"    {i}. {nid} (owner: {owner})")

            idx2 = int(input("  Select target: ")) - 1
            if idx2 < 0 or idx2 >= len(enemies):
                print(C.RED + "  Invalid target!" + C.RESET)
                return

            target = enemies[idx2]
            fleet["location"] = target
            print(C.GREEN + f"  Fleet {fleet_id} attacking {target}!" + C.RESET)

        except ValueError:
            print(C.RED + "  Invalid input! Please enter a number." + C.RESET)
        except:
            print(C.RED + "  Invalid input!" + C.RESET)