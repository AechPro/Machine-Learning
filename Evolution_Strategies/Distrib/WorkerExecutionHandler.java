package Evolution_Strategies.Distrib;

import java.util.ArrayList;

import Evolution_Strategies.Environments.Environment;
import Evolution_Strategies.Population.Worker;

public class WorkerExecutionHandler
{
    private ArrayList<WorkerExecutor> executors;
    
    public WorkerExecutionHandler()
    {
        executors = new ArrayList<WorkerExecutor>();
    }
    public void run(ArrayList<Worker> workers, Environment env)
    {
        for(int i=0;i<workers.size();i++)
        {
            if(i >= executors.size())
            {
                executors.add(new WorkerExecutor(null,null,1));
            }
            executors.get(i).run(workers.get(i),env);
        }
    }
}
