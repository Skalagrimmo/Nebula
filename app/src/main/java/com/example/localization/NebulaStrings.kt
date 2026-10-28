package com.example.localization

import java.util.Locale

enum class GameLanguage(val code: String, val displayName: String, val flag: String) {
    UK("uk", "Українська", "🇺🇦"),
    EN("en", "English", "🇬🇧");

    companion object {
        fun detectSystem(): GameLanguage {
            return if (Locale.getDefault().language.equals("uk", ignoreCase = true)) UK else EN
        }
    }
}

object NebulaStrings {
    private val ukrainian: Map<String, String> = mapOf(
        // App & Menu
        "app_name" to "Небула 4X",
        "app_subtitle" to "Галактична 4X Стратегія та RPG",
        "menu_new_4x" to "Нова гра: 4X Стратегія",
        "menu_new_rpg" to "Нова гра: RPG Корабель",
        "menu_continue" to "Продовжити гру",
        "menu_save" to "Зберегти гру",
        "menu_load" to "Завантажити",
        "menu_how_to_play" to "Як грати",
        "menu_language" to "Мова",
        "menu_quit" to "Вихід",
        "menu_back" to "Назад",

        // Factions
        "faction_solarian" to "Соляріанська Імперія",
        "faction_zergon" to "Вулик Зергон",
        "faction_federation" to "Небесна Федерація",
        "faction_pirates" to "Піратські Клани",
        "select_faction" to "Оберіть фракцію",
        "player" to "Гравець",
        "credits" to "Кредити",
        "military" to "Військо",
        "systems" to "Системи",
        "capital" to "Столиця",

        // 4X Controls
        "mode_4x" to "4X Стратегія",
        "mode_rpg" to "RPG Корабель",
        "cmd_build_fleet" to "Збудувати флот",
        "cmd_move_fleet" to "Рух флоту",
        "cmd_attack" to "Атакувати",
        "cmd_inspect" to "Інспектор",
        "cmd_tech" to "Технології",
        "cmd_diplomacy" to "Дипломатія",
        "cmd_wars" to "Війни",
        "cmd_events" to "Події",
        "cmd_achievements" to "Досягнення",
        "cmd_step" to "Крок",
        "cmd_pause" to "Пауза",
        "cmd_play" to "Старт",
        "stardate" to "Дата",
        "ticks" to "Ходи",
        "speed" to "Швидкість",

        // System Inspector
        "system_details" to "Дані системи",
        "type" to "Тип",
        "owner" to "Власник",
        "neutral" to "Нейтральна",
        "population" to "Населення",
        "economy" to "Економіка",
        "resources" to "Ресурси",
        "stock" to "Склад",
        "demand" to "Попит",
        "neighbors" to "Гіперлінії",
        "no_fleets" to "Немає флотів",
        "strength" to "Сила",

        // Fleet Types
        "fleet_scout" to "Розвідник",
        "fleet_fighter" to "Винищувач",
        "fleet_battleship" to "Лінкор",
        "fleet_assault" to "Штурмовик",
        "fleet_pirate" to "Корсар",
        "fleet_flagship" to "Флагман",

        // System Types
        "systype_capital" to "Столичний світ",
        "systype_rich" to "Багата рудами планета",
        "systype_industrial" to "Промисловий центр",
        "systype_agricultural" to "Аграрна житниця",
        "systype_desert" to "Мінеральна пустеля",
        "systype_ocean" to "Океанічний світ",
        "systype_forest" to "Лісова біосфера",
        "systype_pirate" to "Піратська база",
        "systype_volcanic" to "Вулканічна скарбниця",
        "systype_ice" to "Крижана пустка",

        // Technology
        "tech_title" to "Дерево Технологій",
        "tech_laser" to "Лазерна зброя (+20% атаки)",
        "tech_shield" to "Енергощити (+30% захисту)",
        "tech_warp" to "Варп-двигуни (+50% руху)",
        "tech_economy" to "Квантова торгівля (+25% доходу)",
        "tech_colony" to "Колонізація (+40% населення)",
        "tech_researched" to "Досліджено",
        "tech_research" to "Дослідити",
        "cost" to "Вартість",

        // Diplomacy
        "diplomacy_title" to "Галактична Дипломатія",
        "status" to "Статус",
        "trust" to "Довіра",
        "relation" to "Відносини",
        "propose_peace" to "Запропонувати мир",
        "spy_action" to "Шпигунство (10 кр.)",
        "active_wars" to "Активні війни",
        "no_wars" to "Галактичний мир",

        // RPG Mode
        "ship_hud" to "Місток Зорельота",
        "hull" to "Корпус",
        "shield" to "Щит",
        "weapon" to "Зброя",
        "cargo" to "Вантаж",
        "kills" to "Збито",
        "reputation" to "Репутація",
        "market" to "Ринок товарів",
        "travel" to "Подорож",
        "wormhole" to "Червоточина",
        "upgrade" to "Модернізація",
        "quests" to "Квести",
        "buy" to "Купити",
        "sell" to "Продати",
        "item_ore" to "Руда",
        "item_food" to "Провізія",
        "item_tech" to "Електроніка",
        "jump_success" to "Стрибок здійснено",
        "wormhole_jump" to "Стрибок крізь червоточину",
        "cargo_full" to "Вантажний відсік переповнено!",
        "not_enough_credits" to "Недостатньо кредитів!",

        // Upgrades
        "up_cargo" to "Вантаж (+5) — 50 кр.",
        "up_hull" to "Корпус (+20) — 70 кр.",
        "up_weapon" to "Зброя (+1) — 100 кр.",
        "up_shield" to "Щит (+0.05) — 120 кр.",

        // Combat
        "combat_title" to "Тактичний Бій!",
        "combat_attack" to "Вогонь!",
        "combat_flee" to "Втеча",
        "enemy_destroyed" to "Ворога знищено!",
        "escaped" to "Вдала втеча!",
        "battle_defeat" to "Поразка!",

        // Achievements
        "ach_title" to "Галактичні Досягнення",
        "ach_first_blood" to "Перша кров",
        "ach_first_blood_desc" to "Перемогти у першій космічній битві",
        "ach_empire_builder" to "Будівник Імперії",
        "ach_empire_builder_desc" to "Підкорити 10 зоряних систем",
        "ach_tycoon" to "Магнат",
        "ach_tycoon_desc" to "Накопичити 5,000 кредитів",
        "ach_explorer" to "Першовідкривач",
        "ach_explorer_desc" to "Відвідати 5 систем зоряного сектора",
        "ach_technologist" to "Технократ",
        "ach_technologist_desc" to "Дослідити 3 наукові відкриття",
        "ach_conqueror" to "Завойовник",
        "ach_conqueror_desc" to "Захопити 5 ворожих систем",
        "ach_pirate_hunter" to "Мисливець на піратів",
        "ach_pirate_hunter_desc" to "Знищити 5 піратських рейдерів",
        "ach_diplomat" to "Дипломат",
        "ach_diplomat_desc" to "Укласти військовий альянс",
        "ach_survivor" to "Ветеран",
        "ach_survivor_desc" to "Протриматися 100 галактичних ходів",
        "ach_victor" to "Тріумфатор",
        "ach_victor_desc" to "Здобути остаточну перемогу",

        // Victory
        "victory_title" to "ГАЛАКТИЧНА ПЕРЕМОГА!",
        "victory_domination" to "Перемога пануванням (75% систем під контролем)",
        "victory_economic" to "Економічна перемога (20,000 кредитів)",
        "victory_military" to "Військовий тріумф (усі ворожі флоти знищено)",
        "btn_continue_sandbox" to "Продовжити гру (Пісочниця)",
        "btn_to_menu" to "Головне меню",

        // Guide
        "guide_title" to "Керівництво командувача",
        "guide_text" to "Nebula 4X поєднує космічну 4X стратегію та симулятор зорельота.\n\n" +
                "• 4X Режим: Керуйте імперією. Будуйте флоти на столичних планетах, захоплюйте нейтральні системи, досліджуйте технології, укладайте мирні договори та шпигуйте за суперниками.\n" +
                "• RPG Режим: Натисніть кнопку зміни режиму вгорі, щоб сісти за штурвал особистого зорельота. Торгуйте ресурсами за динамічними цінами, здійснюйте гіперстрибки, остерігайтесь піратів та покращуйте щити і лазери.\n" +
                "• Оптимізація: Векторний тактичний радар спеціально спроектований для максимальної швидкодії навіть на найслабших пристроях."
    )

