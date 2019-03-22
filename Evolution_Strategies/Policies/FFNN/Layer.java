package Evolution_Strategies.Policies.FFNN;

import Evolution_Strategies.Configs.Config;
import Evolution_Strategies.Util.Activations;
import Evolution_Strategies.Util.Maths;
import Evolution_Strategies.Util.Rand;

public class Layer
{
	private Layer prevLayer;
	private boolean outputLayer;
	private double[][] weights;
	private double[] biases;
	private double[] nonActivatedNodes;
	private double[] activatedNodes;
	private int nodes;

	public Layer(int numNodes, boolean isOutput, Layer prev)
	{
		outputLayer = isOutput;
		prevLayer = prev;
		nodes = numNodes;
		buildWeights();
	}

	public void activateNoisy(double[] noiseFlat, int offset, boolean negative)
	{
		if(prevLayer == null)
		{
			System.out.println("PROBLEM IN LAYER ACTIVATE NOISY, NO PREV LAYER DETECTED");
			return;
		}
		double[] input = prevLayer.getActivatedNodes().clone();


		//System.out.println("ACTIVATING ON FLAT WITH:\nFLAT LENGTH:"+noiseFlat.length+"\nOFFSET "+offset+"\nAND PARAMS "+getNumParams());
		double[][] noisyWeights = new double[prevLayer.getNumNodes()][nodes];
		int idx = 0;
		for(int i=0;i<weights.length;i++)
		{
			for(int j=0;j<weights[i].length;j++)
			{
				//System.out.println(offset+" | "+nodes*i+" | "+j + " | "+ (offset + nodes*i + j));
				if(negative)
				{
					noisyWeights[i][j] = weights[i][j] - noiseFlat[offset + idx++]*Config.NOISE_STD_DEV;
				}
				else
				{
					noisyWeights[i][j] = weights[i][j] + noiseFlat[offset + idx++]*Config.NOISE_STD_DEV; 
				}
			}
		}

		activatedNodes = Maths.matDotVec(noisyWeights, input);
		nonActivatedNodes = Maths.matDotVec(noisyWeights, input);

		int num = idx;
		for(int i=0;i<activatedNodes.length;i++)
		{
			if(!outputLayer)
			{
				if(negative)
				{
					nonActivatedNodes[i] += biases[i] - noiseFlat[num + i];
					activatedNodes[i] += biases[i] - noiseFlat[num + i];
				}
				else
				{
					nonActivatedNodes[i] += biases[i] + noiseFlat[num + i];
					activatedNodes[i] += biases[i] + noiseFlat[num + i]; 
				}
			}
			

			activatedNodes[i] = Activations.sigmoid(activatedNodes[i]);
		}
	}
	public void activate(double[] input)
	{
		if(prevLayer == null)
		{
			activatedNodes = input.clone();
			return;
		}

		activatedNodes = Maths.matDotVec(weights, input);
		nonActivatedNodes = Maths.matDotVec(weights, input);

		//System.out.println(activatedNodes.length+" vs "+biases.length);

		for(int i=0;i<activatedNodes.length;i++)
		{
			nonActivatedNodes[i] += biases[i];
			activatedNodes[i] += biases[i];
			activatedNodes[i] = Activations.sigmoid(activatedNodes[i]);
		}
	}
	public void activate()
	{
		activate(prevLayer.getActivatedNodes());
	}

	private void buildWeights()
	{
		if(prevLayer == null)
		{
			return;
		}

		if(!outputLayer)
		{
			biases = new double[nodes];
		}
		weights = new double[nodes][prevLayer.getNumNodes()];

		for(int i=0;i<weights.length;i++)
		{
			for(int j=0;j<weights[i].length;j++)
			{
				if(biases != null)
				{
					biases[i] = Rand.getRandNorm(0, Config.WEIGHT_INIT_STD_DEV);

				}
				weights[i][j] = Rand.getRandNorm(0, Config.WEIGHT_INIT_STD_DEV);
			}
		}
		weights = Maths.transpose(weights);
	}
	public void update(double[][] weightGrad, double[] biasGrad)
	{
		double eta = 10.0;
		weightGrad = Maths.transpose(weightGrad);
		for(int i=0;i<weightGrad.length;i++)
		{
			for(int j=0;j<weightGrad[i].length;j++)
			{
				weights[i][j] += eta*weightGrad[i][j];
			}
		}
		for(int i=0;i<biasGrad.length;i++)
		{
			biases[i] += eta*biasGrad[i];
		}
	}
	public void updateFromFlat(double[] flat, double decay)
	{
		int idx = 0;

		for(int i=0;i<weights.length;i++)
		{
			for(int j=0;j<weights[i].length;j++)
			{
				weights[i][j] += flat[idx++];
			}
		}
		if(outputLayer){return;}
		for(int i=0;i<biases.length;i++)
		{
			biases[i] += flat[idx++];
		}
	}
	public void setParamsFromFlat(double[] flat)
	{
		int idx = 0;
		for(int i=0;i<weights.length;i++)
		{
			for(int j=0;j<weights[i].length;j++)
			{
				weights[i][j] = flat[idx++];
			}
		}
		if(outputLayer)
		{
			return;
		}
		for(int i=0;i<biases.length;i++)
		{
			biases[i] = flat[idx++];
		}
	}
	public int getNumNodes()
	{
		return nodes;
	}

	public double[] getActivatedNodes()
	{
		return activatedNodes;
	}

	public double[] getNonActivatedNodes()
	{
		return nonActivatedNodes;
	}
	public double[][] getWeights()
	{
		return weights;
	}
	public double[] getFlat()
	{
		double[] flat = new double[getNumParams()];
		int idx = 0;
		for(int i=0;i<weights.length;i++)
		{
			for(int j=0;j<weights[i].length;j++)
			{
				flat[idx++] = weights[i][j];
			}
		}
		if(outputLayer)
		{
			return flat;
		}
		for(int i=0;i<biases.length;i++)
		{
			flat[idx++] = biases[i];
		}
		return flat;
	}
	public int getNumParams()
	{
		//System.out.println("LAYER HAS "+(weights[0].length*weights.length+" WEIGHTS & "+biases.length)+" BIASES");
		if(outputLayer)
		{
			return weights[0].length*weights.length;
		}
		return weights[0].length*weights.length + biases.length;
	}
}
