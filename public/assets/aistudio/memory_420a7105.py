def record_epoch(state):

    if state["time"] % 10 != 0:
        return

    snapshot = {
        "time": state["time"],
        "factions": {},
        "systems_control": {}
    }

    for fid, f in state["factions"].items():

        snapshot["factions"][fid] = {
            "credits": f["credits"],
            "systems": len(f["systems"]),
            "aggression": f["personality"]["aggression"]
        }

    for sid, s in state["systems"].items():

        if s["owner"]:
            snapshot["systems_control"][sid] = s["owner"]

    state["epochs"].append(snapshot)


def record_faction_history(state):

    for fid, faction in state["factions"].items():

        if fid not in state["faction_history"]:
            state["faction_history"][fid] = []

        state["faction_history"][fid].append({
            "time": state["time"],
            "credits": faction["credits"],
            "systems": len(faction["systems"]),
            "military": faction["military"],
            "aggression": faction["personality"]["aggression"]
        })

        if len(state["faction_history"][fid]) > 50:
            state["faction_history"][fid].pop(0)