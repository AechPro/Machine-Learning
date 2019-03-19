package NEAT.TestUnits;

import java.util.ArrayList;
import java.util.Random;

import NEAT.Configs.Config;
import NEAT.Genes.Connection;
import NEAT.Genes.Neuron;
import NEAT.Genes.Node;
import NEAT.Population.Genome;
import NEAT.Population.Organism;
import NEAT.util.InnovationTable;

import NEAT.Simulations.BetterFish.*;

public class FishTester extends TestUnit
{
	private Simulator gameWorld;
	private boolean flip = true;
	private int simCount = 0;
	private double best = 0.0;
	private int TSLI = 0;
	public FishTester(Random rng, int w, int h)
	{
		super(rng,w,h);
		numBiasNodes = 1;
		numInputs = 1*4+1;
		numOutputs = 2;
		numHiddenNodes = 1;
		gameWorld = new Simulator(Config.POPULATION_SIZE);
	}
	
	@Override
	public Genome buildMinimalStructure(InnovationTable table)
	{
		ArrayList<Connection> cons = new ArrayList<Connection>();
		ArrayList<Node> nodes = new ArrayList<Node>();
		ArrayList<Neuron> inputNodes = new ArrayList<Neuron>();
		ArrayList<Neuron> outputNodes = new ArrayList<Neuron>();
		ArrayList<Neuron> biasNodes = new ArrayList<Neuron>();
		Genome minimalGenome = null;
		
		for(int i=0;i<numBiasNodes;i++)
		{
			int id = table.createNode(-1, -1, Neuron.BIAS_NEURON);
			Neuron n = new Neuron(0.25*i,0.0,Neuron.BIAS_NEURON,id);
			biasNodes.add(n);
			nodes.add(n);
		}
		for(int i=0;i<numInputs;i++)
		{
			int id = table.createNode(-1, -1, Neuron.INPUT_NODE);
			Neuron n = new Neuron(biasNodes.get(numBiasNodes-1).getSplitX()+0.25*(i+1),0.0,Neuron.INPUT_NODE,id);
			inputNodes.add(n);
			nodes.add(n);
		}
		for(int i=0;i<numOutputs;i++)
		{
			int id = table.createNode(-1, -1, Neuron.OUTPUT_NODE);
			Neuron n = new Neuron(inputNodes.get(0).getSplitX()+0.25*i,1.0,Neuron.OUTPUT_NODE,id);
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
		ArrayList<Neuron> inputNodes = new ArrayList<Neuron>();
		ArrayList<Neuron> outputNodes = new ArrayList<Neuron>();
		ArrayList<Neuron> biasNodes = new ArrayList<Neuron>();
		ArrayList<Neuron> hiddenNodes = new ArrayList<Neuron>();
		Random randf = new Random((long)(Math.random()*Long.MAX_VALUE));
		Genome minimalGenome = null;
		
		for(int i=0;i<numBiasNodes;i++)
		{
			int id = table.createNode(-1, -1, Neuron.BIAS_NEURON);
			Neuron n = new Neuron(0.25*i,0.0,Neuron.BIAS_NEURON,id);
			biasNodes.add(n);
			nodes.add(n);
		}
		for(int i=0;i<numInputs;i++)
		{
			int id = table.createNode(-1, -1, Neuron.INPUT_NODE);
			Neuron n = new Neuron(biasNodes.get(numBiasNodes-1).getSplitX()+0.25*(i+1),0.0,Neuron.INPUT_NODE,id);
			inputNodes.add(n);
			nodes.add(n);
		}
		for(int i=0;i<numOutputs;i++)
		{
			int id = table.createNode(-1, -1, Neuron.OUTPUT_NODE);
			Neuron n = new Neuron(inputNodes.get(0).getSplitX()+0.25*i,1.0,Neuron.OUTPUT_NODE,id);
			outputNodes.add(n);
			nodes.add(n);
		}
		
		int hiddenID = table.createNode(2, 4, Neuron.HIDDEN_NEURON);
		Neuron node = new Neuron(inputNodes.get(1).getSplitX(),0.5,Neuron.HIDDEN_NEURON,hiddenID);
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
		int numFrames = 1200;
		for(Organism org : population)
		{
			org.createPhenotype(width/2,height/2);
			if(!validatePhenotype(org.getPhenotype()))
			{
				org.setPhenotype(null);
			}
		}
		
		gameWorld.buildPop(population);
		simCount++;
		//gameWorld.runVisible(numFrames);
		if(TSLI++>3 || simCount<15) {gameWorld.runInvisible(numFrames);}
		else{gameWorld.runVisible(numFrames);}
		//gameWorld.runInvisible(numFrames);
		double[] fitnessList = gameWorld.getTestResults();
		
		for(int i=0;i<fitnessList.length;i++)
		{
			population.get(i).setFitness(fitnessList[i]);
			if(best<fitnessList[i]) {best=fitnessList[i]; TSLI = 0;}
			if(fitnessList[i] >= 30000) 
			{
				victor=true;
				
				for(int j=0;j<population.size();j++) 
				{
				    population.get(j).setPopChamp(false);
				}
				population.get(i).setPopChamp(true);
				gameWorld.buildPop(population);
				gameWorld.runVisible(numFrames);
				return population.get(i);
			}
		}
		return null;
	}
}
