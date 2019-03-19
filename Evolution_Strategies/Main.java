package Evolution_Strategies;

import java.util.ArrayList;

import Evolution_Strategies.Configs.Config;
import Evolution_Strategies.Distrib.WorkerExecutionHandler;
import Evolution_Strategies.Environments.Environment;
import Evolution_Strategies.Environments.EnvironmentAgent;
import Evolution_Strategies.Environments.CartPole.CartPoleEnvironment;
import Evolution_Strategies.Environments.Snake.Simulator;
import Evolution_Strategies.Policies.FFNN.Network;
import Evolution_Strategies.Population.Worker;
import Evolution_Strategies.Util.Maths;
import core.camera.Camera;
import editor.CameraFocus;

public class Main
{
    private ArrayList<Worker> workers;
    private ArrayList<EnvironmentAgent> agents;
    private Environment environment;
    private int populationSize;
    private int numEpochs;
    private Simulator sim;
    private WorkerExecutionHandler executionHandler;
    private Network policy;
    
    public Main()
    {
        init();
        run();
    }
    public void init()
    {
        numEpochs = 10000;
        populationSize = 100;
        workers = new ArrayList<Worker>();
        
        sim = new Simulator();
        environment = sim.getEnv();
        environment.initEnv();
        
        
        /*CameraFocus subject = new CameraFocus(new double[] {0,0});
        double[] scale = new double[] {1.5,1.5};
        Camera camera = new Camera(1920,1080,scale,1000,subject);
        environment = new CartPoleEnvironment(subject,camera,scale);*/
        
        agents = environment.buildAgents(populationSize);
        policy = new Network(Config.POLICY_INPUT_NODES,Config.POLICY_HIDDEN_NODES,Config.POLICY_OUTPUT_NODES);
        executionHandler = new WorkerExecutionHandler();
        for(int i=0;i<populationSize;i++)
        {
            workers.add(new Worker(policy,i*5));
        }
    }
    public void run()
    {
        Worker best = null;
        double bestFitness = 0;
        ArrayList<ArrayList<Double>> allRewards = new ArrayList<ArrayList<Double>>();
        ArrayList<double[]> noiseFlat = new ArrayList<double[]>();
        
        double[] maxHistory = new double[numEpochs];
        double[] meanHistory = new double[numEpochs];
        
        /*policy.loadParameters("resources/ES/models/snake/weights.txt");
        sim.renderEpisode(workers.get(0));
        System.exit(0);*/
        
        for(int i=0;i<numEpochs;i++)
        {
            allRewards.clear();
            noiseFlat.clear();
            
            System.out.println("EPOCH # "+i);
            //System.out.println("INITIALIZING ENVIRONMENT");
            //environment.initEnv();
            
            //executionHandler.run(workers, environment);
            
            double mean = 0;
            double bestThisEpoch = 0;
            for(int j=0;j<populationSize;j++)
            {
                workers.get(j).playEpisode(environment);
                
                allRewards.add(workers.get(j).getRewards());
                noiseFlat.add(workers.get(j).getNoiseFlat());
                
                if(workers.get(j).getFitness() > bestThisEpoch)
                {
                    bestThisEpoch = workers.get(j).getFitness();
                }
                
                if(workers.get(j).getFitness() > bestFitness)
                {
                    best = workers.get(j);
                    bestFitness = workers.get(j).getFitness();
                    System.out.println("BEST FITNESS SO FAR: "+bestFitness);
                    policy.saveParameters("resources/ES/models/snake/weights.txt");
                    //Network policy = workers.get(j).getPolicy();
                    //policy.updateWeightsFromFlat(workers.get(j).getBestNoiseFlat(), false);
                }
                mean+=workers.get(j).getFitness();
            }
            mean/=workers.size();
            
            meanHistory[i] = mean;
            maxHistory[i] = bestThisEpoch;
            
            System.out.println("MEAN FITNESS: "+mean);
            System.out.println("BEST FITNESS: "+bestThisEpoch);
            System.out.println("CENTERING RANKS");
            
            /*System.out.print("\nRANKS BEFORE: [");
            for(int j=0;j<allRewards.size();j++)
            {
                System.out.print("[");
                for(int k=0;k<allRewards.get(j).size();k++)
                {
                    System.out.print(allRewards.get(j).get(k)+",");
                }
                System.out.print("],");
            }
            System.out.println("]");*/
            
            //ArrayList<ArrayList<Double>> centeredRanks = computeCenteredRanks(allRewards);
            ArrayList<ArrayList<Double>> centeredRanks = allRewards;
            
            /*System.out.print("RANKS CENTERED: [");
            for(int j=0;j<centeredRanks.size();j++)
            {
                System.out.print("[");
                for(int k=0;k<centeredRanks.get(j).size();k++)
                {
                    System.out.print(centeredRanks.get(j).get(k)+",");
                }
                System.out.print("],");
            }
            System.out.println("]\n");*/
            
            System.out.println("APPROXIMATING GRADIENT");
            //double[] gradientApproximation = computeWeightedGradient(centeredRanks,noiseFlat,500,3);
            double[] gradientApproximation = computePaperGradient(centeredRanks,noiseFlat);
            
            /*String out = "";
            for(int j=0;j<gradientApproximation.length;j++)
            {
                out+=gradientApproximation[j]+" ";
                if(j%10 == 0 && j > 1)
                {
                    out+="\n";
                }
            }
            System.out.println("GOT GRADIENT: "+out);*/
            System.out.println("UPDATING POPULATION\n");
            policy.updateWeightsFromFlat(gradientApproximation, true);
            //environment.takeStep();
        }
        
        System.out.println("MAX HISTORY:\n[");
        for(int i=0;i<maxHistory.length-1;i++)
        {
            System.out.print(maxHistory[i]+",");
        }
        System.out.println(maxHistory[maxHistory.length-1]+"]");
        
        
        System.out.print("\nMEAN HISTORY:\n[");
        for(int i=0;i<meanHistory.length-1;i++)
        {
            System.out.print(meanHistory[i]+",");
        }
        System.out.println(meanHistory[meanHistory.length-1]+"]");
       // sim.playWithWorker(best);
    }
    
