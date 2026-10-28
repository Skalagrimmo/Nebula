def update_frontlines(state):

    frontlines = set()

    systems = state["systems"]

    for sid, system in systems.items():

        owner = system.get("owner")

        if not owner:
            continue

        for neighbor in system.get("neighbors", []):

            if neighbor not in systems:
                continue

            if systems[neighbor].get("owner") != owner:

                front = tuple(sorted([sid, neighbor]))
                frontlines.add(front)

    state["frontlines"] = frontlines