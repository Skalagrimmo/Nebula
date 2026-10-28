TECH_TREE = {
    "laser": {"cost": 100, "bonus": 0.2, "desc": "⚡ Laser weapons +20% attack"},
    "shield": {"cost": 150, "bonus": 0.3, "desc": "🛡️ Shield tech +30% defense"},
    "warp": {"cost": 200, "bonus": 0.5, "desc": "🌀 Warp drive +50% movement"},
    "economy": {"cost": 120, "bonus": 0.25, "desc": "💰 Economic boost +25% income"},
    "colony": {"cost": 180, "bonus": 0.4, "desc": "🏛️ Colony tech +40% population"}
}

def init_tech(state):
    """Ініціалізує технології для всіх фракцій"""
    for fid in state["factions"]:
        if "tech" not in state["factions"][fid]:
            state["factions"][fid]["tech"] = []

def research_tech(state, faction_id, tech_name):
    """Досліджує технологію для фракції"""
    tech = TECH_TREE.get(tech_name)
    if not tech:
        return False, "Unknown tech"

    faction = state["factions"][faction_id]
    if tech_name in faction.get("tech", []):
        return False, "Already researched"

    if faction["credits"] < tech["cost"]:
        return False, f"Not enough credits. Need {tech['cost']}"

    faction["credits"] -= tech["cost"]
    faction["tech"].append(tech_name)
    faction["military"] *= (1 + tech["bonus"])
    
    state["events"].append({
        "type": "tech_researched",
        "faction": faction_id,
        "tech": tech_name
    })
    
    return True, f"🔬 Researched {tech_name}! {tech['desc']}"

def get_tech_bonus(faction, tech_name):
    """Повертає бонус від технології"""
    tech = TECH_TREE.get(tech_name)
    if not tech:
        return 0
    return tech["bonus"]