    private double[] computePaperGradient(ArrayList<ArrayList<Double>> ranks, ArrayList<double[]> noise)
    {
        ArrayList<Double> weights = new ArrayList<Double>();
        
        for(int i=0;i<ranks.size();i++)
        {
            weights.add(ranks.get(i).get(0) - ranks.get(i).get(1));
        }
        
        double mean = 0;
        for(int i=0;i<weights.size();i++)
        {
            mean+=weights.get(i);
        }
        mean/=weights.size();
        
        double std = 0;
        for(int i=0;i<weights.size();i++)
        {
            std+=Math.pow(weights.get(i)-mean,2);
        }
        std/=weights.size();
        std+=Config.ADAM_EPSILON_DEFAULT;
        
        for(int i=0;i<weights.size();i++)
        {
            weights.set(i, (weights.get(i) - (mean+Config.ADAM_EPSILON_DEFAULT))/std);
        }
        //System.out.println("RANK MEAN: "+mean+"\nRANK STD: "+std);
        double[] weightedGradientApproximation = new double[policy.getNumParams()];
        double multiplier = 1d;//Config.SGD_STEP_SIZE_DEFAULT/(workers.size()*Config.NOISE_STD_DEV);
        
        for(int i=0;i<noise.size();i++)
        {
            for(int j=0;j<noise.get(i).length;j++)
            {
                weightedGradientApproximation[j]+=noise.get(i)[j]*weights.get(i);
            }
        }
        //double[] flat = policy.getFlat();
        
        for(int i=0;i<weightedGradientApproximation.length;i++)
        {
            weightedGradientApproximation[i] = (weightedGradientApproximation[i])*multiplier;
        }
        return weightedGradientApproximation;
    }

    private double[] computeWeightedGradient(ArrayList<ArrayList<Double>> ranks, ArrayList<double[]> noise, int batchSize, int numBatches)
    {
        
        ArrayList<Double> weights = new ArrayList<Double>();
        for(int i=0;i<ranks.size();i++)
        {
            weights.add(ranks.get(i).get(0) - ranks.get(i).get(1));
        }
        
        ArrayList<ArrayList<double[]>> noiseBatches = constructNoiseBatches(noise,batchSize);
        ArrayList<ArrayList<double[]>> weightBatches = constructWeightBatches(weights,batchSize);
        
        double[] weightedGradientApproximation = new double[policy.getNumParams()];
        double numSummed = 0;
        
        numBatches = Math.min(noiseBatches.size(), numBatches);
        
        //for batch in batches
        for(int i=0;i<numBatches;i++)
        {
            //for entry in batch
            for(int j=0;j<noiseBatches.get(i).size();j++)
            {
                //dot product
                for(int k=0;k<noiseBatches.get(i).get(j).length;k++)
                {
                    weightedGradientApproximation[k] += weightBatches.get(i).get(j)[0]*noiseBatches.get(i).get(j)[k];
                }
            }
            
            numSummed+=weightBatches.get(i).size();
        }
        double[] params = policy.getFlat();
        
        double l2Coeff = Config.L2_COEFFICIENT;
        for(int i=0;i<weightedGradientApproximation.length;i++)
        {
            weightedGradientApproximation[i] = params[i]*l2Coeff - weightedGradientApproximation[i]/numSummed;
        }
        return weightedGradientApproximation;
    }
    
