#!/usr/bin/env python
"""
Nebula 4X — GUI через консоль з кольорами (УКРАЇНСЬКА ВЕРСІЯ)
"""

import os
import sys
import time
import random

from core.state import create_state
from core.engine_core import NebulaEngine
from core.save_load import save_game, load_game
from core.utils import clear_screen

from ui.colors import C
from ui.dynamic_map import DynamicMap
from ui.console import print_factions, print_fleets
from ui.strategy_view import print_strategy_dashboard
from ui.victory_view import print_victory_progress
from ui.replay import print_replay
from ui.ai_view import print_ai_brain
from ui.debug_view import print_debug
from ui.inspector import inspect_system
from ui.stats_view import print_stats
from ui.war_view import print_active_wars
from ui.events_log import print_recent_events
from ui.human_controls import print_human_controls, handle_human_command

from world.victory import check_victory, get_victory_stats
from world.factions import generate_factions
from world.technology import TECH_TREE, research_tech, init_tech
from world.achievements import print_achievements

from rpg.ship import create_ship, ship_status
from rpg.quests import handle_quest_command, check_quests
from rpg.trade import trade as rpg_trade
from rpg.travel import travel as rpg_travel
from rpg.upgrade import upgrade as rpg_upgrade
from rpg.map import map_view as rpg_map
from rpg.stats import stats_full as rpg_stats

from locales import _, set_language, get_current_lang

# ============================================================
# ВСТАНОВЛЕННЯ МОВИ
# ============================================================
set_language("uk")  # Українська за замовчуванням


# ============================================================
# УТИЛІТИ ДЛЯ МЕНЮ
# ============================================================

def print_logo():
    print(C.BOLD + C.CYAN + """
    ╔══════════════════════════════════════════════════════╗
    ║                                                      ║
    ║     ███╗   ██╗███████╗██████╗ ██╗   ██╗██╗      ║
    ║     ████╗  ██║██╔════╝██╔══██╗██║   ██║██║      ║
    ║     ██╔██╗ ██║█████╗  ██████╔╝██║   ██║██║      ║
    ║     ██║╚██╗██║██╔══╝  ██╔══██╗██║   ██║██║      ║
    ║     ██║ ╚████║███████╗██████╔╝╚██████╔╝███████╗ ║
    ║     ╚═╝  ╚═══╝╚══════╝╚═════╝  ╚═════╝ ╚══════╝ ║
    ║                                                      ║
    ║         Н Е Б У Л А   4 X                          ║
    ║         ═══════════════════════════════             ║
    ║     4X Стратегія  |  RPG  |  Відкритий світ       ║
    ╚══════════════════════════════════════════════════════╝
    """ + C.RESET)


def print_menu():
    print(C.BOLD + "\n" + "=" * 60 + C.RESET)
    print(C.YELLOW + _("menu_title") + C.RESET)
    print(C.BOLD + "=" * 60 + C.RESET)
    print()
    print("  " + C.GREEN + "1." + C.RESET + "  " + _("menu_new_4x"))
    print("  " + C.GREEN + "2." + C.RESET + "  " + _("menu_new_rpg"))
    print("  " + C.GREEN + "3." + C.RESET + "  " + _("menu_load"))
    print("  " + C.GREEN + "4." + C.RESET + "  " + _("menu_how_to_play"))
    # Пункт вибору мови
    current_lang = "🇺🇦 Українська" if get_current_lang() == "uk" else "🇬🇧 English"
    print("  " + C.CYAN + "5." + C.RESET + "  " + _("menu_language") + f" [{current_lang}]")
    print("  " + C.RED   + "6." + C.RESET + "  " + _("menu_quit"))
    print()
    print(C.BOLD + "=" * 60 + C.RESET)


def choose_language():
    """Функція для вибору мови"""
    clear_screen()
    print_logo()
    print()
    print(C.BOLD + "=" * 60 + C.RESET)
    print(C.CYAN + _("choose_language_title") + C.RESET)
    print(C.BOLD + "=" * 60 + C.RESET)
    print()
    print("  " + C.GREEN + "1." + C.RESET + "  " + _("lang_uk"))
    print("  " + C.GREEN + "2." + C.RESET + "  " + _("lang_en"))
    print()
    
    choice = input(_("menu_select")).strip()
    
    if choice == "1":
        set_language("uk")
        print(C.GREEN + "  ✅ " + _("lang_changed") + C.RESET)
    elif choice == "2":
        set_language("en")
        print(C.GREEN + "  ✅ " + _("lang_changed") + C.RESET)
    else:
        print(C.RED + "  " + _("invalid_choice") + C.RESET)
    
    input(_("press_enter"))


