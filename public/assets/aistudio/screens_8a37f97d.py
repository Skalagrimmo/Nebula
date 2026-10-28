from termuxgui import LinearLayout, View
from gui.widgets import WidgetFactory
from gui.styles import GuiStyles


class MainMenuScreen:
    """Головне меню"""
    
    def __init__(self, activity, on_start, on_rpg, on_load, on_quit):
        self.activity = activity
        self.on_start = on_start
        self.on_rpg = on_rpg
        self.on_load = on_load
        self.on_quit = on_quit
        self.layout = None
        self.build()
    
    def build(self):
        layout = LinearLayout(self.activity)
        layout.setOrientation(LinearLayout.VERTICAL)
        try:
            layout.setBackgroundColor(GuiStyles.BG_DARK)
        except:
            pass
        try:
            layout.setPadding(20, 20, 20, 20)
        except:
            pass
        
        # Заголовок
        title = WidgetFactory.create_label(
            self.activity,
            "🌌 NEBULA 4X",
            color=GuiStyles.TEXT_GOLD,
            size=32
        )
        try:
            title.setGravity(View.GRAVITY_CENTER)
        except:
            pass
        layout.addView(title)
        
        # Підзаголовок
        subtitle = WidgetFactory.create_label(
            self.activity,
            "Galactic Strategy",
            color=GuiStyles.TEXT_SECONDARY,
            size=18
        )
        try:
            subtitle.setGravity(View.GRAVITY_CENTER)
        except:
            pass
        layout.addView(subtitle)
        
        try:
            layout.addView(View(self.activity))
            layout.addView(View(self.activity))
        except:
            pass
        
        # Кнопки
        btn_start = WidgetFactory.create_button(
            self.activity,
            "🚀 New Game (4X)",
            self.on_start
        )
        try:
            btn_start.setWidth(GuiStyles.BUTTON_WIDTH)
            btn_start.setHeight(GuiStyles.BUTTON_HEIGHT)
            btn_start.setGravity(View.GRAVITY_CENTER)
        except:
            pass
        layout.addView(btn_start)
        
        btn_rpg = WidgetFactory.create_button(
            self.activity,
            "🚀 New Game (RPG)",
            self.on_rpg
        )
        try:
            btn_rpg.setWidth(GuiStyles.BUTTON_WIDTH)
            btn_rpg.setHeight(GuiStyles.BUTTON_HEIGHT)
            btn_rpg.setGravity(View.GRAVITY_CENTER)
        except:
            pass
        layout.addView(btn_rpg)
        
        btn_load = WidgetFactory.create_button(
            self.activity,
            "💾 Load Game",
            self.on_load
        )
        try:
            btn_load.setWidth(GuiStyles.BUTTON_WIDTH)
            btn_load.setHeight(GuiStyles.BUTTON_HEIGHT)
            btn_load.setGravity(View.GRAVITY_CENTER)
        except:
            pass
        layout.addView(btn_load)
        
        btn_quit = WidgetFactory.create_button(
            self.activity,
            "❌ Quit",
            self.on_quit
        )
        try:
            btn_quit.setWidth(GuiStyles.BUTTON_WIDTH)
            btn_quit.setHeight(GuiStyles.BUTTON_HEIGHT)
            btn_quit.setGravity(View.GRAVITY_CENTER)
        except:
            pass
        layout.addView(btn_quit)
        
        self.layout = layout
    
    def get_layout(self):
        return self.layout


