package NEAT.util;
import NEAT.Configs.Config;
import NEAT.Population.*;
import java.util.ArrayList;
import java.util.Random;
public class SpeciesSorter 
{
	private SortingUnit sorter;
	private InnovationTable table;
	private Random rng;
	private ArrayList<Species> species;
	public SpeciesSorter(SortingUnit s, InnovationTable t, Random r)
	{
		sorter = s;
		table = t;
		rng = r;
	}
	public void speciateOrganism(Organism org, boolean sort)
	{
		if(org.getSpeciesID() >= 0) {return;}
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
			//System.out.println("CREATED NEW SPECIES FOR ORG "+org.getID());
			Species newSpecies = new Species(org,this,rng,table.getNextSpeciesID());
			species.add(newSpecies);
		}
		if(sort)
		{
			sorter.sortSpecies(species,0,species.size()-1);
		}
		//System.out.println("ORGANISM "+org.getID()+" PLACED INTO SPECIES "+org.getSpeciesID());
		
	}
	public ArrayList<Species> speciatePopulation(ArrayList<Organism> population)
	{
		species = new ArrayList<Species>();
		for(Organism org : population)
		{
			speciateOrganism(org,false);
		}
		sorter.sortSpecies(species,0,species.size()-1);
		return species;
	}
	public Species getRandomSpecies()
	{
		if(species == null || species.size() == 0) {return null;}
		return species.get((int)(Math.round(Math.random()*(species.size()-1))));
	}
}
