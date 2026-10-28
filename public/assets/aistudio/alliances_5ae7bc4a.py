def update_alliances(state):

    factions = state["factions"]

    if len(factions) < 2:
        return

    # шукаємо найсильнішу фракцію
    leader = max(
        factions,
        key=lambda f: len(factions[f]["systems"])
    )

    alliances = {}

    for fid in factions:

        if fid == leader:
            continue

        alliances[fid] = leader

    state["alliances"] = alliances