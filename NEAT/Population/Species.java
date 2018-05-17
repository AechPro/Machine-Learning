package NEAT.Population;

import java.util.*;
import NEAT.Genes.*;
import NEAT.util.Config;
import NEAT.util.InnovationTable;
import NEAT.util.SelectionUnit;
import NEAT.util.SortingUnit;
public class Species 
{
	private double bestFitness;
	private double spawnAmount;
	private int ID;
	private int age;
	private int timeSinceLastImprovement;
	private int champSpawnAmount;
	private Organism representative;
	private ArrayList<Organism> members;
	private Random rand;
	private SortingUnit sorter;
	private SelectionUnit selector;
	public Species(Organism first, Random rng, int id)
	{
		ID=id;
		rand = rng;
		members = new ArrayList<Organism>();
		members.add(first);
		representative = first;
		sorter = new SortingUnit();
		selector = new SelectionUnit();
		age=0;
		bestFitness=0;
		timeSinceLastImprovement=0;
		champSpawnAmount = 0;
	}
	public void reproduce(InnovationTable table)
	{	
		int popSize = members.size();
		ArrayList<Organism> newPop = new ArrayList<Organism>();
		int numToSpawn = (int)(Math.round(spawnAmount));
		Organism child = null;
		ArrayList<Organism> culledMembers = new ArrayList<Organism>();
		int cutoff = (int)Math.round(Config.WORST_PERCENT_REMOVED*(double)members.size());
		for(int i=cutoff;i<members.size();i++)
		{
			culledMembers.add(members.get(i));
		}
		if(popSize>=Config.SPECIES_SIZE_FOR_CHAMP_CLONING || champSpawnAmount>0)
		{
			for(int i=0,stop=Math.max(1, champSpawnAmount);i<stop;i++)
			{
				child = new Organism(getBestMember());
				newPop.add(child);
			}
		}
		numToSpawn-=newPop.size();
		for(int i=0;i<numToSpawn;i++)
		{
			child = null;
			if(culledMembers.size()==1 || Math.random()>Config.CROSSOVER_RATE)
			{
				child = new Organism(selector.rouletteSelect(1, true, culledMembers).get(0));
				child.mutateGenotype(table);
			}
			else
			{
				ArrayList<Organism> parents = selector.rouletteSelect(2, true, culledMembers);
				child = crossover(parents.get(0),parents.get(1),table);
				if(Math.random()>Config.MATE_NO_MUTATION_CHANCE || parents.get(0) == parents.get(1))
				{
					child.mutateGenotype(table);
				}
			}
			newPop.add(child);
		}
		//System.out.println("REPRODUCTION SPAWNED "+newPop.size()+" CHILDREN");
		members = newPop;
	}
	public Organism crossover(Organism p1, Organism p2, InnovationTable table)
	{
		boolean debugging = false;
		boolean p1Best = false;
		ArrayList<Connection> dominant = p2.getGenotype().getConnections();
		ArrayList<Connection> recessive = p1.getGenotype().getConnections();
		ArrayList<Connection> childGenes = new ArrayList<Connection>();
		Connection selection = null;
		if(debugging)
		{
			System.out.println("---CROSSING OVER GENOMES----");
			System.out.println("\n***P1***\n"+p1.toString(1));
			System.out.println("\n***P2***\n"+p2.toString(1));
		}
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
		if(debugging){System.out.println("P1 DOMINANT: "+p1Best);}
		for(int i=0,stop=dominant.size();i<stop;i++)
		{
			if(debugging) {System.out.println("CHECKING GENE "+dominant.get(i));}
			if(recessive.contains(dominant.get(i)))
			{
				if(debugging) {System.out.println("FOUND SHARED GENE");}
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
				if(debugging) {System.out.println("FOUND DISJOINT OR EXCESS GENE");}
				selection = dominant.get(i);
			}
			childGenes.add(selection);
		}
		for(Connection c : childGenes)
		{
			if(!c.isEnabled())
			{
				if(Math.random() < Config.INHERITED_CONNECTION_ENABLE_RATE)
				{
					c.setEnable(true);
				}
			}
		}
		
		Organism child = new Organism(table.getNextOrganismID());
		child.createChildGenotype(childGenes,p1.getGenotype().getInputs(),p1.getGenotype().getOutputs(),rand,table);
		if(debugging) {System.out.println("\n***CHILD***\n"+child.toString(1));}
		return child;
	}
	public void tick()
	{
		sorter.sortOrganismsAdjustedFitness(members,0,members.size()-1);
		for(Organism org : members)
		{
			if(org.getFitness() > bestFitness)
			{
				bestFitness = org.getFitness();
				timeSinceLastImprovement = 0;
			}
			org.tick();
		}
		timeSinceLastImprovement++;
		age++;
	}
	public void addMember(Organism mem)
	{
		members.add(mem);
		mem.setSpeciesID(ID);
	}
	public void purge()
	{
		for(int i=0;i<members.size();i++)
		{
			members.get(i).setSpeciesID(-1);
		}
		representative = new Organism(getBestMember());
		representative.setFitness(getBestMember().getFitness());
		representative.setAdjustedFitness(getBestMember().getAdjustedFitness());
		members = new ArrayList<Organism>();
		spawnAmount = 0d;
	}
	public void adjustFitnessValues()
	{
		for(Organism org : members)
		{
			double fitness = org.getFitness();
			if(age < Config.SPECIES_YOUNG_THRESHOLD) {fitness*= 1.0 + Config.SPECIES_AGE_FITNESS_MODIFIER;}
			else if(age > Config.SPECIES_OLD_THRESHOLD) {fitness*= 1.0 - Config.SPECIES_AGE_FITNESS_MODIFIER;}
			if(timeSinceLastImprovement > Config.MAX_TIME_SPECIES_STAGNATION)
			{
				fitness*= 0.001; //Extreme penalty for stagnated species.
			}
			fitness/=members.size();
			org.setAdjustedFitness(fitness);
		}
		sorter.sortOrganismsAdjustedFitness(members, 0, members.size()-1);
		
	}
	public void calculateSpawnAmounts(double globalAverage)
	{	
		double fractionComponent = 0d;
		int intComponent = 0;
		spawnAmount = 0.0d;
		double spawns = 0;
		for(Organism org : members)
		{
			
			spawns = org.getAdjustedFitness()/globalAverage;
			intComponent+=(int)(spawns);
			fractionComponent += spawns - (int)spawns;
			
			if(fractionComponent>=1.0d)
			{
				intComponent += (int)(fractionComponent);
				fractionComponent -= (int)fractionComponent;
			}
			org.setSpawnAmount(spawns);
		}
		spawnAmount = intComponent+fractionComponent;
		spawnAmount = (int)Math.round(spawnAmount);
	}
	@Override
	public String toString()
	{
		String output = "\nSPECIES "+ID+"\n";
		output+="Genome ID        Fitness        Adjusted Fitness        Spawn Amount\n";
		for(int i=0,stop=members.size();i<stop;i++)
		{
			output+=members.get(i).getID()+"              "+Math.round(100.0*members.get(i).getFitness())/100.0
					+"             "+Math.round(100.0*members.get(i).getAdjustedFitness())/100.0+"                    "
					+Math.round(100.0*members.get(i).getSpawnAmount())/100.0;
			output+="\n";
		}
		output+="Spawn amounts for this species: "+spawnAmount;
		output+="\nMembers in this species: "+members.size();
		if(members.size()>0){output+="\nBest member for this species:"+getBestMember();}
		return output;
	}
	public void setChampSpawns(int num) {champSpawnAmount=num;}
	public double getBestFitness() {return bestFitness;}
	public void setBestFitness(double bestFitness) {this.bestFitness = bestFitness;}
	public double getSpawnAmount() {return spawnAmount;}
	public void setSpawnAmount(double spawnAmount) {this.spawnAmount = spawnAmount;}
	public int getAge() {return age;}
	public void setAge(int age) {this.age = age;}
	public int getTimeSinceLastImprovement() {return timeSinceLastImprovement;}
	public Organism getRepr() {return representative;}
	public Organism getBestMember() 
	{
		double bestFitness = -1.0;
		Organism bestMember = members.get(0);
		for(Organism org : members)
		{
			if(org.getFitness() > bestFitness)
			{
				bestMember = org;
				bestFitness = org.getFitness();
			}
		}
		return bestMember;
	}
	public void setBestFitness(Organism best) 
	{
		bestFitness = best.getFitness();
		timeSinceLastImprovement = 0;
	}
	public ArrayList<Organism> getMembers() {return members;}
	public void setMembers(ArrayList<Organism> members) {this.members = members;}
}
