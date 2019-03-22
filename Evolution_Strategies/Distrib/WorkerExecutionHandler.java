package Evolution_Strategies.Distrib;

import java.util.ArrayList;

import Evolution_Strategies.Environments.Environment;
import Evolution_Strategies.Environments.CartPole.CartPoleEnvironment;
import Evolution_Strategies.Environments.Snake.Simulator;
import Evolution_Strategies.Population.Worker;
import core.camera.Camera;
import editor.CameraFocus;

public class WorkerExecutionHandler
{
    private ArrayList<WorkerExecutor> executors;
    private ArrayList<Environment> environments;
    
    public WorkerExecutionHandler()
    {
        executors = new ArrayList<WorkerExecutor>();
        environments = new ArrayList<Environment>();
    }
    public void run(ArrayList<Worker> workers)
    {
        for(int i=0;i<workers.size();i++)
        {
            if(i >= executors.size())
            {
            	Environment env = buildEnvironment();
            	env.initEnv();
            	environments.add(env);
                executors.add(new WorkerExecutor(null,null,1));
            }
            executors.get(i).run(workers.get(i),environments.get(i));
        }
    }
    public void stop()
    {
    	for(int i=0;i<executors.size();i++)
    	{
    		executors.get(i).stop();
    	}
    }
    public synchronized boolean isDone()
    {
    	for(int i=0;i<executors.size();i++)
    	{
    		if(!executors.get(i).isDone())
    		{
    			return false;
    		}
    	}
    	return true;
    }
    private Environment buildEnvironment()
    {
    	Simulator sim = new Simulator();
    	return sim.getEnv();
    	/*CameraFocus subject = new CameraFocus(new double[] {0,0});
        double[] scale = new double[] {1.5,1.5};
        Camera camera = new Camera(1920,1080,scale,1000,subject);
        return new CartPoleEnvironment(subject,camera,scale);*/
    }
}
