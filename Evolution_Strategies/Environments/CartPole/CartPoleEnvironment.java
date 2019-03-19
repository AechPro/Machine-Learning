package Evolution_Strategies.Environments.CartPole;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import Evolution_Strategies.Environments.Environment;
import Evolution_Strategies.Environments.EnvironmentAgent;
import core.camera.Camera;
import core.phys.PhysicsObject;

public class CartPoleEnvironment extends Environment
{
    private ArrayList<EnvironmentAgent> agents;
    private ArrayList<Cart> carts;
    
    public CartPoleEnvironment(PhysicsObject p, Camera cam, double[] resScale)
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
        if(carts == null)
        {
            carts = new ArrayList<Cart>();
        }
        else
        {
            carts.clear();
        }
        if(agents == null)
        {
            agents = new ArrayList<EnvironmentAgent>();
        }
        else
        {
            agents.clear();
        }
        entities.clear();
        for(int i=0;i<numAgents;i++)
        {
            double[] start = new double[2];
            Cart cart = new Cart(start,0,camera);
            agents.add(cart);
            carts.add(cart);
            entities.add(cart);
        }
        initCollisionHandler();
        
        return agents;
    }

    @Override
    public void takeStep()
    {
        update();
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void loadMap()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void loadEntities()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void levelUpdate()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void levelRender(Graphics2D g, double delta)
    {
        // TODO Auto-generated method stub
        
    }

}
