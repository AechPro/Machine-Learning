package NEAT.Population;

import java.util.*;

import NEAT.Population.*;
public class Species 
{
	private Organism bestMember;
	private ArrayList<Organism> members;
	private Random rand;
	public Species(Organism first, Random rng)
	{
		rand = rng;
		bestMember = new Organism(first);
		members = new ArrayList<Organism>();
		members.add(first);
	}
}
