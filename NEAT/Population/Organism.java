package NEAT.Population;

import java.util.ArrayList;
import java.util.Random;

import NEAT.Genes.*;
import NEAT.util.*;
public class Organism 
{
	private int ID;
	private int timeSinceLastImprovement;
	private int age;
	private int speciesID;
	private double fitness;
	private double spawnAmount;
	private double adjustedFitness;
	private double bestFitness;
	private Genome genotype;
	private Phenotype phenotype;
	private SortingUnit sorter;
	public Organism(int id)
	{
		ID = id;
		speciesID=-1;
		age = 0;
		timeSinceLastImprovement = 0;
		sorter = new SortingUnit();
	}
	public Organism(Organism other)
	{
		speciesID = other.getSpeciesID();
		genotype = new Genome(other.getGenotype());
		phenotype = null;
		fitness = other.getFitness();
		adjustedFitness = other.getAdjustedFitness();
		spawnAmount = other.getSpawnAmount();
		sorter = new SortingUnit();
		bestFitness = other.getBestFitness();
		timeSinceLastImprovement = other.getTimeSinceLastImprovement();
		age = other.getAge();
		ID = other.getID();
	}
	public void tick()
	{
		age++;
		timeSinceLastImprovement = 0;
	}
	public void mutateGenotype(InnovationTable table)
	{
		if(genotype.getNodes().size()<Config.MAX_ALLOWED_NODES) {genotype.addNode(table);}
		genotype.addConnection(table);
		genotype.mutateNode();
		genotype.mutateWeights();
		genotype.setID(table.getNextGenomeID());
		sorter.sortConnections(genotype.getConnections(), 0, genotype.getConnections().size()-1);
		sorter.sortNodes(genotype.getNodes(), 0, genotype.getNodes().size()-1);
	}
	public void createMinimalGenotype(Genome minimalStructure, InnovationTable table)
	{
		genotype = new Genome(minimalStructure);
		for(int i=0;i<3;i++)
		{
			genotype.mutateNode();
			genotype.mutateWeights();
		}
		genotype.setID(table.getNextGenomeID());
	}
	public void createEmptyGenotype(int numInputs, int numOutputs, Random rand, InnovationTable table)
	{
		genotype = new Genome(table, rand, numInputs, numOutputs);
	}
	public void createChildGenotype(ArrayList<Connection> genes, int numInputs, int numOutputs, Random rand, InnovationTable table)
	{
		genotype = new Genome(genes, table, rand, numInputs, numOutputs);
	}
	public void createPhenotype()
	{
		phenotype = null;
		ArrayList<Node> phenotypeNodes = new ArrayList<Node>();
		for(Connection c : genotype.getConnections())
		{
			if(c.isEnabled())
			{
				int pixelSeperator = 300;
				int inpID = genotype.getNodeIndex(c.getInput());
				int outID = genotype.getNodeIndex(c.getOutput());
				//System.out.println(c.getInput()+"\n"+c.getOutput());
				Node n1 = new Node(genotype.getNodes().get(inpID));
				Node n2 = new Node(genotype.getNodes().get(outID));

				n1.setX((int)(n1.getSplitX()*pixelSeperator));
				n1.setY((int)(-n1.getSplitY()*pixelSeperator));

				n2.setX((int)(n2.getSplitX()*pixelSeperator));
				n2.setY((int)(-n2.getSplitY()*pixelSeperator));
				if(n1.getType() == Node.HIDDEN_NODE){n1.setX((int)(n1.getSplitX()*pixelSeperator/2));}
				if(n2.getType() == Node.HIDDEN_NODE){n2.setX((int)(n2.getSplitX()*pixelSeperator/2));}

				Connection phenCon = new Connection(n1,n2,c.getWeight(),c.isEnabled(),c.getInnovation());
				n1.addOutput(phenCon);
				n2.addInput(phenCon);
				boolean found = false;
				for(int i=0;i<phenotypeNodes.size();i++)
				{
					if(phenotypeNodes.get(i).equals(n1))
					{
						found = true;
						try{phenCon.setInput(phenotypeNodes.get(i));}
						catch(Exception e) {e.printStackTrace();}
						phenotypeNodes.get(i).addOutput(phenCon);
						break;
					}
				}
				if(!found){phenotypeNodes.add(n1);}

				found = false;
				for(int i=0;i<phenotypeNodes.size();i++)
				{
					if(phenotypeNodes.get(i).equals(n2))
					{
						found = true;
						try{phenCon.setOutput(phenotypeNodes.get(i));}
						catch(Exception e) {e.printStackTrace();}
						phenotypeNodes.get(i).addInput(phenCon);
						break;
					}
				}
				if(!found){phenotypeNodes.add(n2);}
			}
		}
		phenotype = new Phenotype(phenotypeNodes);
	}
	public double calculateCompatibility(Organism other)
	{
		ArrayList<Connection> g1 = genotype.getConnections();
		ArrayList<Connection> g2 = other.getGenotype().getConnections();
		double compat = 0.0d;
		double numExcess = 0;
		double numDisjoint = 0;
		double numShared = 0;
		double meanWeight = 0.0d;
		for(Connection c : g1)
		{
			if(g2.contains(c))
			{
				numShared++;
				meanWeight+=Math.abs(c.getWeight() - g2.get(g2.indexOf(c)).getWeight());
			}
			else
			{
				if(c.getInnovation() > g2.get(g2.size()-1).getInnovation()) {numExcess++;}
				else if(c.getInnovation() < g2.get(g2.size()-1).getInnovation()) {numDisjoint++;}
			}
		}
		meanWeight /= numShared;
		int longest = Math.max(g2.size(), g1.size());
		if(longest<20) {longest=1;}
		compat += numExcess * Config.COMPAT_EXCESS_COEF/(double)longest;
		compat += numDisjoint * Config.COMPAT_DISJOINT_COEF/(double)longest;
		compat += meanWeight * Config.COMPAT_SHARED_COEF;
		/*System.out.println("\nCOMPATIBILITY CALCULATION");
		System.out.println("ORGANISM 1:"+toString());
		System.out.println("ORGANISM 2:"+other);
		System.out.println("\nDISJOINT: "+numDisjoint+"\nEXCESS: "+numExcess+"\nSHARED: "+numShared+"\nMEAN WEIGHT: "+meanWeight);
		System.out.println("COMPATIBILITY VALUE: "+compat);*/
		return compat;
	}
	public void setFitness(double i) 
	{
		if(i > bestFitness) 
		{
			bestFitness = i;
			timeSinceLastImprovement = 0;
		}
		fitness=i;
	}
	public void setSpawnAmount(double i) {spawnAmount = i;}
	public void setAdjustedFitness(double i) {adjustedFitness=i;}
	public void setID(int i) {ID=i;}
	public void setSpeciesID(int i) {speciesID = i;}
	public int getSpeciesID(){return speciesID;}
	public int getTimeSinceLastImprovement() {return timeSinceLastImprovement;}
	public int getAge() {return age;}
	public int getID() {return ID;}
	public double getBestFitness() {return bestFitness;}
	public double getFitness() {return fitness;}
	public double getAdjustedFitness() {return adjustedFitness;}
	public double getSpawnAmount() {return spawnAmount;}
	public Phenotype getPhenotype() {return phenotype;}
	public Genome getGenotype() {return genotype;}
}
