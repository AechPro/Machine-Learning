package NEAT.Simulations.PoleBalance;

import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JFrame;

import NEAT.Display.*;
import NEAT.Population.*;
import NEAT.Configs.Config;
public class PoleBalanceWorld
{
	private static final int windowWidth=1285, windowHeight=795;
	private boolean running;
	
	private ArrayList<DisplayObject> displayObjects;
	private ArrayList<Cart> carts;
	private Window displayWindow;
	private JFrame windowFrame;
	private double[] startPos;
	private int popSize;
	
	public PoleBalanceWorld()
	{
		popSize = Config.POPULATION_SIZE;
		init();
	}
	
	public void init()
	{
		displayObjects = new ArrayList<DisplayObject>();
		carts = new ArrayList<Cart>();
		for(int i=0;i<popSize;i++)
		{
			carts.add(new Cart(100,30,10000));
		}
		for(Cart c : carts)
		{
			displayObjects.add(c);
		}
	}
	
	public void run()
	{
		for(int i=0;i<carts.size();i++) {carts.get(i).reset();}
		setupWindow();
		boolean done = false;
		while(!done)
		{
			done = true;
			for(int i=0;i<carts.size();i++)
			{
				if(!carts.get(i).isDone()) {done=false;}
			}
		}
		try
		{
			displayWindow.setRunning(false);
			displayWindow.getThread().join();
			windowFrame.dispose();
			windowFrame = null;
			displayWindow = null;
		}
		catch(Exception e) {e.printStackTrace();}
		
	}
	public void simulate(int iterations)
	{
		for(int i=0;i<iterations;i++)
		{
			//update simulation agents
		}
	}
	public void buildPop(ArrayList<Organism> population)
	{
		popSize = population.size();
		init();
		for(int i=0,stop=population.size();i<stop;i++)
		{
			carts.get(i).setPhenotype(population.get(i).getPhenotype());
			//workers.get(i).setPhenotype(population.get(i));
		}
	}
	public double[] getTestResults()
	{
		double[] results = new double[popSize];
		for(int i=0;i<results.length;i++)
		{
			results[i] = carts.get(i).getSteps();
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
