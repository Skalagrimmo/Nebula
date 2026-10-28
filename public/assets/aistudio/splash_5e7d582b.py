from ui.colors import C
import time
import random

def print_rogue_splash():
    """Друкує заставку в стилі старих рогаликів"""
    
    clear = "\033[2J\033[H"
    print(clear)
    
    # Випадковий ASCII-арт
    arts = [
        """
    ██████╗  ██████╗  ██████╗ ███████╗
    ██╔══██╗██╔═══██╗██╔═══██╗██╔════╝
    ██████╔╝██║   ██║██║   ██║█████╗  
    ██╔══██╗██║   ██║██║   ██║██╔══╝  
    ██║  ██║╚██████╔╝╚██████╔╝███████╗
    ╚═╝  ╚═╝ ╚═════╝  ╚═════╝ ╚══════╝
        """,
        """
    ███╗   ██╗███████╗██████╗ ██╗   ██╗██╗      █████╗ 
    ████╗  ██║██╔════╝██╔══██╗██║   ██║██║     ██╔══██╗
    ██╔██╗ ██║█████╗  ██████╔╝██║   ██║██║     ███████║
    ██║╚██╗██║██╔══╝  ██╔══██╗██║   ██║██║     ██╔══██║
    ██║ ╚████║███████╗██████╔╝╚██████╔╝███████╗██║  ██║
    ╚═╝  ╚═══╝╚══════╝╚═════╝  ╚═════╝ ╚══════╝╚═╝  ╚═╝
        """
    ]
    
    art = random.choice(arts)
    
    print(C.RED + C.BOLD + art + C.RESET)
    print()
    print(C.GRAY + "  A 4X STRATEGY IN THE STYLE OF OLD ROGUELIKES" + C.RESET)
    print(C.DIM + "  ════════════════════════════════════════════════" + C.RESET)
    print()
    print(C.GREEN + "  Loading galaxy..." + C.RESET)
    
    # Анімація завантаження
    for i in range(10):
        print(C.YELLOW + "  ■" * i + C.RESET)
        time.sleep(0.05)
        clear = "\033[1A\033[K"
        print(clear, end="")
    
    print(C.GREEN + "  ■" * 10 + " READY!" + C.RESET)
    time.sleep(0.5)