def record_war(state, attacker_id, defender_id):
    """
    Записує війну між двома фракціями в дипломатію
    """
    diplomacy = state.get("diplomacy", {})
    
    if attacker_id not in diplomacy:
        return
    
    if defender_id not in diplomacy[attacker_id]:
        return
    
    # attacker → defender
    diplomacy[attacker_id][defender_id]["relation"] -= 10
    diplomacy[attacker_id][defender_id]["trust"] -= 5
    diplomacy[attacker_id][defender_id]["wars"] += 1
    diplomacy[attacker_id][defender_id]["status"] = "Hostile"
    
    # defender → attacker
    diplomacy[defender_id][attacker_id]["relation"] -= 15
    diplomacy[defender_id][attacker_id]["trust"] -= 10
    diplomacy[defender_id][attacker_id]["wars"] += 1
    diplomacy[defender_id][attacker_id]["status"] = "Hostile"
    
    # Clamp values (обмежуємо значення)
    for a in (attacker_id, defender_id):
        for b in diplomacy[a]:
            diplomacy[a][b]["relation"] = max(-100, min(100, diplomacy[a][b]["relation"]))
            diplomacy[a][b]["trust"] = max(0, min(100, diplomacy[a][b]["trust"]))


def improve_relations(state, faction_a, faction_b, amount=5):
    """
    Покращує відносини між двома фракціями
    """
    diplomacy = state.get("diplomacy", {})
    
    if faction_a not in diplomacy or faction_b not in diplomacy[faction_a]:
        return
    
    diplomacy[faction_a][faction_b]["relation"] = min(100, diplomacy[faction_a][faction_b]["relation"] + amount)
    diplomacy[faction_b][faction_a]["relation"] = min(100, diplomacy[faction_b][faction_a]["relation"] + amount)
    
    # Оновлюємо статус
    update_relation_status(state, faction_a, faction_b)


def worsen_relations(state, faction_a, faction_b, amount=5):
    """
    Погіршує відносини між двома фракціями
    """
    diplomacy = state.get("diplomacy", {})
    
    if faction_a not in diplomacy or faction_b not in diplomacy[faction_a]:
        return
    
    diplomacy[faction_a][faction_b]["relation"] = max(-100, diplomacy[faction_a][faction_b]["relation"] - amount)
    diplomacy[faction_b][faction_a]["relation"] = max(-100, diplomacy[faction_b][faction_a]["relation"] - amount)
    
    # Оновлюємо статус
    update_relation_status(state, faction_a, faction_b)


def update_relation_status(state, faction_a, faction_b):
    """
    Оновлює статус відносин між двома фракціями на основі relation
    """
    diplomacy = state.get("diplomacy", {})
    
    if faction_a not in diplomacy or faction_b not in diplomacy[faction_a]:
        return
    
    relation = diplomacy[faction_a][faction_b]["relation"]
    
    if relation > 60:
        status = "Ally"
    elif relation > 20:
        status = "Friendly"
    elif relation < -60:
        status = "Hostile"
    elif relation < -20:
        status = "Suspicious"
    else:
        status = "Neutral"
    
    diplomacy[faction_a][faction_b]["status"] = status
    diplomacy[faction_b][faction_a]["status"] = status


def get_relation(state, faction_a, faction_b):
    """
    Повертає поточні відносини між двома фракціями
    """
    diplomacy = state.get("diplomacy", {})
    
    if faction_a not in diplomacy or faction_b not in diplomacy[faction_a]:
        return None
    
    return diplomacy[faction_a][faction_b]


def is_hostile(state, faction_a, faction_b):
    """
    Перевіряє, чи є фракції ворожими
    """
    relation = get_relation(state, faction_a, faction_b)
    if not relation:
        return False
    return relation["status"] == "Hostile" or relation["relation"] < -50


def is_ally(state, faction_a, faction_b):
    """
    Перевіряє, чи є фракції союзниками
    """
    relation = get_relation(state, faction_a, faction_b)
    if not relation:
        return False
    return relation["status"] == "Ally" or relation["relation"] > 70


def get_all_enemies(state, faction_id):
    """
    Повертає список всіх ворогів фракції
    """
    diplomacy = state.get("diplomacy", {})
    enemies = []
    
    if faction_id not in diplomacy:
        return enemies
    
    for other_id, data in diplomacy[faction_id].items():
        if data["status"] == "Hostile" or data["relation"] < -50:
            enemies.append(other_id)
    
    return enemies


def get_all_allies(state, faction_id):
    """
    Повертає список всіх союзників фракції
    """
    diplomacy = state.get("diplomacy", {})
    allies = []
    
    if faction_id not in diplomacy:
        return allies
    
    for other_id, data in diplomacy[faction_id].items():
        if data["status"] == "Ally" or data["relation"] > 70:
            allies.append(other_id)
    
    return allies


def get_diplomacy_summary(state):
    """
    Повертає короткий звіт про всі дипломатичні відносини
    """
    diplomacy = state.get("diplomacy", {})
    summary = {}
    
    for faction_a in diplomacy:
        summary[faction_a] = {}
        for faction_b, data in diplomacy[faction_a].items():
            summary[faction_a][faction_b] = {
                "status": data["status"],
                "relation": data["relation"],
                "trust": data["trust"]
            }
    
    return summary