def print_how_to_play():
    clear_screen()
    print_logo()
    print()
    print(C.BOLD + "=" * 60 + C.RESET)
    print(C.CYAN + _("how_to_play") + C.RESET)
    print(C.BOLD + "=" * 60 + C.RESET)
    print()
    print("  🚀 " + C.YELLOW + _("goal") + C.RESET + " " + _("goal_desc"))
    print()
    print("  🏛️ " + C.YELLOW + _("mode_4x") + C.RESET)
    print("     " + _("mode_4x_desc"))
    print("     " + _("commands") + ": b, m, a, peace, spy, tech, w, r, ach")
    print()
    print("  🚀 " + C.YELLOW + _("mode_rpg") + C.RESET)
    print("     " + _("mode_rpg_desc"))
    print("     " + _("commands") + ": trade, travel, upgrade, map, stats, quests, ach")
    print()
    print("  🔄 " + C.YELLOW + _("switch_mode") + C.RESET)
    print("     " + _("switch_mode_desc"))
    print()
    print("  💾 " + C.YELLOW + _("save") + C.RESET)
    print("     " + _("save_desc"))
    print()
    print("  🏆 " + C.YELLOW + _("achievements") + C.RESET)
    print("     " + _("achievements_desc"))
    print()
    input(C.BOLD + "\n" + _("press_enter") + C.RESET)


def choose_faction(state):
    clear_screen()
    print_logo()
    print()
    print(C.BOLD + "=" * 60 + C.RESET)
    print(C.CYAN + _("choose_faction") + C.RESET)
    print(C.BOLD + "=" * 60 + C.RESET)
    print()

    factions = list(state["factions"].keys())

    for i, fid in enumerate(factions, 1):
        faction = state["factions"][fid]
        name = faction.get("name", fid)
        systems = len(faction.get("systems", []))
        credits = faction.get("credits", 0)
        military = round(faction.get("military", 1), 2)

        color = C.BLUE if fid == "FACTION_A" else C.RED if fid == "FACTION_B" else C.GREEN

        print(f"  {color}{i}. {name}{C.RESET}")
        print(f"     🪐 " + _("systems") + f": {systems}  💰 " + _("credits") + f": {credits}  ⚔️ " + _("military") + f": {military}")
        print()

    print("  " + C.YELLOW + "0. " + _("random_faction") + C.RESET)
    print()

    while True:
        choice = input(_("select_faction")).strip()

        if choice == "0":
            return random.choice(factions)

        try:
            idx = int(choice) - 1
            if 0 <= idx < len(factions):
                return factions[idx]
        except:
            pass

        print(C.RED + "  " + _("invalid_choice") + C.RESET)


def create_unified_state(mode="strategy"):
    state = create_state()
    state["ship"] = create_ship()
    state["mode"] = mode
    state["quests"] = []
    from world.achievements import init_achievements
    init_achievements(state)
    return state


def print_commands(mode):
    print()
    print(C.BOLD + "=" * 50 + C.RESET)
    print(C.CYAN + _("commands").upper() + f" ({mode.upper()})" + C.RESET)
    print(C.BOLD + "=" * 50 + C.RESET)
    
    if mode == "strategy":
        print("  [b] " + _("cmd_build") + "  [m] " + _("cmd_move") + "  [a] " + _("cmd_attack") + "  [i] " + _("cmd_inspect") + "  [f] " + _("cmd_stats"))
        print("  [w] " + _("cmd_wars") + "  [r] " + _("cmd_events") + "  [ach] " + _("cmd_achievements") + "  [peace] " + _("cmd_peace") + "  [spy] " + _("cmd_spy") + "  [tech] " + _("cmd_tech"))
        print("  [mode] " + _("cmd_mode"))
        print("  [p] " + _("cmd_pause") + "  [s] " + _("cmd_step") + "  [S] " + _("cmd_save") + "  [q] " + _("cmd_menu"))
    
    elif mode == "rpg":
        print("  [trade] " + _("cmd_trade") + "   [travel] " + _("cmd_travel") + "  [upgrade] " + _("cmd_upgrade") + "  [map] " + _("cmd_map") + "  [stats] " + _("cmd_stats") + "  [quests] " + _("cmd_quests") + "  [ach] " + _("cmd_achievements"))
        print("  [mode] " + _("cmd_mode"))
        print("  [S] " + _("cmd_save") + "  [q] " + _("cmd_menu"))


