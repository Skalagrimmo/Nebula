import random
from core.validator import validate_state
from core.safe_state import ensure_state, ensure_faction, ensure_system
from core.events import emit
from world.events import trigger_random_event
from world.achievements import check_achievements

from simulation.economy import process_economy
from simulation.trade import process_trade
from simulation.blockade import apply_blockades
from simulation.war import process_war, fleet_battles, army_battles
from simulation.movement import move_fleets
from simulation.production import produce_fleets
from simulation.army_ai import assign_fleets_to_armies, update_army_orders

from world.ai import ai_step
from world.evolution import update_faction_personality
from world.memory import record_epoch, record_faction_history
from world.territory import compute_control_zones, update_system_value
from world.diplomacy import update_relation_status, record_war
from world.diplomacy_actions import apply_diplomacy_actions
from world.ai_strategy import build_strategic_plan
from world.frontlines import update_frontlines
from world.expansion import expand_factions
from world.alliances import update_alliances
from world.imperial_ai import update_empire_strategy


def execute_ai_actions(state, actions):
    for action in actions:
        fleet_id = action["fleet"]
        target = action["target"]
        
        if fleet_id not in state["fleets"]:
            continue
        
        fleet = state["fleets"][fleet_id]
        current = fleet["location"]
        
        if target in state["systems"][current]["neighbors"]:
            fleet["location"] = target


def tick(state):
    state = ensure_state(state)
    
    for sid in state["systems"]:
        ensure_system(state["systems"][sid])
    
    for fid in state["factions"]:
        ensure_faction(state["factions"][fid])
    
    validate_state(state)
    
    process_economy(state)
    produce_fleets(state)
    
    # Випадкові події (кожні 5 ходів)
    if state.get("ticks", 0) % 5 == 0:
        event = trigger_random_event(state)
        if event:
            emit(state, "RANDOM_EVENT", {"message": event})
    
    # Дипломатія та альянси
    update_alliances(state)
    apply_diplomacy_actions(state)
    
    # AI стратегії
    build_strategic_plan(state)
    update_empire_strategy(state)
    
    # Рух та війна
    move_fleets(state)
    assign_fleets_to_armies(state)
    fleet_battles(state)
    army_battles(state)
    
    # Експансія та фронти
    expand_factions(state)
    update_frontlines(state)
    
    # Еволюція та пам'ять
    update_faction_personality(state)
    record_epoch(state)
    record_faction_history(state)
    
    # Перевірка досягнень
    for fid, faction in state["factions"].items():
        context = {
            "battle_won": faction["stats"]["wins"] > 0,
            "systems_controlled": len(faction["systems"]),
            "credits": faction["credits"],
            "tech_count": len(faction.get("tech", [])),
            "turns": state.get("ticks", 0),
            "victory": False,
            "systems_visited": len(faction.get("systems", [])),
            "systems_captured": faction["stats"]["wins"],
            "pirate_kills": 0,
            "alliance_formed": False
        }
        check_achievements(state, context)