    private ArrayList<ArrayList<double[]>> constructNoiseBatches(ArrayList<double[]> noise, int batchSize)
    {
        ArrayList<ArrayList<double[]>> batches = new ArrayList<ArrayList<double[]>>();
        ArrayList<double[]> batch = new ArrayList<double[]>();
        double[] batchEntry = null;
        for(int i=0;i<noise.size();i++)
        {
            batch = new ArrayList<double[]>();
            for(int j=0;j<batchSize;j++)
            {
                if(batchSize*i + j >= noise.size())
                {
                    batches.add(batch);
                    return batches;
                }
                batchEntry = noise.get(batchSize*i + j);
                batch.add(batchEntry);
            }
            batches.add(batch);
        }
        
        return batches;
    }
    
    private ArrayList<ArrayList<double[]>> constructWeightBatches(ArrayList<Double> weightList, int batchSize)
    {
        ArrayList<ArrayList<double[]>> batches = new ArrayList<ArrayList<double[]>>();
        ArrayList<double[]> batch = new ArrayList<double[]>();
        double[] batchEntry = new double[1];
        for(int i=0;i<weightList.size();i++)
        {
            batch = new ArrayList<double[]>();
            for(int j=0;j<batchSize;j++)
            {
                batchEntry = new double[1];
                if(batchSize*i + j >= weightList.size())
                {
                    batches.add(batch);
                    return batches;
                }
                batchEntry[0] = weightList.get(batchSize*i + j);
                batch.add(batchEntry);
            }
            batches.add(batch);
        }
        
        return batches;
    }
    private ArrayList<Integer> computeRanks(ArrayList<ArrayList<Double>> rewards)
    {
        ArrayList<Double> ravel = new ArrayList<Double>();
        ArrayList<Integer> range = new ArrayList<Integer>();
        ArrayList<Integer> ranks = new ArrayList<Integer>();
        int idx = 0;
        for(ArrayList<Double> rewardList : rewards)
        {
            ravel.addAll(rewardList);
            for(int i=0;i<rewardList.size();i++)
            {
                range.add(idx++);
                ranks.add(0);
            }
        }
        /*System.out.print("\nRAVEL: ");
        for(int i=0;i<ravel.size();i++)
        {
            System.out.print(ravel.get(i)+" ");
        }
        System.out.print("\nARG SORT: ");*/
        
        int[] args = Maths.argsort(ravel, true);
        
        for(int i=0;i<args.length;i++)
        {
            //System.out.print(args[i]+" ");
            ranks.set(args[i],range.get(i));
        }
        //System.out.println();
        return ranks;
    }
    private ArrayList<ArrayList<Double>> computeCenteredRanks(ArrayList<ArrayList<Double>> rewards)
    {
        ArrayList<Integer> ranks = computeRanks(rewards);
        ArrayList<ArrayList<Integer>> reshaped = reshape(ranks, rewards);
        ArrayList<ArrayList<Double>> centered = new ArrayList<ArrayList<Double>>();
        
        int size = 0;
        for(int i=0;i<reshaped.size();i++)
        {
            for(int j=0;j<reshaped.get(i).size();j++)
            {
                size++;
            }
        }
        for(int i=0;i<reshaped.size();i++)
        {
            ArrayList<Double> cur = new ArrayList<Double>();
            for(int j=0;j<reshaped.get(i).size();j++)
            {
                double entry = (reshaped.get(i).get(j)/(double)(size-1)) - 0.5;
                cur.add(entry);
            }
            centered.add(cur);
        }
        return centered;
    }
    private ArrayList<ArrayList<Integer>> reshape(ArrayList<Integer> lst, ArrayList<ArrayList<Double>> dest)
    {
        ArrayList<ArrayList<Integer>> out = new ArrayList<ArrayList<Integer>>();
        /*System.out.print("\nRESHAPE GOT LIST: ");
        for(int i=0;i<lst.size();i++)
        {
            System.out.print(lst.get(i)+" ");
        }
        System.out.println();*/
        
        
        int idx = 0;
        for(int i=0;i<dest.size();i++)
        {
            ArrayList<Integer> arg = new ArrayList<Integer>();
            for(int j=0;j<dest.get(i).size();j++)
            {
                arg.add(lst.get(idx + j));
            }
            idx+=dest.get(i).size();
            out.add(arg);
        }
        
        return out;
    }
    public static void main(String[] args){new Main();}
}
