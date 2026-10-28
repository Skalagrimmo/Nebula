#!/usr/bin/env python
"""
Тестова версія для діагностики проблем
"""

import sys
import os

print("Starting test...")
print(f"Python version: {sys.version}")
print(f"Current directory: {os.getcwd()}")

try:
    print("Importing core.state...")
    from core.state import create_state
    print("✅ core.state imported")
    
    print("Creating state...")
    state = create_state()
    print(f"✅ State created: {len(state['systems'])} systems")
    
    print("Importing ui.dynamic_map...")
    from ui.dynamic_map import DynamicMap
    print("✅ ui.dynamic_map imported")
    
    print("Creating map renderer...")
    map_renderer = DynamicMap()
    print("✅ Map renderer created")
    
    print("Rendering map...")
    map_renderer.render(state, 0)
    print("✅ Map rendered")
    
    print("Test completed successfully!")
    
except Exception as e:
    print(f"❌ ERROR: {e}")
    import traceback
    traceback.print_exc()

print("\nPress Enter to exit...")
input()