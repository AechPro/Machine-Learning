package NEAT.Population;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import NEAT.Display.DisplayObject;
import NEAT.Genes.*;
import NEAT.util.SortingUnit;

public class Phenotype extends DisplayObject
{
	private ArrayList<Node> inputNodes;
	private ArrayList<Node> outputNodes;
	private ArrayList<Node> biasNodes;
	private ArrayList<Node> nodes;
	private SortingUnit sorter;
	private int depth;
	private int x,y;
	public Phenotype(ArrayList<Node> ns, int _x, int _y)
	{
		super();
		x = _x;
		y = _y;
		nodes = ns;
		depth = calculateDepth();
		init();
	}
	public Phenotype(ArrayList<Node> ns, int _x, int _y, int _renderPriority, int _updatePriority)
	{
		super(_renderPriority, _updatePriority);
		x = _x;
		y = _y;
		nodes = ns;
		depth = calculateDepth();
		init();
	}
	public void init()
	{
		sorter = new SortingUnit();
		separateNodes();
		sorter.sortNodes(nodes, 0, nodes.size()-1);
		sorter.sortNodes(inputNodes, 0, inputNodes.size()-1);
		sorter.sortNodes(outputNodes, 0, outputNodes.size()-1);
		sorter.sortNodes(biasNodes, 0, biasNodes.size()-1);
		//System.out.println("PHENOTYPE CREATED WITH\n"+nodes.size()+" TOTAL NODES\n"+inputNodes.size()+" INPUT NODES\n"
			//	+ biasNodes.size()+" BIAS NODES\n"+outputNodes.size()+" OUTPUT NODES");
	}
	public boolean activate(double[] input)
	{
		boolean activatedOnce = false;
		double sum = 0;
		int attempts = 0;
		boolean debugging = false;
		loadInputs(input);
		if(debugging) {System.out.println("Beginning activation on input "+input[0]+" | "+input[1]);}
		while(inactiveOutputs() || !activatedOnce)
		{
			if(attempts++>20) {if(debugging) {System.out.println("Returning false");} return false;}
			for(Node n : nodes)
			{
				if(n.getType() == Node.INPUT_NODE || n.getType() == Node.BIAS_NODE){continue;}
				sum = 0;
				n.setInactiveOutput(0d);
				n.setActive(false);
				if(debugging) {System.out.println("Operating on node "+n);}
				for(Connection c : n.getInputs())
				{
					if(debugging) {System.out.println("Propagating connection "+c);}
					if(c.getInput().isActive() || c.getInput().getType() == Node.INPUT_NODE || c.getInput().getType() == Node.BIAS_NODE)
					{
						n.setActive(true);
					}
					//System.out.println("CONNECTION INPUT: "+c.getInput());
				//	System.out.println("NODES CHECK: "+nodes.get(nodes.indexOf(c.getInput())));
					sum+=c.getWeight()*c.getInput().getActiveOutput();
					if(debugging) {System.out.println("SUM SO FAR: "+sum);}
				}
				n.setInactiveOutput(sum);
				if(debugging) {System.out.println("Fully propagated node "+n);}

			}
			if(debugging) {System.out.println("Activating propagated nodes");}
			for(Node n : nodes)
			{
				if(n.getType() != Node.INPUT_NODE && n.getType() != Node.BIAS_NODE && n.isActive())
				{
					n.setActiveOutput(sigmoid(n.getInactiveOutput(),n.getActivationResponse()));
					n.setActivationCount(n.getActivationCount()+1);
					if(debugging) {System.out.println("Activated node "+n);}
				}

			}
			if(debugging) {System.out.println("Loop end");}
			activatedOnce = true;
		}
		if(debugging) {System.out.println("Returning true");}
		return true;
	}
	public double[] readOutputVector()
	{
		double[] outputVector = new double[outputNodes.size()];
		for(int i=0,stop=outputNodes.size();i<stop;i++)
		{
				outputVector[i] = outputNodes.get(i).getActiveOutput();
		}
		return outputVector;
	}
	public void loadInputs(double[] inps)
	{
		//System.out.println("LOADING TO "+inputNodes.size()+" INPUT NODES");
		for(int i=0;i<inputNodes.size();i++)
		{	
			inputNodes.get(i).setActiveOutput(inps[i]);
			inputNodes.get(i).setActive(true);
			inputNodes.get(i).setActivationCount(1);
			//System.out.println("LOADED NODE "+inputNodes.get(i));
		}
		for(int i=0;i<biasNodes.size();i++)
		{
			biasNodes.get(i).setActiveOutput(1.0);
			biasNodes.get(i).setActive(true);
			biasNodes.get(i).setActivationCount(1);
		}
	}
	public boolean inactiveOutputs()
	{
		for(int i=0,stop=outputNodes.size();i<stop;i++)
		{
			if(outputNodes.get(i).getActivationCount() == 0) {return true;}
		}
		return false;
	}
	public void separateNodes()
	{
		inputNodes = new ArrayList<Node>();
		outputNodes = new ArrayList<Node>();
		biasNodes = new ArrayList<Node>();
		for(int i=0,stop=nodes.size();i<stop;i++)
		{
			if(nodes.get(i).getType() == Node.INPUT_NODE) {inputNodes.add(nodes.get(i));}
			if(nodes.get(i).getType() == Node.BIAS_NODE) {biasNodes.add(nodes.get(i));}
			if(nodes.get(i).getType() == Node.OUTPUT_NODE) {outputNodes.add(nodes.get(i));}
		}
	}
	public int calculateDepth()
	{
		ArrayList<Double> uniqueYVals = new ArrayList<Double>();
		for(Node n : nodes)
		{
			boolean contained = false;
			for(int i=0;i<uniqueYVals.size();i++)
			{
				if(uniqueYVals.get(i).doubleValue() == n.getSplitY()){contained = true;}
			}
			if(!contained)
			{
				uniqueYVals.add(n.getSplitY());
			}
		}
		return uniqueYVals.size()-1;
	}
	public void reset()
	{
		for(Node n : nodes)
		{
			n.setInactiveOutput(0d);
			n.setActivationCount(0);
			n.setActiveOutput(0d);
			n.setActive(false);
		}
	}
	public int getDepth() {return depth;}
	public ArrayList<Node> getNodes(){return nodes;}
	public ArrayList<Node> getInputNodes(){return inputNodes;}
	public ArrayList<Node> getOutputNodes(){return outputNodes;}
	public ArrayList<Node> getBiasNodes(){return biasNodes;}
	@Override
	public void update(double delta)
	{
	}
	@Override
	public void render(Graphics2D g) 
	{
		int r = 15;
		g.setColor(Color.WHITE);
		for(Node n : nodes)
		{
			for(Connection c : n.getOutputs())
			{
				int x1 = x + n.getX() + r/2;
				int y1 = y + n.getY() + r/2;
				int x2 = x + c.getOutput().getX()+r/2;
				int y2 = y + c.getOutput().getY()+r/2;
				g.drawLine(x1, y1, x2, y2);
			}
		}
		for(Node n : nodes)
		{
			switch(n.getType())
			{
				case(Node.INPUT_NODE):
					g.setColor(Color.GREEN);
					break;
				case(Node.OUTPUT_NODE):
					g.setColor(Color.RED);
					break;
				case(Node.BIAS_NODE):
					g.setColor(Color.WHITE);
					break;
				default:
					g.setColor(Color.YELLOW);
					break;
			}
			g.fillOval(x+n.getX(),y+n.getY(),r,r);
		}
	}
	public void saveAsImage(String dir, int w, int h)
	{
		BufferedImage im = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D graphics = (Graphics2D) im.getGraphics();
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0,0,w,h);
		int oldX = x;
		int oldY = y;
		x = 100;
		y = h - 30;
		render(graphics);
		graphics.dispose();
		try{ImageIO.write(im, "png", new File(dir));}
		catch(Exception e){e.printStackTrace();}
		x = oldX;
		y = oldY;
	}
	public double sigmoid(double x, double response)
	{
		//System.out.println("CALCULATING SIGMOID OF "+x+" OUTPUT = "+1.0d/(1.0d+(double)(Math.exp(-x/response))));
		return 1.0d/(1.0d+(double)(Math.exp(-x/response)));
	}
}
