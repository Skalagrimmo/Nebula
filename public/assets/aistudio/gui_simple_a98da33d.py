#!/usr/bin/env python
"""
Проста GUI версія для тестування
"""

import os
import sys
import time

def clear_screen():
    os.system('cls' if os.name == 'nt' else 'clear')

def main():
    clear_screen()
    print("=" * 50)
    print("NEBULA 4X - GUI TEST")
    print("=" * 50)
    print()
    print("1. Start Game")
    print("2. Quit")
    print()
    
    choice = input("Select: ").strip()
    
    if choice == "1":
        print("\nLoading game...")
        try:
            from core.state import create_state
            state = create_state()
            print(f"✅ Created state with {len(state['systems'])} systems")
            
            from ui.dynamic_map import DynamicMap
            map_renderer = DynamicMap()
            map_renderer.render(state, 0)
            
            print("\n✅ Game loaded successfully!")
            print("Press Enter to exit...")
            input()
        except Exception as e:
            print(f"\n❌ Error: {e}")
            import traceback
            traceback.print_exc()
            input("Press Enter to exit...")
    else:
        print("Goodbye!")

if __name__ == "__main__":
    main()