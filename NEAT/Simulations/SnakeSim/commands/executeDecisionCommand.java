package NEAT.Simulations.SnakeSim.commands;

import NEAT.Simulations.SnakeSim.workers.Snake;
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
        ((Snake)target).executeDecision();
        return null;
    }

}
