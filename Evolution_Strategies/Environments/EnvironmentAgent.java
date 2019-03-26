package Evolution_Strategies.Environments;

import core.camera.Camera;
import core.entities.Entity;

public abstract class EnvironmentAgent extends Entity
{
    public EnvironmentAgent(double[] startPos, double startAngle, Camera cam)
    {
        super(startPos, startAngle, cam);
    }
    protected Environment env;
    public abstract double takeAction(int actionNum);
    public abstract double[][][] getState();
}
