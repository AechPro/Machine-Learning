package NEAT.Simulations.BetterFish.commands;

import NEAT.Simulations.BetterFish.workers.Fish;
import core.UI.Command;

public class executeDecisionCommand extends Command
{

    public executeDecisionCommand(Object tar)
    {
        super(tar);
    }

    @Override
    public Object execute(Object[] inputs)
    {
        ((Fish)target).executeDecision();
        return null;
    }

}