    private val english: Map<String, String> = mapOf(
        // App & Menu
        "app_name" to "Nebula 4X",
        "app_subtitle" to "Galactic 4X Strategy & RPG",
        "menu_new_4x" to "New Game: 4X Strategy",
        "menu_new_rpg" to "New Game: RPG Starship",
        "menu_continue" to "Continue Game",
        "menu_save" to "Save Game",
        "menu_load" to "Load Game",
        "menu_how_to_play" to "How to Play",
        "menu_language" to "Language",
        "menu_quit" to "Quit",
        "menu_back" to "Back",

        // Factions
        "faction_solarian" to "Solarian Empire",
        "faction_zergon" to "Zergon Hive",
        "faction_federation" to "Celestial Federation",
        "faction_pirates" to "Pirate Clans",
        "select_faction" to "Select Faction",
        "player" to "Player",
        "credits" to "Credits",
        "military" to "Military",
        "systems" to "Systems",
        "capital" to "Capital",

        // 4X Controls
        "mode_4x" to "4X Strategy",
        "mode_rpg" to "RPG Starship",
        "cmd_build_fleet" to "Build Fleet",
        "cmd_move_fleet" to "Move Fleet",
        "cmd_attack" to "Attack",
        "cmd_inspect" to "Inspector",
        "cmd_tech" to "Technology",
        "cmd_diplomacy" to "Diplomacy",
        "cmd_wars" to "Wars",
        "cmd_events" to "Events",
        "cmd_achievements" to "Achievements",
        "cmd_step" to "Step",
        "cmd_pause" to "Pause",
        "cmd_play" to "Play",
        "stardate" to "Stardate",
        "ticks" to "Ticks",
        "speed" to "Speed",

        // System Inspector
        "system_details" to "System Telemetry",
        "type" to "Type",
        "owner" to "Owner",
        "neutral" to "Neutral",
        "population" to "Population",
        "economy" to "Economy",
        "resources" to "Resources",
        "stock" to "Stock",
        "demand" to "Demand",
        "neighbors" to "Hyperlanes",
        "no_fleets" to "No fleets stationed",
        "strength" to "Strength",

        // Fleet Types
        "fleet_scout" to "Scout",
        "fleet_fighter" to "Fighter",
        "fleet_battleship" to "Battleship",
        "fleet_assault" to "Assault",
        "fleet_pirate" to "Corsair",
        "fleet_flagship" to "Flagship",

        // System Types
        "systype_capital" to "Capital Core World",
        "systype_rich" to "Mineral Rich World",
        "systype_industrial" to "Industrial Center",
        "systype_agricultural" to "Agricultural World",
        "systype_desert" to "Mineral Desert",
        "systype_ocean" to "Oceanic Biosphere",
        "systype_forest" to "Forest World",
        "systype_pirate" to "Pirate Stronghold",
        "systype_volcanic" to "Volcanic Crucible",
        "systype_ice" to "Frozen Outpost",

        // Technology
        "tech_title" to "Technology Tree",
        "tech_laser" to "Laser Weapons (+20% Attack)",
        "tech_shield" to "Energy Shields (+30% Defense)",
        "tech_warp" to "Warp Drives (+50% Speed)",
        "tech_economy" to "Quantum Commerce (+25% Income)",
        "tech_colony" to "Deep Colonization (+40% Pop)",
        "tech_researched" to "Researched",
        "tech_research" to "Research",
        "cost" to "Cost",

        // Diplomacy
        "diplomacy_title" to "Galactic Diplomacy",
        "status" to "Status",
        "trust" to "Trust",
        "relation" to "Relation",
        "propose_peace" to "Offer Peace",
        "spy_action" to "Espionage (10 cr.)",
        "active_wars" to "Active Wars",
        "no_wars" to "Galactic Peace",

        // RPG Mode
        "ship_hud" to "Starship Bridge",
        "hull" to "Hull",
        "shield" to "Shield",
        "weapon" to "Weapon",
        "cargo" to "Cargo",
        "kills" to "Kills",
        "reputation" to "Reputation",
        "market" to "Commodities Market",
        "travel" to "Hyperjump",
        "wormhole" to "Wormhole",
        "upgrade" to "Shipyard",
        "quests" to "Contracts",
        "buy" to "Buy",
        "sell" to "Sell",
        "item_ore" to "Raw Ore",
        "item_food" to "Provisions",
        "item_tech" to "Microchips",
        "jump_success" to "Hyperspace jump successful",
        "wormhole_jump" to "Wormhole transit complete",
        "cargo_full" to "Cargo hold is full!",
        "not_enough_credits" to "Not enough credits!",

        // Upgrades
        "up_cargo" to "Cargo (+5) — 50 cr.",
        "up_hull" to "Hull (+20) — 70 cr.",
        "up_weapon" to "Laser (+1) — 100 cr.",
        "up_shield" to "Shield (+0.05) — 120 cr.",

        // Combat
        "combat_title" to "Tactical Engagement!",
        "combat_attack" to "Open Fire!",
        "combat_flee" to "Emergency Warp",
        "enemy_destroyed" to "Target Destroyed!",
        "escaped" to "Escape Successful!",
        "battle_defeat" to "Ship Destroyed!",

        // Achievements
        "ach_title" to "Galactic Achievements",
        "ach_first_blood" to "First Blood",
        "ach_first_blood_desc" to "Win your first naval battle",
        "ach_empire_builder" to "Empire Builder",
        "ach_empire_builder_desc" to "Control 10 star systems",
        "ach_tycoon" to "Galactic Tycoon",
        "ach_tycoon_desc" to "Amass 5,000 credits",
        "ach_explorer" to "Explorer",
        "ach_explorer_desc" to "Visit 5 sector star systems",
        "ach_technologist" to "Technocrat",
        "ach_technologist_desc" to "Research 3 scientific technologies",
        "ach_conqueror" to "Conqueror",
        "ach_conqueror_desc" to "Capture 5 enemy star systems",
        "ach_pirate_hunter" to "Pirate Hunter",
        "ach_pirate_hunter_desc" to "Defeat 5 space corsairs",
        "ach_diplomat" to "Diplomat",
        "ach_diplomat_desc" to "Form an interplanetary alliance",
        "ach_survivor" to "Survivor",
        "ach_survivor_desc" to "Endure 100 galactic turns",
        "ach_victor" to "Victor",
        "ach_victor_desc" to "Attain ultimate galactic triumph",

        // Victory
        "victory_title" to "GALACTIC VICTORY!",
        "victory_domination" to "Domination Victory (75% galaxy under control)",
        "victory_economic" to "Economic Victory (20,000 credits amassed)",
        "victory_military" to "Military Victory (all rival armadas annihilated)",
        "btn_continue_sandbox" to "Continue in Sandbox Mode",
        "btn_to_menu" to "Main Menu",

        // Guide
        "guide_title" to "Commander's Handbook",
        "guide_text" to "Nebula 4X unites grand 4X strategy with tactical starship piloting.\n\n" +
                "• 4X Mode: Command your galactic empire. Build fleets at capital shipyards, colonize neutral worlds, research technologies, negotiate treaties, and spy on rivals.\n" +
                "• RPG Mode: Switch to the bridge at any time. Trade market commodities, jump through hyperspace lanes or dangerous wormholes, engage hostile pirates, and upgrade your vessel.\n" +
                "• Optimization: The vector tactical radar canvas is specifically designed for 60fps responsiveness on low-memory hardware."
    )

    fun get(key: String, lang: GameLanguage): String {
        val dict = if (lang == GameLanguage.UK) ukrainian else english
        return dict[key] ?: english[key] ?: key
    }
}
