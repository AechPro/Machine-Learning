package Evolution_Strategies.Optimizers;

public class SGD extends Optimizer
{
    private double stepSize;
    private double momentum;
    private double[] flat;
    public SGD(double step, double mm)
    {
        stepSize = step;
        momentum = mm;
    }
    public double[] getUpdateStep(double[] gradient)
    {
        if(flat == null)
        {
            flat = new double[gradient.length];
        }
        double[] step = new double[flat.length];

        for(int i=0;i<flat.length;i++)
        {
            flat[i] = flat[i]*momentum + (1.0 - momentum)*gradient[i];
            step[i] = -stepSize*flat[i];
            //step[i] = gradient[i]*stepSize;
        }
        //stepSize/=1.02;
        return step;
    }
}
