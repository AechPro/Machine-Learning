package NEAT.util;

import java.util.ArrayList;
import java.util.Random;

import NEAT.Genes.Connection;
import NEAT.Genes.Node;
import NEAT.Population.Genome;
import NEAT.Population.Phenotype;

public class XORTester 
{
	public final int numInputs = 2;
	public final int numOutputs = 1;
	public final int numBiasNodes = 1;
	public int numHiddenNodes = 0;
	public double[][] inputs;
	public double[][] outputs;
	public Random rand;
	public XORTester(Random rng)
	{
		rand = rng;
		inputs = new double[][]
				{
					{0.0,0.0},
					{0.0,1.0},
					{1.0,0.0},
					{1.0,1.0}
				};
		outputs = new double[][] 
				{
					{0.0},
					{1.0},
					{1.0},
					{0.0}
				};
		
	}
	public Genome buildMinimalStructure(InnovationTable table)
	{
		ArrayList<Connection> cons = new ArrayList<Connection>();
		ArrayList<Node> nodes = new ArrayList<Node>();
		ArrayList<Node> inputNodes = new ArrayList<Node>();
		ArrayList<Node> outputNodes = new ArrayList<Node>();
		ArrayList<Node> biasNodes = new ArrayList<Node>();
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
			Node n = new Node(biasNodes.get(numBiasNodes-1).getSplitX()+0.25*i,0.0,Node.INPUT_NODE,id);
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
		minimalGenome = new Genome(cons,nodes,table,rand,numInputs,numOutputs);
		return minimalGenome;
	}
	public double testPhenotype(Phenotype phen)
	{
		/*
		 * ArrayList<Double> fitnessScores = new ArrayList<Double>();
		if(controller.getPhenotypes().size() == 0)
		{
			for(int i=0,stop=workers.size();i<stop;i++)
			{
				fitnessScores.add(Math.random()*0.01);
			}
		}
		double[][] inputTable = new double[4][2];
		inputTable[0] = new double[]{0,0};
		inputTable[1] = new double[]{0,1};
		inputTable[2] = new double[]{1,0};
		inputTable[3] = new double[]{1,1};
		double[][] outputTable = new double[4][1];
		outputTable[0] = new double[]{0};
		outputTable[1] = new double[]{1};
		outputTable[2] = new double[]{1};
		outputTable[3] = new double[]{0};
		for(int i=0,stop=controller.getPhenotypes().size();i<stop;i++)
		{
			boolean success = false;
			//System.out.println("\n\n\nPHENOTYPE "+i);
			double fitness = 0;
			ArrayList<Double> nnOutputs = new ArrayList<Double>();
			for(int j=0;j<inputTable.length;j++)
			{
				//System.out.println("\n\nNEW INPUT SELECTED");
				ArrayList<Double> inp = new ArrayList<Double>();
				inp.add(inputTable[j][0]);
				inp.add(inputTable[j][1]);
				success = controller.getPhenotypes().get(i).activate(inp, NeuralNetwork.TYPE_SNAPSHOT);
				for(int relax=0;relax<controller.getPhenotypes().get(i).getDepth();relax++)
				{
					success = controller.getPhenotypes().get(i).activate(inp, NeuralNetwork.TYPE_SNAPSHOT);
				}
				nnOutputs.add(controller.getPhenotypes().get(i).getOutputVector().get(0));
				controller.getPhenotypes().get(i).resetNeurons();
			}
			if(success)
			{
				//(fabs(out[0])+fabs(1.0-out[1])+fabs(1.0-out[2])+fabs(out[3]))
				
				fitness = Math.abs(nnOutputs.get(0))
						+ Math.abs(1.0 - nnOutputs.get(1)) 
						+ Math.abs(1.0 - nnOutputs.get(2)) 
						+ Math.abs(nnOutputs.get(3));
				fitness = Math.pow(4.0-fitness, 2);
			}
			else
			{
				fitness = 0.001;
			}
			fitnessScores.add(fitness);
		}
		 */
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
			}
			fitness = Math.pow(4.0 - fitness, 2);
		}
		else{fitness = Math.random()*0.001;}
		return fitness;
	}
}
