package NEAT.util;
import java.util.ArrayList;
import java.util.Arrays;

import NEAT.Population.*;
public class SelectionUnit 
{
	public SelectionUnit() {}
	//Elite selection requires a sorted list low->high of organisms.
	public ArrayList<Organism> eliteSelect(int num, ArrayList<Organism> pop)
	{
		ArrayList<Organism> winners = new ArrayList<Organism>();
		for(int i=pop.size()-1;i>0 && winners.size() < num;i--)
		{
			winners.add(pop.get(i));
		}
		return winners;
	}
	public ArrayList<Organism> randomSelect(int num, ArrayList<Organism> pop)
	{
		ArrayList<Organism> winners = new ArrayList<Organism>();
		if(pop.size() == 0) {return winners;}
		for(int i=0;i<num;i++)
		{
			winners.add(pop.get((int)(Math.round((pop.size()-1)*Math.random()))));
		}
		return winners;
	}
	public ArrayList<Organism> randomSelectFromPool(int num, int poolSize, ArrayList<Organism> pop)
	{
		ArrayList<Organism> winners = new ArrayList<Organism>();
		if(pop.size() == 0) {return winners;}
		for(int i=0;i<num;i++)
		{
			winners.add(pop.get((int)(Math.round((poolSize)*Math.random()))));
		}
		return winners;
	}
	
	//basic tournament selection
	public ArrayList<Organism> tournamentSelect(int num, int tournamentSize, boolean adjusted, ArrayList<Organism> pop)
	{
		ArrayList<Organism> winners = new ArrayList<Organism>();
		for(int tournamentRun=0;tournamentRun<num;tournamentRun++)
		{
			ArrayList<Integer> seen = new ArrayList<Integer>();
			Organism selection = pop.get((int)(Math.round(Math.random()*(pop.size()-1))));
			for(int i=0;i<tournamentSize;i++)
			{
				int idx = (int)(Math.round(Math.random()*(pop.size()-1)));
				while(seen.contains(idx)) {idx = (int)(Math.round(Math.random()*(pop.size()-1)));}
				seen.add(idx);
				if(adjusted)
				{
					if(pop.get(idx).getAdjustedFitness() > selection.getAdjustedFitness())
					{
						selection = pop.get(idx);
					}
				}
				else
				{
					if(pop.get(idx).getFitness() > selection.getFitness())
					{
						selection = pop.get(idx);
					}
				}
			}
			if(selection != null) {winners.add(selection);}
		}
		return winners;
	}
	
	//basic roulette wheel selection
	public ArrayList<Organism> rouletteSelect(int num, boolean adjusted, ArrayList<Organism> pop)
	{
		ArrayList<Organism> winners = new ArrayList<Organism>();
		double[] scores = new double[pop.size()];
		scores[0] = pop.get(0).getAdjustedFitness();
		if(!adjusted) {scores[0] = pop.get(0).getFitness();}
		for(int i=1;i<scores.length;i++)
		{
			if(adjusted) {scores[i] = scores[i-1] + pop.get(i).getAdjustedFitness();}
			else {scores[i] = scores[i-1] + pop.get(i).getFitness();}
		}
		for(int spins=0;spins<num;spins++)
		{
			double randSelection = Math.random()*scores[scores.length-1];
			int idx = Arrays.binarySearch(scores, randSelection);
			if(idx<0) {idx = Math.abs(idx+1);}
			winners.add(pop.get(idx));
		}
		return winners;
	}
}
