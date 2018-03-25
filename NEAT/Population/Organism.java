package NEAT.Population;

import java.util.ArrayList;

import NEAT.Genes.*;

public class Organism 
{
	private int ID;
	private double fitness;
	private double spawnAmount;
	private double adjustedFitness;
	private Genome genotype;
	private Phenotype phenotype;
	public Organism(int id)
	{
		ID = id;
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
	public void setSpawnAmount(double i) {spawnAmount = i;}
	public void setFitness(double i) {fitness=i;}
	public void setAdjustedFitness(double i) {adjustedFitness=i;}
	public void setID(int i) {ID=i;}
	public double getFitness() {return fitness;}
	public double getAdjustedFitness() {return adjustedFitness;}
	public double getSpawnAmount() {return spawnAmount;}
	public Phenotype getPhenotype() {return phenotype;}
	public Genome getGenotype() {return genotype;}
}
