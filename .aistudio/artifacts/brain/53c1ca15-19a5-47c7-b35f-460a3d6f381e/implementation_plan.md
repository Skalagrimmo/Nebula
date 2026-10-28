# Nebula 4X: Galactic Strategy & RPG (Android Edition)

A high-performance Android 4X space strategy and RPG game adapted from the Python prototype and engine specification, engineered with Jetpack Compose for ultra-low memory consumption and smooth execution on low-end devices.

## User Review & Critical Decisions

> [!IMPORTANT]
> The architectural and design decisions below reflect the user's confirmed choices:
> - **Visual Style**: Retro-sci-fi tactical vector radar with clean, modern Material 3 controls. High contrast, sharp vector geometries, low draw-call overhead, minimal textures, and near-zero GPU memory footprint.
> - **Game Modes**: Unified dual-mode structure containing both **4X Galactic Empire Strategy** and **RPG Personal Starship Exploration**, with seamless in-game mode switching.
> - **Language**: Automatically follows the device's system locale (Ukrainian if `uk`, English default), with an instant in-game toggle at any time.
> - **Hardware / RAM Optimization**: Zero heavy assets/bitmaps; all star maps, ship radar blips, combat trajectories, and hyperspace lanes are procedurally rendered via lightweight Compose vector Canvas operations. State allocations avoid boxing and redundant recompositions.

---

## 1. Overview & Core Concept

**Nebula 4X** brings the depth of classic 4X space grand strategy (*eXplore, eXpand, eXploit, eXterminate*) together with an immersive space captain RPG (*trading, bounty hunting, quests, modular ship upgrades, and wormhole travel*).

### Key Value & Pillars
1. **Zero Lag on Low-End Hardware**: Custom vector Canvas drawing runs at a locked 60 FPS with minimal memory overhead, zero bitmap bloat, and fast turn ticks.
2. **Dual Perspective Gameplay**:
   - **4X Strategy**: Command one of 3 galactic empires (Solarian Empire, Zergon Hive, Celestial Federation, or procedurally generated factions), manage star systems, research the 5-branch tech tree (Laser, Shield, Warp, Economy, Colony), manage diplomacy (Peace, Alliance, Spy, War), and lead fleets into tactical battles to achieve Domination (75% systems), Economic (20k credits), or Military Victory.
   - **RPG Mode**: Pilot your personal starship, trade commodities (ore, food, tech) across price-fluctuating systems, jump through wormholes, fight pirate ambushes, upgrade your hull/shields/lasers/cargo, and take procedural quests.
3. **Living Universe**: Dynamic economy, system colonization, inter-faction diplomacy, random galactic events (supernovae, plague, alien encounters, space storms, pirate raids, tech breakthroughs), and persistent achievements.
4. **Full Localization**: Ukrainian and English strings covering all screens, dialogs, events, and HUD metrics.

---

## 2. User Experience & Visual Design

### Aesthetic Direction: Tactical Vector Sci-Fi
- **Atmosphere**: Deep space black (`#070B12`) with tactical cyan (`#00E5FF`), imperial amber/gold (`#FFD700`), weapon red (`#FF3B30`), shield emerald (`#00E676`), and celestial blue (`#3D82FF`).
- **Tactical Vector Radar**: Star systems are rendered as glowing tactical beacons with orbital rings, faction color borders, fleet strike arrows (`▲`), and connecting hyperlanes / wormholes (`---`).
- **Typography & Clean M3 UI**: Monospace numerical telemetry and bold sci-fi status readouts paired with compact, high-touch-target (min 48dp) Material 3 controls.
- **Low-RAM Rendering Rule**: No multi-megabyte PNG/WEBP texture atlases. The star map is rendered directly on Compose `Canvas` with hardware-accelerated geometric primitives (`drawCircle`, `drawLine`, `drawPath`).

### Key User Flows
1. **Home & Mode Select**:
   - Start 4X Strategy (faction selection with credits, systems, military, AI personality badges, or random).
   - Start RPG Mode (personal ship cockpit at Sol, ready to trade or explore).
   - Continue / Load Game (load local save state).
   - How to play / Instructions modal.
   - Language selector (EN / UK).
