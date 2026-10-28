from ui.colors import C, get_faction_color


def print_header(title):
    """Друкує заголовок"""
    print()
    print(C.BOLD + "=" * 50 + C.RESET)
    print(C.CYAN + title + C.RESET)
    print(C.BOLD + "=" * 50 + C.RESET)


def print_factions(state):
    """Показує фракції — кожна на новому рядку, компактно"""
    print()
    print(C.BOLD + "=" * 50 + C.RESET)
    print(C.CYAN + "🏛️  ФРАКЦІЇ" + C.RESET)
    print(C.BOLD + "=" * 50 + C.RESET)

    for fid, f in state["factions"].items():
        name = f.get("name", fid)
        display = f.get("display_name", name)
        color = get_faction_color(fid)
        
        # Скорочуємо назву до 12 символів, щоб не розтягувати
        short_name = name[:12]
        
        print()
        print(f"  {color}{short_name}{C.RESET}")
        print(f"    💰 Кредити: {f['credits']}")
        print(f"    🪐  Системи: {len(f['systems'])}")
        print(f"    ⚔️  Військо: {round(f['military'], 2)}")
        print(f"    📊 Стратегія: {f.get('strategy', 'збалансована')[:12]}")
        
        tech = f.get("tech", [])
        if tech:
            print(f"    🔬 Технології: {', '.join(tech)}")


def print_fleets(state):
    """Показує флоти — кожен на новому рядку, компактно"""
    print()
    print(C.BOLD + "=" * 50 + C.RESET)
    print(C.CYAN + "🚀  ФЛОТИ" + C.RESET)
    print(C.BOLD + "=" * 50 + C.RESET)

    fleets = state.get("fleets", {})
    if not fleets:
        print("  Немає флотів.")
        return

    for fid, f in fleets.items():
        owner = f.get("owner")
        owner_name = state["factions"].get(owner, {}).get("name", owner) if owner else "—"
        
        # Скорочуємо до 10 символів
        short_owner = owner_name[:10]
        
        fleet_type = f.get("type_name", "Невідомо")[:10]
        strength = f.get("strength", 0)
        loc = f.get("location", "—")[:10]
        
        print(f"  {fid[:8]:<8} [{fleet_type}]")
        print(f"    Власник: {short_owner:<10}  Сила: {strength:<4}  Локація: {loc}")


def print_system_info(state, system_id):
    """Показує інформацію про систему — компактно"""
    if system_id not in state["systems"]:
        print(C.RED + "  Систему не знайдено!" + C.RESET)
        return
    
    s = state["systems"][system_id]
    
    print()
    print(C.BOLD + "=" * 50 + C.RESET)
    print(C.CYAN + f"🌍  СИСТЕМА: {system_id}" + C.RESET)
    print(C.BOLD + "=" * 50 + C.RESET)
    
    sys_type = s.get("type_name", "Невідомо")
    print(f"  Тип: {sys_type[:15]}")
    print(f"  Власник: {s.get('owner', '—')}")
    print(f"  Населення: {s.get('population', 0)}")
    print(f"  Економіка: {s.get('economy', 0)}")
    print(f"  Ресурси: {s.get('resources', 0)}")
    print(f"  Склад: {s.get('stock', 0)}")
    print(f"  Попит: {s.get('demand', 0)}")
    print(f"  Столиця: {'Так' if s.get('capital') else 'Ні'}")
    
    neighbors = s.get("neighbors", [])
    if neighbors:
        print(f"  Сусіди: {', '.join(neighbors[:5])}{'...' if len(neighbors) > 5 else ''}")


def print_factions_short(state):
    """Короткий вивід фракцій для статус-бару"""
    factions = state.get("factions", {})
    result = []
    for fid, f in factions.items():
        name = f.get("name", fid)[:8]
        color = get_faction_color(fid)
        result.append(f"{color}{name}{C.RESET}")
    return " | ".join(result)