package Evolution_Strategies.Population;

import java.util.ArrayList;

import Evolution_Strategies.Main;
import Evolution_Strategies.Configs.Config;
import Evolution_Strategies.Environments.Environment;
import Evolution_Strategies.Environments.EnvironmentAgent;
import Evolution_Strategies.Policies.FFNN.Network;
import Evolution_Strategies.Util.Maths;
import Evolution_Strategies.Util.NoiseTable;
import Evolution_Strategies.Util.Rand;

public class Worker
{
    private ArrayList<Double> rewards;
    private int timeSteps;
    private Network policy;
    
    private double fitness, fitnessNeg;
    private double[] noiseFlat;
    
    private boolean playing;
    
    private int currentNoiseIdx;

    public Worker(Network p, int startNoiseIdx)
    {
        timeSteps = 0;
        rewards = new ArrayList<Double>();
        policy = p;
        noiseFlat = new double[policy.getNumParams()];
        currentNoiseIdx = startNoiseIdx;
    }
    public void playTestEpisode(Environment e)
    {
    	playing = true;
        fitness = 0;
        timeSteps = 0;
        
        e.initEnv();
        ArrayList<EnvironmentAgent> agents = e.buildAgents(1);
        e.takeStep();
        while(playTimeStep(agents.get(0), false))
        {
            e.takeStep();
        }
        playing = false;
    }
    public void playEpisode(Environment e)
    {   
    	//System.out.println("Started playing");
    	playing = true;
        fitness = 0;
        fitnessNeg = 0;
        timeSteps = 0;
        
        if(rewards == null){rewards=new ArrayList<Double>();}
        else{rewards.clear();}
        
        computeNoiseFlat();
        
        e.initEnv();
        ArrayList<EnvironmentAgent> agents = e.buildAgents(1);
        e.takeStep();
        while(playTimeStep(agents.get(0), false))
        {
        	//System.out.println("WORKER TAKING STEP");
            e.takeStep();
        }
        
        /*timeSteps = 0;
        e.initEnv();
        agents = e.buildAgents(1);
        e.takeStep();
        while(playTimeStep(agents.get(0), true))
        {
            e.takeStep();
        }*/
        rewards.clear();
        
        
        rewards.add(fitness);
        //rewards.add(fitnessNeg);
        playing = false;
        //System.out.println("Done playing");
    }

    public boolean playTimeStep(EnvironmentAgent e, boolean negative)
    {
        double[] state = e.getState();

        if(state == null || policy == null){return false;}

        policy.activateNoisy(state,noiseFlat,negative);
        int action = findAction(policy.readOutputVector());
        double reward =  e.takeAction(action);
        double rewardNoise = 0.001;
        if(Rand.rand.nextBoolean())
        {
        	rewardNoise = -rewardNoise;
        }
        if(!negative)
        {
            fitness += reward+rewardNoise;
        }
        else
        {
            fitnessNeg += reward+rewardNoise;
        }
        
        timeSteps++;

        return true;
    }
    
    public void updatePolicy(double[] gradient)
    {
        policy.updateWeightsFromFlat(gradient,true);
    }
    private void computeNoiseFlat()
    {
        for(int i=0;i<noiseFlat.length;i++)
        {
            noiseFlat[i] = NoiseTable.noise[i+currentNoiseIdx];
            //noiseFlat[i] = Rand.getRandNorm(0.0, Config.NOISE_STD_DEV);
        }
        currentNoiseIdx+=noiseFlat.length;
        if(currentNoiseIdx >= NoiseTable.noise.length)
        {
            currentNoiseIdx = 0;
        }
    }
    
    public double[] getBestNoiseFlat()
    {
        if(fitnessNeg > fitness)
        {
            for(int i=0;i<noiseFlat.length;i++)
            {
                noiseFlat[i] = -noiseFlat[i];
            }
        }
        return noiseFlat;
    }
    
    public double getFitness()
    {
        return Math.max(fitness,fitnessNeg);
    }
    public void copyParams(Worker other)
    {
        policy.copyParams(other.getPolicy());
    }
    public Network getPolicy()
    {
        return policy;
    }

    private int findAction(double[] policyDist)
    {
        return Maths.argmax(policyDist);
    }
    public ArrayList<Double> getRewards()
    {
        return rewards;
    }
    public double[] getNoiseFlat()
    {
        return noiseFlat;
    }
    
    public int getNumTimeSteps()
    {
        return timeSteps;
    }
    
    public boolean isPlaying()
    {
    	return playing;
    }
    
    public void setPlaying(boolean i)
    {
    	playing = i;
    }
}
