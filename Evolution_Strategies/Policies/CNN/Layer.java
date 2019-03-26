package Evolution_Strategies.Policies.CNN;

import Evolution_Strategies.Configs.Config;
import Evolution_Strategies.Util.NoiseTable;
import Evolution_Strategies.Util.Rand;

public class Layer
{
    private FeatureFilter[] filters;
    private double[][][] outputVolume;
    private int[] kernel;
    private int numParams;
    public Layer(int numFilters, int[] _kernel, int[] _step)
    {
        filters = new FeatureFilter[numFilters];
        for(int i=0;i<numFilters;i++)
        {
            filters[i] = new FeatureFilter(_kernel, _step, Rand.rand.nextInt(NoiseTable.noise.length-1));
        }
        kernel = _kernel.clone();
        computeNumParams();
    }
    
    public double[][][] activate(double[][][] input, double[] noiseFlat)
    {
        int[] outputShape = filters[0].getOutputShape(new int[] {input[0].length, input[0][0].length});
        outputVolume = new double[filters.length*Config.NUM_IMAGE_COLOR_CHANNELS][outputShape[0]][outputShape[1]];
        int noiseIdx = 0;
        int filterIdx = 0;
        for(int i=0;i<filters.length;i++)
        {
            double[] noise = new double[filters[i].getNumParams()];
            for(int j=0;j<noise.length;j++)
            {
                noise[j] = noiseFlat[noiseIdx++];
            }
            
            double[][][] filterOutput = filters[i].convolveTensor(input, noise);
            for(int channel=0; channel < Config.NUM_IMAGE_COLOR_CHANNELS; channel++)
            {
                for(int x = 0; x < filterOutput[channel].length;x++)
                {
                    for(int y = 0; y < filterOutput[channel][x].length;y++)
                    {
                        //System.out.println(filterOutput[channel][x].length+","+kernel[1]);

                        outputVolume[filterIdx][x][y] = filterOutput[channel][x][y];
                    }
                }
                filterIdx++;
            }
        }
        return outputVolume;
    }
    
    public double[] getFlat()
    {
        if(numParams == 0){computeNumParams();}
        double[] flat = new double[numParams];
        
        int idx = 0;
        for(int i=0;i<filters.length;i++)
        {
            double[] filterFlat = filters[i].getFlat();
            for(int j=0;j<filterFlat.length;j++)
            {
                flat[idx++] = filterFlat[j];
            }
        }
        return flat;
    }
    public void setFlat(double[] flat)
    {
        int idx = 0;
        for(int i=0;i<filters.length;i++)
        {
            double[] filterFlat = new double[filters[i].getNumParams()];
            for(int j=0;j<filterFlat.length;j++)
            {
                filterFlat[j] = flat[idx++];
            }
            filters[i].setFlat(filterFlat);
        }
    }
    public int getNumParams()
    {
        if(numParams == 0){computeNumParams();}
        return numParams;
    }
    private void computeNumParams()
    {
        numParams = 0;
        for(int i=0;i<filters.length;i++)
        {
           numParams += filters[i].getNumParams();
        }
    }
}
