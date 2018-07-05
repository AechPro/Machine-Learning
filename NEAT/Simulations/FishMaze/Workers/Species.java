package NEAT.Simulations.FishMaze.Workers;

import java.util.ArrayList;
import java.util.Arrays;

import evolution.Genomes.*;

public class Species
{
	private double bestFitness;
	private NEATGenome bestMember;
	private ArrayList<NEATGenome> members;
	private int ID;
	private int epochsSinceImprovement;
	private int age;
	private int numToSpawn;
	private double historicalBestFitness;
	private int oldThresh, youngThresh;
	private ArrayList<NEATGenome[]> parentSelections;
	private ArrayList<NEATGenome> children;
	public Species(NEATGenome firstMember, int id)
	{
		members = new ArrayList<NEATGenome>();
		ID = id;
		age = 0;
		numToSpawn = 0;
		epochsSinceImprovement = 0;
		historicalBestFitness = 0.0;
		youngThresh = 10;
		oldThresh = 50;
		setBestMember(firstMember);
		addMember(firstMember);
	}
	public NEATGenome selectMember()
	{
		double[] scores = new double[members.size()];
		scores[0] = members.get(0).getAdjustedFitness();
		for(int i=1;i<scores.length;i++)
		{
			scores[i] = scores[i-1] + members.get(i).getAdjustedFitness();
		}
		double randSelection = Math.random()*scores[scores.length-1];
		int idx = Arrays.binarySearch(scores, randSelection);
		if(idx<0) {idx = Math.abs(idx+1);}
		return members.get(idx);
	}
	public void reproduce(double crossover)
	{
		NEATGenome child = null;
		parentSelections = new ArrayList<NEATGenome[]>();
		children = new ArrayList<NEATGenome>();
		for(int spawnNum = 0,stop=(int)(Math.round(getNumToSpawn())); spawnNum<stop;spawnNum++)
		{
			if(members.size()==1) {children.add(new NEATGenome(members.get(0))); continue;}
			NEATGenome p1 = selectMember();
			NEATGenome p2 = selectMember();
			if(p1.equals(p2))
			{
				child = new NEATGenome(p1);
				children.add(child);
			}
			else
			{
				if(Math.random()<crossover)
				{
					parentSelections.add(new NEATGenome[] {p1,p2});
				}
				else
				{
					if(p2.getAdjustedFitness() > p1.getAdjustedFitness()) {p1 = p2;}
					child = new NEATGenome(p1);
					children.add(child);
				}
			}
		}
	}
	public void calculateSpawns(double avg)
	{
		double val = 0;
		numToSpawn = 0;
		for(NEATGenome member : members)
		{
			val = member.getFitness()/avg;
			numToSpawn+=val;
			member.setNumChildren(val);
		}
	}
	public void purge()
	{
		for(NEATGenome mem : members)
		{
			mem.setSpecies(-1);
		}
		members = new ArrayList<NEATGenome>();
	}
	public void addMember(NEATGenome member)
	{
		member.setSpecies(ID);
		members.add(member);
	}
	public void findBestMember()
	{
		double bestF = 0.0;
		for(NEATGenome g : members)
		{
			if(g.getFitness() > bestF)
			{
				setBestMember(g);
			}
		}
	}
	private void setBestMember(NEATGenome member)
	{
		
		if(member.getFitness() > historicalBestFitness)
		{
			epochsSinceImprovement = 0;
			historicalBestFitness = member.getFitness();
		}
		bestMember = new NEATGenome(member);
		bestFitness = bestMember.getFitness();
	}
	public double getAvgFitness(boolean adjusted)
	{
		double avg = 0;
		for(NEATGenome mem : members)
		{
			if(adjusted){avg+=mem.getAdjustedFitness();}
			else{avg+=mem.getFitness();}
		}
		avg/=members.size();
		return avg;
	}
	public void adjustFitness()
	{
		double fitness = 0;
		double size = members.size();
		for(NEATGenome mem : members)
		{
			fitness = mem.getFitness();
			if(fitness > bestFitness){setBestMember(mem);}
			if(age<youngThresh){fitness *= 1.3;}
			else if(age>oldThresh){fitness*=0.7;}
			mem.setAdjustedFitness(fitness/size);
		}
		sort(0,members.size()-1);
	}
	public String toString()
	{
		String output = "\nSPECIES "+ID+"\n";
		output+="Genome ID        Fitness        Adjusted Fitness        Spawn Amount\n";
		for(int i=0,stop=members.size();i<stop;i++)
		{
			output+=members.get(i).getGenomeID()+"              "+Math.round(100.0*members.get(i).getFitness())/100.0
					+"             "+Math.round(100.0*members.get(i).getAdjustedFitness())/100.0+"                    "+Math.round(100.0*members.get(i).getNumChildren())/100.0;
			output+="\n";
		}
		output+="Spawn amounts for this species: "+getNumToSpawn();
		output+="\nMembers in this species: "+members.size();
		output+="\nBest member for this species:"+bestMember.toString(0);
		return output;
	}
	
	public void sort(int low, int high)
	{
		if(low >= high) {return;}
		int index = partition(low,high);
		if(low<index-1){sort(low,index-1);}
		if(index<high){sort(index,high);}
	}
	public int partition(int low, int high)
	{
		NEATGenome temp;
		int i = low, j = high;
		
		double pivot = members.get((low+high)/2).getFitness();
		
		while(i<=j)
		{
			while(members.get(i).getFitness()<pivot){i++;}
			while(members.get(j).getFitness()>pivot){j--;}
			if(i<=j)
			{
				temp = members.get(i);
				members.set(i,members.get(j));
				members.set(j,temp);
				i++;
				j--;
			}
		}
		return i;
	}
	public ArrayList<NEATGenome> getMembers(){return members;}
	public double getNumToSpawn()
	{
		double num = 0;
		for(int i=0;i<members.size();i++)
		{
			num+=members.get(i).getNumChildren();
		}
		return num;
	}
	public int timeSinceImprovement(){return epochsSinceImprovement;}
	public NEATGenome getBestMember(){return bestMember;}
	public void incrementAge(){age++; epochsSinceImprovement++;}
	public int getID(){return ID;}
	public ArrayList<NEATGenome[]> getParentSelections() {return parentSelections;}
	public ArrayList<NEATGenome> getChildren() {return children;}
	
}
