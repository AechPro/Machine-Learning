package Evolution_Strategies.Policies.CNN;

import Evolution_Strategies.Configs.Config;
import Evolution_Strategies.Policies.FFNN.FFNetwork;

public class CNetwork
{
    Layer[] layers;
    private FFNetwork net;
    private int numParams;
    public CNetwork()
    {
        numParams = 0;
        initLayers();
        initNet();
        computeNumParams();
    }
    public void initLayers()
    {
        int[][] layerInfo = Config.CONV_LAYER_INFO;
        layers = new Layer[layerInfo.length];

        for(int i=0;i<layerInfo.length;i++)
        {
            int[] kernel = new int[] {layerInfo[i][1], layerInfo[i][2]};
            int[] step = new int[]{layerInfo[i][3], layerInfo[i][4]};
            layers[i] = new Layer(layerInfo[i][0], kernel, step);
        }
    }
    
    public void initNet()
    {
        double[][][] input = new double[Config.NUM_IMAGE_COLOR_CHANNELS][Config.IMAGE_DIMS[0]][Config.IMAGE_DIMS[1]];
        for(int i=0;i<layers.length;i++)
        {
            double[] noise = new double[layers[i].getNumParams()];
            input = layers[i].activate(input, noise);
        }
        double[] flat = flattenTensor(input);
        System.out.println("BUILDING FFNN WITH PARAMS: "+flat.length+",64,64,4");
        
        int[] layerData = new int[] {flat.length, Config.FFNN_LAYER_INFO[1], Config.FFNN_LAYER_INFO[2], Config.FFNN_LAYER_INFO[3]};
        net = new FFNetwork(layerData);
    }
    
    public double[] activate(double[][][] input, double[] noise)
    {
        int idx = 0;
        double[][][] inp = input.clone();
        for(int i=0;i<layers.length;i++)
        {
            double[] noiseFlat = new double[layers[i].getNumParams()];
            for(int j=0;j<noiseFlat.length;j++)
            {
                noiseFlat[j] = noise[idx++];
            }
            inp = layers[i].activate(input, noiseFlat);
        }
        
        double[] flat = flattenTensor(inp);
        double[] netNoise = new double[net.getNumParams()];
        for(int i=0;i<netNoise.length;i++)
        {
            netNoise[i] = noise[idx++];
        }
        return net.activateNoisy(flat, netNoise);
    }
    public double[] flattenTensor(double[][][] tensor)
    {
        double[] flat = new double[tensor.length*tensor[0].length*tensor[0][0].length];
        int idx = 0;
        for(int i=0;i<tensor.length;i++)
        {
            for(int j=0;j<tensor[i].length;j++)
            {
                for(int k=0;k<tensor[i][j].length;k++)
                {
                    flat[idx++] = tensor[i][j][k];
                }
            }
        }
        return flat;
    }
    public double[] getFlat()
    {
        if(numParams == 0) {computeNumParams();}
        double[] flat = new double[numParams];
        int idx = 0;
        for(int i=0;i<layers.length;i++)
        {
            double[] layerFlat = layers[i].getFlat();
            for(int j=0;j<layerFlat.length;j++)
            {
                flat[idx++] = layerFlat[j];
            }
        }
        double[] netFlat = net.getFlat();
        for(int i=0;i<netFlat.length;i++)
        {
            flat[idx++] = netFlat[i];
        }
        return flat;
    }
    public void setFlat(double[] flat)
    {
        int idx = 0;
        for(int i=0;i<layers.length;i++)
        {
            double[] layerFlat = new double[layers[i].getNumParams()];
            for(int j=0;j<layerFlat.length;j++)
            {
                layerFlat[j] = flat[idx++];
            }
            layers[i].setFlat(layerFlat);
        }
        
        double[] netFlat = new double[net.getNumParams()];
        for(int i=0;i<netFlat.length;i++)
        {
            netFlat[i] = flat[idx++];
        }
        net.setFlat(netFlat);
    }
    public int getNumParams()
    {
        if(numParams == 0) {computeNumParams();}
        return numParams;
    }
    private void computeNumParams()
    {
        numParams = 0;
        for(int i=0;i<layers.length;i++)
        {
            numParams+=layers[i].getNumParams();
        }
        numParams += net.getNumParams();
    }
}
