package NEAT.Simulations.FlappySquares;

import java.util.ArrayList;

import javax.swing.JFrame;

import NEAT.Display.*;
import NEAT.Population.Organism;
import NEAT.Population.Phenotype;
import NEAT.Simulations.FishMaze.Workers.Fish;

public class GameWorld 
{
	private Window displayWindow;
	private JFrame windowFrame;
	private ArrayList<DisplayObject> displayObjects;
	private int windowWidth, windowHeight;
	
	private ArrayList<NotAPole> poles;
	private int gapSize;
	private int poleWidth;
	private int distBetweenPoles;
	
	private ArrayList<NotABird> birds;
	private double birdX;
	private double birdY;
	private int birdWidth;
	private int birdHeight;
	
	private int popSize;
	
	public GameWorld()
	{
	}
	public void init()
	{
		displayObjects = new ArrayList<DisplayObject>();
		poles = new ArrayList<NotAPole>();
		birds = new ArrayList<NotABird>();
		displayWindow = null;
		windowWidth = 1280;
		windowHeight = 720;
		
		poleWidth = 75;
		distBetweenPoles = 270;
		gapSize = 100;
		
		birdX = poleWidth;
		birdY = windowHeight/2;
		birdWidth = 20;
		birdHeight = 20;
		
		for(int i=0;i<10;i++)
		{
			NotAPole pole = new NotAPole(gapSize,poleWidth,windowHeight,windowWidth,windowWidth/4
					+ (poleWidth+distBetweenPoles)*i);
			displayObjects.add(pole);
			poles.add(pole);
		}
		
		for(int i=0;i<popSize;i++)
		{
			NotABird bird = new NotABird(birdX,birdY,birdWidth,birdHeight,windowWidth,windowHeight,poles);
			birds.add(bird);
			displayObjects.add(bird);
		}
	}
	public void run()
	{
		setupWindow();
		boolean running = true;
		NotAPole temp = null;
		
		try
		{
			while(running)
			{
				synchronized(displayObjects)
				{
					temp = getFurthestBack();
					for(NotAPole p : poles)
					{
						if(p.doesNeedReset()) 
						{
							p.reset(temp.getX() + poleWidth+distBetweenPoles);
						}
					}
					
					running = false;
					for(NotABird b : birds)
					{
						if(!b.isColliding())
						{
							running = true;
						}
					}
				}
			}	
			displayWindow.setRunning(false);
			displayWindow.getThread().join();
			windowFrame.dispose();
			windowFrame = null;
			displayWindow = null;
		}
		catch(Exception e) {e.printStackTrace();}
	}
	
	public void simulate()
	{
		
		boolean running = true;
		NotAPole temp = null;
		while(running)
		{
			synchronized(displayObjects)
			{
				temp = getFurthestBack();
				for(NotAPole p : poles)
				{
					p.update(1.0);
					if(p.doesNeedReset()) 
					{
						p.reset(temp.getX() + poleWidth+distBetweenPoles);
					}
				}
				
				running = false;
				for(NotABird b : birds)
				{
					b.update(1.0);
					if(!b.isColliding())
					{
						running = true;
					}
				}
			}
		}
	}
	
	public void reset()
	{
		for(NotABird b : birds)
		{
			b.setX(birdX);
			b.setY(birdY);
			b.setFitness(0);
			b.setColliding(false);
		}
		for(int i=0;i<poles.size();i++)
		{
			poles.get(i).reset(windowWidth/2+(poleWidth+distBetweenPoles)*i);
		}
	}
	
	public void buildPop(ArrayList<Organism> population)
	{
		popSize = population.size();
		init();
		for(int i=0,stop=population.size();i<stop;i++)
		{
			birds.get(i).setPhenotype(population.get(i));
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
	public void setupWindow()
	{
		System.out.println("Setting up window...");
		displayWindow = new Window(windowWidth,windowHeight,60,displayObjects);
		windowFrame = new JFrame("NEAT");
		windowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		windowFrame.setContentPane(displayWindow);
		windowFrame.setSize(windowWidth+20,windowHeight+50);
		windowFrame.setVisible(true);
	}
	
	//public static void main(String[] args) {new GameWorld();}
}
