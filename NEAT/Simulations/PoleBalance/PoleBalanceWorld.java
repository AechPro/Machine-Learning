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
	private int numTrials;
	private int popSize;
	
	public PoleBalanceWorld(int nt)
	{
		popSize = Config.POPULATION_SIZE;
		numTrials = nt;
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
			c.setNumResets(numTrials);
			displayObjects.add(c);
		}
	}
	
	public void run()
	{
		int[] xList = new int[10];
		int[] yList = new int[xList.length];
		int[] bestIndices = new int[xList.length];
		double bFitness = 0;
		for(int i=0;i<xList.length;i++)
		{
			xList[i] = 1280/2 - 100 + (25*6*i);
			yList[i] = 0;
		}
		for(int i=0;i<carts.size();i++) 
		{
			carts.get(i).reset(); 
			carts.get(i).resetFitness();
			if(carts.get(i).getPhenotype() == null) {continue;}
			carts.get(i).setRenderPhenotype(false);
			carts.get(i).getPhenotype().setY(720);
		}
		setupWindow();
		boolean done = false;
		//System.out.println("running");
		try
		{
			while(!done)
			{
			    //System.out.println("while loop");
				synchronized(displayObjects)
				{
				    //System.out.println("sync");
					bFitness = 0;
					done = true;
					for(int i=0;i<carts.size();i++)
					{
					    //System.out.println("carts");
						carts.get(i).setRenderPhenotype(false);
						if(!carts.get(i).isDone()) 
						{
							if(carts.get(i).getFitness() > bFitness+10) 
							{
								bFitness=carts.get(i).getFitness();
								carts.get(i).setRenderPhenotype(true);
								carts.get(i).getPhenotype().setX(xList[0]);
							}
							done=false;
						}
					}
					//System.out.println("release sync");
				}
				//System.out.println("sleep");
				Thread.sleep(100);
			}
			//System.out.println("joining with display window");
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
		for(int j=0;j<carts.size();j++) {carts.get(j).reset();}
		boolean done = false;
		while(!done)
		{
			done = true;
			for(int j=0;j<carts.size();j++)
			{
				carts.get(j).update(1.0);
				if(!carts.get(j).isDone()) {done=false;}
			}
		}
	}
	public void buildPop(ArrayList<Organism> population)
	{
		popSize = population.size();
		init();
		for(int i=0,stop=population.size();i<stop;i++)
		{
			carts.get(i).setPhenotype(population.get(i).getPhenotype());
			carts.get(i).setColor(population.get(i).getColorMarker());
			//workers.get(i).setPhenotype(population.get(i));
		}
	}
	public double[] getTestResults()
	{
		double[] results = new double[popSize];
		for(int i=0;i<results.length;i++)
		{
			results[i] = carts.get(i).getFitness();
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
