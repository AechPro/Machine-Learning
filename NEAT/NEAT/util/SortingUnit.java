package NEAT.util;
import java.util.ArrayList;

import NEAT.Genes.*;
import NEAT.Population.*;
public class SortingUnit 
{
	public SortingUnit() {}
	public void sortNodes(ArrayList<Node> nodes, int low, int high)
	{
		if(low>=high) {return;}
		int index = partitionNodes(nodes,low,high);
		if(low<index-1){sortNodes(nodes,low,index-1);}
		if(index<high){sortNodes(nodes,index,high);}
	}
	public int partitionNodes(ArrayList<Node> nodes, int low, int high)
	{
		Node temp;
		int i = low, j = high;

		double pivot = nodes.get((low+high)/2).getSplitY();

		while(i<=j)
		{
			while(nodes.get(i).getSplitY()<pivot){i++;}
			while(nodes.get(j).getSplitY()>pivot){j--;}
			if(i<=j)
			{
				temp = nodes.get(i);
				nodes.set(i,nodes.get(j));
				nodes.set(j,temp);
				i++;
				j--;
			}
		}
		return i;
	}
	
	public void sortConnections(ArrayList<Connection> cons, int low, int high)
	{
		if(low>=high) {return;}
		int index = partitionConnections(cons,low,high);
		if(low<index-1){sortConnections(cons,low,index-1);}
		if(index<high){sortConnections(cons,index,high);}
	}
	public int partitionConnections(ArrayList<Connection> cons, int low, int high)
	{
		Connection temp;
		int i = low, j = high;

		double pivot = cons.get((low+high)/2).getInnovation();

		while(i<=j)
		{
			while(cons.get(i).getInnovation()<pivot){i++;}
			while(cons.get(j).getInnovation()>pivot){j--;}
			if(i<=j)
			{
				temp = cons.get(i);
				cons.set(i,cons.get(j));
				cons.set(j,temp);
				i++;
				j--;
			}
		}
		return i;
	}
	
	public void sortOrganisms(ArrayList<Organism> orgs, int low, int high)
	{
		if(low>=high) {return;}
		int index = partitionOrganisms(orgs,low,high);
		if(low<index-1){sortOrganisms(orgs,low,index-1);}
		if(index<high){sortOrganisms(orgs,index,high);}
	}
	public int partitionOrganisms(ArrayList<Organism> orgs, int low, int high)
	{
		Organism temp;
		int i = low, j = high;

		double pivot = orgs.get((low+high)/2).getFitness();

		while(i<=j)
		{
			while(orgs.get(i).getFitness()<pivot){i++;}
			while(orgs.get(j).getFitness()>pivot){j--;}
			if(i<=j)
			{
				temp = orgs.get(i);
				orgs.set(i,orgs.get(j));
				orgs.set(j,temp);
				i++;
				j--;
			}
		}
		return i;
	}
	
	public void sortOrganismsAdjustedFitness(ArrayList<Organism> orgs, int low, int high)
	{
		if(low>=high) {return;}
		int index = partitionOrganismsAdjustedFitness(orgs,low,high);
		if(low<index-1){sortOrganismsAdjustedFitness(orgs,low,index-1);}
		if(index<high){sortOrganismsAdjustedFitness(orgs,index,high);}
	}
	public int partitionOrganismsAdjustedFitness(ArrayList<Organism> orgs, int low, int high)
	{
		Organism temp;
		int i = low, j = high;

		double pivot = orgs.get((low+high)/2).getAdjustedFitness();

		while(i<=j)
		{
			while(orgs.get(i).getAdjustedFitness()<pivot){i++;}
			while(orgs.get(j).getAdjustedFitness()>pivot){j--;}
			if(i<=j)
			{
				temp = orgs.get(i);
				orgs.set(i,orgs.get(j));
				orgs.set(j,temp);
				i++;
				j--;
			}
		}
		return i;
	}
}
