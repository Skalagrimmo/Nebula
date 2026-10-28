import random
import time
from ui.colors import C
from world.achievements import check_achievements

ANIMATE_BATTLES = True


def process_war(state):
    fleets_to_remove = []
    
    for fleet_id, fleet in list(state["fleets"].items()):
        loc = fleet["location"]
        owner = fleet["owner"]
        strength = fleet.get("strength", 10)
        
        if loc not in state["systems"]:
            continue
            
        system = state["systems"][loc]
        system_owner = system.get("owner")
        
        if system_owner is None:
            if strength > 15:
                system["owner"] = owner
                state["factions"][owner]["systems"].append(loc)
                fleet["strength"] *= 0.85
                from core.events import emit
                emit(state, "CONQUEST", f"{owner} colonized {loc}")
            continue
        
        if system_owner != owner:
            defender_fleet = None
            defender_fleet_id = None
            
            for fid, f in state["fleets"].items():
                if f["location"] == loc and f["owner"] == system_owner:
                    defender_fleet = f
                    defender_fleet_id = fid
                    break
            
            if not defender_fleet:
                if strength > 20:
                    old_owner = system_owner
                    system["owner"] = owner
                    
                    if old_owner in state["factions"]:
                        if loc in state["factions"][old_owner]["systems"]:
                            state["factions"][old_owner]["systems"].remove(loc)
                    
                    state["factions"][owner]["systems"].append(loc)
                    fleet["strength"] *= 0.8
                    
                    from core.events import emit
                    emit(state, "CONQUEST", f"{owner} captured {loc} from {old_owner}")
                    
                    reward_wins(state, owner, old_owner)
                continue
            
            defender_strength = defender_fleet.get("strength", 10)
            strength = max(strength, 15)
            defender_strength = max(defender_strength, 15)
            
            if ANIMATE_BATTLES:
                animate_battle(fleet_id, defender_fleet_id, strength, defender_strength, loc)
            
            attack_power = strength * random.uniform(0.85, 1.15)
            defense_power = defender_strength * random.uniform(0.85, 1.15)
            
            if attack_power > defense_power:
                old_owner = system_owner
                system["owner"] = owner
                
                if old_owner in state["factions"]:
                    if loc in state["factions"][old_owner]["systems"]:
                        state["factions"][old_owner]["systems"].remove(loc)
                
                state["factions"][owner]["systems"].append(loc)
                fleet["strength"] *= 0.75
                fleets_to_remove.append(defender_fleet_id)
                
                reward_wins(state, owner, old_owner)
                record_battle_results(state, owner, old_owner, loc)
                
                # Перевірка досягнень
                context = {
                    "battle_won": True,
                    "systems_controlled": len(state["factions"][owner]["systems"]),
                    "credits": state["factions"][owner]["credits"],
                    "tech_count": len(state["factions"][owner].get("tech", [])),
                    "turns": state.get("ticks", 0),
                    "systems_captured": state["factions"][owner]["stats"]["wins"]
                }
                check_achievements(state, context)
                
                from core.events import emit
                emit(state, "BATTLE", f"{owner} defeated {old_owner} at {loc}")
                
            else:
                defender_fleet["strength"] *= 0.8
                fleet["strength"] *= 0.6
                
                if fleet["strength"] < 5:
                    fleets_to_remove.append(fleet_id)
                
                record_battle_results(state, system_owner, owner, loc)
                
                from core.events import emit
                emit(state, "BATTLE", f"{system_owner} repelled {owner} at {loc}")
    
    for fid in fleets_to_remove:
        if fid in state["fleets"]:
            del state["fleets"][fid]
    
    for fid, faction in state["factions"].items():
        total_strength = sum(f["strength"] for f in state["fleets"].values() if f["owner"] == fid)
        faction["military"] = max(1, total_strength / 80)


def animate_battle(fleet1, fleet2, strength1, strength2, system_name):
    print(C.BOLD + C.RED + f"\n⚔️ BATTLE for {system_name}!" + C.RESET)
    print(f"  {C.RED}{fleet1}{C.RESET} ({strength1}) vs {C.BLUE}{fleet2}{C.RESET} ({strength2})")
    time.sleep(0.5)


def fleet_battles(state):
    process_war(state)


def army_battles(state):
    process_war(state)


def process_fleet_combat(state, attacker_id=None, defender_id=None):
    if not attacker_id or not defender_id:
        return
    if attacker_id not in state["fleets"] or defender_id not in state["fleets"]:
        return
    
    attacker = state["fleets"][attacker_id]
    defender = state["fleets"][defender_id]
    
    if attacker["strength"] > defender["strength"]:
        defender["strength"] *= 0.5
        attacker["strength"] *= 0.8
        return attacker_id
    else:
        attacker["strength"] *= 0.5
        defender["strength"] *= 0.8
        return defender_id


def fleet_conquest(state, attacker_id, target_system):
    if target_system not in state["systems"]:
        return False
    
    system = state["systems"][target_system]
    old_owner = system.get("owner")
    
    system["owner"] = attacker_id
    
    if old_owner and old_owner in state["factions"]:
        if target_system in state["factions"][old_owner]["systems"]:
            state["factions"][old_owner]["systems"].remove(target_system)
    
    state["factions"][attacker_id]["systems"].append(target_system)
    return True


def reward_wins(state, winner_id, loser_id):
    if winner_id in state["factions"]:
        state["factions"][winner_id]["stats"]["wins"] += 1
        state["factions"][winner_id]["stats"]["battles"] += 1
        state["factions"][winner_id]["credits"] += 15
        state["factions"][winner_id]["military"] *= 1.03
    
    if loser_id in state["factions"]:
        state["factions"][loser_id]["stats"]["losses"] += 1
        state["factions"][loser_id]["stats"]["battles"] += 1
        state["factions"][loser_id]["credits"] = max(0, state["factions"][loser_id]["credits"] - 5)


def record_battle_results(state, attacker_id, defender_id, system_id):
    from core.events import emit
    emit(state, "battle_result", {
        "attacker": attacker_id,
        "defender": defender_id,
        "system": system_id
    })