def build_trade_routes(state):

    routes = []

    for sid, system in state["systems"].items():

        for n in system["neighbors"]:

            route = tuple(sorted([sid, n]))

            if route not in routes:
                routes.append(route)

    return routes