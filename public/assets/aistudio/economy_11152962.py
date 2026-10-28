def process_economy(state):
    """Обробка економіки (збалансована версія)"""
    
    # 1️⃣ ВИРОБНИЦТВО РЕСУРСІВ (ЗМЕНШЕНО)
    for sid, system in state["systems"].items():
        production = system.get("economy", 50) * 0.12  # Було 0.3
        production += system.get("population", 100) * 0.02  # Було 0.05
        
        if system.get("capital", False):
            production *= 1.3  # Було 1.5
        
        system["stock"] += int(production)
        system["stock"] = min(1000, system["stock"])
    
    # 2️⃣ СПОЖИВАННЯ (ЗБІЛЬШЕНО)
    for sid, system in state["systems"].items():
        consumption = system.get("population", 100) * 0.15  # Було 0.1
        consumption += system.get("demand", 50) * 0.25  # Було 0.2
        
        system["stock"] -= int(consumption)
        system["stock"] = max(0, system["stock"])
        
        # Голод
        if system["stock"] < 15:  # Було 10
            system["population"] = max(40, system["population"] - 2)  # Було -1
        elif system["stock"] > 200:
            system["population"] += 1
    
    # 3️⃣ ДОХІД ФРАКЦІЙ (ЗМЕНШЕНО)
    for fid, faction in state["factions"].items():
        income = 0
        
        for sid in faction.get("systems", []):
            if sid in state["systems"]:
                system = state["systems"][sid]
                income += system.get("economy", 50) * 0.05  # Було 0.1
                income += system.get("population", 100) * 0.015  # Було 0.05
                income += system.get("stock", 0) * 0.003  # Було 0.01
        
        income += 2  # Було 5
        faction["credits"] += int(income)
        faction["credits"] = min(10000, faction["credits"])
    
    # 4️⃣ ВИТРАТИ НА ФЛОТИ (ЗБІЛЬШЕНО)
    for fid, faction in state["factions"].items():
        fleet_count = sum(1 for f in state["fleets"].values() if f["owner"] == fid)
        
        # Базова вартість утримання
        maintenance = fleet_count * 4  # Було 2
        
        # Додаткова вартість за силу
        total_strength = sum(f.get("strength", 10) for f in state["fleets"].values() if f["owner"] == fid)
        maintenance += int(total_strength * 0.08)  # Було 0.05
        
        faction["credits"] -= maintenance
        faction["credits"] = max(0, faction["credits"])
        
        # Якщо грошей немає — флоти слабшають
        if faction["credits"] < 15 and fleet_count > 0:  # Було 10
            for f in state["fleets"].values():
                if f["owner"] == fid:
                    f["strength"] *= 0.92  # Було 0.95
                    if f["strength"] < 3:
                        # Флот розформовується
                        del state["fleets"][f["id"]]