import os

def clear_screen():
    """Очищує екран (Windows / Linux / Mac)"""
    os.system('cls' if os.name == 'nt' else 'clear')

def print_divider(char="=", length=50):
    """Друкує роздільник"""
    print(char * length)

def input_int(prompt, default=None):
    """Безпечне введення числа"""
    while True:
        try:
            val = input(prompt)
            if val == "" and default is not None:
                return default
            return int(val)
        except ValueError:
            print("  Please enter a number!")