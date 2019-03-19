package Evolution_Strategies.Environments;

import java.util.ArrayList;

import core.camera.Camera;
import core.level.Level;
import core.phys.PhysicsObject;

public abstract class Environment extends Level
{
    public Environment(PhysicsObject p, Camera cam, double[] resScale)
    {
        super(p, cam, resScale);
    }
    public abstract void initEnv();
    public abstract ArrayList<EnvironmentAgent> buildAgents(int numAgents);
    public abstract void takeStep();
}
