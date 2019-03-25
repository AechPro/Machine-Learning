package Evolution_Strategies;

import java.util.ArrayList;

import Evolution_Strategies.Configs.Config;
import Evolution_Strategies.Distrib.WorkerExecutionHandler;
import Evolution_Strategies.Environments.Environment;
import Evolution_Strategies.Environments.Snake.Simulator;
import Evolution_Strategies.Optimizers.Adam;
import Evolution_Strategies.Optimizers.BasicOpt;
import Evolution_Strategies.Optimizers.Optimizer;
import Evolution_Strategies.Policies.FFNN.Network;
import Evolution_Strategies.Population.Worker;
import Evolution_Strategies.Util.Maths;
import Evolution_Strategies.Util.MetricLogger;

public class Main
{
    private ArrayList<Worker> pop;
    private Environment env;
    private Simulator sim;
    private Network policy;
    private Optimizer opt;
    private MetricLogger statsLogger;
    private WorkerExecutionHandler distHandler;
    
    public Main()
    {
        init();
        run();
    }
    public void init()
    {
       pop = new ArrayList<Worker>();
       sim = new Simulator();
       env = sim.getEnv();
       policy = new Network(Config.POLICY_LAYER_INFO);
      
       opt = new Adam(policy.getNumParams(), Config.ADAM_STEP_SIZE_DEFAULT);
       //opt = new BasicOpt();
       statsLogger = new MetricLogger("resources/ES/stats.txt");
       distHandler = new WorkerExecutionHandler();
       
       for(int i=0;i<Config.POPULATION_SIZE;i++)
       {
           pop.add(new Worker(policy, i*20));
       }
    }
    public void run()
    {
    	double best = 18.0;
    	policy.loadParameters("resources/ES/models/snake/weights.txt");
    	//sim.renderEpisode(policy);
    	//System.exit(0);
        double[] fitnesses = new double[pop.size()];
        for(int i=0;i<Config.NUM_EPOCHS;i++)
        {
        	//System.out.println("epoch start");
            double max = 0;
            double mean = 0;
            
            if(i % 100 == 0)
            {
            	System.out.println("SAVING PARAMS");
                policy.saveParameters("resources/ES/models/snake/weights.txt");
            }
            
            runEpochDist();
            //System.out.println("test done");
            for(int j=0;j<pop.size();j++)
            {
                //pop.get(j).playEpisode(env);
                fitnesses[j] = pop.get(j).getFitness();
                mean+=fitnesses[j];
                if(fitnesses[j] > max)
                {
                    max = fitnesses[j];
                }
                if(fitnesses[j] > best)
                {
                	best = fitnesses[j];
                    policy.saveParameters("resources/ES/models/snake/weights.txt");
                }
            }
            mean/=pop.size();
            
            System.out.println("\n***EPOCH "+i+"***");
            System.out.println("MEAN: "+mean);
            System.out.println("MAX: "+max);
            
            double[] gradient = updateWorkers();
            if(gradient != null)
            {
                statsLogger.update(fitnesses, policy.getFlat(), gradient);
            }
        }
        
        distHandler.stop();
        statsLogger.finalize();
    }
    private double[] updateWorkers()
    {
        double[][] noiseFlats = new double[pop.size()][policy.getNumParams()];
        double[] rewards = new double[pop.size()];
        
        for(int i=0;i<noiseFlats.length;i++)
        {
            noiseFlats[i] = pop.get(i).getNoiseFlat().clone();
            rewards[i] = pop.get(i).getFitness();
        }
        
        double[] gradient = computeAdamGradient(noiseFlats, rewards);
        if(gradient == null){return null;}
        
        double[] update = opt.computeUpdate(policy.getFlat(),gradient);
        policy.setFlat(update);
        
        return gradient;
    }
    
    private double[] computeAdamGradient(double[][] noise, double[] rewards)
    {
        double[] gradient = new double[noise[0].length];
        
        rewards = computeCenteredRanks(rewards);
        
        if(rewards == null){return null;}
        
        //num entries
        for(int i=0;i<noise.length;i++)
        {
            //current entry
            for(int j=0;j<gradient.length;j++)
            {
                gradient[j] += rewards[i]*noise[i][j];
            }
        }
        
        double[] theta = policy.getFlat();
        for(int i=0;i<gradient.length;i++)
        {
            gradient[i] = (theta[i]*Config.L2_COEFFICIENT - gradient[i])/gradient.length;
        }
        return gradient;
    }
    
    private double[] computeGradient(double[][] noise, double[] rewards)
    {
        double[] gradient = new double[noise[0].length];
        
        rewards = normalize(rewards);
        
        if(rewards == null){return null;}
        
        //num entries
        for(int i=0;i<noise.length;i++)
        {
            //current entry
            for(int j=0;j<gradient.length;j++)
            {
                gradient[j] += rewards[i]*noise[i][j];
            }
        }
        return gradient;
    }
    
    private double[] normalize(double[] rewards)
    {
        double std = 0, mean = 0;
        for(int i=0;i<rewards.length;i++)
        {
            mean+=rewards[i];
        }
        mean/=rewards.length;
        
        for(int i=0;i<rewards.length;i++)
        {
            std+=Math.pow(rewards[i] - mean,2);
        }
        std/=rewards.length;
        
        if(std == 0){return null;}
        
        double[] norm = new double[rewards.length];
        for(int i=0;i<rewards.length;i++)
        {
            norm[i] = (rewards[i] - mean)/std;
        }
        
        return norm;
    }
    private double[] computeCenteredRanks(double[] rewards)
    {
        double[] centered = new double[rewards.length];
        int[] indices = Maths.argsort(rewards, true);
        double[] range = new double[rewards.length];
        for(int i=0;i<rewards.length;i++)
        {
            range[i] = (double)i+1;
        }
        
        for(int i=0;i<rewards.length;i++)
        {
            centered[indices[i]] = (range[i]/(centered.length-1)) - 0.5;
        }
        return centered;
    }
    
    
    private double[] computeCenteredExponentialRanks(double[] rewards)
    {
        double[] centered = new double[rewards.length];
        int[] indices = Maths.argsort(rewards, true);
        double[] range = new double[rewards.length];
        for(int i=0;i<rewards.length;i++)
        {
            range[i] = Math.exp((double)i/(double)rewards.length);
        }
        
        for(int i=0;i<rewards.length;i++)
        {
            centered[indices[i]] = (range[i]/(centered.length)) - 0.5;
        }
        return centered; 
    }
    private void runEpochDist()
    {
        for(Worker w : pop)
        {
            w.setRunning(true);
        }
        distHandler.run(pop);
        
        boolean wait = true;
        while(wait)
        {
            wait = false;
            for(Worker w : pop)
            {
                if(w.isRunning())
                {
                    wait = true;
                }
            }
        }
    }
    public static void main(String[] args){new Main();}
}
