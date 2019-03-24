package Evolution_Strategies.Population;

import java.util.ArrayList;

import Evolution_Strategies.Configs.Config;
import Evolution_Strategies.Environments.Environment;
import Evolution_Strategies.Environments.EnvironmentAgent;
import Evolution_Strategies.Policies.FFNN.Network;
import Evolution_Strategies.Util.Maths;
import Evolution_Strategies.Util.NoiseTable;
import Evolution_Strategies.Util.Rand;

public class Worker
{
    private int timeSteps;
    private Network policy;
    
    private double fitness, fitnessNeg;
    private double prevFit;
    private double[] noiseFlat;
    
    private int currentNoiseIdx;
    private boolean running;

    public Worker(Network p, int startNoiseIdx)
    {
        timeSteps = 0;
        policy = p;
        noiseFlat = new double[policy.getNumParams()];
        currentNoiseIdx = startNoiseIdx;
    }

    public void playEpisode(Environment e)
    {   
        running = true;
        fitness = 0;
        timeSteps = 0;
        prevFit = 0;
        
        computeNoiseFlat();
        
        e.initEnv();
        ArrayList<EnvironmentAgent> agents = e.buildAgents(1);
        e.takeStep();
        while(playTimeStep(agents.get(0)))
        {
            e.takeStep();
        }
        //System.out.println("WORKER STOPPED RUNNING");
        running = false;
    }

    public boolean playTimeStep(EnvironmentAgent e)
    {
        double[] state = e.getState();

        if(state == null || policy == null){return false;}

        double[] decision = policy.activateNoisy(state,noiseFlat);
        int action = decodeAction(decision);
        
        double fit =  e.takeAction(action);
        double reward = fit - prevFit;
        prevFit = fit;
        
        if(Rand.rand.nextBoolean())
        {
            reward += 0.001;
        }
        else
        {
            reward -= 0.001;
        }
        
        fitness += reward;
        timeSteps++;

        return true;
    }
    
    private void computeNoiseFlat()
    {
        if(currentNoiseIdx >= NoiseTable.noise.length)
        {
            currentNoiseIdx = 0;
        }
        
        for(int i=0;i<noiseFlat.length;i++)
        {
            noiseFlat[i] = NoiseTable.noise[i+currentNoiseIdx];
        }
        
        currentNoiseIdx++;
        
    }
    public Network getPolicy()
    {
        return policy;
    }

    private int decodeAction(double[] policyDist)
    {
        return Maths.argmax(policyDist);
    }
    public double getFitness()
    {
        return fitness;
    }
    public double[] getNoiseFlat()
    {
        return noiseFlat;
    }
    
    public int getNumTimeSteps()
    {
        return timeSteps;
    }
    public void setRunning(boolean i)
    {
        running = i;
    }
    public synchronized boolean isRunning()
    {
    	//System.out.println(running);
        return running;
    }
}
