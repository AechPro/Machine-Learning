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
	private int generation;
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
		generation = 0;
		table = new InnovationTable();
		testUnit = new XORTester(rng);
		sorter = new SortingUnit();
		Genome minimalStructure = testUnit.buildMinimalStructure(table);
		for(int i=0;i<Config.POPULATION_SIZE;i++)
		{
			Organism org = new Organism(table.getNextOrganismID());
			org.createMinimalGenotype(minimalStructure,table);
			population.add(org);
		}
		//setupWindow();
		run();
	}
	public void run()
	{
		boolean running = true;
		while(running)
		{
			epoch();
			generation++;
			try{Thread.sleep(250);}
			catch(Exception e) {e.printStackTrace();}
		}
	}
	public void epoch()
	{
		reset();
		speciate();
		tick();
		repopulate();
		printOutput();
		testPhenotypes(generation%20==0);
	}
	public void printOutput()
	{
		System.out.println("\nGENERATION: "+generation);
		System.out.println("POP SIZE: "+population.size());
		System.out.println("SPECIES: "+species.size());
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
	}
	public void tick()
	{
		double avg = calculateAverageFitness();
		for(Species s : species)
		{
			s.tick();
			s.adjustFitnessValues();
			s.calculateSpawnAmounts(avg);
			System.out.println(s);
		}
		//for(Organism org : population) {System.out.println(org);}
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
		for(Species s : species)
		{
			for(Organism org : s.getMembers())
			{
				org.createPhenotype(width/2,height/2);
				fitness = testUnit.testPhenotype(org.getPhenotype());
				if(fitness > bestFitness) {bestFitness = fitness;}
				org.setFitness(fitness);
				displayObjects.add(org.getPhenotype());
				if(save)
				{
					org.getPhenotype().saveAsImage("resources/NEAT/debug/phenotypes/phenotype_"+(++itr)+".png", 600,600);
				}
				
			}
		}
	}
	public double calculateAverageFitness()
	{
		if(population.size() == 0) {return 0d;}
		double avg = 0d;
		for(Organism org : population)
		{
			avg+=org.getFitness();
		}
		return avg/population.size();
	}
	public void setupWindow()
	{
		JFrame f = new JFrame("NEAT");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(new Window(width,height,60,displayObjects));
		f.setSize(width,height);
		f.setVisible(true);
	}
	public static void main(String[] args){new Main();}
}
