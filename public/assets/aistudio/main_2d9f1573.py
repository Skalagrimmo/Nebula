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

# RPG модулі
from rpg.ship import create_ship, ship_status
from rpg.quests import handle_quest_command, check_quests
from rpg.combat import fight as rpg_fight
from rpg.trade import trade as rpg_trade
from rpg.travel import travel as rpg_travel
from rpg.upgrade import upgrade as rpg_upgrade
from rpg.map import map_view as rpg_map
from rpg.stats import stats_full as rpg_stats


# ============================================================
# УТИЛІТИ ДЛЯ МЕНЮ
# ============================================================

def print_logo():
    """Друкує логотип гри"""
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
    ║         N E B U L A   U N I V E R S E              ║
    ║         ═══════════════════════════════             ║
    ║     4X Strategy  |  RPG  |  Open World             ║
    ║                                                      ║
    ╚══════════════════════════════════════════════════════╝
    """ + C.RESET)


def print_menu():
    """Друкує головне меню"""
    print(C.BOLD + "\n" + "=" * 60 + C.RESET)
    print(C.YELLOW + "MAIN MENU" + C.RESET)
    print(C.BOLD + "=" * 60 + C.RESET)
    print()
    print("  " + C.GREEN + "1." + C.RESET + "  New Game (4X Strategy)")
    print("  " + C.GREEN + "2." + C.RESET + "  New Game (RPG Mode)")
    print("  " + C.GREEN + "3." + C.RESET + "  Load Game")
    print("  " + C.GREEN + "4." + C.RESET + "  How to Play")
    print("  " + C.RED   + "5." + C.RESET + "  Quit")
    print()
    print(C.BOLD + "=" * 60 + C.RESET)


def print_how_to_play():
    """Показує інструкцію"""
    clear_screen()
    print_logo()
    print()
    print(C.BOLD + "=" * 60 + C.RESET)
    print(C.CYAN + "HOW TO PLAY" + C.RESET)
    print(C.BOLD + "=" * 60 + C.RESET)
    print()
    print("  🚀 " + C.YELLOW + "Goal:" + C.RESET + " Control 75% of all systems or amass 20000 credits!")
    print()
    print("  🏛️ " + C.YELLOW + "4X Strategy Mode:" + C.RESET)
    print("     Command a faction, build fleets, conquer systems")
    print("     Commands: b, m, a, peace, spy, tech, w, r, ach")
    print()
    print("  🚀 " + C.YELLOW + "RPG Mode:" + C.RESET)
    print("     Pilot your own ship, trade, fight, complete quests")
    print("     Commands: trade, travel, upgrade, map, stats, quests, ach")
    print()
    print("  🔄 " + C.YELLOW + "Switch Mode:" + C.RESET)
    print("     Type 'mode' to switch between 4X and RPG")
    print()
    print("  💾 " + C.YELLOW + "Save:" + C.RESET)
    print("     Type 'S' to save your game anytime")
    print()
    print("  🏆 " + C.YELLOW + "Achievements:" + C.RESET)
    print("     Type 'ach' to view your achievements")
    print()
    print("  🔄 " + C.YELLOW + "After Victory:" + C.RESET)
    print("     You can continue playing even after winning!")
    print()
    input(C.BOLD + "\nPress Enter to return to menu..." + C.RESET)


def choose_faction(state):
    """Дозволяє гравцеві обрати фракцію"""
    clear_screen()
    print_logo()
    print()
    print(C.BOLD + "=" * 60 + C.RESET)
    print(C.CYAN + "CHOOSE YOUR FACTION" + C.RESET)
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
        print(f"     🪐 Systems: {systems}  💰 Credits: {credits}  ⚔️ Military: {military}")
        print()

    print("  " + C.YELLOW + "0. Random" + C.RESET)
    print()

    while True:
        choice = input("Select faction: ").strip()

        if choice == "0":
            return random.choice(factions)

        try:
            idx = int(choice) - 1
            if 0 <= idx < len(factions):
                return factions[idx]
        except:
            pass

        print(C.RED + "  Invalid choice! Try again." + C.RESET)


def create_unified_state(mode="strategy"):
    """Створює об'єднаний стан для 4X + RPG"""
    state = create_state()
    state["ship"] = create_ship()
    state["mode"] = mode
    state["quests"] = []
    state["victory_achieved"] = False
    state["winner"] = None
    state["victory_type"] = None
    from world.achievements import init_achievements
    init_achievements(state)
    return state


