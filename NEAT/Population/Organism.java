package NEAT.Population;

import java.util.ArrayList;
import java.util.Random;

import NEAT.Configs.Config;
import NEAT.Genes.*;
import NEAT.util.*;
public class Organism 
{
	private int ID;
	private int timeSinceLastImprovement;
	private int age;
	private int speciesID;
	private int colorMarker;
	private boolean deathMark;
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
		fitness = 0.0;
		colorMarker = 0;
		sorter = new SortingUnit();
		//There can be no circumstance under which a new organism is marked for death
		deathMark = false;
	}
	public Organism(Organism other)
	{
		speciesID = other.getSpeciesID();
		genotype = new Genome(other.getGenotype());
		phenotype = null;
		fitness = other.getFitness();
		adjustedFitness = other.getAdjustedFitness();
		spawnAmount = 0.0;
		sorter = new SortingUnit();
		bestFitness = other.getBestFitness();
		timeSinceLastImprovement = other.getTimeSinceLastImprovement();
		colorMarker = other.getColorMarker();
		age = other.getAge();
		ID = other.getID();
		//There can be no circumstance under which a new organism is marked for death
		deathMark = false;
	}
	public void tick()
	{
		age++;
		timeSinceLastImprovement++;
	}
	public void mutateGenotype(InnovationTable table)
	{
		genotype.addConnection(table);
		if(genotype.getNodes().size()<Config.MAX_ALLOWED_NODES) {genotype.addNode(table);}
		//genotype.mutateNodes();
		//genotype.mutateConnections();
		genotype.mutateWeights();
		genotype.setID(table.getNextGenomeID());
		sorter.sortConnections(genotype.getConnections(), 0, genotype.getConnections().size()-1);
		sorter.sortNodes(genotype.getNodes(), 0, genotype.getNodes().size()-1);
	}
	public void mutateGenotypeNonStructural(InnovationTable table)
	{
		//genotype.mutateNodes();
		genotype.mutateWeights();
		genotype.setID(table.getNextGenomeID());
		sorter.sortConnections(genotype.getConnections(), 0, genotype.getConnections().size()-1);
		sorter.sortNodes(genotype.getNodes(), 0, genotype.getNodes().size()-1);
	}
	public void createMinimalGenotype(Genome minimalStructure, InnovationTable table)
	{
		genotype = new Genome(minimalStructure);
		genotype.randomize();
		genotype.setID(table.getNextGenomeID());
		sorter.sortConnections(genotype.getConnections(), 0, genotype.getConnections().size()-1);
		sorter.sortNodes(genotype.getNodes(), 0, genotype.getNodes().size()-1);
	}
	public void createEmptyGenotype(int numInputs, int numOutputs, Random rand, InnovationTable table)
	{
		genotype = new Genome(table, rand, numInputs, numOutputs);
		sorter.sortConnections(genotype.getConnections(), 0, genotype.getConnections().size()-1);
		sorter.sortNodes(genotype.getNodes(), 0, genotype.getNodes().size()-1);
	}
	public void createChildGenotype(ArrayList<Connection> genes, int numInputs, int numOutputs, Random rand, InnovationTable table)
	{
		genotype = new Genome(genes, table, rand, numInputs, numOutputs);
		sorter.sortConnections(genotype.getConnections(), 0, genotype.getConnections().size()-1);
		sorter.sortNodes(genotype.getNodes(), 0, genotype.getNodes().size()-1);
	}
	public void createPhenotype(int renderX, int renderY)
	{
		phenotype = null;
		ArrayList<Node> phenotypeNodes = new ArrayList<Node>();
		for(Connection c : genotype.getConnections())
		{
			if(c.isEnabled())
			{
				int pixelSeperator = 300;
				//System.out.println(c.getInput()+"\n"+c.getOutput());
				Node n1 = new Node(c.getInput());
				Node n2 = new Node(c.getOutput());

				n1.setX((int)(n1.getSplitX()*pixelSeperator));
				n1.setY((int)(-n1.getSplitY()*pixelSeperator));

				n2.setX((int)(n2.getSplitX()*pixelSeperator));
				n2.setY((int)(-n2.getSplitY()*pixelSeperator));
				if(n1.getType() == Node.HIDDEN_NODE){n1.setX((int)(n1.getSplitX()*pixelSeperator/1.3));}
				if(n2.getType() == Node.HIDDEN_NODE){n2.setX((int)(n2.getSplitX()*pixelSeperator/1.3));}
				boolean found = false;
				for(int i=0;i<phenotypeNodes.size();i++)
				{
					if(phenotypeNodes.get(i).equals(n1))
					{
						n1 = phenotypeNodes.get(i);
						found = true;
						break;
					}
				}
				if(!found)
				{phenotypeNodes.add(n1);}

				found = false;
				for(int i=0;i<phenotypeNodes.size();i++)
				{
					if(phenotypeNodes.get(i).equals(n2))
					{
						n2 = phenotypeNodes.get(i);
						found = true;
						break;
					}
				}
				if(!found){phenotypeNodes.add(n2);}
				Connection phenCon = new Connection(n1,n2,c.getWeight(),c.isEnabled(),c.getInnovation());
				n1.addOutput(phenCon);
				n2.addInput(phenCon);
			}
		}
		phenotype = new Phenotype(phenotypeNodes,renderX,renderY);
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
	public String toString(int verbosity)
	{
		if(verbosity == 0) {return toString();}
		String output = "\nORGANISM "+ID;
		output+="\nFitness: "+fitness;
		output+="\nAdj Fitness: "+adjustedFitness;
		output+="\nAge: "+age;
		output+="\nLast improvement: "+timeSinceLastImprovement;
		output+="\nSpawn amounts: "+spawnAmount;
		output+="\nGenome: "+genotype.toString(verbosity);
		output+="\nPhenotype: "+phenotype;
		return output;
	}
	@Override
	public String toString()
	{
		String output = "\nORGANISM "+ID;
		output+="\nFitness: "+fitness;
		output+="\nAdj Fitness: "+adjustedFitness;
		output+="\nAge: "+age;
		output+="\nLast improvement: "+timeSinceLastImprovement;
		output+="\nSpawn amounts: "+spawnAmount;
		output+=genotype;
		output+="\nPhenotype: "+phenotype;
		return output;
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
	public void markForDeath() {deathMark = true;}
	public boolean markedForDeath() {return deathMark;}
	public void setSpawnAmount(double i) {spawnAmount = i;}
	public void setAdjustedFitness(double i) {adjustedFitness=i;}
	public void setID(int i) {ID=i;}
	public void setSpeciesID(int i) {speciesID = i;}
	public void setColorMarker(int i) {colorMarker=i;}
	public void setPhenotype(Phenotype phen) {phenotype=phen;}
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
	public int getColorMarker() {return colorMarker;}
}
