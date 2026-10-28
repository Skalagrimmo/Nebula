from termuxgui import View, TextView, Button, LinearLayout, FrameLayout

class WidgetFactory:
    @staticmethod
    def create_button(activity, text, on_click, **kwargs):
        """Створює кнопку зі стилем"""
        btn = Button(activity, text, **kwargs)
        btn.set_on_click(on_click)
        return btn
    
    @staticmethod
    def create_label(activity, text, color="#ffffff", size=18, **kwargs):
        """Створює текстовий напис"""
        label = TextView(activity, text, **kwargs)
        try:
            label.setTextSize(size)
        except:
            pass
        try:
            label.setTextColor(color)
        except:
            pass
        return label
    
    @staticmethod
    def create_panel(activity, **kwargs):
        """Створює панель"""
        return FrameLayout(activity, **kwargs)
    
    @staticmethod
    def create_scroll(activity, **kwargs):
        """Створює прокручуваний контейнер (заглушка)"""
        return FrameLayout(activity, **kwargs)