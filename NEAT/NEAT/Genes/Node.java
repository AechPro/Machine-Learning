package NEAT.Genes;

import java.util.ArrayList;

public class Node
{
	public static final int INPUT_NODE = 0;
	public static final int HIDDEN_NODE = 1;
	public static final int OUTPUT_NODE = 2;
	public static final int BIAS_NODE = 3;
	private int type;
	private int id;
	private int activationCount;
	private boolean active;
	private double activeOutput;
	private double activationResponse;
	private double splitX, splitY;
	private double inactiveActiveOutput;
	private int x,y;
	private ArrayList<Connection> inputs, outputs;
	
	public Node(double sx, double sy, int nt, int nid)
	{
		x = 0;
		y = 0;
		activationCount = 0;
		activeOutput = 0.0d;
		active = false;
		splitX = sx;
		splitY = sy;
		type = nt;
		id = nid;
		inactiveActiveOutput = 0;
		activationResponse = 1d;
		inputs = new ArrayList<Connection>();
		outputs = new ArrayList<Connection>();
	}
	public Node(Node other)
	{
		x = 0;
		y = 0;
		activeOutput = other.getActiveOutput();
		activationCount = other.getActivationCount();
		active = other.isActive();
		splitX = other.getSplitX();
		splitY = other.getSplitY();
		type = other.getType();
		id = other.getID();
		inactiveActiveOutput = other.getInactiveOutput();
		activationResponse = other.getActivationResponse();
		inputs = new ArrayList<Connection>();
		outputs = new ArrayList<Connection>();
	}
	
	@Override
	public boolean equals(Object other)
	{
		if(other == null){return false;}
		if(!Node.class.isAssignableFrom(other.getClass())){return false;}
		final Node a = (Node)other;
		return a.getID() == getID() && a.getType() == getType();
	}
	@Override
	public String toString()
	{
		String repr = "";
		switch(type)
		{
			case BIAS_NODE:
				repr+="Bias Node";
				break;
			case INPUT_NODE:
				repr+="Input Node";
				break;
			case OUTPUT_NODE:
				repr+="Output Node";
				break;
			case HIDDEN_NODE:
				repr+="Hidden Node";
				break;
		}
		repr+=" | ID = "+id+" | Active = "+isActive()+" | Count = "+getActivationCount();
		repr+=" | Raw Value = "+Math.round(getInactiveOutput()*100)/100.0+" | Active Value = "+Math.round(getActiveOutput()*100)/100d;
		repr+=" | Response = "+getActivationResponse();
		return repr;
	}
	public int getType(){return type;}
	public int getID(){return id;}
	public int getX(){return x;}
	public int getY(){return y;}
	public int getActivationCount() {return activationCount;}
	public double getActiveOutput() {if(getActivationCount()>0) {return activeOutput;} else{return 0.0;}}
	public double getInactiveOutput(){return inactiveActiveOutput;}
	public double getActivationResponse(){return activationResponse;}
	public double getSplitX() {return splitX;}
	public double getSplitY() {return splitY;}
	public boolean isActive() {return active;}
	public ArrayList<Connection> getInputs(){return inputs;}
	public ArrayList<Connection> getOutputs(){return outputs;}
	
	public void setInactiveOutput(double i){inactiveActiveOutput=i;}
	public void setActiveOutput(double i) {activeOutput=i;}
	public void setType(int i){type=i;}
	public void setID(int i){id=i;}
	public void setActivationResponse(double i){activationResponse=i;}
	public void setActivationCount(int i) {activationCount=i;}
	public void setX(int i){x=i;}
	public void setY(int i){y=i;}
	public void setActive(boolean i) {active=i;}

	public void addInput(Connection c){inputs.add(c);}
	public void addOutput(Connection c){outputs.add(c);}
}
