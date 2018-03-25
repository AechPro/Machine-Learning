package NEAT.Population;

import java.util.*;
import NEAT.Genes.*;
import NEAT.util.InnovationTable;
public class Species 
{
	private double bestFitness;
	private double spawnAmount;
	private int age;
	private int timeSinceLastImprovement;
	private Organism bestMember;
	private ArrayList<Organism> members;
	private Random rand;
	public Species(Organism first, Random rng)
	{
		rand = rng;
		setBestMember(first);
		members = new ArrayList<Organism>();
		members.add(first);
	}
	public void reproduce(ArrayList<Organism> newPop, InnovationTable table)
	{
		
	}
	public Organism crossover(Organism p1, Organism p2, InnovationTable table)
	{
		boolean p1Best = false;
		ArrayList<Connection> dominant = p2.getGenotype().getConnections();
		ArrayList<Connection> recessive = p1.getGenotype().getConnections();
		ArrayList<Connection> childGenes = new ArrayList<Connection>();
		Connection selection = null;
		if(p1.getAdjustedFitness() == p2.getAdjustedFitness())
		{
			if(p1.getGenotype().getConnections().size() == p2.getGenotype().getConnections().size())
			{p1Best = Math.random()>=0.5;}
			else
			{
				p1Best = p1.getGenotype().getConnections().size() < p2.getGenotype().getConnections().size();
			}
		}
		else {p1Best = p1.getAdjustedFitness() > p2.getAdjustedFitness();}
		if(p1Best)
		{
			dominant = p1.getGenotype().getConnections();
			recessive = p2.getGenotype().getConnections();
		}
		
		for(int i=0,stop=dominant.size();i<stop;i++)
		{
			if(recessive.contains(dominant.get(i)))
			{
				if(Math.random()>=0.5)
				{
					selection = dominant.get(i);
				}
				else
				{
					selection = recessive.get(recessive.indexOf(dominant.get(i)));
				}
			}
			else
			{
				selection = dominant.get(i);
			}
			childGenes.add(selection);
		}
		Organism child = new Organism(table.getNextOrganismID());
		//	public void createGenotype(ArrayList<Connection> genes, int numInputs, int numOutputs, Random rand, InnovationTable table)
		child.createGenotype(childGenes,p1.getGenotype().getInputs(),p1.getGenotype().getOutputs(),rand,table);
		return child;
	}
	public void tick()
	{
		for(Organism org : members)
		{
			org.tick();
		}
		timeSinceLastImprovement++;
		age++;
	}
	public double getBestFitness() {return bestFitness;}
	public void setBestFitness(double bestFitness) {this.bestFitness = bestFitness;}
	public double getSpawnAmount() {return spawnAmount;}
	public void setSpawnAmount(double spawnAmount) {this.spawnAmount = spawnAmount;}
	public int getAge() {return age;}
	public void setAge(int age) {this.age = age;}
	public int getTimeSinceLastImprovement() {return timeSinceLastImprovement;}
	public Organism getBestMember() {return bestMember;}
	public void setBestMember(Organism best) 
	{
		this.bestMember = new Organism(best);
		bestFitness = best.getFitness();
		timeSinceLastImprovement = 0;
	}
	public ArrayList<Organism> getMembers() {return members;}
	public void setMembers(ArrayList<Organism> members) {this.members = members;}
}
