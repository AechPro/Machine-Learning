package NEAT.TestUnits;

import java.util.ArrayList;
import java.util.Random;

import NEAT.Genes.Connection;
import NEAT.Genes.Node;
import NEAT.Population.Genome;
import NEAT.Population.Organism;
import NEAT.Population.Phenotype;
import NEAT.Simulations.PoleBalance.PoleBalanceWorld;
import NEAT.util.InnovationTable;

public class PoleTester extends TestUnit
{
	private PoleBalanceWorld gameWorld;
	private double bestFitness;
	private int age;
	private int numTrials;
	public PoleTester(Random rng, int windowWidth, int windowHeight) 
	{
		super(rng, windowWidth, windowHeight);
		numInputs = 3;
		numOutputs = 2;
		numHiddenNodes = 0;
		numBiasNodes = 1;
		bestFitness = 0;
		numTrials = 1;
		gameWorld = new PoleBalanceWorld(numTrials);
	}

	@Override
	public Genome buildMinimalStructure(InnovationTable table) 
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
			//int id = table.createConnection(inputNodes.get(i).getID(), node.getID());
			//Connection c = new Connection(inputNodes.get(i),node,randf.nextGaussian(),true,id);
			//cons.add(c);
			for(int j=0;j<numOutputs;j++)
			{
				int id = table.createConnection(inputNodes.get(i).getID(), outputNodes.get(j).getID());
				Connection c = new Connection(inputNodes.get(i),outputNodes.get(j),randf.nextGaussian(),true,id);
				cons.add(c);
			}
		}
		for(int i=0;i<numBiasNodes;i++)
		{
			//int id = table.createConnection(biasNodes.get(i).getID(), node.getID());
			//Connection c = new Connection(biasNodes.get(i),node,randf.nextGaussian(),true,id);
			//cons.add(c);
			for(int j=0;j<numOutputs;j++)
			{
				int id = table.createConnection(biasNodes.get(i).getID(), outputNodes.get(j).getID());
				Connection c = new Connection(biasNodes.get(i),outputNodes.get(j),randf.nextGaussian(),true,id);
				cons.add(c);
			}
		}
		/*for(int i=0;i<numOutputs;i++)
		{
			int id = table.createConnection(node.getID(),outputNodes.get(i).getID());
			Connection c = new Connection(node,outputNodes.get(i),randf.nextGaussian(),true,id);
			cons.add(c);
		}*/
		
		minimalGenome = new Genome(cons,nodes,table,randf,numInputs,numOutputs);
		return minimalGenome;
	}

	@Override
	public Genome buildMinimalSolution(InnovationTable table) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Organism testPhenotypes(ArrayList<Organism> population) 
	{
		int maxSteps = 10000;
		int idx = -1;
		boolean simulate = true;
		age++;
		for(Organism org : population)
		{
			org.createPhenotype(width/2,height/2);
			if(!validatePhenotype(org.getPhenotype()))
			{
				org.setPhenotype(null);
			}
		}
		gameWorld.buildPop(population);
		if(simulate) {gameWorld.simulate(0);}
		else{gameWorld.run();}
		double[] fitnessList = gameWorld.getTestResults();
		
		for(int i=0;i<fitnessList.length;i++)
		{
			population.get(i).setFitness(fitnessList[i]);
			if(fitnessList[i] > bestFitness)
			{
				//if(Math.abs(fitnessList[i] - bestFitness)>100) {idx = i;}
				bestFitness = fitnessList[i];
				
			}
			if(fitnessList[i] > maxSteps*numTrials && simulate)
			{
				gameWorld.run();
				return population.get(i);
			}
		}
		//if(idx>=0 && age>10) {gameWorld.run();}
		return null;
	}
}
