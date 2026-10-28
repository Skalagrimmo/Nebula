def are_neighbors(state, system_a, system_b):

    return (
        system_b in state["systems"][system_a]["neighbors"]
    )