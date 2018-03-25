package NEAT.util;


import java.util.ArrayList;

import NEAT.Genes.Node;

public class InnovationTable
{
	public final int NEW_NODE = 0;
	public final int NEW_CONNECTION = 1;
	private ArrayList<int[]> innovations;
	private int currentID;
	private int currentNodeID;
	public InnovationTable()
	{
		innovations = new ArrayList<int[]>();
		currentID = 0;
		currentNodeID = 0;
	}
	public int createInnovation(int innovType, int input, int output, int nodeID, int nodeType)
	{
		if(innovType == NEW_NODE){currentNodeID++;}
		currentID++;
		int[] list = new int[]{currentID, innovType, input, output, nodeID, nodeType};
		innovations.add(list);
		return currentID;
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
		int traitIdx1 = 0;
		int traitIdx2 = 0;
		if(innovType == NEW_NODE)
			{traitIdx1 = 4;
			traitIdx2 = 5;}
		else
			{traitIdx1 = 2;
			traitIdx2 = 3;}
		for(int i=0,stop=innovations.size();i<stop;i++)
		{
			int[] list = innovations.get(i);
			if(list[traitIdx2] == trait2 && list[traitIdx1] == trait1){return list[0];}
		}
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
		return -1;
	}
	@Override
	public String toString()
	{
		String output = "ID   INTYPE   INPUT   OUTPUT   NID   NTYPE\n ";
		for(int i=0,stop=innovations.size();i<stop;i++)
		{
			int[] list = innovations.get(i);
			output += list[0]+"  ";
			if(list[1] == NEW_NODE){output += "NEW_NODE   ";}
			else{output+="NEW_CON   ";}
			output += list[2]+"       "+list[3]+"      "+list[4]+"     ";
			switch(list[5])
			{
				case(Node.HIDDEN_NODE):
					output+="HID";
					break;
				case(Node.BIAS_NODE):
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
