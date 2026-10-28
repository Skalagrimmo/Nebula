#!/usr/bin/env python
"""
Nebula 4X — GUI версія для Termux:GUI
"""

import sys
import time

from termuxgui import Activity, View

from core.state import create_state
from core.engine_core import NebulaEngine
from core.save_load import load_game

from gui.screens import MainMenuScreen, GameScreen, GameOverScreen
from gui.styles import GuiStyles


class NebulaApp:
    """Головний клас додатку"""
    
    def __init__(self):
        try:
            self.activity = Activity(self)
        except Exception as e:
            print(f"Failed to connect to Termux:GUI: {e}")
            print("Make sure Termux:GUI is installed and running.")
            print("Run: pkg install termux-gui-package")
            sys.exit(1)
        
        self.state = None
        self.engine = None
        self.current_screen = None
        self.running = True
        
        self.show_menu()
        self.run()
    
    def show_menu(self):
        menu = MainMenuScreen(
            self.activity,
            on_start=lambda: self.start_game("strategy"),
            on_rpg=lambda: self.start_game("rpg"),
            on_load=self.load_game,
            on_quit=self.quit_app
        )
        self.set_screen(menu)
    
    def start_game(self, mode="strategy"):
        self.state = create_state()
        self.engine = NebulaEngine(self.state)
        
        from rpg.ship import create_ship
        self.state["ship"] = create_ship()
        self.state["mode"] = mode
        self.state["quests"] = []
        from world.achievements import init_achievements
        init_achievements(self.state)
        
        game = GameScreen(
            self.activity,
            self.state,
            self.engine,
            on_back=self.show_menu
        )
        self.set_screen(game)
        
        # Запускаємо ігровий цикл
        self.game_loop()
    
    def game_loop(self):
        while self.running and self.state:
            try:
                if self.engine:
                    self.engine.step()
                    
                    from world.victory import check_victory
                    victory = check_victory(self.state)
                    if victory:
                        self.activity.runOnUiThread(lambda: self.show_victory(victory))
                        break
                
                time.sleep(0.5)
                
                if hasattr(self, 'current_screen') and hasattr(self.current_screen, 'update_info'):
                    try:
                        self.activity.runOnUiThread(self.current_screen.update_info)
                    except:
                        pass
            except Exception as e:
                print(f"Game loop error: {e}")
                break
    
    def show_victory(self, victory_data):
        victory = GameOverScreen(
            self.activity,
            victory_data,
            on_menu=self.show_menu
        )
        self.set_screen(victory)
    
    def load_game(self):
        data, msg = load_game()
        if data:
            self.state = data
            from world.achievements import init_achievements
            init_achievements(self.state)
            self.engine = NebulaEngine(self.state)
            
            game = GameScreen(
                self.activity,
                self.state,
                self.engine,
                on_back=self.show_menu
            )
            self.set_screen(game)
            self.game_loop()
        else:
            print(f"Load failed: {msg}")
    
    def set_screen(self, screen):
        self.current_screen = screen
        try:
            layout = screen.get_layout()
            self.activity.setContentView(layout)
        except Exception as e:
            print(f"Error setting screen: {e}")
    
    def quit_app(self):
        self.running = False
        try:
            self.activity.finish()
        except:
            pass
        sys.exit(0)
    
    def run(self):
        try:
            self.activity.run()
        except KeyboardInterrupt:
            self.quit_app()


if __name__ == "__main__":
    app = NebulaApp()