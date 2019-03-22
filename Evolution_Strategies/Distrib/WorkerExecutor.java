package Evolution_Strategies.Distrib;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import Evolution_Strategies.Environments.Environment;
import Evolution_Strategies.Population.Worker;

public class WorkerExecutor
{
    private Worker w;
    private Environment env;
    private ScheduledExecutorService executorService;
    
    public WorkerExecutor(Worker worker, Environment environment, int ticksPerSec)
    {
        w = worker;
        env = environment;
        init();
    }

    private void init()
    {
        executorService = Executors.newSingleThreadScheduledExecutor();
    }
    
    public void run(Worker worker, Environment environment)
    {
        executorService.schedule(() -> worker.playEpisode(environment), 1, 
                                 TimeUnit.MICROSECONDS);
        //executorService.shutdown();
    }
    
    public void start()
    {
        executorService.schedule(() -> tick(), 1, TimeUnit.NANOSECONDS);
    }
    public void stop()
    {
        executorService.shutdown();
    }
    public void tick()
    {
        w.playEpisode(env);
    }
    public boolean isDone()
    {
    	return executorService.isShutdown();
    }
}
