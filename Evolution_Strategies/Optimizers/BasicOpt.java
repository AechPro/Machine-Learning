package Evolution_Strategies.Optimizers;

import Evolution_Strategies.Configs.Config;

public class BasicOpt extends Optimizer
{
    private double lr;
    private double decay;
    public BasicOpt()
    {
        lr = Config.SGD_STEP_SIZE_DEFAULT;
        decay = Config.WEIGHT_DECAY_RATE;
    }
    public double[] computeUpdate(double[] flat, double[] gradient)
    {
        double[] update = getUpdateStep(gradient);
        for(int i=0;i<flat.length;i++)
        {
            update[i] += flat[i];
        }
        return update;
    }
    @Override
    public double[] getUpdateStep(double[] gradient)
    {
        double coeff = lr/(Config.NOISE_STD_DEV*Config.POPULATION_SIZE);
        lr *= decay;
        double[] update = new double[gradient.length];
        for(int i=0;i<update.length;i++)
        {
            update[i] = gradient[i]*coeff;
        }
        return update;
    }

}
