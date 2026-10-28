import random

def process_trade(state):

    # перевіряємо всі пари систем
    for sid, system in state["systems"].items():

        if system["stock"] <= system["demand"]:

            # шукаємо донорів серед сусідів
            neighbors = system["neighbors"]

            random.shuffle(neighbors)

            for n in neighbors:

                donor = state["systems"][n]

                if donor["stock"] > donor["demand"]:

                    transfer = min(
                        15,
                        donor["stock"] - donor["demand"]
                    )

                    donor["stock"] -= transfer
                    system["stock"] += transfer

                    state["history"].append(
                        f"FLOW {n} → {sid} ({transfer})"
                    )

                    break