def create_army(state, owner_id):

    army_id = f"ARMY_{len(state['armies'])}"

    state["armies"][army_id] = {
        "id": army_id,
        "owner": owner_id,
        "fleets": [],
        "target": None
    }

    return army_id
    
def update_army_orders(state):

    for army in state["armies"].values():

        target = choose_army_target(state, army)

        army["target"] = target