import random

def assign_capitals(state):
    """Призначає столиці фракціям з урахуванням назв"""
    systems = list(state["systems"].keys())
    factions = list(state["factions"].keys())

    random.shuffle(systems)
    used = set()

    for faction_id in factions:
        capital_id = None

        for s in systems:
            if s not in used:
                capital_id = s
                used.add(s)
                break

        if not capital_id:
            break

        state["systems"][capital_id]["capital"] = True
        state["systems"][capital_id]["owner"] = faction_id

        faction = state["factions"][faction_id]
        faction["capital"] = capital_id
        faction["systems"].append(capital_id)
        
        # Логування
        name = faction.get("name", faction_id)
        print(f"  🏛️ {name} capital: {capital_id}")