package Evolution_Strategies.Optimizers;

import Evolution_Strategies.Configs.Config;

public class Adam extends Optimizer
{
    private double beta1, beta2, epsilon;
    private double[] m;
    private double[] v;
    private double stepSize;
    private int timeStep;
    
    public Adam(int numParams, double step)
    {
        stepSize = step;
        
        beta1 = Config.ADAM_BETA1_DEFAULT;
        beta2 = Config.ADAM_BETA2_DEFAULT;
        epsilon = Config.ADAM_EPSILON_DEFAULT;
        
        m = new double[numParams];
        v = new double[numParams];
        timeStep = 0;
    }
    
    public double[] computeUpdate(double[] theta, double[] gradient)
    {
        double[] step = getUpdateStep(gradient);
        
        for(int i=0;i<theta.length;i++)
        {
            step[i]+=theta[i];
        }
        return step;
    }
    
    @Override
    public double[] getUpdateStep(double[] gradient)
    {
        timeStep++;
        double a = stepSize*Math.sqrt(1-Math.pow(beta2,timeStep))/(1 - Math.pow(beta1, timeStep));
        for(int i=0;i<m.length;i++)
        {
            m[i] = beta1*m[i] + (1.0 - beta1)*gradient[i];
            v[i] = beta2*v[i] + (1.0 - beta2)*(gradient[i]*gradient[i]);
            
        }
        double[] step = new double[gradient.length];
        for(int i=0;i<step.length;i++)
        {
            step[i] = -a*m[i]/(Math.sqrt(v[i]) + epsilon);
        }
        
        return step;
    }

}
