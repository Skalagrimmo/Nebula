from world.reasoning import explain_decision
from core.events import emit


def ai_step(state):

    actions = []

    for fid, faction in state["factions"].items():

        # приклад дії (спрощено)
        action = {
            "fleet": list(state["fleets"].keys())[0],
            "target": list(state["systems"].keys())[0],
            "type": "MOVE"
        }

        actions.append(action)

        # 🧠 пояснення рішення
        reasons = explain_decision(state, fid, action)

        emit(state, "AI_DECISION", {
            "faction": fid,
            "action": action,
            "reasons": reasons
        })
def choose_target(state, faction_id):

    best_system = None
    best_value = -1

    for sid, system in state["systems"].items():

        if system["owner"] == faction_id:
            continue

        if system["value"] > best_value:

            best_value = system["value"]
            best_system = sid

    return best_system

    return actions