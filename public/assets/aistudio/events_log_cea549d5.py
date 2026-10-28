from ui.colors import C


def print_recent_events(state, limit=15):
    """Показує останні події у грі (тільки цікаві)"""
    events = state.get("events", [])
    if not events:
        print(C.GRAY + "  No events yet." + C.RESET)
        return

    print()
    print(C.BOLD + "=" * 60 + C.RESET)
    print(C.CYAN + "📜 RECENT EVENTS" + C.RESET)
    print(C.BOLD + "=" * 60 + C.RESET)

    # Пропускаємо технічні події
    skip_types = ["TICK_START", "TICK_END", "TICK", "tick"]
    
    filtered = [
        e for e in events[-limit*2:] 
        if e.get("type") not in skip_types
    ]
    
    if not filtered:
        print(C.GRAY + "  No interesting events yet." + C.RESET)
        return
    
    for e in filtered[-limit:]:
        t = e.get("time", 0)
        et = e.get("type", "UNKNOWN")
        data = e.get("data", {})
        
        if et == "CONQUEST":
            msg = data if isinstance(data, str) else data.get("data", str(data))
            print(f"  {C.GREEN}🏴‍☠️ {msg}{C.RESET}")
        elif et == "BATTLE":
            msg = data if isinstance(data, str) else data.get("data", str(data))
            print(f"  {C.RED}⚔️ {msg}{C.RESET}")
        elif et == "fleet_built":
            faction = data.get("faction", "Unknown")
            fleet_type = data.get("fleet_type", "")
            if fleet_type:
                print(f"  {C.BLUE}🔧 {faction} built a {fleet_type}{C.RESET}")
            else:
                print(f"  {C.BLUE}🔧 {faction} built a fleet{C.RESET}")
        elif et == "RANDOM_EVENT":
            msg = data.get("message", "")
            name = data.get("name", "")
            if msg:
                print(f"  {C.YELLOW}🌀 {msg}{C.RESET}")
            elif name:
                print(f"  {C.YELLOW}🌀 {name}{C.RESET}")
        elif et == "alliance_formed":
            print(f"  {C.MAGENTA}🤝 {data.get('a', '?')} allied with {data.get('b', '?')}{C.RESET}")
        elif et == "war_declared":
            print(f"  {C.RED}💢 {data.get('attacker', '?')} declared war on {data.get('defender', '?')}{C.RESET}")
        elif et == "AI_DECISION":
            faction = data.get('faction', '?')
            action = data.get('action', {}).get('type', 'unknown')
            print(f"  {C.CYAN}🧠 {faction} decided: {action}{C.RESET}")
        elif et == "expansion":
            print(f"  {C.YELLOW}📈 {data.get('faction', '?')} expanded to {data.get('system', '?')}{C.RESET}")
        elif et == "battle_result":
            print(f"  {C.RED}⚔️ {data.get('attacker', '?')} defeated {data.get('defender', '?')} at {data.get('system', '?')}{C.RESET}")
        elif et == "tech_researched":
            print(f"  {C.GREEN}🔬 {data.get('faction', '?')} researched {data.get('tech', '?')}{C.RESET}")
        else:
            if isinstance(data, dict) and data:
                print(f"  {C.GRAY}• {et}: {data}{C.RESET}")
            else:
                print(f"  {C.GRAY}• {et}{C.RESET}")