import random
from ui.colors import C
from world.galaxy import SYSTEMS

QUEST_TEMPLATES = {
    "delivery": {
        "name": "📦 Delivery",
        "desc": "Deliver {amount} {item} to {destination}",
        "reward": 50,
        "bonus": 20
    },
    "hunt": {
        "name": "🎯 Bounty Hunt",
        "desc": "Destroy {amount} pirates in {destination}",
        "reward": 80,
        "bonus": 30
    },
    "explore": {
        "name": "🧭 Exploration",
        "desc": "Visit {destination} and return with data",
        "reward": 60,
        "bonus": 25
    },
    "escort": {
        "name": "🛡️ Escort",
        "desc": "Protect a convoy to {destination}",
        "reward": 70,
        "bonus": 20
    },
    "rescue": {
        "name": "🆘 Rescue",
        "desc": "Rescue a stranded ship near {destination}",
        "reward": 90,
        "bonus": 35
    }
}


def generate_quest(state, location):
    """Генерує випадковий квест"""
    quest_type = random.choice(list(QUEST_TEMPLATES.keys()))
    template = QUEST_TEMPLATES[quest_type]
    
    systems = list(SYSTEMS.keys())
    available = [s for s in systems if s != location]
    
    if not available:
        return None
    
    destination = random.choice(available)
    items = ["ore", "food", "tech"]
    item = random.choice(items)
    amount = random.randint(5, 15)
    
    return {
        "id": f"q_{random.randint(1000, 9999)}",
        "type": quest_type,
        "name": template["name"],
        "desc": template["desc"].format(amount=amount, item=item, destination=destination),
        "destination": destination,
        "item": item,
        "amount": amount,
        "reward": template["reward"] + random.randint(-10, 20),
        "bonus": template["bonus"],
        "status": "active",
        "source": location
    }


def accept_quest(state, quest):
    if quest is None:
        return False, "No quest available"
    state["quests"].append(quest)
    return True, f"📜 Accepted: {quest['name']}"


def complete_quest(state, quest):
    if quest["status"] != "active":
        return
    
    quest["status"] = "completed"
    reward = quest["reward"]
    state["ship"]["credits"] += reward
    state["ship"]["reputation"] = min(100, state["ship"]["reputation"] + 5)
    
    if random.random() < 0.3:
        bonus = quest["bonus"]
        state["ship"]["credits"] += bonus
        print(C.GOLD + f"  ⭐ Bonus! +{bonus} credits!" + C.RESET)
    
    print(C.GREEN + f"  ✅ Quest completed! +{reward} credits" + C.RESET)


def check_quests(state):
    """Перевіряє квести при зміні локації"""
    completed = []
    
    for quest in state.get("quests", []):
        if quest["status"] != "active":
            continue
        
        current = state["ship"]["location"]
        dest = quest["destination"]
        
        if quest["type"] in ["delivery", "explore", "escort"]:
            if current == dest:
                complete_quest(state, quest)
                completed.append(quest)
        
        elif quest["type"] == "hunt":
            if state["ship"]["kills"] >= quest.get("kills_needed", 3):
                complete_quest(state, quest)
                completed.append(quest)
    
    return completed


def print_quests(state):
    """Показує всі квести"""
    quests = state.get("quests", [])
    if not quests:
        print(C.GRAY + "  No quests" + C.RESET)
        return
    
    print()
    print(C.BOLD + "=" * 50 + C.RESET)
    print(C.CYAN + "📜 QUESTS" + C.RESET)
    print(C.BOLD + "=" * 50 + C.RESET)
    
    active = [q for q in quests if q["status"] == "active"]
    completed = [q for q in quests if q["status"] == "completed"]
    failed = [q for q in quests if q["status"] == "failed"]
    
    if active:
        print(C.YELLOW + "  🔥 Active:" + C.RESET)
        for q in active:
            print(f"    {q['name']}")
            print(f"      {q['desc']}")
            print(f"      Reward: {q['reward']} credits")
            print()
    
    if completed:
        print(C.GREEN + "  ✅ Completed:" + C.RESET)
        for q in completed:
            print(f"    {q['name']} (reward: {q['reward']})")
    
    if failed:
        print(C.RED + "  ❌ Failed:" + C.RESET)
        for q in failed:
            print(f"    {q['name']}")


def handle_quest_command(state):
    """Обробка команди quests"""
    print(C.BOLD + "\n" + "=" * 50 + C.RESET)
    print(C.CYAN + "📜 QUESTS" + C.RESET)
    print(C.BOLD + "=" * 50 + C.RESET)
    
    location = state["ship"]["location"]
    
    active_quests = [q for q in state.get("quests", []) if q["status"] == "active"]
    if not active_quests and random.random() < 0.5:
        quest = generate_quest(state, location)
        if quest:
            print(C.YELLOW + f"\n  📜 New quest available at {location}:" + C.RESET)
            print(f"    {quest['name']}")
            print(f"    {quest['desc']}")
            print(f"    Reward: {quest['reward']} credits")
            print()
            
            choice = input("  Accept? (y/n): ").strip().lower()
            if choice == "y":
                ok, msg = accept_quest(state, quest)
                print(C.GREEN + f"  {msg}" + C.RESET)
                return
    
    print_quests(state)