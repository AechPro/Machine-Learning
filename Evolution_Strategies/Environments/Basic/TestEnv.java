package Evolution_Strategies.Environments.Basic;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import Evolution_Strategies.Environments.Environment;
import Evolution_Strategies.Environments.EnvironmentAgent;
import core.camera.Camera;
import core.phys.PhysicsObject;

public class TestEnv extends Environment
{

    private ArrayList<EnvironmentAgent> agents;
    public TestEnv(PhysicsObject p, Camera cam, double[] resScale)
    {
        super(p, cam, resScale);
    }

    @Override
    public void initEnv()
    {
        
    }

    @Override
    public ArrayList<EnvironmentAgent> buildAgents(int numAgents)
    {
        if(agents == null)
        {
            agents = new ArrayList<EnvironmentAgent>();
        }
        else
        {
            agents.clear();
        }
        double[] p = new double[] {0,0};
        for(int i=0;i<100;i++)
        {
            agents.add(new TestAgent(p,0,camera));
        }
        return agents;
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    @Override
    public void loadMap()
    {
    }

    @Override
    public void loadEntities()
    {
    }

    @Override
    public void levelUpdate()
    {
    }

    @Override
    public void levelRender(Graphics2D g, double delta)
    {
    }

    @Override
    public void takeStep()
    {
        // TODO Auto-generated method stub
        
    }

}
