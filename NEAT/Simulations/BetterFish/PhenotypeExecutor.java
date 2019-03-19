package NEAT.Simulations.BetterFish;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import core.UI.Command;

public class PhenotypeExecutor
{
    private ScheduledExecutorService executorService;
    private int tickCount;
    private double tickRate;
    private Object[] data;

    private ArrayList<Command> commands;

    public PhenotypeExecutor(int ticksPerSecond)
    {
        //System.out.println(ticksPerSecond);
        tickRate = (1d/ticksPerSecond);
        init();
    }

    private void init()
    {
        commands = new ArrayList<Command>();
        data = new Object[1];
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void start()
    {
        executorService.scheduleAtFixedRate(() -> tick(), 1, 
                (int)Math.round(1000*tickRate), 
                TimeUnit.MILLISECONDS);
    }
    public void stop()
    {
        executorService.shutdown();
    }
    public void attachCommand(Command comm)
    {
        commands.add(comm);
    }
    public void tick()
    {
        data[0] = tickCount;
        for(int i=0,stop=commands.size();i<stop;i++)
        {
            commands.get(i).execute(data);
        }

    }
}
