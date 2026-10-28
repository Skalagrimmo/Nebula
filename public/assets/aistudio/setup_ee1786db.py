import random

def assign_starting_systems(state):

    systems = list(state["systems"].keys())
    random.shuffle(systems)

    for i, faction in enumerate(state["factions"].keys()):

        for j in range(2):

            sid = systems[i*2 + j]

            state["systems"][sid]["owner"] = faction
            state["factions"][faction]["systems"].append(sid)