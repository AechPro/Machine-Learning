/*
 * Neuro-Evolution Through Augmenting Topologies (NEAT)
 * Implementation for Java 1.8.x
 * @Author: Matthew Allen
 * 
 * This file is the entry point to the program, and contains all code necessary to run the
 * NEAT algorithm with a given test unit.
 */

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

	//Constructor doesn't do much because we have an init function.
	public Main(){numTests = 1;}

	//Initialize everything.
	public void init()
	{
		//Population of organisms.
		population = new ArrayList<Organism>();

		generation = 0;
		bestFitness = 0;
		success = false;
		table = new InnovationTable();

		//This is the test unit that will be used to evaluate phenotypes and build the
		//initial structure.
		testUnit = new FishTester(rng,width,height);

		sorter = new SortingUnit();
		minimalStructure = testUnit.buildMinimalStructure(table);
		speciationUnit = new SpeciesSorter(sorter,table,rng);

		//Create our population.
		for(int i=0;i<Config.POPULATION_SIZE;i++)
		{
			Organism org = new Organism(table.getNextOrganismID());
			org.createMinimalGenotype(minimalStructure,table);
			population.add(org);
		}

		//Initial epoch 0 test to speciate first organisms.
		testPhenotypes(false);
		species = speciationUnit.speciatePopulation(population);

		timeSinceLastImprovement = 0;
		running = true;
	}

	//Method to run the algorithm for n tests each being m generations long.
	public void run()
	{
		int numVictories = 0;
		double avgGen = 0;
		double avgFitness = 0;
		double avgPopSize = 0;

		//For each complete test to run.
		for(int i=0;i<numTests;i++)
		{
			//Re-initialize everything per complete test.
			init();

			//This is the loop for a single test.
			while(running && generation < 10000)
			{
				epoch();
				generation++;
				//try{Thread.sleep(250);}
				//catch(Exception e) {e.printStackTrace();}
			}

			//If we succeeded, add to our stats.
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

	//Method to perform a single epoch.
	public void epoch()
	{
		tick();
		repopulate();
		reset();
		testPhenotypes(false);
		printOutput();
	}

	//Debug method.
	public void printOutput()
	{
		/*for(Organism org : population)
		{
			System.out.println(org.getAverageConnectionWeight()+" | "+org.getFitness());
		}*/
		System.out.println("\nGENERATION: "+generation);
		System.out.println("POP SIZE: "+population.size());
		System.out.println("SPECIES: "+species.size());
		System.out.println("AVERAGE FITNESS: "+calculateAverageAdjustedFitness());
		System.out.println("AVERAGE SPECIES AGE: "+calculateAverageSpeciesAge());
		System.out.println("AVERAGE SPECIES TSLI: "+calculateAverageSpeciesTSLI());
		System.out.println("BEST FITNESS: "+bestFitness);

		//System.out.println("--INNOVATION TABLE--\n"+table);
	}

	//Method to remove old species and organisms from the previous generation, adjust fitness
	//values and perform per-species repopulation. Inter-species mating is not implemented at this point.
	public void repopulate()
	{
		double totalExpected = 0.0;

		//Mark our old organisms for death this generation.
		if(generation%30==0)
		{
			for(int i=0,stop=species.size();i<stop;i++)
			{
				if(species.get(i).getAge()>=20)
				{
					species.get(i).setObliterate(true);
					break;
				}
			}
		}

		//Perform species specific fitness sharing.
		for(Species s : species)
		{
			s.adjustFitnessValues();
			for(Organism org : s.getMembers())
			{
				org.markForDeath();
			}
		}

		/*if(timeSinceLastImprovement >= Config.MAX_TIME_POPULATION_STAGNATION)
		{
			deltaCoding();
			return;
		}*/

		//Calculate the spawn amounts for each species.
		double avg = calculateAverageAdjustedFitness();
		for(Species s : species) 
		{
			s.calculateSpawnAmounts(avg);
			totalExpected+=Math.round(s.getSpawnAmount());
			//System.out.println(s);
		}

		//If we're off by some amount of spawns, adjust the spawn amounts of each species
		//weighted by their predicted spawn amount until we have met the config population size.
		if(totalExpected!=Config.POPULATION_SIZE)
		{
			double coeff = (Config.POPULATION_SIZE - totalExpected)/totalExpected;
			for(Species s : species)
			{
				s.setSpawnAmount(s.getSpawnAmount() + s.getSpawnAmount()*coeff);
			}
		}

		//Perform reproduction inside each species. Note that this uses the stop integer instead of species.size()
		//each loop because children get speciated every reproduction loop, so new species may be created during
		//this for loop.
		for(int i=0,stop=species.size();i<stop;i++)
		{ 
			//System.out.println(species.get(i));
			species.get(i).reproduce(table);
		}

		//Remove old generation of organisms from population.
		for(Species s : species)
		{
			s.removeOldGeneration();
			//numSpawned+=s.getMembers().size();
		}
		//System.out.println("REPRODUCTION SPAWNED "+numSpawned+" NEW MEMBERS\nEXPECTED TO SPAWN "+totalExpected+" MEMBERS");
		//System.out.println("REPRODUCTION SPAWNED "+(species.size()-s1)+" NEW SPECIES");
	}

	//Function to track population champion and global stagnation.
	public void tick()
	{
		timeSinceLastImprovement++;
		int itr = 0;

		//Color marker is for visualization in simulations.
		for(Species s : species)
		{
			itr++;
			for(Organism org : s.getMembers())
			{
				org.setColorMarker(itr);
			}

			s.tick();
		}

		//Find population champion.
		int champIndex = -1;
		for(int i=0,stop=population.size();i<stop;i++)
		{
			if(population.get(i).getFitness() > bestFitness) 
			{
				bestFitness=population.get(i).getFitness();
				timeSinceLastImprovement = 0;
				champIndex=i;
			}
		}

		//Label the population champion as such.
		if(champIndex>-1)
		{
			for(int i=0,stop=population.size();i<stop;i++)
			{
				population.get(i).setPopChamp(false);
			}
			population.get(champIndex).setPopChamp(true);
		}
	}

	//Function to reset the population and species list per epoch so we can properly
	//keep track of the new generation after reproduction.
	public void reset()
	{
		//Return if we have no population.
		if(species.size()==0){return;}
		population = new ArrayList<Organism>();

		ArrayList<Species> newSpecies = new ArrayList<Species>();
		for(Species s : species)
		{
			//Skip over species that are empty (dead).
			if(s.getMembers().size()==0) {continue;}

			//Add each organism inside surviving species to the population list.
			for(Organism org : s.getMembers())
			{
				population.add(org);
			}

			//Add surviving species to the species list.
			newSpecies.add(s);
		}
		//System.out.println("FOUND "+population.size()+" NEW MEMBERS\n");


		//Re-fill global species list. We do this because the species sorter object contains
		//a reference to this species list so re-initializing it here would reset the
		//reference and we would be unable to sort species.
		species.clear();
		for(Species s : newSpecies){species.add(s);}
		sorter.sortSpecies(species, 0, species.size()-1);
		sorter.sortOrganisms(population, 0, population.size()-1);
	}

	//Function to use the test unit to evaluate each organism in the population.
	public void testPhenotypes(boolean save)
	{
		//Test population and store victor if there is one.
		Organism victor = testUnit.testPhenotypes(population);

		//System.out.println("RUNNING PHENOTYPE TEST ON "+population.size()+" MEMBERS");
		int itr = 0;
		
		//Optionally save each phenotype in an image to view later.
		if(save)
		{
			for(Organism org : population)
			{
				if(org.getPhenotype() != null)
				{
					org.getPhenotype().saveAsImage("resources/NEAT/debug/phenotypes/phenotype_"+(++itr)+".png", 500,500);
				}
			}
		}

		//If we found a victor, print some stuff and save it.
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

	//Optional delta-coding implementation for population stagnation.
	//I have found that this is entirely destructive in all my tests, so do not use it.
	public void deltaCoding()
	{
		if(species.size()==0) {return;}
		System.out.println("PERFORMING DELTA CODING");
		timeSinceLastImprovement = 0;
		Species survivor1 = species.get(species.size()-1);
		Species survivor2 = null;
		int side1 = Config.POPULATION_SIZE/2;
		int side2 = Config.POPULATION_SIZE - side1;
		survivor1.setTimeSinceLastImprovement(0);
		if(species.size()>=2) 
		{
			survivor2 = species.get(species.size()-2);
			survivor2.setTimeSinceLastImprovement(0);
			survivor2.deltaCode(side2,false,table);
			survivor1.deltaCode(side1, true, table);
		}
		else
		{
			survivor1.deltaCode(side2+side1,true,table);
		}
		species.clear();
		if(survivor2 != null) {species.add(survivor2);}
		species.add(survivor1);
		population = new ArrayList<Organism>();
		for(Species s : species)
		{
			for(Organism org : s.getMembers())
			{
				population.add(org);
			}
		}
	}

	//Basic average calculation functions.
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

	//Actual program entry point.
	public static void main(String[] args){new Main().run();}
}