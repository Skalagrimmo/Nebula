import random

def update_faction_personality(state):

    for fid, faction in state["factions"].items():

        stats = faction["stats"]
        p = faction["personality"]

        # результативність
        success_rate = stats["wins"] - stats["losses"]

        # якщо виграє → стає агресивнішою
        if success_rate > 3:

            p["aggression"] = min(1.0, p["aggression"] + 0.05)
            p["risk"] = min(1.0, p["risk"] + 0.03)

        # якщо програє → стає обережнішою
        if success_rate < -3:

            p["aggression"] = max(0.1, p["aggression"] - 0.05)
            p["risk"] = max(0.1, p["risk"] - 0.05)

        # економічні кризи → економізація
        if faction["credits"] < 200:

            p["economy_focus"] = min(1.0, p["economy_focus"] + 0.05)
            p["aggression"] = max(0.1, p["aggression"] - 0.05)

        # випадкові мутації (еволюція)
        if random.random() < 0.05:

            key = random.choice(list(p.keys()))
            p[key] = min(1.0, max(0.1, p[key] + random.uniform(-0.1, 0.1)))