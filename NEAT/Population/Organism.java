package NEAT.Population;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
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
	private boolean populationChamp;
	private Genome genotype;
	private Phenotype phenotype;
	private SortingUnit sorter;
	public Organism(int id)
	{
		ID = id;
		speciesID=-1;
		populationChamp = false;
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
		populationChamp = other.isPopChamp();
		phenotype = null;
		fitness = other.getFitness();
		adjustedFitness = other.getAdjustedFitness();
		spawnAmount = other.getSpawnAmount();
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
		if(genotype.addNode(table));
		else if(genotype.addConnection(table));
		else
		{
			genotype.mutateConnections();
			genotype.mutateNodes();
			genotype.mutateWeights();
		}
		genotype.setID(table.getNextGenomeID());
		sorter.sortConnections(genotype.getConnections(), 0, genotype.getConnections().size()-1);
		sorter.sortNodes(genotype.getNodes(), 0, genotype.getNodes().size()-1);
	}
	public void mutateGenotypeNonStructural(InnovationTable table)
	{
		//genotype.mutateNodes();
		genotype.mutateWeights();
		if(table == null) {}
		else{genotype.setID(table.getNextGenomeID());}
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
				Node n1 = null;
				Node n2 = null;
				
				if(c.getInput() instanceof Neuron)
                {
                    n1 = new Neuron(c.getInput());
                }
				else
				{
				    n1 = new FeatureFilter(c.getInput());
				}
				
				if(c.getOutput() instanceof Neuron)
                {
                    n2 = new Neuron(c.getOutput());
                }
				else
				{
				    n2 = new FeatureFilter(c.getOutput());
				}
				
				//System.out.println("AFTER CREATION: "+n1.getOutputs());

				n1.setX((int)(n1.getSplitX()*pixelSeperator));
				n1.setY((int)(-n1.getSplitY()*pixelSeperator));

				n2.setX((int)(n2.getSplitX()*pixelSeperator));
				n2.setY((int)(-n2.getSplitY()*pixelSeperator));
				if(n1.getType() == Neuron.HIDDEN_NEURON){n1.setX((int)(n1.getSplitX()*pixelSeperator/1.3));}
				if(n2.getType() == Neuron.HIDDEN_NEURON){n2.setX((int)(n2.getSplitX()*pixelSeperator/1.3));}
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
	public void save(String filePath)
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            Connection c = null;
            Node n = null;
            writer.write("org id:"+ID+"\n");
            writer.write("species id:"+speciesID+"\n");
            writer.write("age:"+age+"\n");
            writer.write("TSLI:"+timeSinceLastImprovement+"\n");
            writer.write("fitness:"+fitness+"\n");
            writer.write("genome id:"+genotype.getID()+"\n");
            writer.write("num genes:"+genotype.getConnections().size()+"\n");
            for(int i=0,stop=genotype.getConnections().size();i<stop;i++)
            {
                c = genotype.getConnections().get(i);
                n = c.getInput();
                writer.write("connection "+i+":"+c.getInnovation()+" "+c.getWeight()+" "+c.isEnabled());
                writer.write(" "+n.getID()+" "+n.getType()+" "+n.getSplitX()+" "+n.getSplitY());
                n = c.getOutput();
                writer.write(" "+n.getID()+" "+n.getType()+" "+n.getSplitX()+" "+n.getSplitY()+"\n");
            }
            writer.close();
        }
        catch(Exception e) {e.printStackTrace();}
    }
	public void load(String filePath)
	{
	    //System.out.println("\n\n************BEFORE LOADING***********"+toString(1));
	    try
	    {
	        BufferedReader reader = new BufferedReader(new FileReader(filePath));
	        ArrayList<Connection> connections = new ArrayList<Connection>();
	        ArrayList<Neuron> nodes = new ArrayList<Neuron>();
	        int cid = 0;
	        double w = 0;
	        boolean en = true;
	        int nid = 0;
	        int ntype = 0;
	        double sx = 0;
	        double sy = 0;
	        int inputs = 0;
            int outputs = 0;
	        int genomeID = 0;
            int numCons = 0;
	        String line = null;
	        
	        line = reader.readLine().toLowerCase();
            ID = Integer.parseInt(line.substring(line.indexOf(":")+1, line.length()));

            line = reader.readLine().toLowerCase();
            speciesID = Integer.parseInt(line.substring(line.indexOf(":")+1, line.length()));
            
            line = reader.readLine().toLowerCase();
            age = Integer.parseInt(line.substring(line.indexOf(":")+1, line.length()));
            
            line = reader.readLine().toLowerCase();
            timeSinceLastImprovement = Integer.parseInt(line.substring(line.indexOf(":")+1, line.length()));
            
            line = reader.readLine().toLowerCase();
            fitness = Double.parseDouble(line.substring(line.indexOf(":")+1, line.length()));
            
            line = reader.readLine().toLowerCase();
            genomeID = Integer.parseInt(line.substring(line.indexOf(":")+1, line.length()));
            
            line = reader.readLine().toLowerCase();
            numCons = Integer.parseInt(line.substring(line.indexOf(":")+1, line.length()));
            
            for(int i = 0;i<numCons;i++)
            {
                line = reader.readLine().toLowerCase();
                line = line.substring(line.indexOf(":")+1, line.length());
                String[] data = line.split(" ");
                cid = Integer.parseInt(data[0]);
                w = Double.parseDouble(data[1]);
                en = Boolean.parseBoolean(data[2]);
                
                nid = Integer.parseInt(data[3]);
                ntype = Integer.parseInt(data[4]);
                sx = Double.parseDouble(data[5]);
                sy = Double.parseDouble(data[6]);
                Neuron in = new Neuron(sx,sy,ntype,nid);
                if(ntype == Node.OUTPUT_NODE) {outputs++;}
                if(ntype == Node.INPUT_NODE) {inputs++;}
                
                nid = Integer.parseInt(data[7]);
                ntype = Integer.parseInt(data[8]);
                sx = Double.parseDouble(data[9]);
                sy = Double.parseDouble(data[10]);
                Neuron out = new Neuron(sx,sy,ntype,nid);
                if(ntype == Node.OUTPUT_NODE) {outputs++;}
                if(ntype == Node.INPUT_NODE) {inputs++;}
                
                Connection c = new Connection(in,out,w,en,cid);
                if(!nodes.contains(out)) {nodes.add(out);}
                if(!nodes.contains(in)) {nodes.add(in);}
                connections.add(c);
                
            }
            genotype = new Genome(connections,null,null,inputs,outputs);
            genotype.setID(genomeID);
	        reader.close();
	    }
	    catch(Exception e) {e.printStackTrace();}
	    //System.out.println("\n\n****************AFTER LOADING****************"+toString(1));
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
	public double getAverageConnectionWeight()
	{
		return genotype.getAverageConnectionWeight();
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
	public void setPopChamp(boolean i) {populationChamp=i;}
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
	public boolean isPopChamp() {return populationChamp;}
}