class GameScreen:
    """Основний ігровий екран"""
    
    def __init__(self, activity, state, engine, on_back):
        self.activity = activity
        self.state = state
        self.engine = engine
        self.on_back = on_back
        self.layout = None
        self.info_labels = []
        self.time_label = None
        self.build()
    
    def build(self):
        layout = LinearLayout(self.activity)
        layout.setOrientation(LinearLayout.VERTICAL)
        try:
            layout.setBackgroundColor(GuiStyles.BG_DARK)
        except:
            pass
        try:
            layout.setPadding(10, 10, 10, 10)
        except:
            pass
        
        # Верхня панель
        top_panel = LinearLayout(self.activity)
        top_panel.setOrientation(LinearLayout.HORIZONTAL)
        
        # Кнопка назад
        btn_back = WidgetFactory.create_button(
            self.activity,
            "← Back",
            self.on_back
        )
        try:
            btn_back.setWidth(100)
            btn_back.setHeight(40)
        except:
            pass
        top_panel.addView(btn_back)
        
        # Інформація про час
        self.time_label = WidgetFactory.create_label(
            self.activity,
            "Time: 0",
            color=GuiStyles.TEXT_PRIMARY,
            size=16
        )
        top_panel.addView(self.time_label)
        
        layout.addView(top_panel)
        
        # Панель інформації
        self.info_panel = LinearLayout(self.activity)
        self.info_panel.setOrientation(LinearLayout.VERTICAL)
        layout.addView(self.info_panel)
        
        # Нижня панель з кнопками управління
        bottom_panel = LinearLayout(self.activity)
        bottom_panel.setOrientation(LinearLayout.HORIZONTAL)
        try:
            bottom_panel.setGravity(View.GRAVITY_CENTER)
        except:
            pass
        
        btn_build = WidgetFactory.create_button(
            self.activity,
            "🏗️",
            lambda: self.do_action("b")
        )
        try:
            btn_build.setWidth(80)
            btn_build.setHeight(40)
        except:
            pass
        bottom_panel.addView(btn_build)
        
        btn_move = WidgetFactory.create_button(
            self.activity,
            "🚀",
            lambda: self.do_action("m")
        )
        try:
            btn_move.setWidth(80)
            btn_move.setHeight(40)
        except:
            pass
        bottom_panel.addView(btn_move)
        
        btn_attack = WidgetFactory.create_button(
            self.activity,
            "⚔️",
            lambda: self.do_action("a")
        )
        try:
            btn_attack.setWidth(80)
            btn_attack.setHeight(40)
        except:
            pass
        bottom_panel.addView(btn_attack)
        
        btn_tech = WidgetFactory.create_button(
            self.activity,
            "🔬",
            lambda: self.do_action("tech")
        )
        try:
            btn_tech.setWidth(80)
            btn_tech.setHeight(40)
        except:
            pass
        bottom_panel.addView(btn_tech)
        
        btn_pause = WidgetFactory.create_button(
            self.activity,
            "⏸️",
            lambda: self.do_action("pause")
        )
        try:
            btn_pause.setWidth(80)
            btn_pause.setHeight(40)
        except:
            pass
        bottom_panel.addView(btn_pause)
        
        layout.addView(bottom_panel)
        
        self.layout = layout
        self.update_info()
    
    def update_info(self):
        """Оновлює інформацію на екрані"""
        try:
            self.info_panel.removeAllViews()
        except:
            pass
        
        if self.time_label:
            try:
                self.time_label.setText(f"Time: {self.state.get('time', 0)}")
            except:
                pass
        
        # Фракції
        for fid, faction in self.state.get("factions", {}).items():
            name = faction.get("name", fid)
            systems = len(faction.get("systems", []))
            credits = faction.get("credits", 0)
            military = round(faction.get("military", 1), 2)
            
            color = GuiStyles.TEXT_BLUE if fid == "FACTION_A" else GuiStyles.TEXT_RED if fid == "FACTION_B" else GuiStyles.TEXT_GREEN
            
            line = WidgetFactory.create_label(
                self.activity,
                f"{name}: 💰{credits} 🪐{systems} ⚔️{military}",
                color=color,
                size=16
            )
            try:
                self.info_panel.addView(line)
            except:
                pass
        
        # Флоти
        fleets = self.state.get("fleets", {})
        if fleets:
            fleet_line = WidgetFactory.create_label(
                self.activity,
                f"🚀 Fleets: {len(fleets)}",
                color=GuiStyles.TEXT_SECONDARY,
                size=14
            )
            try:
                self.info_panel.addView(fleet_line)
            except:
                pass
    
    def do_action(self, action):
        """Виконує дію гравця"""
        print(f"Action: {action}")
        if action == "pause":
            pass
        else:
            self.engine.step()
            self.update_info()
    
    def get_layout(self):
        return self.layout


class GameOverScreen:
    """Екран завершення гри"""
    
    def __init__(self, activity, victory_data, on_menu):
        self.activity = activity
        self.victory_data = victory_data
        self.on_menu = on_menu
        self.layout = None
        self.build()
    
    def build(self):
        layout = LinearLayout(self.activity)
        layout.setOrientation(LinearLayout.VERTICAL)
        try:
            layout.setBackgroundColor(GuiStyles.BG_DARK)
            layout.setGravity(View.GRAVITY_CENTER)
            layout.setPadding(20, 20, 20, 20)
        except:
            pass
        
        # Заголовок
        title = WidgetFactory.create_label(
            self.activity,
            "🎉 GALACTIC VICTORY! 🎉",
            color=GuiStyles.TEXT_GOLD,
            size=28
        )
        try:
            title.setGravity(View.GRAVITY_CENTER)
        except:
            pass
        layout.addView(title)
        
        # Інформація про переможця
        winner = self.victory_data.get("winner", "Unknown")
        vtype = self.victory_data.get("type", "Unknown")
        
        info = WidgetFactory.create_label(
            self.activity,
            f"{winner} wins by {vtype}!",
            color=GuiStyles.TEXT_GREEN,
            size=20
        )
        try:
            info.setGravity(View.GRAVITY_CENTER)
        except:
            pass
        layout.addView(info)
        
        # Кнопка в меню
        btn_menu = WidgetFactory.create_button(
            self.activity,
            "🏠 Main Menu",
            self.on_menu
        )
        try:
            btn_menu.setWidth(GuiStyles.BUTTON_WIDTH)
            btn_menu.setHeight(GuiStyles.BUTTON_HEIGHT)
            btn_menu.setGravity(View.GRAVITY_CENTER)
        except:
            pass
        layout.addView(btn_menu)
        
        self.layout = layout
    
    def get_layout(self):
        return self.layout