def print_commands(mode):
    """Показує команди залежно від режиму"""
    print()
    print(C.BOLD + "=" * 50 + C.RESET)
    print(C.CYAN + f"COMMANDS ({mode.upper()})" + C.RESET)
    print(C.BOLD + "=" * 50 + C.RESET)
    
    if mode == "strategy":
        print("  [b] Build  [m] Move  [a] Attack  [i] Inspect  [f] Stats")
        print("  [w] Wars  [r] Events  [ach] Achievements  [peace] Peace  [spy] Spy  [tech] Tech")
        print("  [mode] Switch to RPG")
        print("  [p] Pause  [s] Step  [S] Save  [q] Menu")
    
    elif mode == "rpg":
        print("  [trade]   [travel]  [upgrade]  [map]  [stats]  [quests]  [ach] Achievements")
        print("  [mode] Switch to Strategy")
        print("  [S] Save  [q] Menu")


def switch_mode(state):
    """Перемикає режим гри"""
    if state["mode"] == "strategy":
        state["mode"] = "rpg"
        print(C.GREEN + "  🚀 Switched to RPG mode!" + C.RESET)
        print(C.GRAY + "  You are now piloting your personal ship." + C.RESET)
    else:
        state["mode"] = "strategy"
        print(C.GREEN + "  🏛️ Switched to Strategy mode!" + C.RESET)
        print(C.GRAY + "  You are now commanding your faction." + C.RESET)
    
    input("Press Enter to continue...")


# ============================================================
# ДОДАТКОВІ ФУНКЦІЇ ДЛЯ СТРАТЕГІЇ
# ============================================================

def handle_peace(state, player_faction):
    """Обробка мирної угоди"""
    if not player_faction:
        print(C.RED + "  Only human player can propose peace." + C.RESET)
        return

    enemies = []
    for fid in state["factions"]:
        if fid != player_faction:
            rel = state["diplomacy"].get(player_faction, {}).get(fid, {}).get("relation", 0)
            if rel < -20:
                enemies.append(fid)

    if not enemies:
        print(C.GREEN + "  No enemies to propose peace to." + C.RESET)
        return

    print(C.YELLOW + "  Propose peace to which faction?" + C.RESET)
    for i, fid in enumerate(enemies, 1):
        print(f"    {i}. {fid}")

    try:
        choice = int(input("  Select: "))
        target = enemies[choice - 1]
        state["diplomacy"][player_faction][target]["relation"] = max(-20, state["diplomacy"][player_faction][target]["relation"] + 30)
        state["diplomacy"][target][player_faction]["relation"] = max(-20, state["diplomacy"][target][player_faction]["relation"] + 30)
        print(C.GREEN + f"  ☮️ Peace proposed to {target}!" + C.RESET)
    except:
        print(C.RED + "  Invalid choice." + C.RESET)


def handle_spy(state, player_faction):
    """Обробка шпигунства"""
    if not player_faction:
        print(C.RED + "  Only human player can spy." + C.RESET)
        return

    targets = [f for f in state["factions"] if f != player_faction]
    if not targets:
        print(C.RED + "  No targets to spy on." + C.RESET)
        return

    print(C.YELLOW + "  Spy on which faction?" + C.RESET)
    for i, fid in enumerate(targets, 1):
        print(f"    {i}. {fid}")

    try:
        choice = int(input("  Select: "))
        target = targets[choice - 1]

        success = random.random() < 0.4
        if success:
            info = {
                "credits": state["factions"][target]["credits"],
                "systems": len(state["factions"][target]["systems"]),
                "military": round(state["factions"][target]["military"], 2),
                "fleets": sum(1 for f in state["fleets"].values() if f["owner"] == target)
            }
            print(C.GREEN + f"  🕵️ Intel on {target}:" + C.RESET)
            print(f"     💰 Credits: {info['credits']}")
            print(f"     🪐 Systems: {info['systems']}")
            print(f"     ⚔️ Military: {info['military']}")
            print(f"     🚀 Fleets: {info['fleets']}")
        else:
            print(C.RED + f"  🕵️ Spy mission to {target} failed!" + C.RESET)
            state["factions"][player_faction]["credits"] = max(0, state["factions"][player_faction]["credits"] - 10)
    except:
        print(C.RED + "  Invalid choice." + C.RESET)


def handle_tech(state, player_faction):
    """Обробка технологій"""
    if not player_faction:
        print(C.RED + "  Only human player can research tech." + C.RESET)
        return

    print(C.YELLOW + "  Available technologies:" + C.RESET)
    techs = list(TECH_TREE.keys())
    for i, name in enumerate(techs, 1):
        data = TECH_TREE[name]
        researched = name in state["factions"][player_faction].get("tech", [])
        status = C.GREEN + "✅" + C.RESET if researched else C.GRAY + "⬜" + C.RESET
        print(f"    {i}. {status} {name} - {data['desc']} (cost: {data['cost']})")

    try:
        choice = int(input("  Select tech to research: "))
        tech_name = techs[choice - 1]
        ok, msg = research_tech(state, player_faction, tech_name)
        if ok:
            print(C.GREEN + f"  {msg}" + C.RESET)
        else:
            print(C.RED + f"  {msg}" + C.RESET)
    except:
        print(C.RED + "  Invalid choice." + C.RESET)


