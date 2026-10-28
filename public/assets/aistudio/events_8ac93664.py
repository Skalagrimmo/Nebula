def emit(state, event_type, data=None):
    if data is None:
        data = {}
    
    # Пропускаємо технічні події
    skip_types = ["TICK_START", "TICK_END", "TICK"]
    if event_type in skip_types:
        return
    
    event = {
        "time": state["time"],
        "type": event_type,
        "data": data
    }
    
    state["events"].append(event)
    
    if len(state["events"]) > 1000:
        state["events"].pop(0)