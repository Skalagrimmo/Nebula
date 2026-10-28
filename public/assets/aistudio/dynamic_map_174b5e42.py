import os
import time
from ui.colors import C, get_faction_color


class DynamicMap:
    """Динамічна карта для Termux (робоча версія)"""
    
    def __init__(self):
        self.last_update = 0
        self.previous_state = None
        self.change_log = []
        self.size_x = 45
        self.size_y = 25
    
    def clear_screen(self):
        os.system('cls' if os.name == 'nt' else 'clear')
    
    def render(self, state, tick_count, show_changes=True):
        changes = self.detect_changes(state) if self.previous_state else []
        self.render_map(state, changes)
        
        if show_changes and changes:
            self.show_changes(changes)
        
        self.previous_state = self.snapshot(state)
        self.last_update = tick_count
        
        return changes
    
    def render_map(self, state, changes):
        size_x = self.size_x
        size_y = self.size_y
        
        grid = [[" " for _ in range(size_x)] for _ in range(size_y)]
        change_map = [[False for _ in range(size_x)] for _ in range(size_y)]
        
        for change in changes:
            if change["type"] in ["system", "capital", "new_fleet"]:
                x, y = change.get("pos", (0, 0))
                if 0 <= x < size_x and 0 <= y < size_y:
                    change_map[y][x] = True
        
        systems = state.get("systems", {})
        fleets = state.get("fleets", {})
        factions = state.get("factions", {})
        
        # Малюємо системи
        for sid, system in systems.items():
            pos = system.get("pos", (0, 0))
            if len(pos) < 2:
                continue
            x, y = pos
            
            if 0 <= x < size_x and 0 <= y < size_y:
                owner = system.get("owner")
                capital = system.get("capital", False)
                is_changed = change_map[y][x]
                
                if capital:
                    char = "★"
                else:
                    char = "●"
                
                if owner and owner in factions:
                    color = get_faction_color(owner)
                else:
                    color = C.GRAY
                    char = "·"
                
                if is_changed:
                    color = C.YELLOW + C.BOLD
                    for change in changes:
                        if change.get("sid") == sid and change["type"] == "system":
                            char = "⚡"
                            break
                        if change.get("sid") == sid and change["type"] == "capital":
                            char = "⭐"
                            break
                
                grid[y][x] = color + char + C.RESET
        
        # Додаємо флоти
        for fid, fleet in fleets.items():
            loc = fleet.get("location")
            if loc and loc in systems:
                pos = systems[loc].get("pos", (0, 0))
                if len(pos) < 2:
                    continue
                x, y = pos
                if 0 <= x < size_x and 0 <= y < size_y:
                    is_changed = change_map[y][x]
                    color = C.YELLOW
                    if is_changed:
                        color = C.GOLD + C.BOLD
                    grid[y][x] = color + "▲" + C.RESET
        
        # Друкуємо карту
        print()
        print(C.BOLD + "╔" + "═" * (size_x + 2) + "╗" + C.RESET)
        for row in grid:
            print(C.BOLD + "║" + C.RESET + "".join(row) + C.BOLD + "║" + C.RESET)
        print(C.BOLD + "╚" + "═" * (size_x + 2) + "╝" + C.RESET)
        
        # Легенда
        print()
        legend = C.GRAY + "  ·" + C.RESET + " Neutral  "
        for fid, faction in factions.items():
            name = faction.get("name", fid)
            color = get_faction_color(fid)
            legend += color + "●" + C.RESET + f" {name[:12]}  "
        legend += C.YELLOW + "▲" + C.RESET + " Fleet  "
        legend += C.GOLD + "★" + C.RESET + " Capital  "
        legend += C.YELLOW + "⚡" + C.RESET + " Changed"
        print(legend)
    
    def detect_changes(self, state):
        changes = []
        
        if not self.previous_state:
            return changes
        
        prev_systems = self.previous_state.get("systems", {})
        curr_systems = state.get("systems", {})
        prev_fleets = self.previous_state.get("fleets", {})
        curr_fleets = state.get("fleets", {})
        
        for sid, system in curr_systems.items():
            if sid in prev_systems:
                prev = prev_systems[sid]
                curr = system
                
                if prev.get("owner") != curr.get("owner"):
                    changes.append({
                        "type": "system",
                        "pos": curr.get("pos", (0, 0)),
                        "old_owner": prev.get("owner"),
                        "new_owner": curr.get("owner"),
                        "sid": sid
                    })
                
                if prev.get("capital") != curr.get("capital"):
                    changes.append({
                        "type": "capital",
                        "pos": curr.get("pos", (0, 0)),
                        "sid": sid
                    })
            else:
                changes.append({
                    "type": "new_system",
                    "pos": system.get("pos", (0, 0)),
                    "sid": sid
                })
        
        old_fleets = set(prev_fleets.keys())
        new_fleets = set(curr_fleets.keys())
        
        for fid in new_fleets - old_fleets:
            fleet = curr_fleets[fid]
            loc = fleet.get("location")
            if loc and loc in curr_systems:
                changes.append({
                    "type": "new_fleet",
                    "pos": curr_systems[loc].get("pos", (0, 0)),
                    "fid": fid
                })
        
        for fid in old_fleets - new_fleets:
            changes.append({
                "type": "destroyed_fleet",
                "fid": fid
            })
        
        return changes
    
    def show_changes(self, changes):
        if not changes:
            return
        
        print()
        print(C.BOLD + C.YELLOW + "⚡ CHANGES THIS TURN:" + C.RESET)
        print(C.BOLD + C.GRAY + "─" * 50 + C.RESET)
        
        factions = self.previous_state.get("factions", {}) if self.previous_state else {}
        
        for change in changes:
            if change["type"] == "system":
                sid = change["sid"]
                old = change.get("old_owner", "None")
                new = change.get("new_owner", "None")
                old_name = factions.get(old, {}).get("name", old) if old else "None"
                new_name = factions.get(new, {}).get("name", new) if new else "None"
                print(f"  {C.RED}◉{C.RESET} {C.BOLD}{sid}{C.RESET}: {C.GRAY}{old_name}{C.RESET} → {C.GREEN}{new_name}{C.RESET}")
            
            elif change["type"] == "capital":
                print(f"  {C.GOLD}★{C.RESET} {C.BOLD}{change['sid']}{C.RESET} became {C.GOLD}CAPITAL{C.RESET}!")
            
            elif change["type"] == "new_system":
                print(f"  {C.GREEN}+{C.RESET} New system {C.BOLD}{change['sid']}{C.RESET} discovered!")
            
            elif change["type"] == "new_fleet":
                print(f"  {C.GREEN}+{C.RESET} New fleet {C.BOLD}{change['fid']}{C.RESET} built!")
            
            elif change["type"] == "destroyed_fleet":
                print(f"  {C.RED}×{C.RESET} Fleet {C.BOLD}{change['fid']}{C.RESET} {C.RED}destroyed!{C.RESET}")
        
        print(C.BOLD + C.GRAY + "─" * 50 + C.RESET)
        print()
    
    def snapshot(self, state):
        systems = {}
        for sid, s in state.get("systems", {}).items():
            systems[sid] = {
                "owner": s.get("owner"),
                "capital": s.get("capital", False),
                "pos": s.get("pos", (0, 0))
            }
        
        fleets = {}
        for fid, f in state.get("fleets", {}).items():
            fleets[fid] = {
                "location": f.get("location")
            }
        
        return {
            "systems": systems,
            "fleets": fleets,
            "factions": state.get("factions", {})
        }
    
    def render_mini(self, state):
        size_x = 20
        size_y = 10
        
        grid = [[" " for _ in range(size_x)] for _ in range(size_y)]
        factions = state.get("factions", {})
        
        for sid, system in state.get("systems", {}).items():
            pos = system.get("pos", (0, 0))
            if len(pos) < 2:
                continue
            x = pos[0] // 2
            y = pos[1] // 2
            
            if 0 <= x < size_x and 0 <= y < size_y:
                owner = system.get("owner")
                if owner and owner in factions:
                    name = factions[owner].get("name", owner)
                    grid[y][x] = get_faction_color(owner) + name[0] + C.RESET
                else:
                    grid[y][x] = C.GRAY + "." + C.RESET
        
        print()
        print(C.BOLD + "MINI-MAP:" + C.RESET)
        for row in grid:
            print("".join(row))
    
    def get_faction_display_name(self, faction_id):
        if faction_id in self.state.get("factions", {}):
            return self.state["factions"][faction_id].get("name", faction_id)
        return faction_id