from ui.colors import C

def print_ai_brain(state):

    print()
    print(C.BOLD + "=" * 60 + C.RESET)
    print(C.CYAN + "AI DECISION LOGIC" + C.RESET)
    print(C.BOLD + "=" * 60 + C.RESET)

    for e in state["events"][-10:]:

        if e["type"] != "AI_DECISION":
            continue

        d = e["data"]

        print()
        print(C.YELLOW + d["faction"] + C.RESET)
        print("action:", d["action"])
        print("reasons:")

        for r in d["reasons"]:
            print(" -", r)