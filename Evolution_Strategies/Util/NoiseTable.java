package Evolution_Strategies.Util;

import Evolution_Strategies.Configs.Config;

public class NoiseTable
{
    public static final double[] noise = GENERATE_NOISE();
    private static final double[] GENERATE_NOISE()
    {
        double[] noise = new double[125000000]; //1 gigabyte of 64-bit doubles.
        //double[] noise = new double[6000]; //1 gigabyte of 64-bit doubles.
        for(int i=0;i<noise.length;i++)
        {
            noise[i] = Rand.getRandNorm(0,1.0);
        }
        System.out.println("noise generated");
        return noise;
    }
}
