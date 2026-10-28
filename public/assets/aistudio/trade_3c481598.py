from core.utils import input_int
from ui.colors import C
from world.galaxy import SYSTEMS


def trade(ship):
    """Торгівля в поточній системі"""
    sys = SYSTEMS[ship["location"]]
    
    print(C.BOLD + "\n" + "=" * 50 + C.RESET)
    print(C.CYAN + "💰 MARKET" + C.RESET)
    print(C.BOLD + "=" * 50 + C.RESET)
    
    print(f"\n  {C.YELLOW}Items for sale:{C.RESET}")
    for k, v in sys["prices"].items():
        print(f"    {k}: {v} credits")
    
    print(f"\n  Your cargo: {ship['cargo']}")
    print(f"  Cargo space: {sum(ship['cargo'].values())}/{ship['cargo_limit']}")
    print()
    
    action = input("  [buy/sell/back] > ").strip().lower()
    
    if action == "buy":
        item = input("  Item: ").strip().lower()
        if item not in sys["prices"]:
            print(C.RED + "  Invalid item!" + C.RESET)
            return
        
        qty = input_int("  Quantity: ", 1)
        if qty < 1:
            return
        
        cost = sys["prices"][item] * qty
        if sum(ship["cargo"].values()) + qty > ship["cargo_limit"]:
            print(C.RED + "  No cargo space!" + C.RESET)
            return
        
        if ship["credits"] >= cost:
            ship["credits"] -= cost
            ship["cargo"][item] += qty
            print(C.GREEN + f"  Bought {qty} {item} for {cost} credits" + C.RESET)
        else:
            print(C.RED + "  Not enough credits!" + C.RESET)
    
    elif action == "sell":
        item = input("  Item: ").strip().lower()
        if item not in sys["prices"]:
            print(C.RED + "  Invalid item!" + C.RESET)
            return
        
        qty = input_int("  Quantity: ", 1)
        if qty < 1:
            return
        
        if ship["cargo"][item] < qty:
            print(C.RED + "  Not enough cargo!" + C.RESET)
            return
        
        gain = sys["prices"][item] * qty
        ship["credits"] += gain
        ship["cargo"][item] -= qty
        print(C.GREEN + f"  Sold {qty} {item} for {gain} credits" + C.RESET)