package NEAT.TestUnits;

import java.util.ArrayList;
import java.util.Random;

import NEAT.Genes.Connection;
import NEAT.Genes.Node;
import NEAT.Population.Genome;
import NEAT.Population.Organism;
import NEAT.util.InnovationTable;

import evolution.GameWorld;

public class FishTester extends TestUnit
{
	private GameWorld gameWorld;
	private boolean flip = true;
	private int simCount = 0;
	private double best = 0.0;
	private int TSLI = 0;
	public FishTester(Random rng, int w, int h)
	{
		super(rng,w,h);
		numBiasNodes = 1;
		numInputs = 18;
		numOutputs = 2;
		numHiddenNodes = 0;
		gameWorld = new GameWorld();
	}
	
	@Override
	public Genome buildMinimalStructure(InnovationTable table)
	{
		ArrayList<Connection> cons = new ArrayList<Connection>();
		ArrayList<Node> nodes = new ArrayList<Node>();
		ArrayList<Node> inputNodes = new ArrayList<Node>();
		ArrayList<Node> outputNodes = new ArrayList<Node>();
		ArrayList<Node> biasNodes = new ArrayList<Node>();
		Genome minimalGenome = null;
		
		for(int i=0;i<numBiasNodes;i++)
		{
			int id = table.createNode(-1, -1, Node.BIAS_NODE);
			Node n = new Node(0.25*i,0.0,Node.BIAS_NODE,id);
			biasNodes.add(n);
			nodes.add(n);
		}
		for(int i=0;i<numInputs;i++)
		{
			int id = table.createNode(-1, -1, Node.INPUT_NODE);
			Node n = new Node(biasNodes.get(numBiasNodes-1).getSplitX()+0.25*(i+1),0.0,Node.INPUT_NODE,id);
			inputNodes.add(n);
			nodes.add(n);
		}
		for(int i=0;i<numOutputs;i++)
		{
			int id = table.createNode(-1, -1, Node.OUTPUT_NODE);
			Node n = new Node(inputNodes.get(0).getSplitX()+0.25*i,1.0,Node.OUTPUT_NODE,id);
			outputNodes.add(n);
			nodes.add(n);
		}
		for(int i=0;i<numInputs;i++)
		{
			for(int j=0;j<numOutputs;j++)
			{
				int id = table.createConnection(inputNodes.get(i).getID(), outputNodes.get(j).getID());
				Connection c = new Connection(inputNodes.get(i),outputNodes.get(j),randf.nextGaussian(),true,id);
				cons.add(c);
			}
		}
		for(int i=0;i<numBiasNodes;i++)
		{
			for(int j=0;j<numOutputs;j++)
			{
				int id = table.createConnection(biasNodes.get(i).getID(), outputNodes.get(j).getID());
				Connection c = new Connection(biasNodes.get(i),outputNodes.get(j),randf.nextGaussian(),true,id);
				cons.add(c);
			}
		}
		minimalGenome = new Genome(cons,nodes,table,randf,numInputs,numOutputs);
		return minimalGenome;
	}
	
	@Override
	public Genome buildMinimalSolution(InnovationTable table)
	{
		ArrayList<Connection> cons = new ArrayList<Connection>();
		ArrayList<Node> nodes = new ArrayList<Node>();
		ArrayList<Node> inputNodes = new ArrayList<Node>();
		ArrayList<Node> outputNodes = new ArrayList<Node>();
		ArrayList<Node> biasNodes = new ArrayList<Node>();
		ArrayList<Node> hiddenNodes = new ArrayList<Node>();
		Random randf = new Random((long)(Math.random()*Long.MAX_VALUE));
		Genome minimalGenome = null;
		
		for(int i=0;i<numBiasNodes;i++)
		{
			int id = table.createNode(-1, -1, Node.BIAS_NODE);
			Node n = new Node(0.25*i,0.0,Node.BIAS_NODE,id);
			biasNodes.add(n);
			nodes.add(n);
		}
		for(int i=0;i<numInputs;i++)
		{
			int id = table.createNode(-1, -1, Node.INPUT_NODE);
			Node n = new Node(biasNodes.get(numBiasNodes-1).getSplitX()+0.25*(i+1),0.0,Node.INPUT_NODE,id);
			inputNodes.add(n);
			nodes.add(n);
		}
		for(int i=0;i<numOutputs;i++)
		{
			int id = table.createNode(-1, -1, Node.OUTPUT_NODE);
			Node n = new Node(inputNodes.get(0).getSplitX()+0.25*i,1.0,Node.OUTPUT_NODE,id);
			outputNodes.add(n);
			nodes.add(n);
		}
		
		int hiddenID = table.createNode(2, 4, Node.HIDDEN_NODE);
		Node node = new Node(inputNodes.get(1).getSplitX(),0.5,Node.HIDDEN_NODE,hiddenID);
		hiddenNodes.add(node);
		nodes.add(node);
		
		for(int i=0;i<numInputs;i++)
		{
			int id = table.createConnection(inputNodes.get(i).getID(), node.getID());
			Connection c = new Connection(inputNodes.get(i),node,randf.nextGaussian(),true,id);
			cons.add(c);
			for(int j=0;j<numOutputs;j++)
			{
				id = table.createConnection(inputNodes.get(i).getID(), outputNodes.get(j).getID());
				c = new Connection(inputNodes.get(i),outputNodes.get(j),randf.nextGaussian(),true,id);
				cons.add(c);
			}
		}
		for(int i=0;i<numBiasNodes;i++)
		{
			int id = table.createConnection(biasNodes.get(i).getID(), node.getID());
			Connection c = new Connection(biasNodes.get(i),node,randf.nextGaussian(),true,id);
			cons.add(c);
			for(int j=0;j<numOutputs;j++)
			{
				id = table.createConnection(biasNodes.get(i).getID(), outputNodes.get(j).getID());
				c = new Connection(biasNodes.get(i),outputNodes.get(j),randf.nextGaussian(),true,id);
				cons.add(c);
			}
		}
		for(int i=0;i<numOutputs;i++)
		{
			int id = table.createConnection(node.getID(),outputNodes.get(i).getID());
			Connection c = new Connection(node,outputNodes.get(i),randf.nextGaussian(),true,id);
			cons.add(c);
		}
		
		minimalGenome = new Genome(cons,nodes,table,randf,numInputs,numOutputs);
		return minimalGenome;
	}
	
	public Organism testPhenotypes(ArrayList<Organism> population)
	{
		for(Organism org : population)
		{
			org.createPhenotype(width/2,height/2);
			if(!validatePhenotype(org.getPhenotype()))
			{
				org.setPhenotype(null);
			}
		}
		
		gameWorld.buildPop(population);
		if(simCount++>10)
		{
			simCount=0;
			flip=!flip;
		}
		
		if(TSLI++>10) {gameWorld.simulate(1000);}
		else{gameWorld.run();}
		double[] fitnessList = gameWorld.getTestResults();
		
		for(int i=0;i<fitnessList.length;i++)
		{
			if(best<fitnessList[i]) {best=fitnessList[i]; TSLI = 0;}
			population.get(i).setFitness(fitnessList[i]);
		}
		return null;
	}
}