2. **4X Command Screen**:
   - Top Bar: Turn count, galactic stardate, player credits, system count, military power, victory progress bar.
   - Main View: Interactive Tactical Star Map with zoom/pan and system node selection.
   - Quick Command Dock:
     - `[Build Fleet]` (at capital or shipyards),
     - `[Move Fleets]` (directional hyperlane transfers),
     - `[Attack System]` (initiates tactical battle simulation),
     - `[Diplomacy]` (peace treaties, spy intel on rival credits/military, alliance status),
     - `[Research Tech]` (Laser, Shield, Warp, Economy, Colony with real mechanical buffs),
     - `[System Inspector]` (population, production, stock, demand, resources),
     - `[Galactic Wars & Events]` (log of recent conquests, battles, crises),
     - `[Achievements]` (10 unlockable medals),
     - `[Switch to RPG]` (smooth transition into your flagship's captain seat).
3. **RPG Cockpit Screen**:
   - Starship HUD: Hull integrity bar, shield level, laser weapon power, cargo bay capacity (`ore`, `food`, `tech`), credits, kills, reputation.
   - Navigation: System hyperjump or risky wormhole leap.
   - Galactic Market: Buy/Sell trade commodities with dynamic regional supply/demand prices.
   - Quest Terminal: Delivery, Bounty Hunt, Exploration, Escort, and Rescue contracts.
   - Shipyard Upgrades: Expand cargo, reinforce hull, boost shields, upgrade weapon tier.
   - Tactical Combat: Turn-based pirate encounters with Attack, Shield Focus, and Flee options.
4. **Victory & Sandbox Mode**:
   - Domination (75% galaxy captured), Economic (20,000 credits), or Military (sole fleet power remaining).
   - Complete victory statistics screen with full option to **continue playing in sandbox mode**.

---

## 3. Key Product Decisions & Trade-Offs

| Decision | Chosen Approach | Rationale | Alternatives Considered |
|---|---|---|---|
| **Rendering Pipeline** | Compose `Canvas` vector drawing | Native, lightweight, 0 texture memory, runs at 60 FPS on any Android 7.0+ device without GPU memory pressure. | OpenGL ES / GLSurfaceView (too much setup, high battery draw), Text/ASCII terminal (poor touch ergonomics on mobile). |
| **State & Simulation** | Kotlin State Engine with unidirectional DataFlow (`StateFlow`) | Direct port of Python simulation rules into structured Kotlin models; instant ticks without thread blocking. | Python interpreter embedding / Chaquopy (bloats APK by 30+ MB, slow startup, extreme RAM usage). |
| **Persistence** | Local JSON serialization via Kotlinx/Room-compatible DataStore | Clean human-readable save state compatible with the original `savegame.json` format; saves automatically every 50 ticks. | Cloud Firestore (overkill for offline single-player low-resource game). |
| **Localization** | Dynamic in-memory bilingual resource dictionary | Instant language switching without restarting the Android Activity; auto-selects Ukrainian on `uk` system locale. | Standard Android XML strings only (cannot toggle runtime language without recreating Activity). |

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Nebula 4X Android UI                            │
│                                                                        │
│  ┌─────────────────────────┐   ┌────────────────────────────────────┐  │
│  │   MainMenuScreen        │   │        TacticalTopAppBar           │  │
│  │   (New 4X, RPG, Load,   │   │  (Credits, Stardate, Mode Switch,  │  │
│  │    Lang Toggle, Guide)  │   │   Victory Progress, Speed, Save)   │  │
│  └────────────┬────────────┘   └─────────────────┬──────────────────┘  │
│               │                                  │                     │
│               ▼                                  ▼                     │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                     Active Mode Viewport                         │  │
│  │                                                                  │  │
│  │   [4X Mode]                             [RPG Mode]               │  │
│  │   • TacticalStarMapCanvas (Zoom/Pan)    • ShipCockpitHUD         │  │
│  │   • SystemInspectorSheet / Orders       • TradeMarketDialog      │  │
│  │   • TechTreeModal                       • Travel & Wormhole Nav  │  │
│  │   • Diplomacy & Intel Dialog            • ShipUpgradeStation     │  │
│  │   • Wars & Events Log Drawer            • QuestBoardDialog       │  │
│  │   • BattleAnimationOverlay              • SpaceCombatOverlay     │  │
│  └─────────────────────────────────┬────────────────────────────────┘  │
└────────────────────────────────────┼───────────────────────────────────┘
                                     │ User Actions & Timers
                                     ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        NebulaGameViewModel                             │
│  • MutableStateFlow<NebulaGameState>                                   │
│  • Ticking Game Loop (Coroutines: Auto step / Manual pause)            │
│  • Mode Switcher (4X <-> RPG)                                          │
│  • Language Manager (UK <-> EN, system locale detector)                │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
                                     ▼
┌────────────────────────────────────────────────────────────────────────┐
│                         Core Simulation Engine                         │
│                                                                        │
│  ┌───────────────────┐ ┌───────────────────┐ ┌──────────────────────┐  │
│  │ Economy & Market  │ │ Fleet Movement    │ │ War & Combat         │  │
│  │ • Stock / Demand  │ │ • Hyperlane paths │ │ • Power calculation  │  │
│  │ • Maintenance fee │ │ • AI target pick  │ │ • Damage & Conquest  │  │
│  └───────────────────┘ └───────────────────┘ └──────────────────────┘  │
│  ┌───────────────────┐ ┌───────────────────┐ ┌──────────────────────┐  │
│  │ Diplomacy & Intel │ │ Random Events     │ │ Victory & Achievem.  │  │
│  │ • Trust / Status  │ │ • 14 event types  │ │ • Domination, Econ,  │  │
│  │ • Wars, Peace     │ │ • System effects  │ │   Military, 10 goals │  │
│  └───────────────────┘ └───────────────────┘ └──────────────────────┘  │
│  ┌───────────────────┐ ┌───────────────────┐ ┌──────────────────────┐  │
│  │ RPG Engine        │ │ Technology Tree   │ │ Save / Load Manager  │  │
│  │ • Ship stats      │ │ • 5 tech nodes    │ │ • JSON serialize     │  │
│  │ • Quests & Trade  │ │ • Military buffs  │ │ • Autosave (50 ticks)│  │
│  └───────────────────┘ └───────────────────┘ └──────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

### Data Model & State Entities
- **`StarSystem`**: `id`, `owner`, `capital`, `population`, `economy`, `resources`, `stock`, `demand`, `pos` (X, Y in galaxy grid), `neighbors` (hyperlane links), `systemType` (Capital, Rich, Industrial, Agricultural, Desert, Ocean, Forest, Pirate, Volcanic, Ice).
- **`Faction`**: `id`, `name`, `capital`, `credits`, `military`, `systems`, `tech`, `personality` (aggression, economyFocus, expansionFocus, defenseFocus, risk), `stats` (wins, losses, battles), `strategy`.
- **`Fleet`**: `id`, `owner`, `location`, `strength`, `speed`, `type` (Scout, Fighter, Battleship, Assault, Pirate, Flagship), `role`.
- **`PlayerShip`**: `location`, `credits`, `cargo` (ore, food, tech), `cargoLimit`, `hull`, `hullMax`, `weapon`, `shield`, `reputation`, `kills`.
- **`Quest`**: `id`, `type` (delivery, hunt, explore, escort, rescue), `name`, `desc`, `destination`, `item`, `amount`, `reward`, `bonus`, `status`.
- **`DiplomacyRelation`**: `relation`, `trust`, `wars`, `status` (Ally, Friendly, Neutral, Suspicious, Hostile).
- **`Achievement`**: 10 distinct trackable records with instant visual notifications.

---

## 5. Implementation Roadmap (Phases)

1. **Core Domain & Engine Port**:
   - Create clean, memory-efficient data classes and full simulation rules (Galaxy generator, System types, Factions, Fleets, Economy, Movement, Combat, Diplomacy, Tech tree, Random events, Quests, Ship upgrades, Victory checker, Achievements).
2. **Localization Engine (`NebulaStrings`)**:
   - Complete Ukrainian and English dictionaries covering all 4X and RPG strings, with automatic system locale detection and fast in-app switching.
3. **Tactical Vector Radar & Star Map Component**:
   - Custom Jetpack Compose `Canvas` rendering galaxy star systems, hyperlane vectors, owner borders, capital crowns, and moving fleet indicators with touch selection.
4. **4X Command Interface & Dialogs**:
   - Status bar, turn controls (Play/Pause/Step/Speed), System Inspector, Fleet Command (Build/Move/Attack), Tech Tree modal, Diplomacy & Spy terminal, Wars & Events log.
5. **RPG Cockpit & Exploration View**:
   - Personal ship HUD, Commodities Exchange (Buy/Sell with dynamic stock pricing), Navigation terminal (Hyperlane & Wormhole leaps), Shipyard Upgrade bay, Quest contracts board, Turn-based pirate space combat.
6. **Save/Load System & App Shell Polish**:
   - JSON save/load with autosave support, high-contrast M3 theme, custom vector launcher icon, accessibility standards, edge-to-edge support, and compilation verification.
