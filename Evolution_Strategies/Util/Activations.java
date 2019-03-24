package Evolution_Strategies.Util;

public class Activations
{
    public static double sigmoid(double x)
    {
        return (1d / (1d + Math.exp(-x)));
    }
    
    public static void sigmoid(double[] x)
    {
        for(int i=0;i<x.length;i++)
        {
            x[i] = sigmoid(x[i]);
        }
    }
    
    public static double sigmoidPrime(double x)
    {
        return sigmoid(x)*(1.0-sigmoid(x));
    }
    
    public static void sigmoidPrime(double[] x)
    {
        for(int i=0;i<x.length;i++)
        {
            x[i] = sigmoidPrime(x[i]);
        }
    }
    
    public static double tanh(double x)
    {
    	return Math.tanh(x);
    }
    
    public static void tanh(double[] x)
    {
    	for(int i=0;i<x.length;i++)
        {
            x[i] = tanh(x[i]);
        }
    }
    
    public static void softmax(double[] inp)
    {
        double sum = 0;
        for(int i=0;i<inp.length;i++)
        {
            sum+=Math.exp(inp[i]);
            
        }
        for(int i=0;i<inp.length;i++)
        {
            inp[i] = Math.exp(inp[i])/sum;
        }
    }
    
    public static double[] getCostDerivative(double[] x, double[] y)
    {
        double[] out = new double[x.length];
        for(int i=0;i<x.length;i++)
        {
            out[i] = y[i] - x[i];
        }
        return out;
    }
}
