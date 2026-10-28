def apply_blockades(state):

    for fid, fleet in state["fleets"].items():

        if fleet["strength"] <= 60:
            continue

        loc = fleet["location"]

        # флот блокує trade flow через систему
        system = state["systems"][loc]

        system["stock"] -= 8

        state["history"].append(
            f"BLOCKADE at {loc} by {fid}"
        )