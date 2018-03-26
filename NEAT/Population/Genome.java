package NEAT.Population;
import java.util.ArrayList;
import java.util.Random;

import NEAT.Genes.*;
import NEAT.util.*;
public class Genome 
{
	private int ID;
	private int inputs;
	private int outputs;
	private ArrayList<Node> nodes;
	private ArrayList<Connection> connections;
	private InnovationTable table;
	private Random rand;
	
	//Constructor for minimal structure
	public Genome(ArrayList<Connection> cons, ArrayList<Node> ns, InnovationTable innovTable, Random rng, int inputs, int outputs)
	{
		rand = rng;
		table = innovTable;
		ID = table.getNextGenomeID();
		nodes = new ArrayList<Node>();
		connections = new ArrayList<Connection>();
		for(Connection c : cons) {connections.add(new Connection(c));}
		for(Node n : ns) {nodes.add(new Node(n));}
	}
	//Constructor for creating a genome from child genes
	public Genome(ArrayList<Connection> cons, InnovationTable innovTable, Random rng, int inputs, int outputs)
	{
		rand = rng;
		table = innovTable;
		ID = table.getNextGenomeID();
		nodes = new ArrayList<Node>();
		connections = new ArrayList<Connection>();
		for(Connection c : cons) 
		{
			Node inp = null;
			Node out = null;
			if(!duplicateNode(c.getInput())) 
			{
				inp = new Node(c.getInput());
				nodes.add(inp);
			}
			else{inp = nodes.get(getNodeIndex(c.getInput()));}
			if(!duplicateNode(c.getOutput())) 
			{
				out = new Node(c.getOutput());
				nodes.add(out);
			}
			else {out = nodes.get(getNodeIndex(c.getOutput()));}
			if(out == null || inp == null)
			{
				System.out.println("ERROR TRYING TO FIND NODES FOR CONNECTION WHEN CREATING NEW GENOME");
				System.out.println(c);
				System.exit(0);
			}
			Connection con = new Connection(inp,out,c.getWeight(),c.isEnabled(),c.getInnovation());
			connections.add(con);
		}
	}
	//Constructor for blank genome
	public Genome(InnovationTable innovTable, Random rng, int inputs, int outputs)
	{
		rand = rng;
		table = innovTable;
		ID = table.getNextGenomeID();
		nodes = new ArrayList<Node>();
		connections = new ArrayList<Connection>();
	}
	//Copy constructor
	public Genome(Genome other)
	{
		duplicate(other);
	}
	public void addConnection(InnovationTable table)
	{
		double mutationRate = Config.CONNECTION_ADD_CHANCE;
		int maxAttempts = Config.MAX_ATTEMPTS_ADD_CONNECTION;
		if(Math.random()>mutationRate){return;}
		Node n1 = null;
		Node n2 = null;
		int inpIdx = -1;
		int outIdx = -1;
		boolean found = false;
		while(maxAttempts-->0)
		{
			inpIdx = rand.nextInt(nodes.size());
			outIdx = rand.nextInt(nodes.size()-inputs) + inputs;
			if(outIdx == inpIdx){continue;}
			n1 = nodes.get(inpIdx);
			n2 = nodes.get(outIdx);
			if(n2.getType() == Node.INPUT_NODE || n1.equals(n2) || duplicateConnection(n1.getID(),n2.getID())
					|| n2.getType() == Node.BIAS_NODE || n1.getType() == Node.OUTPUT_NODE){continue;}
			else
			{
				maxAttempts = 0;
				found = true;
				break;
			}
		}
		if(n1 == null || n2 == null || !found){return;}
		int id = table.checkInnovation(table.NEW_CONNECTION, n1.getID(),n2.getID());
		if(id==-1){id = table.createConnection(n1.getID(),n2.getID());}
		Connection newCon = new Connection(n1, n2, rand.nextGaussian(), true, id);
		connections.add(newCon);
	}
	public void addNode(InnovationTable table)
	{
		double mutationRate = Config.NODE_ADD_CHANCE;
		int maxAttempts = Config.MAX_ATTEMPTS_ADD_NODE;
		if(Math.random()>mutationRate){return;}
		
		boolean foundSplit = false;
		int sizeThresh = inputs+outputs+5;
		int idx = 0;
		Node inp;
		
		while(maxAttempts-->0 && !foundSplit)
		{
			if(connections.size()<sizeThresh){idx = rand.nextInt(connections.size()-(int)(Math.sqrt(connections.size())));}
			else{idx = rand.nextInt(connections.size());}
			//idx = rand.nextInt(connections.size()-1);
			inp = connections.get(idx).getInput();
			
			if(inp.getType() != Node.BIAS_NODE && connections.get(idx).isEnabled()){foundSplit = true;}
		}
		
		if(!foundSplit){return;}
		
		connections.get(idx).setEnable(false);
		
		double oldWeight = connections.get(idx).getWeight();
		
		Node out = connections.get(idx).getOutput();
		inp = connections.get(idx).getInput();
		
		double newDepth = (out.getSplitY() + inp.getSplitY())/2;
		double newWidth = (out.getSplitX() + inp.getSplitX())/2;
		
		int id = table.checkInnovation(table.NEW_NODE,inp.getID(),out.getID());
		int nodeID = -1;
		if(id>=0)
		{
			nodeID = table.getNodeID(id);
			if(duplicateNode(nodeID)){id=-1;}
		}
		if(id < 0)
		{
			nodeID = table.createNode(inp.getID(), out.getID(),Node.HIDDEN_NODE);
			Node n = new Node(newWidth, newDepth, Node.HIDDEN_NODE, nodeID);
			nodes.add(n);
			int link1ID = table.createConnection(inp.getID(), n.getID());
			int link2ID = table.createConnection(n.getID(), out.getID());
			Connection con1 = new Connection(inp,n,1.0,true,link1ID);
			Connection con2 = new Connection(n,out,oldWeight,true,link2ID);
			connections.add(con1);
			connections.add(con2);
		}
		else
		{
			nodeID = table.getNodeID(id);
			Node n = new Node(newWidth, newDepth, Node.HIDDEN_NODE, nodeID);
			nodes.add(n);
			int link1ID = table.checkInnovation(table.NEW_CONNECTION, inp.getID(), n.getID());
			int link2ID = table.checkInnovation(table.NEW_CONNECTION, n.getID(), out.getID());
			Connection con1 = new Connection(inp,n,1.0,true,link1ID);
			Connection con2 = new Connection(n,out,oldWeight,true,link2ID);
			connections.add(con1);
			connections.add(con2);
		}
	}
	public void mutateWeights()
	{
		double mutationRate = Config.WEIGHT_MUTATION_RATE;
		double replaceProb = Config.WEIGHT_REPLACEMENT_RATE;
		double maxPerturb = Config.MAX_MUTATION_PERTURBATION;
		for(Connection con : connections)
		{
			if(con.getInput().getType() == Node.BIAS_NODE){continue;}
			if(Math.random()<mutationRate)
			{
				double mutationValue = new Random().nextGaussian();
				con.setWeight(con.getWeight() + mutationValue*maxPerturb);
			}
			else if(Math.random()<replaceProb)
			{
				double mutationValue = new Random().nextGaussian();
				con.setWeight(mutationValue);
			}
		}
	}
	public void mutateNode()
	{
		double mutationRate = Config.ACTIVATION_RESPONSE_MUTATION_RATE;
		double maxPerturb = Config.MAX_MUTATION_PERTURBATION;
		for(Node n : nodes)
		{
			if(Math.random()<mutationRate)
			{
				double mutationValue = new Random().nextGaussian()*maxPerturb;
				n.setActivationResponse(n.getActivationResponse()+mutationValue);
			}
		}
	}
	public boolean duplicateConnection(Connection c){return duplicateConnection(c.getInput().getID(), c.getOutput().getID());}
	public boolean duplicateConnection(int in, int out)
	{
		for(Connection c : connections)
		{
			if(c.getInput().getID() == in && c.getOutput().getID() == out) {return true;}
		}
		return false;
	}
	public boolean duplicateNode(Node n){return duplicateNode(n.getID());}
	public boolean duplicateNode(int id)
	{
		for(Node n : nodes)
		{
			if(n.getID() == id) {return true;}
		}
		return false;
	}
	public int getNodeIndex(Node n)
	{
		return getNodeIndex(n.getID());
	}
	public int getNodeIndex(int id)
	{
		for(int i=0,stop=nodes.size();i<stop;i++)
		{
			if(nodes.get(i).getID() == id) {return i;}
		}
		return -1;
	}
	public void randomize()
	{
		for(Connection c : connections)
		{
			c.setWeight(rand.nextGaussian());
		}
		for(Node n : nodes)
		{
			n.setActivationResponse(rand.nextGaussian());
		}
	}
	public void duplicate(Genome other)
	{
		nodes = new ArrayList<Node>();
		connections = new ArrayList<Connection>();
		for(Connection c : other.getConnections()) {connections.add(new Connection(c));}
		for(Node n : other.getNodes()) {nodes.add(new Node(n));}
		ID = other.getID();
		inputs = other.getInputs();
		outputs = other.getOutputs();
		table = other.getTable();
		rand = other.getRand();
	}
	public String toString(int verbosity)
	{
		if(verbosity == 0) {return toString();}
		String output = "\nGENOME "+ID;
		output+="\nNUM CONNECTIONS: "+connections.size();
		output+="\nNUM NODES: "+nodes.size();
		output+="\nGENOME CONNECTIONS\n";
		for(Connection c : connections)
		{
			output+=c+"\n";
		}
		output+="\nGENOME NODES\n";
		for(Node n : nodes)
		{
			output+=n+"\n";
		}
		return output;
	}
	@Override
	public String toString()
	{
		String output = "\nGENOME "+ID;
		output+="\nCONNECTIONS: "+connections.size();
		output+="\nNODES: "+nodes.size();
		return output;
	}
	public Random getRand() {return rand;}
	public void setRand(Random rng) {rand = rng;}
	public int getID() {return ID;}
	public void setID(int iD) {ID = iD;}
	public int getInputs() {return inputs;}
	public void setInputs(int inputs) {this.inputs = inputs;}
	public int getOutputs() {return outputs;}
	public void setOutputs(int outputs) {this.outputs = outputs;}
	public ArrayList<Node> getNodes() {return nodes;}
	public void setNodes(ArrayList<Node> nodes) {this.nodes = nodes;}
	public ArrayList<Connection> getConnections() {return connections;}
	public void setConnections(ArrayList<Connection> connections) {this.connections = connections;}
	public InnovationTable getTable() {return table;}
	public void setTable(InnovationTable table) {this.table = table;}
}