def switch_mode(state):
    if state["mode"] == "strategy":
        state["mode"] = "rpg"
        print(C.GREEN + "  🚀 " + _("switch_to_rpg") + C.RESET)
    else:
        state["mode"] = "strategy"
        print(C.GREEN + "  🏛️ " + _("switch_to_4x") + C.RESET)
    input(_("press_enter"))


# ============================================================
# ДОДАТКОВІ ФУНКЦІЇ ДЛЯ СТРАТЕГІЇ
# ============================================================

def handle_peace(state, player_faction):
    if not player_faction:
        print(C.RED + "  " + _("only_human_peace") + C.RESET)
        return

    enemies = []
    for fid in state["factions"]:
        if fid != player_faction:
            rel = state["diplomacy"].get(player_faction, {}).get(fid, {}).get("relation", 0)
            if rel < -20:
                enemies.append(fid)

    if not enemies:
        print(C.GREEN + "  " + _("no_enemies_peace") + C.RESET)
        return

    print(C.YELLOW + "  " + _("propose_peace") + C.RESET)
    for i, fid in enumerate(enemies, 1):
        print(f"    {i}. {fid}")

    try:
        choice = int(input("  " + _("select") + ": "))
        target = enemies[choice - 1]
        state["diplomacy"][player_faction][target]["relation"] = max(-20, state["diplomacy"][player_faction][target]["relation"] + 30)
        state["diplomacy"][target][player_faction]["relation"] = max(-20, state["diplomacy"][target][player_faction]["relation"] + 30)
        print(C.GREEN + f"  ☮️ " + _("peace_proposed") + f" {target}!" + C.RESET)
    except:
        print(C.RED + "  " + _("invalid_choice") + C.RESET)


def handle_spy(state, player_faction):
    if not player_faction:
        print(C.RED + "  " + _("only_human_spy") + C.RESET)
        return

    targets = [f for f in state["factions"] if f != player_faction]
    if not targets:
        print(C.RED + "  " + _("no_spy_targets") + C.RESET)
        return

    print(C.YELLOW + "  " + _("spy_on") + C.RESET)
    for i, fid in enumerate(targets, 1):
        print(f"    {i}. {fid}")

    try:
        choice = int(input("  " + _("select") + ": "))
        target = targets[choice - 1]

        success = random.random() < 0.4
        if success:
            info = {
                "credits": state["factions"][target]["credits"],
                "systems": len(state["factions"][target]["systems"]),
                "military": round(state["factions"][target]["military"], 2),
                "fleets": sum(1 for f in state["fleets"].values() if f["owner"] == target)
            }
            print(C.GREEN + f"  🕵️ " + _("intel_on") + f" {target}:" + C.RESET)
            print(f"     💰 " + _("credits") + f": {info['credits']}")
            print(f"     🪐 " + _("systems") + f": {info['systems']}")
            print(f"     ⚔️ " + _("military") + f": {info['military']}")
            print(f"     🚀 " + _("fleets") + f": {info['fleets']}")
        else:
            print(C.RED + f"  🕵️ " + _("spy_failed") + f" {target}!" + C.RESET)
            state["factions"][player_faction]["credits"] = max(0, state["factions"][player_faction]["credits"] - 10)
    except:
        print(C.RED + "  " + _("invalid_choice") + C.RESET)


def handle_tech(state, player_faction):
    if not player_faction:
        print(C.RED + "  " + _("only_human_tech") + C.RESET)
        return

    print(C.YELLOW + "  " + _("available_tech") + C.RESET)
    techs = list(TECH_TREE.keys())
    for i, name in enumerate(techs, 1):
        data = TECH_TREE[name]
        researched = name in state["factions"][player_faction].get("tech", [])
        status = C.GREEN + "✅" + C.RESET if researched else C.GRAY + "⬜" + C.RESET
        print(f"    {i}. {status} {name} - {data['desc']} (" + _("cost") + f": {data['cost']})")

    try:
        choice = int(input("  " + _("select_tech") + ": "))
        tech_name = techs[choice - 1]
        ok, msg = research_tech(state, player_faction, tech_name)
        if ok:
            print(C.GREEN + f"  {msg}" + C.RESET)
        else:
            print(C.RED + f"  {msg}" + C.RESET)
    except:
        print(C.RED + "  " + _("invalid_choice") + C.RESET)


