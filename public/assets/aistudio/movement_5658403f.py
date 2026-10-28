from world.fleet_ai import choose_fleet_target


def move_fleets(state):

    systems = state["systems"]

    for fleet in state["fleets"].values():

        current = fleet["location"]

        if current not in systems:
            continue

        system = systems[current]

        target = choose_fleet_target(state, fleet)

        # якщо можемо піти напряму
        if target in system["neighbors"]:
            fleet["location"] = target

        else:
            # рух до першого сусіда
            if system["neighbors"]:
                fleet["location"] = system["neighbors"][0]