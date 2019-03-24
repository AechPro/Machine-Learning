package Evolution_Strategies.Policies.FFNN;

import Evolution_Strategies.Configs.Config;
import java.io.*;

public class Network
{
    private int numInputs;
    private int numOutputs;
    private int[] hiddenLayers;
    private Layer[] layers;
    public Network(int[] layerData)
    {
        numInputs = layerData[0];
        numOutputs = layerData[layerData.length-1];
        hiddenLayers = new int[layerData.length-2];
        
        for(int i=1;i<layerData.length-1;i++)
        {
            hiddenLayers[i-1] = layerData[i];
        }
        layers = new Layer[layerData.length];
        
        buildLayers();
    }
    private void buildLayers()
    {
        layers[0] = new Layer(numInputs,null);
        for(int i=0;i<hiddenLayers.length;i++)
        {
            layers[i+1] = new Layer(hiddenLayers[i],layers[i]);
        }
        layers[layers.length-1] = new Layer(numOutputs,layers[layers.length-2]);
        
    }
    public double[] activateNoisy(double[] input, double[] noise)
    {
        int offset = 0;
        double[] inp = input.clone();
        double[] out = null;
        for(int i=1;i<layers.length;i++)
        {
            int size = layers[i].getNumParams();
            double[] noiseFlat = new double[size];
            for(int j=0;j<size;j++)
            {
                noiseFlat[j] = noise[offset + j]*Config.NOISE_STD_DEV;
            }
            offset+=size;
            
            if(i == layers.length-1)
            {
                out = layers[i].activateNoisy(inp, noiseFlat, true);
            }
            else
            {
                inp = layers[i].activateNoisy(inp, noiseFlat, false);
            }
        }
        return out;
    }
    public void setFlat(double[] flat)
    {
        int offset = 0;
        for(int i=1;i<layers.length;i++)
        {
            int size = layers[i].getNumParams();
            double[] layerFlat = new double[size];
            for(int j=0;j<size;j++)
            {
                layerFlat[j] = flat[offset+j];
            }
            offset+=size;
            
            layers[i].setFromFlat(layerFlat);
        }
    }
    public double[] getFlat()
    {
        int size = 0;
        for(int i=1;i<layers.length;i++)
        {
            size+=layers[i].getNumParams();
        }
        double[] flat = new double[size];
        
        int offset = 0;
        for(int i=1;i<layers.length;i++)
        {
            size = layers[i].getNumParams();
            double[] layer = layers[i].getFlat();
            for(int j=0;j<size;j++)
            {
                flat[offset + j] = layer[j];
            }
            offset += size;
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
    public void saveParameters(String filePath)
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath)));
            double[] flat = getFlat();
            String out = "";
            for(int i=0;i<flat.length;i++)
            {
                out+=flat[i]+",";
            }
            writer.write(out);
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
            double[] flat = new double[getNumParams()];
            String line = reader.readLine();
            String[] params = line.split(",");
            for(int i=0;i<flat.length;i++)
            {
            	flat[i] = Double.parseDouble(params[i]);
            }
            setFlat(flat);
            reader.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
