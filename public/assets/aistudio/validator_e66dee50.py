def validate_state(state):

    required_keys = [
        "time",
        "systems",
        "factions",
        "fleets",
        "events"
    ]

    for key in required_keys:

        if key not in state:
            raise ValueError(f"STATE BROKEN: missing {key}")

    return True