package Evolution_Strategies.Policies.FFNN;

import Evolution_Strategies.Configs.Config;
import Evolution_Strategies.Util.Activations;
import Evolution_Strategies.Util.Initializers;
import Evolution_Strategies.Util.Rand;

public class Layer
{
    private int numNodes;
    private Layer prevLayer;
    private double[][] weights;
    private double[] biases;
    private int numParams;

    public Layer(int _numNodes,Layer prev)
    {
        numNodes = _numNodes;
        prevLayer = prev;
        numParams = 0;
        initParams();
    }
    
    private void initParams()
    {
        if(prevLayer == null) {return;}
        
        int prevNodes = prevLayer.getNumNodes();
        weights = new double[prevNodes][numNodes];
        biases = new double[numNodes];
        numParams = prevNodes*numNodes + numNodes;
        for(int i=0;i<prevNodes;i++)
        {
            for(int j=0;j<numNodes;j++)
            {
                weights[i][j] = Rand.getRandNorm(0, Config.WEIGHT_INIT_STD);
            }
        }
        for(int i=0;i<numNodes;i++)
        {
            biases[i] = Rand.getRandNorm(0, Config.WEIGHT_INIT_STD);
        }
        
        
    }

    public double[] activateNoisy(double[] input, double[] noiseFlat, boolean softmax)
    {
       int noiseIdx = 0;
       double[] activated = new double[numNodes];
       //prev nodes
       for(int i=0;i<weights.length;i++)
       {
           //our nodes
           for(int j=0;j<weights[0].length;j++)
           {
               activated[j] += (weights[i][j] + noiseFlat[noiseIdx++])*input[i];
           }
       }
       
       for(int i=0;i<numNodes;i++)
       {
           activated[i] = activated[i] + biases[i] + noiseFlat[noiseIdx++];
       }
       
       
       if(softmax)
       {
           Activations.softmax(activated);
       }
       else
       {
           Activations.tanh(activated);
       }
       
       //Activations.tanh(activated);
       for(int i=0;i<numNodes;i++)
       {
    	   activated[i] += Rand.getRandNorm(0, Config.ACTION_NOISE_STD);
       }
       
       return activated;
    }
    public void activate(double[] input)
    {
       
    }
    
    public void setFromFlat(double[] flat)
    {
        int idx = 0;
        for(int i=0;i<weights.length;i++)
        {
            for(int j=0;j<weights[0].length;j++)
            {
                weights[i][j] = flat[idx++];
            }
        }
        for(int i=0;i<numNodes;i++)
        {
            biases[i] = flat[idx++];
        }
    }
    
    public int getNumNodes()
    {
        return numNodes;
    }

    public double[][] getWeights()
    {
        return weights;
    }
    public double[] getFlat()
    {
        int idx = 0;
        double[] flat = new double[numParams];
        for(int i=0;i<weights.length;i++)
        {
            for(int j=0;j<weights[0].length;j++)
            {
                flat[idx++] = weights[i][j];
            }
        }
        for(int i=0;i<numNodes;i++)
        {
            flat[idx++] = biases[i];
        }
        return flat;
    }
    public int getNumParams()
    {
        return numParams;
    }
}
