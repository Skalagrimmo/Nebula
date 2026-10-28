from core.engine import tick


class NebulaEngine:

    def __init__(self, state):

        self.state = state
        self.ticks = 0

    def step(self):

        if self.state is None:
            raise ValueError("STATE IS NONE — engine crashed")

        tick(self.state)

        self.ticks += 1
        self.state["ticks"] = self.ticks

    def run(self, max_ticks=1000):

        while self.ticks < max_ticks:
            self.step()