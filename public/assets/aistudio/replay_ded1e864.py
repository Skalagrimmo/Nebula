from ui.colors import C


def print_replay(state):

    print()
    print(C.BOLD + "=" * 60 + C.RESET)
    print(C.CYAN + "EVENT STREAM" + C.RESET)
    print(C.BOLD + "=" * 60 + C.RESET)

    for e in state["events"][-10:]:

        t = e.get("time", state.get("time", 0))
        et = e.get("type", "UNKNOWN")

        if et == "CONQUEST":

            print(
                f"t{t}",
                C.YELLOW + "CONQUEST" + C.RESET,
                e.get("data", "")
            )

        elif et == "BATTLE":

            print(
                f"t{t}",
                C.RED + "BATTLE" + C.RESET,
                e.get("data", "")
            )

        elif et == "battle_result":

            print(
                f"t{t}",
                C.RED + "WAR" + C.RESET,
                f"{e.get('attacker')} defeated "
                f"{e.get('defender')} at "
                f"{e.get('system')}"
            )

        elif et == "fleet_built":

            print(
                f"t{t}",
                C.GREEN + "BUILD" + C.RESET,
                e.get("faction")
            )

        elif et == "TICK_START":
            continue

        else:

            print(f"t{t}", et)