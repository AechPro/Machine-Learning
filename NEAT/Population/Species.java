package NEAT.Population;

import java.util.*;

import NEAT.Configs.Config;
import NEAT.Genes.*;
import NEAT.util.InnovationTable;
import NEAT.util.SelectionUnit;
import NEAT.util.SortingUnit;
import NEAT.util.SpeciesSorter;
public class Species 
{
	private double bestFitness;
	private double spawnAmount;
	private int ID;
	private int age;
	private int timeSinceLastImprovement;
	private int champSpawnAmount;
	private boolean obliterate;
	private Organism representative;
	private ArrayList<Organism> members;
	private Random rand;
	private SortingUnit sorter;
	private SelectionUnit selector;
	private SpeciesSorter speciationUnit;
	public Species(Organism first, SpeciesSorter sUnit, Random rng, int id)
	{
		ID=id;
		speciationUnit = sUnit;
		rand = rng;
		members = new ArrayList<Organism>();
		members.add(first);
		representative = first;
		first.setID(ID);
		sorter = new SortingUnit();
		selector = new SelectionUnit();
		age=0;
		bestFitness=0;
		timeSinceLastImprovement=0;
		champSpawnAmount = 0;
		obliterate = false;
	}
	public void reproduce(InnovationTable table)
	{	
		if(members.size() == 0) {return;}
		int popSize = members.size();
		int numToSpawn = (int)(Math.round(spawnAmount));
		//poolSize set here in case we add any children to our member list during
		//reproduction. The children will be appended to the end of our member list
		//which means we won't select them if we cap our random selection pool to the
		//size of our member list
		int poolSize = members.size()-1;
		Organism child = null;
		int spawned = 0;
		if(numToSpawn > 0 && (numToSpawn>=Config.SPECIES_SIZE_FOR_CHAMP_CLONING || champSpawnAmount>0))
		{
			if(!getBestMember().isPopChamp() || true)
			{
				for(int i=0,stop=Math.max(1, champSpawnAmount);i<stop;i++)
				{
					child = new Organism(getBestMember());
					members.add(child);
					numToSpawn--;
					spawned++;
				}
			}
		}
		/*if(getBestMember().isPopChamp())
		{
			Organism best = getBestMember();
			spawned += Config.NUM_POP_CHAMP_MUTATIONS + 1;
			numToSpawn -= Config.NUM_POP_CHAMP_MUTATIONS + 1;
			child = new Organism(best);
			members.add(child);
			for(int i=0;i<Config.NUM_POP_CHAMP_MUTATIONS;i++)
			{
				child = new Organism(best);
				child.mutateGenotypeNonStructural(table);
				members.add(child);
			}
		}*/
		//System.out.println("STARTING AT "+members.size()+" MEMBERS");
		for(int i=0;i<numToSpawn;i++)
		{
			child = null;
			if(popSize==1 || Math.random()>Config.CROSSOVER_RATE)
			{

				Organism selection = selector.randomSelectFromPool(1,poolSize,members).get(0);
				child = new Organism(selection);
				child.mutateGenotype(table);
			}
			else
			{
				ArrayList<Organism> parents = selector.randomSelectFromPool(2, poolSize, members);
				child = crossover(parents.get(0),parents.get(1),table);
				if(Math.random()>Config.MATE_NO_MUTATION_CHANCE || parents.get(0) == parents.get(1))
				{
					child.mutateGenotype(table);
				}
			}
			if(child != null) {spawned++;}
			child.setSpeciesID(-1);
			speciationUnit.speciateOrganism(child,false);
			//System.out.println(members.size());
		}

		//System.out.println("SPECIES "+ID+" SPAWNED "+spawned+" OF "+spawnAmount+" CHILDREN");
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
		if(members.size() == 0) {return;}
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
		Organism org = members.get((int)(Math.round(Math.random()*(members.size()-1))));
		representative = new Organism(org);
	}
	public void removeOldGeneration()
	{
		ArrayList<Organism> newMembers = new ArrayList<Organism>();
		for(int i=0,stop=members.size();i<stop;i++)
		{
			if(!members.get(i).markedForDeath()) {newMembers.add(members.get(i));}
		}
		//System.out.println(newMembers.size());
		members = newMembers;
	}
	public void purge()
	{
		for(int i=0;i<members.size();i++)
		{
			members.get(i).setSpeciesID(-1);
		}
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
			if(timeSinceLastImprovement > Config.MAX_TIME_SPECIES_STAGNATION || obliterate)
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
		spawnAmount = fractionComponent+intComponent;
		sorter.sortOrganismsAdjustedFitness(members, 0, members.size()-1);
		ArrayList<Organism> culledMembers = new ArrayList<Organism>();
		int cutoff = (int)(Config.WORST_PERCENT_REMOVED*(double)members.size());
		for(int i=cutoff;i<members.size();i++)
		{
			culledMembers.add(members.get(i));
		}
		members = culledMembers;
		
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
	public int getID() {return ID;}
	public int getTimeSinceLastImprovement() {return timeSinceLastImprovement;}
	public void setTimeSinceLastImprovement(int i) {timeSinceLastImprovement=i;}
	public Organism getRepr() {return representative;}
	public Organism getBestMember() 
	{
		double bestFitness = -1.0;
		Organism bestMember = members.get(0);
		for(Organism org : members)
		{
			//if(org.isPopChamp()) {return org;}
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
	public void setObliterate(boolean i) {obliterate=i;}
	public boolean getObliterate() {return obliterate;}
}
