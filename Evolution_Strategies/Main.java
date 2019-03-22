package Evolution_Strategies;

import java.util.ArrayList;
import java.io.*;
import Evolution_Strategies.Configs.Config;
import Evolution_Strategies.Distrib.WorkerExecutionHandler;
import Evolution_Strategies.Environments.Environment;
import Evolution_Strategies.Environments.EnvironmentAgent;
import Evolution_Strategies.Environments.CartPole.CartPoleEnvironment;
import Evolution_Strategies.Environments.Flappy.Simulator;
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
	private Worker best;
	private double bestFitness;
	public static double STD_DEV;
	private ArrayList<Double> policyProgress;
	private double lr;
	public Main()
	{
		init();
		run();
	}
	public void init()
	{
		best = null;
		bestFitness = 0;

		numEpochs = 1000;
		populationSize = 15;
		workers = new ArrayList<Worker>();

		sim = new Simulator();
		environment = sim.getEnv();
		environment.initEnv();
		policyProgress = new ArrayList<Double>();
		/*CameraFocus subject = new CameraFocus(new double[] {0,0});
        double[] scale = new double[] {1.5,1.5};
        Camera camera = new Camera(1920,1080,scale,1000,subject);
        environment = new CartPoleEnvironment(subject,camera,scale);*/

		//agents = environment.buildAgents(populationSize);
		policy = new Network(Config.POLICY_INPUT_NODES,Config.POLICY_HIDDEN_NODES,Config.POLICY_OUTPUT_NODES);
		executionHandler = new WorkerExecutionHandler();
		for(int i=0;i<populationSize;i++)
		{
			workers.add(new Worker(policy,i*500));
		}
		STD_DEV = Config.NOISE_STD_DEV;
		lr = Config.SGD_STEP_SIZE_DEFAULT;
	}
	public void run()
	{
		ArrayList<ArrayList<Double>> allRewards = new ArrayList<ArrayList<Double>>();
		ArrayList<double[]> noiseFlat = new ArrayList<double[]>();

		double[] maxHistory = new double[numEpochs];
		double[] meanHistory = new double[numEpochs];
		double[] thetaHistory = new double[numEpochs];
		double[] stdThetaHistory = new double[numEpochs];

		double[] gradientHistory = new double[numEpochs];
		double[] gradientStdHistory = new double[numEpochs];

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

			for(int j=0;j<populationSize;j++)
			{
				workers.get(j).setPlaying(true);
			}

			executionHandler.run(workers);

			boolean done = false;
			while(!done)
			{
				done = true;
				for(int j=0;j<populationSize;j++)
				{
					if(workers.get(j).isPlaying())
					{
						//System.out.println("worker");
						done = false;
					}
				}
			}

			double mean = 0;
			double bestThisEpoch = 0;
			double[] update = null;
			for(int j=0;j<populationSize;j++)
			{
				//workers.get(j).playEpisode(environment);

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
					//update = workers.get(j).getBestNoiseFlat();
				}
				mean+=workers.get(j).getFitness();
			}
			mean/=workers.size();

			if(i%20 == 0)
			{
				policyProgress.add(evaluatePolicy());
			}



			double avgTheta = 0;
			double[] theta = policy.getFlat();
			for(int j=0;j<theta.length;j++)
			{
				avgTheta+=theta[j];
			}
			avgTheta/=theta.length;
			thetaHistory[i] = avgTheta;

			double stdTheta = 0;
			for(int j=0;j<theta.length;j++)
			{
				stdTheta+=Math.pow(theta[j]-avgTheta, 2);
			}
			stdTheta/=theta.length;
			stdThetaHistory[i] = stdTheta;

			/*if(mean >= 0.999*bestThisEpoch)
            {
            	init();
            }*/

			meanHistory[i] = mean;
			maxHistory[i] = bestThisEpoch;

			System.out.println("MEAN FITNESS: "+mean);
			System.out.println("BEST FITNESS: "+bestThisEpoch);
			System.out.println("CENTERING RANKS");

			if(update != null)
			{
				policy.updateWeightsFromFlat(update, false);
			}
			else
			{
				System.out.println("APPROXIMATING GRADIENT");
				//ArrayList<ArrayList<Double>> centeredRanks = computeCenteredRanks(allRewards);
				ArrayList<ArrayList<Double>> centeredRanks = allRewards;

				//double[] gradientApproximation = computeWeightedGradient(centeredRanks,noiseFlat,500,3);
				double[] gradientApproximation = computePaperGradient(centeredRanks,noiseFlat);
				System.out.println("UPDATING POPULATION\n");

				if(gradientApproximation == null)
				{
					continue;
				}
				policy.updateWeightsFromFlat(gradientApproximation, false);

				double gradientAvg = 0;
				for(int j=0;j<gradientApproximation.length;j++)
				{
					gradientAvg+=Math.abs(gradientApproximation[j]);
				}
				gradientAvg/=gradientApproximation.length;
				gradientHistory[i] = gradientAvg;

				double gradientStd = 0;
				for(int j=0;j<gradientApproximation.length;j++)
				{
					gradientStd+=Math.pow(Math.abs(gradientApproximation[j])-gradientAvg,2);
				}
				gradientStd/=gradientApproximation.length;
				gradientStdHistory[i] = gradientStd;
			}

			//environment.takeStep();
		}

		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File("resources/ES/metrics.txt")));

			String str = "";
			for(int i=0;i<maxHistory.length;i++)
			{
				str += maxHistory[i]+",";
			}
			writer.write(str+"\n");

			str = "";
			for(int i=0;i<meanHistory.length;i++)
			{
				str+= meanHistory[i]+",";
			}
			writer.write(str+"\n");

			str = "";
			for(int i=0;i<thetaHistory.length;i++)
			{
				str += thetaHistory[i]+",";
			}
			writer.write(str+"\n");

			str = "";
			for(int i=0;i<stdThetaHistory.length;i++)
			{
				str += stdThetaHistory[i]+",";
			}
			writer.write(str+"\n");

			str = "";
			for(int i=0;i<gradientHistory.length;i++)
			{
				str += gradientHistory[i]+",";
			}
			writer.write(str+"\n");

			str = "";
			for(int i=0;i<gradientStdHistory.length;i++)
			{
				str += gradientStdHistory[i]+",";
			}
			writer.write(str+"\n");
			
			str = "";
			for(int i=0;i<policyProgress.size();i++)
			{
				str += policyProgress.get(i)+",";
			}
			writer.write(str);	
			writer.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		executionHandler.stop();
	}

	private double[] computePaperGradient(ArrayList<ArrayList<Double>> ranks, ArrayList<double[]> noise)
	{
		ArrayList<Double> weights = new ArrayList<Double>();

		for(int i=0;i<ranks.size();i++)
		{
			weights.add(ranks.get(i).get(0));
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

		if(std == 0)
		{
			return null;
		}

		for(int i=0;i<weights.size();i++)
		{
			weights.set(i, (weights.get(i) - mean)/std);
		}

		//System.out.println("RANK MEAN: "+mean+"\nRANK STD: "+std);
		double[] weightedGradientApproximation = new double[policy.getNumParams()];
		double multiplier = lr/(workers.size()*STD_DEV);
		lr*=0.999;
		//STD_DEV*=0.99;

		int numSummed = 0;

		for(int i=0;i<noise.size();i++)
		{
			for(int j=0;j<noise.get(i).length;j++)
			{
				weightedGradientApproximation[j]+=noise.get(i)[j]*weights.get(i);
			}
			numSummed++;
		}

		//double[] params = policy.getFlat();
		//System.out.print("\nFLAT: ");
		for(int i=0;i<weightedGradientApproximation.length;i++)
		{
			//System.out.print(params[i]+" ");
			weightedGradientApproximation[i] = (weightedGradientApproximation[i])*multiplier;
			//weightedGradientApproximation[i] = params[i]*Config.L2_COEFFICIENT - weightedGradientApproximation[i]/(numSummed*Config.NOISE_STD_DEV);
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
	private double evaluatePolicy()
	{
		Worker w = new Worker(policy,0);
		w.playTestEpisode(environment);
		System.out.println("POLICY EVALUATED TO: "+w.getFitness());
		return w.getFitness();
	}
	public static void main(String[] args){new Main();}
}
