def update_system_value(state):

    for sid, system in state["systems"].items():

        system["value"] = (
            system["population"] * 0.6 +
            system["economy"] * 1.5 +
            system["resources"] * 0.3 +
            (10 if system["capital"] else 0)
        )


def compute_control_zones(state):

    zones = {
        "CONTESTED": []
    }

    for fid in state["factions"].keys():
        zones[fid] = []

    for sid, system in state["systems"].items():

        owner = system.get("owner")

        if owner is None:
            zones["CONTESTED"].append(sid)
        else:
            zones[owner].append(sid)

    return zones