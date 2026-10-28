import random
from core.events import emit
from world.reasoning import explain_decision


def choose_target_system(state, faction_id):
    """Вибирає найкращу систему для атаки"""
    faction = state["factions"][faction_id]
    
    weakest = None
    weakest_value = 999999
    
    for sid, system in state["systems"].items():
        if system["owner"] == faction_id:
            continue
        
        # Пріоритет: низький stock + високий demand
        value = system["stock"] - system["demand"]
        
        if value < weakest_value:
            weakest_value = value
            weakest = sid
    
    return weakest


def find_fleet(state, faction_id):
    """Знаходить перший флот фракції"""
    for fid, fleet in state["fleets"].items():
        if fleet["owner"] == faction_id:
            return fid
    return None


def choose_target(state, faction_id):
    """Альтернативний вибір цілі (за цінністю)"""
    best_system = None
    best_value = -1
    
    for sid, system in state["systems"].items():
        if system["owner"] == faction_id:
            continue
        
        if system.get("value", 0) > best_value:
            best_value = system["value"]
            best_system = sid
    
    return best_system


def ai_step(state):
    """Основний AI крок"""
    actions = []
    
    for faction_id in state["factions"]:
        fleet_id = find_fleet(state, faction_id)
        
        if not fleet_id:
            continue
        
        fleet = state["fleets"][fleet_id]
        
        # Вибираємо ціль
        target = choose_target_system(state, faction_id)
        
        if not target:
            continue
        
        # Проста логіка агресії
        if fleet["strength"] > 60:
            action = {
                "type": "move_attack",
                "fleet": fleet_id,
                "target": target
            }
            actions.append(action)
            
            # Записуємо рішення AI
            reasons = explain_decision(state, faction_id, action)
            emit(state, "AI_DECISION", {
                "faction": faction_id,
                "action": action,
                "reasons": reasons
            })
    
    return actions