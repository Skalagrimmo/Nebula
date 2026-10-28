import random


def expand_factions(state):
    """Експансія фракцій (збалансована версія)"""
    
    factions = state["factions"]
    systems = state["systems"]
    
    for fid, faction in factions.items():
        owned = faction["systems"]
        
        if not owned:
            continue
        
        # 🆕 Шанс експансії залежить від військової сили
        military = faction.get("military", 1)
        chance = min(0.3, military * 0.04)  # Було 0.1
        
        if random.random() > chance:
            continue
        
        # 🆕 Потрібно мати хоча б 2 флоти для експансії
        fleet_count = sum(1 for f in state["fleets"].values() if f["owner"] == fid)
        if fleet_count < 2:
            continue
        
        origin = random.choice(owned)
        origin_sys = systems[origin]
        neighbors = origin_sys.get("neighbors", [])
        random.shuffle(neighbors)
        
        for target in neighbors:
            if target not in systems:
                continue
            
            if systems[target].get("owner") == fid:
                continue
            
            # 🆕 Складніше захопити систему з населенням
            target_pop = systems[target].get("population", 100)
            if target_pop > 300 and random.random() < 0.5:
                continue
            
            old_owner = systems[target].get("owner")
            
            # Захоплення
            systems[target]["owner"] = fid
            faction["systems"].append(target)
            
            if old_owner and old_owner in factions:
                if target in factions[old_owner]["systems"]:
                    factions[old_owner]["systems"].remove(target)
            
            state["events"].append({
                "type": "expansion",
                "faction": fid,
                "system": target,
                "from": old_owner
            })
            
            break