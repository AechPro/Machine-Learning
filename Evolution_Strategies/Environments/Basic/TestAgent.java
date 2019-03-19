package Evolution_Strategies.Environments.Basic;

import java.awt.Graphics2D;

import Evolution_Strategies.Environments.EnvironmentAgent;
import core.camera.Camera;

public class TestAgent extends EnvironmentAgent
{
    public TestAgent(double[] startPos, double startAngle, Camera cam)
    {
        super(startPos, startAngle, cam);
    }

    @Override
    public double takeAction(int actionNum)
    {
        if(actionNum == 0)
        {
            position[0]--;
        }
        else
        {
            position[0]++;
        }
        
        if(position[0] >= 1000)
        {
            return 1;
        }
        else if(position[0] <= -1000)
        {
            return -1;
        }
        return 0;
    }

    @Override
    public double[] getState()
    {
        double[] state = new double[] {position[0]};
        if(position[0] <= -1000)
        {
            return null;
        }
        return state;
    }

    @Override
    public void init()
    {
        
    }

    @Override
    public void eUpdate()
    {
        
    }

    @Override
    public void eRender(Graphics2D g, double interp)
    {
        
    }
    
}
