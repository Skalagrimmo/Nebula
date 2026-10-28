# locales/__init__.py
# Менеджер локалізації з підтримкою перемикання мови в реальному часі

import os

# Глобальні змінні для поточної мови та перекладів
_current_lang = "uk"
_translations = {}

def load_language(lang_code):
    """Завантажує словник для вказаної мови"""
    if lang_code == "uk":
        from locales.uk import LANG
    else:
        from locales.en import LANG
    return LANG

def set_language(lang_code):
    """Встановлює поточну мову"""
    global _current_lang, _translations
    _current_lang = lang_code
    _translations = load_language(lang_code)

def _(key):
    """Повертає переклад для ключа"""
    return _translations.get(key, key)

def get_current_lang():
    """Повертає код поточної мови"""
    return _current_lang

# Ініціалізуємо стандартною мовою (за замовчуванням українська)
set_language("uk")