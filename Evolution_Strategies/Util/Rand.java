package Evolution_Strategies.Util;

import java.util.Random;

public class Rand
{
    public static final Random rand = new Random((long)(Math.random()*Long.MAX_VALUE));
    
    public static double getRandNorm(double mean, double std)
    {
        return std*rand.nextGaussian()+mean;
    }
}
