package NEAT;

import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;

import NEAT.Display.*;
import NEAT.Population.*;
import NEAT.Genes.*;
import NEAT.util.*;

public class Main
{
	private static final int width = 1920, height = 1080;
	private double bestFitness;
	private ArrayList<Phenotype> phenotypes;
	private ArrayList<Species> species;
	private ArrayList<Organism> population;
	private ArrayList<DisplayObject> displayObjects;
	private SortingUnit sorter;
	private XORTester testUnit;
	private Genome minimalStructure;
	private int timeSinceLastImprovement;
	private int age;
	private int generation;
	private boolean running;
	private int stagnationTime;
	private boolean success;
	private int numTests;
	private static InnovationTable table;
	private static final Random rng = new Random((long)(Math.random()*Long.MAX_VALUE));
	public Main()
	{
		init();
	}
	public void init()
	{
		population = new ArrayList<Organism>();
		phenotypes = new ArrayList<Phenotype>();
		species = new ArrayList<Species>();
		displayObjects = new ArrayList<DisplayObject>();
		numTests = 1000;
		generation = 0;
		bestFitness = 0;
		success = false;
		age = 0;
		table = new InnovationTable();
		testUnit = new XORTester(rng);
		sorter = new SortingUnit();
		minimalStructure = testUnit.buildMinimalStructure(table);
		stagnationTime = Config.MAX_TIME_SPECIES_STAGNATION + 5;
		
		for(int i=0;i<Config.POPULATION_SIZE;i++)
		{
			Organism org = new Organism(table.getNextOrganismID());
			org.createMinimalGenotype(minimalStructure,table);
			population.add(org);
		}
		testPhenotypes(false);
		timeSinceLastImprovement = 0;
		//setupWindow();
		running = true;
	}
	public void run()
	{
		int numVictories = 0;
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
			if(success) {numVictories++;}
		}
		System.out.println("Successfully completed "+numVictories+" out of "+numTests+" tests");
		
	}
	public void epoch()
	{
		reset();
		speciate();
		tick();
		repopulate();
		testPhenotypes(false);
		//printOutput();
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
		ArrayList<Organism> newPop = new ArrayList<Organism>();
		for(Species s : species)
		{ 
			s.reproduce(table);
			for(Organism org : s.getMembers())
			{
				newPop.add(org);
			}
		}
		population = newPop;
		sorter.sortOrganisms(population, 0, population.size()-1);
	}
	public void speciate()
	{
		ArrayList<Species> activeSpecies = new ArrayList<Species>();
		
		for(Organism org : population)
		{
			boolean found = false;
			for(Species s : species)
			{
				if(org.calculateCompatibility(s.getRepr()) < Config.SPECIES_COMPAT_THRESHOLD)
				{
					found = true;
					s.addMember(org);
					break;
				}
			}
			if(!found)
			{
				Species newSpecies = new Species(org,rng,table.getNextSpeciesID());
				species.add(newSpecies);
			}
		}
		for(Species s : species) 
		{
			if(s.getMembers().size() > 0)
			{
				activeSpecies.add(s);
			}
		}
		species = activeSpecies;
		sorter.sortSpecies(species,0,species.size()-1);
	}
	public void tick()
	{
		age++;
		timeSinceLastImprovement++;
		
		for(Species s : species)
		{
			s.tick();
			s.adjustFitnessValues();
		}
		
		//Note that this is not in the above species loop because the global average adjusted
		//fitness must be known before spawn amounts can be calculated.
		double avg = calculateAverageAdjustedFitness();
		for(Species s : species) 
		{
			s.calculateSpawnAmounts(avg);
			//System.out.println(s);
		}
		/*if(timeSinceLastImprovement > stagnationTime)
		{
			resetPop();
			stagnationTime = age;
			age = 0;
		}*/
	}
	public void reset()
	{
		if(species.size()==0)
		{
			return;
		}
		ArrayList<Species> newSpecies = new ArrayList<Species>();
		for(Species s : species)
		{
			if(s.getMembers().size()==0) {continue;}
			s.purge();
			newSpecies.add(s);
		}
		species = newSpecies;
	}
	public void testPhenotypes(boolean save)
	{
		displayObjects.clear();
		double fitness = 0d;
		int itr = 0;
		for(Organism org : population)
		{
			org.createPhenotype(width/2,height/2);
			fitness = testUnit.testPhenotype(org.getPhenotype());
			if(fitness > bestFitness) 
			{
				bestFitness = fitness;
				timeSinceLastImprovement=0;
			}
			org.setFitness(fitness);
			displayObjects.add(org.getPhenotype());
			if(save)
			{
				org.getPhenotype().saveAsImage("resources/NEAT/debug/phenotypes/phenotype_"+(++itr)+".png", 600,600);
			}
			if(testUnit.victor)
			{
				System.out.println("\n\n******FOUND VICTOR!******");
				System.out.println("GENERATION: "+generation+org);
				org.getPhenotype().saveAsImage("resources/NEAT/debug/victor/phenotype.png", 600,600);
				running = false;
				success = true;
			}
		}
	}
	public void resetPop()
	{
		Organism globalChampion = species.get(0).getBestMember();
		for(Species s : species)
		{
			if(s.getBestMember().getFitness() > globalChampion.getFitness())
			{
				globalChampion = s.getBestMember();
			}
		}
		species = new ArrayList<Species>();
		population = new ArrayList<Organism>();
		for(int i=1;i<Config.POPULATION_SIZE;i++)
		{
			Organism org = new Organism(table.getNextOrganismID());
			org.createMinimalGenotype(minimalStructure,table);
			population.add(org);
		}
		population.add(globalChampion);
		speciate();
		timeSinceLastImprovement = 0;
	}
	public void deltaCoding()
	{
		Species survivor1 = species.get(species.size()-1);
		Species survivor2 = null;
		int side1 = Config.POPULATION_SIZE/2;
		int side2 = Config.POPULATION_SIZE - side1;
		survivor1.setSpawnAmount(0);
		survivor1.setChampSpawns(side1);
		if(species.size()>=2) 
		{
			survivor2 = species.get(species.size()-2);
			survivor2.setSpawnAmount(side2);
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
	public void setupWindow()
	{
		JFrame f = new JFrame("NEAT");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(new Window(width,height,60,displayObjects));
		f.setSize(width,height);
		f.setVisible(true);
	}
	public static void main(String[] args){new Main().run();}
}
