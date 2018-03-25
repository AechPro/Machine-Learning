package NEAT.Population;
import java.util.ArrayList;

import NEAT.Genes.*;
import NEAT.util.InnovationTable;
public class Genome 
{
	private int ID;
	private int inputs;
	private int outputs;
	private ArrayList<Node> nodes;
	private ArrayList<Connection> connections;
	private InnovationTable table;
	public Genome(ArrayList<Connection> cons, ArrayList<Node> ns, InnovationTable innovTable, int inputs, int outputs, int id)
	{
		table = innovTable;
		nodes = new ArrayList<Node>();
		connections = new ArrayList<Connection>();
		for(Connection c : cons) {connections.add(new Connection(c));}
		for(Node n : ns) {nodes.add(new Node(n));}
	}
	public Genome(InnovationTable innovTable, int inputs, int outputs, int id)
	{
		table = innovTable;
		nodes = new ArrayList<Node>();
		connections = new ArrayList<Connection>();
	}
	public Genome(Genome other)
	{
		duplicate(other);
	}
	public void createPhenotype()
	{
		
	}
	public void duplicate(Genome other)
	{
		
	}
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