# ============================================================
# ОСНОВНИЙ ЦИКЛ ГРИ
# ============================================================

def run_game(mode="strategy", loaded_state=None):
    if loaded_state:
        state = loaded_state
        if "mode" not in state:
            state["mode"] = mode
        if "ship" not in state:
            state["ship"] = create_ship()
        if "quests" not in state:
            state["quests"] = []
        from world.achievements import init_achievements
        if "achievements" not in state:
            init_achievements(state)
    else:
        state = create_unified_state(mode)
    
    engine = NebulaEngine(state)
    dynamic_map = DynamicMap()
    
    if mode == "strategy" and not loaded_state:
        player_faction = choose_faction(state)
        print(C.GREEN + f"\n  " + _("you_are") + f" {player_faction}" + C.RESET)
        print(C.YELLOW + "  " + _("press_enter_continue") + C.RESET)
        input()
    else:
        player_faction = list(state["factions"].keys())[0]
    
    auto = True
    running = True
    speed = 0.5
    
    while running:
        try:
            if auto and state.get("mode") == "strategy":
                engine.step()
                
                victory = check_victory(state)
                if victory:
                    clear_screen()
                    print()
                    print("=" * 60)
                    print(C.BOLD + C.GREEN + "🎉 " + _("victory") + " 🎉" + C.RESET)
                    print("=" * 60)
                    print()
                    print(f"  {victory['winner']} " + _("winner") + f" {victory['type']} ({victory['percent']}%)")
                    print()
                    
                    stats = get_victory_stats(state)
                    print(C.YELLOW + "  " + _("game_stats") + C.RESET)
                    print(f"    " + _("turns") + f": {stats['turns']}")
                    print(f"    " + _("total_systems") + f": {stats['total_systems']}")
                    print(f"    " + _("total_fleets") + f": {stats['total_fleets']}")
                    print()
                    
                    print(C.CYAN + "  " + _("final_standings") + C.RESET)
                    for fid, data in stats["factions"].items():
                        name = data["name"]
                        color = C.GREEN if fid == stats["winner"] else C.GRAY
                        print(f"    {color}{name}{C.RESET}: {data['systems']} " + _("systems") + f", {data['credits']} " + _("credits"))
                        print(f"      " + _("wins") + f": {data['wins']}, " + _("losses") + f": {data['losses']}")
                    
                    print()
                    print("=" * 60)
                    print()
                    print(C.YELLOW + "  " + _("continue_playing") + C.RESET)
                    print(C.GRAY + "  " + _("continue_playing_hint") + C.RESET)
                    choice = input("  > ").strip().lower()
                    
                    if choice == "y" or choice == "yes" or choice == "т" or choice == "так":
                        print(C.GREEN + "  " + _("continue_game") + C.RESET)
                        input(_("press_enter"))
                        continue
                    else:
                        print(C.CYAN + "  " + _("thanks_playing") + C.RESET)
                        input(_("press_enter_return"))
                        return
            
            clear_screen()
            
            if state.get("victory_achieved", False):
                print(C.GOLD + "  👑 " + state.get("winner", "Unknown") + " " + _("already_won") + C.RESET)
                print(C.GRAY + "  " + _("continue_challenges") + C.RESET)
                print()
            
            current_mode = state.get("mode", "strategy")
            
            if current_mode == "strategy":
                dynamic_map.render(state, engine.ticks)
                print_factions(state)
                print_fleets(state)
                print_strategy_dashboard(state)
                print_victory_progress(state)
                print_ai_brain(state)
                print_debug(state)
                print_replay(state)
                
                print()
                print("=" * 50)
                print(f"⏱️ " + _("time") + f": {state['time']} | 🔄 " + _("ticks") + f": {engine.ticks}")
                player = state["factions"][player_faction]
                print(f"👤 " + _("you") + f": {player_faction} | 💰 {player['credits']} | 🪐 {len(player['systems'])}")
                
            else:
                ship_status(state.get("ship", {}))
                active_quests = [q for q in state.get("quests", []) if q.get("status") == "active"]
                if active_quests:
                    print(f"  📜 " + _("quests") + f": {len(active_quests)} " + _("active"))
            
            if engine.ticks > 0 and engine.ticks % 50 == 0:
                save_game(state, "autosave.json")
            
            print_commands(current_mode)
            
            try:
                cmd = input(">> ").strip()
            except EOFError:
                print("\n" + C.YELLOW + "  " + _("exiting_menu") + C.RESET)
                running = False
                break
            
            if cmd == "q" or cmd == "Q":
                running = False
            
            elif cmd == "S":
                ok, msg = save_game(state)
                print(C.GREEN + msg + C.RESET)
                input(_("press_enter"))
            
            elif cmd.lower() == "mode":
                switch_mode(state)
            
            else:
                cmd = cmd.lower()
                
                if cmd == "ach":
                    print_achievements(state)
                    input(_("press_enter"))
                
                elif current_mode == "strategy":
                    if cmd == "p":
                        auto = not auto
                    elif cmd == "s":
                        if auto:
                            auto = False
                        engine.step()
                    elif cmd in ["b", "m", "a"]:
                        handle_human_command(state, cmd, player_faction)
                    elif cmd == "i":
                        sys_id = input("  " + _("enter_system_id") + ": ").strip()
                        inspect_system(state, sys_id)
                        input(_("press_enter"))
                    elif cmd == "f":
                        print_stats(state, engine)
                        input(_("press_enter"))
                    elif cmd == "w":
                        print_active_wars(state)
                        input(_("press_enter"))
                    elif cmd == "r":
                        print_recent_events(state)
                        input(_("press_enter"))
                    elif cmd == "peace":
                        handle_peace(state, player_faction)
                        input(_("press_enter"))
                    elif cmd == "spy":
                        handle_spy(state, player_faction)
                        input(_("press_enter"))
                    elif cmd == "tech":
                        handle_tech(state, player_faction)
                        input(_("press_enter"))
                    else:
                        print(C.RED + f"  " + _("unknown_command") + f": {cmd}" + C.RESET)
                
                elif current_mode == "rpg":
                    ship = state["ship"]
                    
                    if cmd == "trade":
                        rpg_trade(ship)
                        input(_("press_enter"))
                    elif cmd == "travel":
                        rpg_travel(ship)
                        completed = check_quests(state)
                        if completed:
                            print(C.GREEN + f"  ✅ {len(completed)} " + _("quests_completed") + C.RESET)
                        input(_("press_enter"))
                    elif cmd == "upgrade":
                        rpg_upgrade(ship)
                        input(_("press_enter"))
                    elif cmd == "map":
                        rpg_map(ship)
                        input(_("press_enter"))
                    elif cmd == "stats":
                        rpg_stats(ship)
                        input(_("press_enter"))
                    elif cmd == "quests":
                        handle_quest_command(state)
                        input(_("press_enter"))
                    else:
                        print(C.RED + f"  " + _("unknown_command") + f": {cmd}" + C.RESET)
            
            time.sleep(speed)
        
        except KeyboardInterrupt:
            running = False
        except EOFError:
            print("\n" + C.YELLOW + "  " + _("exiting_menu") + C.RESET)
            running = False
        except Exception as e:
            print(C.RED + f"  " + _("error") + f": {e}" + C.RESET)
            import traceback
            traceback.print_exc()
            input(_("press_enter"))
            running = False
    
    clear_screen()


