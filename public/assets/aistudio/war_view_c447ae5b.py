from ui.colors import C

def print_active_wars(state):
    """Показує активні війни між фракціями"""
    print()
    print(C.BOLD + "=" * 60 + C.RESET)
    print(C.RED + "🔥 ACTIVE WARS" + C.RESET)
    print(C.BOLD + "=" * 60 + C.RESET)

    diplomacy = state.get("diplomacy", {})
    wars_found = False

    for a in diplomacy:
        for b in diplomacy[a]:
            if a < b:  # щоб не дублювати
                rel = diplomacy[a][b].get("relation", 0)
                status = diplomacy[a][b].get("status", "Neutral")
                if status == "Hostile" or rel < -50:
                    wars_found = True
                    fleets_a = sum(1 for f in state["fleets"].values() if f["owner"] == a)
                    fleets_b = sum(1 for f in state["fleets"].values() if f["owner"] == b)
                    print(f"  {C.RED}⚔{C.RESET} {C.YELLOW}{a}{C.RESET} vs {C.YELLOW}{b}{C.RESET}")
                    print(f"     🚀 {a}: {fleets_a} fleets  |  {b}: {fleets_b} fleets")
                    print(f"     📉 Relation: {rel}")
                    print()

    if not wars_found:
        print(C.GREEN + "  ☮️ No active wars. Peace reigns in the galaxy." + C.RESET)