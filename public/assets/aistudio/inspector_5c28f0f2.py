from ui.colors import C

def inspect_system(state, system_id):

    if system_id not in state["systems"]:
        print("No such system")
        return

    s = state["systems"][system_id]

    print()
    print(C.BOLD + "=" * 60 + C.RESET)
    print(C.CYAN + f"SYSTEM INSPECTOR: {system_id}" + C.RESET)
    print(C.BOLD + "=" * 60 + C.RESET)

    print("Owner:", s["owner"])
    print("Stock:", s["stock"])
    print("Demand:", s["demand"])
    print("Resources:", s["resources"])

    print()
    print("Neighbors:", s["neighbors"])

    print()
    print("📊 LAST STATES:")

    for entry in s.get("log", [])[-5:]:

        print(
            "t", entry["time"],
            "| stock:", entry["stock"],
            "| owner:", entry["owner"]
        )