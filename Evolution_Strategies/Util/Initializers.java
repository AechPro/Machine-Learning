package Evolution_Strategies.Util;

public class Initializers 
{
	public static double[] init_random(int shape)
	{
		double[] weights = new double[shape];
		for(int i=0;i<shape;i++)
		{
			weights[i] = NoiseTable.noise[Rand.rand.nextInt(NoiseTable.noise.length-1)];
		}
		return weights;
	}
	
	public static double[] init_xavier(int shape, int incoming, int outgoing)
	{
		return null;
	}
	
	public static double[] init_kaining(int shape, int incoming)
	{
		return null;
	}
}
