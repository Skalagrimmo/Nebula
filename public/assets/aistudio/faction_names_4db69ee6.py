import random

PREFIXES = {
    "empire": ["Solarian", "Galactic", "Imperial", "Cosmic", "Stellar", "Astral", "Celestial", "Lunar", "Solar", "Nebular"],
    "hive": ["Zergon", "Kryll", "Vorr", "Xylox", "Thorn", "Scar", "Fang", "Claw", "Venom", "Shadow"],
    "federation": ["United", "Free", "Allied", "Grand", "Commonwealth", "Union", "Concord", "League", "Pact", "Alliance"],
    "corporation": ["Omni", "Mega", "Star", "Galaxy", "Nova", "Quantum", "Apex", "Fusion", "Eclipse", "Phoenix"],
    "cult": ["Eternal", "Void", "Silent", "Dark", "Light", "Pure", "Sacred", "Hidden", "Ancient", "Forgotten"]
}

SUFFIXES = {
    "empire": ["Empire", "Dominion", "Realm", "Kingdom", "Imperium"],
    "hive": ["Hive", "Swarm", "Collective", "Brood", "Nest"],
    "federation": ["Federation", "Republic", "Council", "Senate", "Congress"],
    "corporation": ["Corporation", "Industries", "Systems", "Dynamics", "Enterprises"],
    "cult": ["Cult", "Order", "Church", "Sect", "Circle"]
}

FACTION_STYLES = ["empire", "hive", "federation", "corporation", "cult"]

def generate_faction_name(style=None):
    if style is None:
        style = random.choice(FACTION_STYLES)
    prefix = random.choice(PREFIXES[style])
    suffix = random.choice(SUFFIXES[style])
    return f"{prefix} {suffix}"

def generate_all_faction_names(count=3):
    styles = random.sample(FACTION_STYLES, min(count, len(FACTION_STYLES)))
    names = []
    used_names = set()
    for style in styles:
        name = generate_faction_name(style)
        while name in used_names:
            name = generate_faction_name(style)
        used_names.add(name)
        names.append(name)
    return names

def get_faction_emoji(style):
    emojis = {
        "empire": "👑",
        "hive": "🐝",
        "federation": "🌌",
        "corporation": "💼",
        "cult": "🔮"
    }
    return emojis.get(style, "⭐")

def get_faction_color(style):
    colors = {
        "empire": "blue",
        "hive": "red",
        "federation": "green",
        "corporation": "yellow",
        "cult": "magenta"
    }
    return colors.get(style, "white")