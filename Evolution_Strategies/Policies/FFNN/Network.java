package Evolution_Strategies.Policies.FFNN;

import java.io.*;
import java.util.ArrayList;

import Evolution_Strategies.Configs.Config;
import Evolution_Strategies.Optimizers.*;
import Evolution_Strategies.Util.Activations;
import Evolution_Strategies.Util.Maths;

public class Network
{
    private Layer[] layers;
    private Optimizer opt;
    private double weightDecay;
    public Network(int inputSize, int[] layerSizes, int outputSize)
    {
        Layer inp = new Layer(inputSize,false,null);
        layers = new Layer[1+layerSizes.length+1];
        layers[0] = inp;

        for(int i=0;i<layerSizes.length;i++)
        {
            Layer layer = new Layer(layerSizes[i],false,layers[i]);
            layers[i+1] = layer;
        }
        Layer out = new Layer(outputSize,true,layers[layers.length-2]);
        layers[layers.length-1] = out;
        opt = new Adam(getNumParams(),Config.ADAM_STEP_SIZE_DEFAULT);
        //opt = new SGD(Config.SGD_STEP_SIZE_DEFAULT, Config.SGD_MOMENTUM_DEFAULT);
        weightDecay = 1.0 + Config.WEIGHT_DECAY_RATE;
    }

    public void activate(double[] input)
    {
        layers[0].activate(input);
        for(int i=1;i<layers.length;i++)
        {
            layers[i].activate();
        }
    }
    public void activateNoisy(double[] input, double[] noiseFlat, boolean negative)
    {
        layers[0].activate(input);
        int noiseOffset = 0;
        for(int i=1;i<layers.length;i++)
        {
            layers[i].activateNoisy(noiseFlat,noiseOffset,negative);
            noiseOffset += layers[i].getNumParams();
        }
    }

    public double[] readOutputVector()
    {
        double[] out = layers[layers.length-1].getNonActivatedNodes();
        //Activations.softmax(out);
        return out;
    }

    public ArrayList<ArrayList> backprop(double[] input, double[] label)
    {
        activate(input);
        ArrayList<double[]> biasErrs = new ArrayList<double[]>();
        ArrayList<double[][]> weightErrs = new ArrayList<double[][]>();

        int cur = layers.length-1;

        double[] costVec = Activations.getCostDerivative(layers[cur].getActivatedNodes(), label);
        double[] derivativeVec = layers[cur].getNonActivatedNodes();
        Activations.sigmoidPrime(derivativeVec);

        cur = layers.length-2;

        double[] delta = Maths.vecMulElem(costVec, derivativeVec);
        double[] activations = layers[cur].getActivatedNodes();
        double[][] weightGradient = new double[delta.length][activations.length];

        for(int i=0;i<delta.length;i++)
        {
            for(int j=0;j<activations.length;j++)
            {
                weightGradient[i][j] = delta[i]*activations[j];
            }
        }
        weightErrs.add(weightGradient.clone());
        biasErrs.add(delta.clone());

        for(int i=cur;i>1;i--)
        {
            derivativeVec = layers[i].getNonActivatedNodes();
            Activations.sigmoidPrime(derivativeVec);
            delta = Maths.matDotVec(Maths.transpose(layers[i+1].getWeights()), delta);
            delta = Maths.vecMulElem(delta,derivativeVec);
            activations = layers[i-1].getActivatedNodes();
            weightGradient = new double[delta.length][activations.length];
            for(int j=0;j<delta.length;j++)
            {
                for(int k=0;k<activations.length;k++)
                {
                    weightGradient[j][k] = delta[j]*activations[k];
                }
            }
            weightErrs.add(weightGradient.clone());
            biasErrs.add(delta.clone());
        }

        ArrayList<ArrayList> errs = new ArrayList<ArrayList>();
        errs.add(weightErrs);
        errs.add(biasErrs);
        return errs;
    }
    public void updateWeightsFromFlat(double[] flat, boolean useOptimizer)
    {
        int offset = 0;
        int numParams = 0;
        weightDecay += Config.WEIGHT_DECAY_RATE;

        if(useOptimizer)
        {
            flat = opt.getUpdateStep(flat);
        }

        double[] update = null;
        for(int i=1;i<layers.length;i++)
        {
            numParams = layers[i].getNumParams();
            update = new double[numParams];
            
            for(int j=0;j<numParams;j++)
            {
                update[j] = flat[offset + j];
            }
            offset += numParams;

            layers[i].updateFromFlat(update, weightDecay);
        }
    }
    public void updateWeights(ArrayList<double[][]> deltaW, ArrayList<double[]> deltaB)
    {
        int end = layers.length-1;
        for(int i=0;i<deltaW.size();i++)
        {
            layers[end-i].update(deltaW.get(i),deltaB.get(i));
        }
    }
    public void copyParams(Network other)
    {
        layers = other.getLayers().clone();
    }
    public Layer[] getLayers()
    {
        return layers;
    }
    public double[] getFlat()
    {
        double[] flat = new double[getNumParams()];
        int idx = 0;
        double[] layerFlat = null;
        for(int i=1;i<layers.length;i++)
        {
            layerFlat = layers[i].getFlat();
            for(int j=0;j<layerFlat.length;j++)
            {
                flat[idx++] = layerFlat[j];
            }
        }
        return flat;
    }
    public int getNumParams()
    {
        int num = 0;
        for(int i=1;i<layers.length;i++)
        {
            num+=layers[i].getNumParams();
        }
        return num;
    }
    public void setParamsFromFlat(double[] flat)
    {
        int offset = 0;
        int numParams = 0;

        double[] update = null;
        for(int i=1;i<layers.length;i++)
        {
            numParams = layers[i].getNumParams();
            update = new double[numParams];
            
            for(int j=0;j<numParams;j++)
            {
                update[j] = flat[offset + j];
            }
            offset += numParams;

            layers[i].setParamsFromFlat(update);
        }
    }
    public void saveParameters(String filePath)
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath)));
            double[] params = getFlat();
            for(int i=0;i<params.length;i++)
            {
                writer.write(params[i]+" ");
            }
            writer.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void loadParameters(String filePath)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
            String l = reader.readLine();
            String[] params = l.split(" ");
            double[] flat = new double[getNumParams()];
            for(int i=0;i<params.length;i++)
            {
                flat[i] = Double.parseDouble(params[i]);
            }
            setParamsFromFlat(flat);
            reader.close();
            
            //writer.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
