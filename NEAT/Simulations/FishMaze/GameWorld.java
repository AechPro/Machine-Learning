package NEAT.Simulations.FishMaze;

import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JFrame;

import NEAT.Display.*;
import NEAT.Population.*;
import NEAT.Configs.Config;
import NEAT.Simulations.FishMaze.Workers.*;


public class GameWorld 
{
	private static final int windowWidth=1285, windowHeight=795;
	private boolean running;
	
	private GameBoard board;
	private ArrayList<Worker> workers; 
	private ArrayList<DisplayObject> displayObjects;
	private Window displayWindow;
	private JFrame windowFrame;
	private double[] startPos;
	private int popSize;
	
	public GameWorld()
	{
		popSize = Config.POPULATION_SIZE;
		init();
	}
	
	public void init()
	{
		displayObjects = new ArrayList<DisplayObject>();
		workers = new ArrayList<Worker>();
		
		board = new GameBoard(new int[]{32,32});
		board.create("resources/tileMap.txt");
		
		board.setRenderPriority(DisplayObject.RENDER_FIRST);
		displayObjects.add(board);
		
		Tile home = board.getHome();
		startPos = new double[]{home.getPosition()[0]+board.getTileSize()[0]/2-4,
				 				home.getPosition()[1]+board.getTileSize()[1]/2-4};
		double startAccel = 0.0;
		for(int i=0;i<popSize;i++)
		{
			Worker w = new Fish(startPos,Math.random()*Math.PI*2,startAccel,board.getDest().getPosition(),board);
			workers.add(w);
			displayObjects.add(w);
		}
	}
	public void reset()
	{
		Tile home = board.getHome();
		startPos = new double[]{home.getPosition()[0]+board.getTileSize()[0]/2-4,
				 				home.getPosition()[1]+board.getTileSize()[1]/2-4};
		for(Worker worker : workers)
		{
			worker.setVelocity(0);
			worker.setAngle(0);
			worker.setPosition(startPos);
		}
	}
	public void run(int numFrames)
	{
		setupWindow();
		try
		{
			while(displayWindow.getFramesSinceStart()<numFrames);
			displayWindow.setRunning(false);
			displayWindow.getThread().join();
			windowFrame.dispose();
			windowFrame = null;
			displayWindow = null;
		}
		catch(Exception e) {e.printStackTrace();}
		
	}
	public boolean winCondition()
	{
		boolean done = true;
		for(Worker w : workers)
		{
			if(((Fish)w).getTSLI()<120) {done=false;}
		}
		return done;
	}
	public void simulate(int iterations)
	{
		long t1 = 0;
		for(int i=0;i<iterations;i++)
		{
			for(Worker w : workers)
			{
				w.update(1.0);
			}
		}
	}
	public void buildPop(ArrayList<Organism> population)
	{
		popSize = population.size();
		init();
		for(int i=0,stop=population.size();i<stop;i++)
		{
			workers.get(i).setPhenotype(population.get(i));
		}
	}
	public double[] getTestResults()
	{
		double[] results = new double[popSize];
		for(int i=0;i<results.length;i++)
		{
			results[i] = workers.get(i).getFitness();
			if(((Fish)workers.get(i)).checkVictoryCondition()) {results[i] = 3000;}
		}
		return results;
	}
	public void setupWindow()
	{
		System.out.println("Setting up window...");
		displayWindow = new Window(windowWidth,windowHeight,600,displayObjects);
		windowFrame = new JFrame("NEAT");
		windowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		windowFrame.setContentPane(displayWindow);
		windowFrame.setSize(windowWidth,windowHeight);
		windowFrame.setVisible(true);
	}
}
