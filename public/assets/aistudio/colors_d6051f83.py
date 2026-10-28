class C:
    RESET = "\033[0m"
    BOLD = "\033[1m"
    DIM = "\033[2m"
    
    RED = "\033[91m"
    GREEN = "\033[92m"
    YELLOW = "\033[93m"
    BLUE = "\033[94m"
    MAGENTA = "\033[95m"
    CYAN = "\033[96m"
    WHITE = "\033[97m"
    GRAY = "\033[90m"
    GOLD = "\033[93m"
    ORANGE = "\033[91m"

def get_faction_color(faction_id):
    colors = {
        "FACTION_A": C.BLUE,
        "FACTION_B": C.RED,
        "FACTION_C": C.GREEN
    }
    return colors.get(faction_id, C.WHITE)