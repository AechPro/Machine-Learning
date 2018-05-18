package NEAT.util;

import java.util.ArrayList;
import java.util.Random;

import NEAT.Genes.Connection;
import NEAT.Genes.Node;
import NEAT.Population.Genome;
import NEAT.Population.Organism;
import NEAT.Population.Phenotype;

public class XORTester 
{
	public final int numInputs = 2;
	public final int numOutputs = 1;
	public final int numBiasNodes = 1;
	public final int minimumHiddenNodes = 1;
	public int numHiddenNodes = 0;
	public boolean victor;
	public Random randf;
	private int width, height;
	public XORTester(Random rng,int w,int h)
	{
		randf = rng;
		victor = false;
		width=w;
		height=h;
	}
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
		double fitness = 0;
		for(Organism org : population)
		{
			org.createPhenotype(width/2,height/2);
			//System.out.println(org);
			try
			{
				fitness = testPhenotype(org.getPhenotype());
			}
			catch(Exception e)
			{
				System.out.println("FAILED");
				System.out.println(org);
				System.out.println(org.getPhenotype().getDepth());
				System.out.println(org.getPhenotype().getNodes().size());
				org.getPhenotype().saveAsImage("resources/NEAT/debug/victor/failure.png", 600,600);
				e.printStackTrace();
				System.exit(0);
			}
			org.setFitness(fitness);
			if(victor)
			{
				return org;
			}
		}
		return null;
	}
	public double testPhenotype(Phenotype phen)
	{
		if(!validatePhenotype(phen)) {return Math.random()*0.001;}
		victor = false;
		double accuracy = 0.0;
		double fitness = 0.0;
		double[] NNOutputs = new double[outputs.length];
		boolean success = false;
		for(int i=0;i<inputs.length;i++)
		{
			success = phen.activate(inputs[i]);
			for(int relax = 0;relax<phen.getDepth();relax++)
			{
				success = phen.activate(inputs[i]);
			}
			NNOutputs[i] = phen.readOutputVector()[0];
			phen.reset();
		}
		if(success)
		{
			for(int i=0;i<outputs.length;i++)
			{
				//System.out.println("INPUT: "+inputs[i][0]+" | "+inputs[i][1]);
				//System.out.println("OUTPUT: "+NNOutputs[i]);
				fitness += Math.abs(NNOutputs[i] - outputs[i][0]);
				if(NNOutputs[i] < 0.5 && outputs[i][0] == 0.0) {accuracy++;}
				else if(NNOutputs[i] >= 0.5 && outputs[i][0] == 1.0) {accuracy++;}
			}
			fitness = Math.pow(4.0 - fitness, 2) - phen.getNodes().size()/10;
		}
		else{fitness = Math.random()*0.001;}
		accuracy/=outputs.length;
		victor = accuracy == 1.0;
		return fitness;
	}
	public boolean validatePhenotype(Phenotype phen)
	{
		if(phen.getDepth() == -1) {return false;}
		if(phen.getOutputNodes().size() == 0) {return false;}
		return true;
	}
	public static final double[][] inputs = new double[][]
	{
		{0.0,0.0},
		{0.0,1.0},
		{1.0,0.0},
		{1.0,1.0}
	};
	public static final double[][] outputs = new double[][] 
	{
		{0.0},
		{1.0},
		{1.0},
		{0.0}
	};
}
