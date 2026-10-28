from world.territory import compute_control_zones
from ui.colors import C

def render_map(state):

    size_x = 45
    size_y = 25

    grid = [[" " for _ in range(size_x)] for _ in range(size_y)]

    zones = compute_control_zones(state)

    for sid, system in state["systems"].items():

        x, y = system["pos"]

        char = "o"

        if sid in zones["FACTION_A"]:
            char = C.BLUE + "A" + C.RESET

        elif sid in zones["FACTION_B"]:
            char = C.RED + "B" + C.RESET

        if sid in zones["CONTESTED"]:
            char = C.YELLOW + "X" + C.RESET

        grid[y][x] = char

    for fid, fleet in state["fleets"].items():

        loc = fleet["location"]
        system = state["systems"][loc]

        x, y = system["pos"]

        grid[y][x] = C.MAGENTA + "F" + C.RESET

    print()
    print(C.BOLD + "=" * 60 + C.RESET)
    print(C.CYAN + "GALAXY MAP (CONTROL MODE)" + C.RESET)
    print(C.BOLD + "=" * 60 + C.RESET)

    for row in grid:
        print("".join(row))