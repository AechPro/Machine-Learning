package NEAT.util;


import java.util.ArrayList;

import NEAT.Genes.Neuron;
import NEAT.Genes.Node;

public class InnovationTable
{
	public final int NEW_NODE = 0;
	public final int NEW_CONNECTION = 1;
	public final int NEW_FILTER = 2;
	
	private ArrayList<int[]> innovations;
	private int currentID;
	private int currentNodeID;
	private int currentSpeciesID;
	private int currentOrganismID;
	private int currentGenomeID;
	private int currentFilterID;
	public InnovationTable()
	{
		innovations = new ArrayList<int[]>();
		currentFilterID = 0;
		currentID = 0;
		currentNodeID = 0;
		currentGenomeID = 0;
		currentOrganismID = 0;
		currentGenomeID = 0;
	}
	public int createInnovation(int innovType, int input, int output, int nodeID, int nodeType)
	{
		if(innovType == NEW_NODE){currentNodeID++;}
		currentID++;
		int[] list = new int[]{currentID, innovType, input, output, nodeID, nodeType};
		innovations.add(list);
		return currentID;
	}
	public int createFilter(int input, int output)
	{
	    currentID++;
	    currentNodeID++;
	    int[] list = new int[] {currentID, NEW_FILTER, input, output, currentNodeID,-1};
	    innovations.add(list);
	    return currentNodeID;
	}
	public int createConnection(int input, int output)
	{
		currentID++;
		int[] list = new int[]{currentID, NEW_CONNECTION, input, output, -1,-1};
		innovations.add(list);
		//System.out.println(toString());
		return currentID;
	}
	public int createNode(int inp, int out,int nodeType)
	{
		currentID++;
		currentNodeID++;
		int[] list = new int[]{currentID, NEW_NODE, inp, out, currentNodeID, nodeType};
		innovations.add(list);
		//System.out.println(toString());
		return currentNodeID;
	}
	public int checkInnovation(int innovType, int trait1, int trait2)
	{
		for(int i=0,stop=innovations.size();i<stop;i++)
		{
			int[] list = innovations.get(i);
			if(list[3] == trait2 && list[2] == trait1 && list[1] == innovType){return list[0];}
		}
		return -1;
	}
	public int getFilterID(int innovNum)
	{
	    for(int i=0,stop=innovations.size();i<stop;i++)
        {
            int[] list = innovations.get(i);
            if(list[0] == innovNum && list[1] == NEW_FILTER)
            {
                return list[4];
            }
        }
        System.out.println("RETURNING BAD FILTER ID FOR INNOV "+innovNum);
        return -1;
	}
	public int getNodeID(int innovNum)
	{
		for(int i=0,stop=innovations.size();i<stop;i++)
		{
			int[] list = innovations.get(i);
			if(list[0] == innovNum && list[1] == NEW_NODE)
			{
				return list[4];
			}
		}
		System.out.println("RETURNING BAD NODE ID FOR INNOV "+innovNum);
		return -1;
	}
	public int getNextOrganismID() {return currentOrganismID++;}
	public int getNextGenomeID() {return currentGenomeID++;}
	public int getNextSpeciesID() {return currentSpeciesID++;}
	@Override
	public String toString()
	{
		String output = "ID   INTYPE   INPUT   OUTPUT   NID   NTYPE\n ";
		for(int i=0,stop=innovations.size();i<stop;i++)
		{
			int[] list = innovations.get(i);
			output += list[0]+"  ";
			if(list[1] == NEW_NODE){output += "NEW_NODE   ";}
			else if(list[1] == NEW_CONNECTION){output+="NEW_CON   ";}
			else {output+="NEW_FIL   ";}
			output += list[2]+"       "+list[3]+"      "+list[4]+"     ";
			switch(list[5])
			{
				case(Neuron.HIDDEN_NEURON):
					output+="HID";
					break;
				case(Neuron.BIAS_NEURON):
					output+="BIA";
					break;
				case(Node.INPUT_NODE):
					output+="INP";
					break;
				case(Node.OUTPUT_NODE):
					output+="OUT";
					break;
				default:
					output+="NONE";
					break;
			}
			output+="\n ";
		}
		return output;
	}
}
