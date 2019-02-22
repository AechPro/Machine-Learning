package NEAT.Genes;

import java.util.ArrayList;

public abstract class Node
{
    public static final int INPUT_NODE = 0;
    public static final int HIDDEN_NEURON = 1;
    public static final int OUTPUT_NODE = 2;
    public static final int BIAS_NEURON = 3;
    public static final int FEATURE_FILTER = 4;
    
    protected int x,y;
    protected double splitX, splitY;
    protected int type;
    protected int id;
    protected boolean active;
    protected double activationResponse;
    protected ArrayList<Connection> inputs, outputs;
    
    protected int activationCount;
    protected double activeOutput;
    protected double inactiveActiveOutput;
    public Node(double sx, double sy, int nt, int nid)
    {
        x = 0;
        y = 0;
        active = false;
        splitX = sx;
        splitY = sy;
        type = nt;
        id = nid;
        activationCount = 0;
        activeOutput = 0.0d;
        inactiveActiveOutput = 0;
        activationResponse = 1d;
        inputs = new ArrayList<Connection>();
        outputs = new ArrayList<Connection>();
    }

    public Node(Node other)
    {
        x = 0;
        y = 0;
        active = other.isActive();
        splitX = other.getSplitX();
        splitY = other.getSplitY();
        type = other.getType();
        id = other.getID();
        activationResponse = other.getActivationResponse();
        inputs = new ArrayList<Connection>();
        outputs = new ArrayList<Connection>();
    }
    
    public int getType(){return type;}
    public int getID(){return id;}
    public int getX(){return x;}
    public int getY(){return y;}
    public double getActivationResponse(){return activationResponse;}
    public double getSplitX() {return splitX;}
    public double getSplitY() {return splitY;}
    public boolean isActive() {return active;}
    public ArrayList<Connection> getInputs(){return inputs;}
    public ArrayList<Connection> getOutputs(){return outputs;}
    
    public void setType(int i){type=i;}
    public void setID(int i){id=i;}
    public void setActivationResponse(double i){activationResponse=i;}
    public void setX(int i){x=i;}
    public void setY(int i){y=i;}
    public void setActive(boolean i) {active=i;}

    public void addInput(Connection c){inputs.add(c);}
    public void addOutput(Connection c){outputs.add(c);}
    
    public int getActivationCount() {return activationCount;}
    public double getActiveOutput() 
    {
        if(getActivationCount()>0) {return activeOutput;} 
        else{return 0.0;}
    }
    public double getInactiveOutput(){return inactiveActiveOutput;}
    
    
    public void setInactiveOutput(double i){inactiveActiveOutput=i;}
    public void setActiveOutput(double i) {activeOutput=i;}
    public void setActivationCount(int i) {activationCount=i;}
}
