from world.achievements import check_achievements


def check_victory(state):
    """Перевіряє умови перемоги"""
    
    # Якщо вже була перемога, не перевіряємо знову
    if state.get("victory_achieved", False):
        return None
    
    total_systems = len(state["systems"])
    
    if total_systems == 0:
        return None
    
    # 🏛️ ДОМІНУВАННЯ: 75%
    for faction_id, faction in state["factions"].items():
        owned = len(faction["systems"])
        ratio = owned / total_systems
        
        if ratio >= 0.75:
            context = {
                "victory": True,
                "systems_controlled": owned,
                "credits": faction["credits"],
                "tech_count": len(faction.get("tech", [])),
                "turns": state.get("ticks", 0),
                "systems_captured": faction["stats"]["wins"]
            }
            check_achievements(state, context)
            
            state["victory_achieved"] = True
            state["victory_type"] = "DOMINATION"
            state["winner"] = faction_id
            
            return {
                "winner": faction_id,
                "type": "DOMINATION",
                "percent": round(ratio * 100, 1),
                "details": f"Controls {owned}/{total_systems} systems"
            }
    
    # 💰 ЕКОНОМІЧНА: 20000
    for faction_id, faction in state["factions"].items():
        if faction.get("credits", 0) >= 20000:
            context = {
                "victory": True,
                "systems_controlled": len(faction["systems"]),
                "credits": faction["credits"],
                "tech_count": len(faction.get("tech", [])),
                "turns": state.get("ticks", 0),
                "systems_captured": faction["stats"]["wins"]
            }
            check_achievements(state, context)
            
            state["victory_achieved"] = True
            state["victory_type"] = "ECONOMIC"
            state["winner"] = faction_id
            
            return {
                "winner": faction_id,
                "type": "ECONOMIC",
                "percent": 100,
                "details": "Amassed 20000 credits!"
            }
    
    # ⚔️ ВІЙСЬКОВА
    factions_with_fleets = set()
    for fleet in state["fleets"].values():
        factions_with_fleets.add(fleet["owner"])
    
    if len(factions_with_fleets) == 1 and len(state["factions"]) > 1:
        winner = list(factions_with_fleets)[0]
        alive_factions = [f for f in state["factions"] if len(state["factions"][f]["systems"]) > 0]
        if len(alive_factions) == 1:
            context = {
                "victory": True,
                "systems_controlled": len(state["factions"][winner]["systems"]),
                "credits": state["factions"][winner]["credits"],
                "tech_count": len(state["factions"][winner].get("tech", [])),
                "turns": state.get("ticks", 0),
                "systems_captured": state["factions"][winner]["stats"]["wins"]
            }
            check_achievements(state, context)
            
            state["victory_achieved"] = True
            state["victory_type"] = "MILITARY"
            state["winner"] = winner
            
            return {
                "winner": winner,
                "type": "MILITARY",
                "percent": 100,
                "details": "All enemy fleets destroyed!"
            }
    
    return None


def get_victory_progress(state):
    total_systems = len(state["systems"])
    progress = {}
    
    for faction_id, faction in state["factions"].items():
        owned = len(faction["systems"])
        
        if total_systems == 0:
            percent = 0
        else:
            percent = round(owned / total_systems * 100, 1)
        
        credits = faction.get("credits", 0)
        fleets = len([f for f in state["fleets"].values() if f["owner"] == faction_id])
        
        progress[faction_id] = {
            "systems": percent,
            "credits": credits,
            "fleets": fleets
        }
    
    return progress


def get_victory_stats(state):
    """Повертає статистику гри після перемоги"""
    return {
        "winner": state.get("winner", "Unknown"),
        "type": state.get("victory_type", "Unknown"),
        "turns": state.get("ticks", 0),
        "total_systems": len(state["systems"]),
        "total_fleets": len(state["fleets"]),
        "total_events": len(state.get("events", [])),
        "factions": {
            fid: {
                "name": faction.get("name", fid),
                "systems": len(faction["systems"]),
                "credits": faction["credits"],
                "wins": faction["stats"]["wins"],
                "losses": faction["stats"]["losses"]
            }
            for fid, faction in state["factions"].items()
        }
    }