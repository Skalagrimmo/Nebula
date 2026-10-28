import random
from ui.colors import C

# ============================================================
# БАЗА ДОСЯГНЕНЬ
# ============================================================

ACHIEVEMENTS = {
    "first_blood": {
        "id": "first_blood",
        "name": "⚔️ First Blood",
        "desc": "Win your first battle",
        "icon": "⚔️",
        "unlocked": False
    },
    "empire": {
        "id": "empire",
        "name": "🏛️ Empire Builder",
        "desc": "Control 10 systems",
        "icon": "🏛️",
        "unlocked": False
    },
    "rich": {
        "id": "rich",
        "name": "💰 Tycoon",
        "desc": "Amass 5000 credits",
        "icon": "💰",
        "unlocked": False
    },
    "explorer": {
        "id": "explorer",
        "name": "🧭 Explorer",
        "desc": "Visit 5 different systems",
        "icon": "🧭",
        "unlocked": False
    },
    "technologist": {
        "id": "technologist",
        "name": "🔬 Technologist",
        "desc": "Research 3 technologies",
        "icon": "🔬",
        "unlocked": False
    },
    "conqueror": {
        "id": "conqueror",
        "name": "👑 Conqueror",
        "desc": "Capture 5 systems in one game",
        "icon": "👑",
        "unlocked": False
    },
    "pirate_hunter": {
        "id": "pirate_hunter",
        "name": "🏴‍☠️ Pirate Hunter",
        "desc": "Destroy 10 pirate ships (RPG)",
        "icon": "🏴‍☠️",
        "unlocked": False
    },
    "diplomat": {
        "id": "diplomat",
        "name": "🤝 Diplomat",
        "desc": "Form an alliance with another faction",
        "icon": "🤝",
        "unlocked": False
    },
    "survivor": {
        "id": "survivor",
        "name": "🛡️ Survivor",
        "desc": "Survive 100 turns",
        "icon": "🛡️",
        "unlocked": False
    },
    "victory": {
        "id": "victory",
        "name": "🏆 Victor",
        "desc": "Win the game",
        "icon": "🏆",
        "unlocked": False
    }
}


# ============================================================
# ФУНКЦІЇ ДЛЯ РОБОТИ З ДОСЯГНЕННЯМИ
# ============================================================

def init_achievements(state):
    """Ініціалізує досягнення в стані гри"""
    if "achievements" not in state:
        state["achievements"] = {
            "unlocked": [],
            "total": 0
        }
    
    # Копіюємо базу досягнень у стан
    if "achievement_data" not in state["achievements"]:
        state["achievements"]["achievement_data"] = {}
        for key, data in ACHIEVEMENTS.items():
            state["achievements"]["achievement_data"][key] = {
                "unlocked": False,
                "unlocked_at": None
            }


def unlock_achievement(state, achievement_id):
    """Розблоковує досягнення"""
    if achievement_id not in ACHIEVEMENTS:
        return False
    
    if "achievements" not in state:
        init_achievements(state)
    
    # Перевіряємо, чи вже розблоковано
    if state["achievements"]["achievement_data"][achievement_id]["unlocked"]:
        return False
    
    # Розблоковуємо
    state["achievements"]["achievement_data"][achievement_id]["unlocked"] = True
    state["achievements"]["achievement_data"][achievement_id]["unlocked_at"] = state.get("time", 0)
    state["achievements"]["unlocked"].append(achievement_id)
    state["achievements"]["total"] += 1
    
    # Показуємо повідомлення
    achievement = ACHIEVEMENTS[achievement_id]
    print()
    print(C.BOLD + C.GOLD + "═" * 60 + C.RESET)
    print(C.BOLD + C.GREEN + "🏆 ACHIEVEMENT UNLOCKED! 🏆" + C.RESET)
    print(C.BOLD + C.GOLD + "═" * 60 + C.RESET)
    print(f"  {achievement['icon']} {C.YELLOW}{achievement['name']}{C.RESET}")
    print(f"  {achievement['desc']}")
    print(C.BOLD + C.GOLD + "═" * 60 + C.RESET)
    print()
    
    return True


def check_achievements(state, context):
    """
    Перевіряє досягнення.
    context: dict з даними для перевірки
    """
    if "achievements" not in state:
        init_achievements(state)
    
    unlocked = []
    
    # 1. ⚔️ First Blood
    if context.get("battle_won", False):
        if unlock_achievement(state, "first_blood"):
            unlocked.append("first_blood")
    
    # 2. 🏛️ Empire Builder (10 систем)
    if context.get("systems_controlled", 0) >= 10:
        if unlock_achievement(state, "empire"):
            unlocked.append("empire")
    
    # 3. 💰 Tycoon (5000 кредитів)
    if context.get("credits", 0) >= 5000:
        if unlock_achievement(state, "rich"):
            unlocked.append("rich")
    
    # 4. 🧭 Explorer (5 систем відвідано)
    if context.get("systems_visited", 0) >= 5:
        if unlock_achievement(state, "explorer"):
            unlocked.append("explorer")
    
    # 5. 🔬 Technologist (3 технології)
    if context.get("tech_count", 0) >= 3:
        if unlock_achievement(state, "technologist"):
            unlocked.append("technologist")
    
    # 6. 👑 Conqueror (5 систем захоплено)
    if context.get("systems_captured", 0) >= 5:
        if unlock_achievement(state, "conqueror"):
            unlocked.append("conqueror")
    
    # 7. 🏴‍☠️ Pirate Hunter (10 піратів у RPG)
    if context.get("pirate_kills", 0) >= 10:
        if unlock_achievement(state, "pirate_hunter"):
            unlocked.append("pirate_hunter")
    
    # 8. 🤝 Diplomat (альянс)
    if context.get("alliance_formed", False):
        if unlock_achievement(state, "diplomat"):
            unlocked.append("diplomat")
    
    # 9. 🛡️ Survivor (100 ходів)
    if context.get("turns", 0) >= 100:
        if unlock_achievement(state, "survivor"):
            unlocked.append("survivor")
    
    # 10. 🏆 Victor (перемога)
    if context.get("victory", False):
        if unlock_achievement(state, "victory"):
            unlocked.append("victory")
    
    return unlocked


def get_achievement_progress(state, achievement_id):
    """Повертає прогрес для досягнення"""
    if "achievements" not in state:
        return 0
    
    data = state["achievements"]["achievement_data"].get(achievement_id, {})
    if data.get("unlocked", False):
        return 100
    
    return 0


def print_achievements(state):
    """Показує всі досягнення"""
    if "achievements" not in state:
        init_achievements(state)
    
    print()
    print(C.BOLD + "=" * 60 + C.RESET)
    print(C.CYAN + "🏆 ACHIEVEMENTS" + C.RESET)
    print(C.BOLD + "=" * 60 + C.RESET)
    
    total = state["achievements"]["total"]
    max_total = len(ACHIEVEMENTS)
    print(f"  Unlocked: {C.GREEN}{total}{C.RESET} / {max_total}")
    print()
    
    for key, data in ACHIEVEMENTS.items():
        unlocked = state["achievements"]["achievement_data"][key]["unlocked"]
        
        if unlocked:
            status = C.GREEN + "✅" + C.RESET
            color = C.GREEN
        else:
            status = C.GRAY + "⬜" + C.RESET
            color = C.GRAY
        
        print(f"  {status} {color}{data['icon']} {data['name']}{C.RESET}")
        print(f"      {data['desc']}")
        print()