# ============================================================
# ГОЛОВНЕ МЕНЮ
# ============================================================

def main():
    while True:
        clear_screen()
        print_logo()
        print_menu()
        
        try:
            choice = input("\n  " + _("menu_select")).strip()
        except EOFError:
            print("\n" + C.CYAN + "  " + _("thanks_for_playing") + C.RESET)
            sys.exit()
        
        if choice == "1":
            run_game(mode="strategy")
        elif choice == "2":
            run_game(mode="rpg")
        elif choice == "3":
            data, msg = load_game()
            if data:
                print(C.GREEN + f"  {msg}" + C.RESET)
                input(_("press_enter"))
                mode = data.get("mode", "strategy")
                run_game(mode=mode, loaded_state=data)
            else:
                print(C.RED + f"  {msg}" + C.RESET)
                input()
        elif choice == "4":
            print_how_to_play()
        elif choice == "5":
            choose_language()
        elif choice == "6":
            clear_screen()
            print(C.CYAN + "\n  " + _("thanks_for_playing") + " 🌌" + C.RESET)
            print()
            sys.exit()
        else:
            print(C.RED + "  " + _("invalid_choice") + C.RESET)
            input()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n" + C.YELLOW + "  " + _("interrupted") + C.RESET)
        sys.exit(0)
    except EOFError:
        print("\n" + C.CYAN + "  " + _("thanks_for_playing") + " 🌌" + C.RESET)
        sys.exit(0)