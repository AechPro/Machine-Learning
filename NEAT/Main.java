package NEAT;

import java.util.ArrayList;
import java.util.Random;

import NEAT.Configs.Config;
import NEAT.Population.*;
import NEAT.TestUnits.*;
import NEAT.util.*;

public class Main
{
	private static final int width = 1920, height = 1080;
	private double bestFitness;
	private ArrayList<Species> species;
	private ArrayList<Organism> population;
	private SortingUnit sorter;
	private TestUnit testUnit;
	private Genome minimalStructure;
	private int timeSinceLastImprovement;
	private int generation;
	private boolean running;
	private boolean success;
	private int numTests;
	private static InnovationTable table;
	private static final Random rng = new Random((long)(Math.random()*Long.MAX_VALUE));
	private static SpeciesSorter speciationUnit;
	public Main(){numTests = 1;}
	public void init()
	{
		population = new ArrayList<Organism>();
		
		generation = 0;
		bestFitness = 0;
		success = false;
		table = new InnovationTable();
		testUnit = new PoleTester(rng,width,height);
		sorter = new SortingUnit();
		minimalStructure = testUnit.buildMinimalStructure(table);
		speciationUnit = new SpeciesSorter(sorter,table,rng);
		for(int i=0;i<Config.POPULATION_SIZE;i++)
		{
			Organism org = new Organism(table.getNextOrganismID());
			org.createMinimalGenotype(minimalStructure,table);
			population.add(org);
		}
		testPhenotypes(false);
		species = speciationUnit.speciatePopulation(population);
		timeSinceLastImprovement = 0;
		running = true;
	}
	public void run()
	{
		int numVictories = 0;
		double avgGen = 0;
		double avgFitness = 0;
		double avgPopSize = 0;
		for(int i=0;i<numTests;i++)
		{
			init();
			while(running && generation < 100)
			{
				epoch();
				generation++;
				//try{Thread.sleep(250);}
				//catch(Exception e) {e.printStackTrace();}
			}
			if(success) 
			{
				numVictories++;
				avgGen += generation;
				avgFitness += bestFitness;
				avgPopSize = population.size();
			}
		}
		avgGen/=numVictories;
		avgFitness/=numVictories;
		avgPopSize/=numVictories;
		System.out.println("\nSuccessfully completed "+numVictories+" out of "+numTests+" tests");
		System.out.println("Average generation: "+avgGen+"\nAverage pop size: "+avgPopSize+"\nAverage fitness: "+avgFitness);
	}
	public void epoch()
	{
		repopulate();
		tick();
		reset();
		testPhenotypes(false);
		printOutput();
	}
	public void printOutput()
	{
		System.out.println("\nGENERATION: "+generation);
		System.out.println("POP SIZE: "+population.size());
		System.out.println("SPECIES: "+species.size());
		System.out.println("AVERAGE FITNESS: "+calculateAverageAdjustedFitness());
		System.out.println("AVERAGE SPECIES AGE: "+calculateAverageSpeciesAge());
		System.out.println("AVERAGE SPECIES TSLI: "+calculateAverageSpeciesTSLI());
		System.out.println("BEST FITNESS: "+bestFitness);

		//System.out.println("--INNOVATION TABLE--\n"+table);
	}
	public void repopulate()
	{
		double totalExpected = 0.0;
		for(Organism org : population)
		{
			org.markForDeath();
		}
		for(Species s : species)
		{
			s.adjustFitnessValues();
		}
		//Note that this is not in the above species loop because the global average adjusted
		//fitness must be known before spawn amounts can be calculated.
		double avg = calculateAverageAdjustedFitness();
		for(Species s : species) 
		{
			s.calculateSpawnAmounts(avg);
			totalExpected+=Math.round(s.getSpawnAmount());
			//System.out.println(s);
		}
		if(totalExpected!=Config.POPULATION_SIZE)
		{
			double coeff = (Config.POPULATION_SIZE - totalExpected)/totalExpected;
			for(Species s : species)
			{
				s.setSpawnAmount(s.getSpawnAmount() + s.getSpawnAmount()*coeff);
			}
		}
		if(timeSinceLastImprovement > Config.MAX_TIME_POPULATION_STAGNATION)
		{
			//deltaCoding();
			//resetPop();
		}
		//int numSpawned = 0;
		//int s1 = species.size();
		for(int i=0,stop=species.size();i<stop;i++)
		{ 
			//System.out.println(species.get(i));
			species.get(i).reproduce(table);
			
		}
		for(Species s : species)
		{
			s.removeOldGeneration();
			//numSpawned+=s.getMembers().size();
		}
		//System.out.println("REPRODUCTION SPAWNED "+numSpawned+" NEW MEMBERS\nEXPECTED TO SPAWN "+totalExpected+" MEMBERS");
		//System.out.println("REPRODUCTION SPAWNED "+(species.size()-s1)+" NEW SPECIES");
	}
	public void tick()
	{
		timeSinceLastImprovement++;
		int itr = 0;
		for(Species s : species)
		{
			for(Organism org : s.getMembers())
			{
				org.setColorMarker(itr++);
			}
			s.tick();
		}
		for(Organism org : population)
		{
			if(org.getFitness() > bestFitness) 
			{
				bestFitness=org.getFitness();
				timeSinceLastImprovement = 0;
			}
		}
		
	}
	public void reset()
	{
		if(species.size()==0)
		{
			return;
		}
		population = new ArrayList<Organism>();
		//System.out.println("\nRESETTING POPULATION WITH "+species.size()+" LIVING SPECIES");
		ArrayList<Species> newSpecies = new ArrayList<Species>();
		for(Species s : species)
		{
			if(s.getMembers().size()==0) {continue;}
			for(Organism org : s.getMembers())
			{
				population.add(org);
			}
			newSpecies.add(s);
		}
		//System.out.println("FOUND "+population.size()+" NEW MEMBERS\n");
		species.clear();
		for(Species s : newSpecies)
		{
			species.add(s);
		}
		sorter.sortOrganisms(population, 0, population.size()-1);
	}
	public void testPhenotypes(boolean save)
	{
		Organism victor = testUnit.testPhenotypes(population);
		//System.out.println("RUNNING PHENOTYPE TEST ON "+population.size()+" MEMBERS");
		int itr = 0;
		for(Organism org : population)
		{
			//System.out.println("ORGANISM "+org.getID()+":  "+org.getFitness());
			if(save)
			{
				if(org.getPhenotype() != null)
				{
					org.getPhenotype().saveAsImage("resources/NEAT/debug/phenotypes/phenotype_"+(++itr)+".png", 500,500);
				}
			}
		}
		
		if(victor != null)
		{
			System.out.println("\n\n******FOUND VICTOR!******");
			System.out.println("GENERATION: "+generation);
			System.out.println("POP SIZE: "+population.size());
			System.out.println(victor);
			bestFitness = victor.getFitness();
			victor.getPhenotype().saveAsImage("resources/NEAT/debug/victor/phenotype.png", 600,600);
			running = false;
			success = true;
		}
		sorter.sortOrganisms(population, 0, population.size()-1);
	}
	public void deltaCoding()
	{
		if(species.size()==0) {return;}
		//System.out.println("PERFORMING DELTA CODING");
		timeSinceLastImprovement = 0;
		Species survivor1 = species.get(species.size()-1);
		Species survivor2 = null;
		int side1 = Config.POPULATION_SIZE/2;
		int side2 = Config.POPULATION_SIZE - side1;
		for(Species s : species) {s.setSpawnAmount(0);}
		survivor1.setChampSpawns(side1);
		survivor1.setTimeSinceLastImprovement(0);
		if(species.size()>=2) 
		{
			survivor2 = species.get(species.size()-2);
			survivor2.setSpawnAmount(side2);
			survivor2.setTimeSinceLastImprovement(0);
		}
		else
		{
			survivor1.setChampSpawns(side1+side2);
		}
	}
	public double calculateAverageAdjustedFitness()
	{
		if(population.size() == 0) {return 0d;}
		double avg = 0d;
		for(Organism org : population)
		{
			avg+=org.getAdjustedFitness();
		}
		return avg/population.size();
	}
	public double calculateAverageSpeciesAge()
	{
		if(species.size() == 0) {return 0d;}
		double avg = 0d;
		for(Species s : species)
		{
			avg+=s.getAge();
		}
		return avg/species.size();
	}
	public double calculateAverageSpeciesTSLI()
	{
		if(species.size() == 0) {return 0d;}
		double avg = 0d;
		for(Species s : species)
		{
			avg+=s.getTimeSinceLastImprovement();
		}
		return avg/species.size();
	}
	public static void main(String[] args){new Main().run();}
}
