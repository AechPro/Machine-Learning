package Evolution_Strategies.Environments.Flappy.Environment;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JFrame;

import Evolution_Strategies.Environments.Environment;
import Evolution_Strategies.Environments.EnvironmentAgent;
import Evolution_Strategies.Environments.Flappy.Agent.NotABird;
import NEAT.Display.Window;
import core.camera.Camera;
import core.entities.*;
import core.map.TileMap;
import core.phys.PhysicsObject;


public class GameWorld extends Environment
{
	private int windowWidth, windowHeight;
	private Window window;
	
	private ArrayList<NotAPole> poles;
	private int gapSize;
	private int poleWidth;
	private int distBetweenPoles;
	
	private ArrayList<NotABird> birds;
	private double birdX;
	private double birdY;
	private ArrayList<EnvironmentAgent> agents;
	
	private int popSize;
	public GameWorld(PhysicsObject p, Camera cam, double[] resScale)
    {
        super(p, cam, resScale);
    }
	public void init()
	{
		windowWidth = 1280;
		windowHeight = 512;
		
		poleWidth = 50;
		distBetweenPoles = 270;
		gapSize = 100;
		
		birdX = poleWidth;
		birdY = windowHeight/2;
		window = new Window(1280,720,60,this);
	}
	@Override
    public void loadMap()
    {
	    map = new TileMap(camera);
        map.loadMap("resources/snake_level.txt");
    }
    @Override
    public void loadEntities()
    {
        entities = new ArrayList<Entity>();
        poles = new ArrayList<NotAPole>();
        birds = new ArrayList<NotABird>();
        agents = new ArrayList<EnvironmentAgent>();
        double[] startPos = new double[] {0,0};
        for(int i=0;i<10;i++)
        {
            startPos = new double[] {windowWidth/4 + (poleWidth+distBetweenPoles)*i,0};

            NotAPole pole = new NotAPole(startPos, 0, camera, gapSize, windowHeight - 60);
            entities.add(pole);
            poles.add(pole);
        }
        
        for(int i=0;i<popSize;i++)
        {
            startPos = new double[] {birdX,birdY};
            NotABird bird = new NotABird(startPos,0,camera,windowWidth,windowHeight,poles);
            birds.add(bird);
            agents.add(bird);
            entities.add(bird);
        }
    }
    @Override
    public void takeStep()
    {
    	//System.out.println("ENVIRONMENT UPDATING");
        update();
    }
	
	@Override
    public void levelUpdate()
    {
        NotAPole temp = getFurthestBack();
        for(NotAPole p : poles)
        {
            if(p.doesNeedReset()) 
            {
                p.reset(temp.getX() + poleWidth+distBetweenPoles);
            }
        }
    }
	
	@Override
    public void initEnv()
    {
		loadEntities();
	    reset();
	    /*window.buildWindow();
	    while(!isDone());
	    window.setRunning(false);
        window.destroy();*/
    }
	
    @Override
    public ArrayList<EnvironmentAgent> buildAgents(int numAgents)
    {
        double[] startPos = new double[] {0,0};
        if(popSize < numAgents)
        {
        	for(int i=0;i<numAgents-popSize;i++)
            {
                startPos = new double[] {birdX,birdY};
                NotABird bird = new NotABird(startPos,0,camera,windowWidth,windowHeight,poles);
                birds.add(bird);
                agents.add(bird);
                entities.add(bird);
            }
        	initCollisionHandler();
        }
        popSize = numAgents;
        //System.out.println("building agents");
        //loadEntities();
        reset();
        return agents;
    }
	
	public void reset()
	{
		for(NotABird b : birds)
		{
			b.setX(birdX);
			b.setY(birdY);
			b.setFitness(0);
			b.setColliding(false);
			b.init();
		}
		for(int i=0;i<poles.size();i++)
		{
			poles.get(i).reset(windowWidth/2+(poleWidth+distBetweenPoles)*i);
		}
	}
	
	public double[] getTestResults()
	{
		double[] results = new double[popSize];
		for(int i=0;i<results.length;i++)
		{
			results[i] = birds.get(i).getFitness();
		}
		return results;
	}
	
	public NotAPole getFurthestBack()
	{
		NotAPole temp = poles.get(0);
		for(NotAPole p : poles)
		{
			if(p.getX() > temp.getX()) {temp = p;}
		}
		return temp;
		
	}
	public boolean isDone()
	{
	    for(NotABird b : birds)
        {
            if(!b.isColliding())
            {
                return false;
            }
        }
	    return true;
	}
    
    @Override
    public void keyPressed(KeyEvent e)
    {
        for(NotABird b : birds)
        {
            b.jump();
        }
        
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
    public void levelRender(Graphics2D g, double delta)
    {
        
    }
    
	
	//public static void main(String[] args) {new GameWorld();}
}
