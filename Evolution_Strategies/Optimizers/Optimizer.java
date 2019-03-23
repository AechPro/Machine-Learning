package Evolution_Strategies.Optimizers;

public abstract class Optimizer
{
    public abstract double[] getUpdateStep(double[] gradient);
    public abstract double[] computeUpdate(double[] flat, double[] gradient);
}