# ============================================================
# ОСНОВНИЙ ЦИКЛ ГРИ
# ============================================================

def run_game(mode="strategy", loaded_state=None):
    """Запускає об'єднану гру"""
    
    if loaded_state:
        state = loaded_state
        if "mode" not in state:
            state["mode"] = mode
        if "ship" not in state:
            state["ship"] = create_ship()
        if "quests" not in state:
            state["quests"] = []
        if "victory_achieved" not in state:
            state["victory_achieved"] = False
        if "winner" not in state:
            state["winner"] = None
        if "victory_type" not in state:
            state["victory_type"] = None
        from world.achievements import init_achievements
        if "achievements" not in state:
            init_achievements(state)
    else:
        state = create_unified_state(mode)
    
    engine = NebulaEngine(state)
    dynamic_map = DynamicMap()
    
    if mode == "strategy" and not loaded_state:
        player_faction = choose_faction(state)
        print(C.GREEN + f"\n  You are: {player_faction}" + C.RESET)
        print(C.YELLOW + "  Press Enter to continue..." + C.RESET)
        try:
            input()
        except EOFError:
            print("\n" + C.YELLOW + "  Returning to menu..." + C.RESET)
            return
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
                    print(C.BOLD + C.GREEN + "🎉 GALACTIC VICTORY! 🎉" + C.RESET)
                    print("=" * 60)
                    print()
                    print(f"  {victory['winner']} wins by {victory['type']} ({victory['percent']}%)")
                    print()
                    
                    # Показуємо статистику
                    stats = get_victory_stats(state)
                    print(C.YELLOW + "  GAME STATISTICS:" + C.RESET)
                    print(f"    Turns: {stats['turns']}")
                    print(f"    Total Systems: {stats['total_systems']}")
                    print(f"    Total Fleets: {stats['total_fleets']}")
                    print()
                    
                    print(C.CYAN + "  FINAL STANDINGS:" + C.RESET)
                    for fid, data in stats["factions"].items():
                        name = data["name"]
                        color = C.GREEN if fid == stats["winner"] else C.GRAY
                        print(f"    {color}{name}{C.RESET}: {data['systems']} systems, {data['credits']} credits")
                        print(f"      Wins: {data['wins']}, Losses: {data['losses']}")
                    
                    print()
                    print("=" * 60)
                    
                    # 🆕 ПИТАННЯ ПРО ПРОДОВЖЕННЯ
                    print()
                    print(C.YELLOW + "  Continue playing? (y/n)" + C.RESET)
                    print(C.GRAY + "  (You can keep playing even after victory)" + C.RESET)
                    choice = input("  > ").strip().lower()
                    
                    if choice == "y" or choice == "yes":
                        print(C.GREEN + "  Continuing the game..." + C.RESET)
                        print(C.GRAY + "  New challenges await!" + C.RESET)
                        input("Press Enter to continue...")
                        continue
                    else:
                        print(C.CYAN + "  Thanks for playing!" + C.RESET)
                        input("Press Enter to return to menu...")
                        return
            
            clear_screen()
            
            # Якщо перемога вже була, показуємо статус
            if state.get("victory_achieved", False):
                print(C.GOLD + "  👑 " + state.get("winner", "Unknown") + " has already won!" + C.RESET)
                print(C.GRAY + "  Continue playing for more challenges." + C.RESET)
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
                print(f"TIME: {state['time']} | TICKS: {engine.ticks}")
                player = state["factions"][player_faction]
                print(f"YOU: {player_faction} | 💰 {player['credits']} | 🪐 {len(player['systems'])}")
                
            else:
                ship_status(state.get("ship", {}))
                active_quests = [q for q in state.get("quests", []) if q.get("status") == "active"]
                if active_quests:
                    print(f"  📜 Quests: {len(active_quests)} active")
            
            if engine.ticks > 0 and engine.ticks % 50 == 0:
                save_game(state, "autosave.json")
            
            print_commands(current_mode)
            
            try:
                cmd = input(">> ").strip()
            except EOFError:
                print("\n" + C.YELLOW + "  Exiting to menu..." + C.RESET)
                running = False
                break
            
            # ============================================
            # КОМАНДИ БЕЗ .lower() (регістрозалежні)
            # ============================================
            if cmd == "q" or cmd == "Q":
                running = False
            
            elif cmd == "S":
                ok, msg = save_game(state)
                print(C.GREEN + msg + C.RESET)
                try:
                    input("Press Enter to continue...")
                except EOFError:
                    pass
            
            elif cmd.lower() == "mode":
                switch_mode(state)
            
            # ============================================
            # ВСІ ІНШІ КОМАНДИ (.lower())
            # ============================================
            else:
                cmd = cmd.lower()
                
                # Досягнення (доступні в обох режимах)
                if cmd == "ach":
                    print_achievements(state)
                    try:
                        input("Press Enter to continue...")
                    except EOFError:
                        pass
                
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
                        sys_id = input("Enter system ID: ").strip()
                        inspect_system(state, sys_id)
                        try:
                            input("Press Enter to continue...")
                        except EOFError:
                            pass
                    elif cmd == "f":
                        print_stats(state, engine)
                        try:
                            input("Press Enter to continue...")
                        except EOFError:
                            pass
                    elif cmd == "w":
                        print_active_wars(state)
                        try:
                            input("Press Enter to continue...")
                        except EOFError:
                            pass
                    elif cmd == "r":
                        print_recent_events(state)
                        try:
                            input("Press Enter to continue...")
                        except EOFError:
                            pass
                    elif cmd == "peace":
                        handle_peace(state, player_faction)
                        try:
                            input("Press Enter to continue...")
                        except EOFError:
                            pass
                    elif cmd == "spy":
                        handle_spy(state, player_faction)
                        try:
                            input("Press Enter to continue...")
                        except EOFError:
                            pass
                    elif cmd == "tech":
                        handle_tech(state, player_faction)
                        try:
                            input("Press Enter to continue...")
                        except EOFError:
                            pass
                    else:
                        print(C.RED + f"  Unknown command: {cmd}" + C.RESET)
                
                elif current_mode == "rpg":
                    ship = state["ship"]
                    
                    if cmd == "trade":
                        rpg_trade(ship)
                        try:
                            input("Press Enter to continue...")
                        except EOFError:
                            pass
                    elif cmd == "travel":
                        rpg_travel(ship)
                        completed = check_quests(state)
                        if completed:
                            print(C.GREEN + f"  ✅ {len(completed)} quest(s) completed!" + C.RESET)
                        try:
                            input("Press Enter to continue...")
                        except EOFError:
                            pass
                    elif cmd == "upgrade":
                        rpg_upgrade(ship)
                        try:
                            input("Press Enter to continue...")
                        except EOFError:
                            pass
                    elif cmd == "map":
                        rpg_map(ship)
                        try:
                            input("Press Enter to continue...")
                        except EOFError:
                            pass
                    elif cmd == "stats":
                        rpg_stats(ship)
                        try:
                            input("Press Enter to continue...")
                        except EOFError:
                            pass
                    elif cmd == "quests":
                        handle_quest_command(state)
                        try:
                            input("Press Enter to continue...")
                        except EOFError:
                            pass
                    else:
                        print(C.RED + f"  Unknown command: {cmd}" + C.RESET)
            
            time.sleep(speed)
        
        except KeyboardInterrupt:
            running = False
        except EOFError:
            print("\n" + C.YELLOW + "  Exiting to menu..." + C.RESET)
            running = False
        except Exception as e:
            print(C.RED + f"Error: {e}" + C.RESET)
            import traceback
            traceback.print_exc()
            try:
                input("Press Enter to continue...")
            except EOFError:
                pass
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
            choice = input("\n  Select option: ").strip()
        except EOFError:
            print("\n" + C.CYAN + "  Thanks for playing! 🌌" + C.RESET)
            sys.exit()
        
        if choice == "1":
            run_game(mode="strategy")
        elif choice == "2":
            run_game(mode="rpg")
        elif choice == "3":
            data, msg = load_game()
            if data:
                print(C.GREEN + f"  {msg}" + C.RESET)
                input("Press Enter to continue...")
                mode = data.get("mode", "strategy")
                run_game(mode=mode, loaded_state=data)
            else:
                print(C.RED + f"  {msg}" + C.RESET)
                input()
        elif choice == "4":
            print_how_to_play()
        elif choice == "5":
            clear_screen()
            print(C.CYAN + "\n  Thanks for playing Nebula Universe! 🌌" + C.RESET)
            print()
            sys.exit()
        else:
            print(C.RED + "  Invalid choice!" + C.RESET)
            input()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n" + C.YELLOW + "  Interrupted by user." + C.RESET)
        sys.exit(0)
    except EOFError:
        print("\n" + C.CYAN + "  Thanks for playing! 🌌" + C.RESET)
        sys.exit(0)