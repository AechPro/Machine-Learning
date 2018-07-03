package NEAT.TestUnits;

import java.util.ArrayList;
import java.util.Random;
import NEAT.Population.*;
import NEAT.util.InnovationTable;

public abstract class TestUnit 
{
	protected int numInputs = 2;
	protected int numOutputs = 1;
	protected int numBiasNodes = 1;
	protected int numHiddenNodes = 0;
	protected boolean victor;
	protected Random randf;
	protected int width, height;
	public TestUnit(Random rng,int windowWidth,int windowHeight)
	{
		width = windowWidth;
		height = windowHeight;
		randf  = rng;
		victor = false;
	}
	public abstract Genome buildMinimalStructure(InnovationTable table);
	public abstract Genome buildMinimalSolution(InnovationTable table);
	public abstract Organism testPhenotypes(ArrayList<Organism> population);
	public boolean validatePhenotype(Phenotype phen)
	{
		return !(phen.getOutputNodes().size() == 0 || 
				phen.getDepth() == -1 || phen == null);
	}
}
