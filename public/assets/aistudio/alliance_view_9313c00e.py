def print_alliances(state):

    print()
    print("=" * 60)
    print("ALLIANCES")
    print("=" * 60)

    alliances = state.get("alliances", {})

    if not alliances:
        print("No alliances")
        return

    for member, target in alliances.items():
        print(member, "opposes", target)