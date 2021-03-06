package NEAT.Population;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;

import NEAT.Configs.Config;
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
        if(rand == null) {rand = new Random((long)(Math.random()*Long.MAX_VALUE));}
        table = innovTable;

        if(table == null){ID = -1;}
        else {ID = table.getNextGenomeID();}

        nodes = new ArrayList<Node>();
        connections = new ArrayList<Connection>();
        for(Connection c : cons) {connections.add(new Connection(c));}
        for(Node n : ns) 
        {
            if(n instanceof Neuron)
            {
                nodes.add(new Neuron(n));
            }
            else if(n instanceof FeatureFilter)
            {
                nodes.add(new FeatureFilter(n));
            }
        }
    }
    //Constructor for creating a genome from child genes
    public Genome(ArrayList<Connection> cons, InnovationTable innovTable, Random rng, int inputs, int outputs)
    {
        rand = rng;
        if(rand == null) {rand = new Random((long)(Math.random()*Long.MAX_VALUE));}
        table = innovTable;
        if(table == null){ID = -1;}
        else {ID = table.getNextGenomeID();}
        nodes = new ArrayList<Node>();
        connections = new ArrayList<Connection>();
        for(Connection c : cons) 
        {
            Node inp = null;
            Node out = null;
            //System.out.println("Checking input: "+c.getInput());
            if(!duplicateNode(c.getInput())) 
            {
                if(c.getInput() instanceof Neuron)
                {
                    inp = new Neuron(c.getInput());
                }
                else
                {
                    inp = new FeatureFilter(c.getInput());
                }
                nodes.add(inp);
            }
            else
            {
                inp = nodes.get(getNodeIndex(c.getInput()));
             //   System.out.println("Duplicate node detected, retrieved index of input.");
            }
           // System.out.println("Checking Output: "+c.getOutput());
            if(!duplicateNode(c.getOutput())) 
            {
                if(c.getOutput() instanceof Neuron)
                {
                    out = new Neuron(c.getOutput());
                }
                else
                {
                    out = new FeatureFilter(c.getOutput());
                }
                
                nodes.add(out);
            }
            else 
            {
                out = nodes.get(getNodeIndex(c.getOutput()));
                //System.out.println("Duplicate node detected, retrieved index of output.");
            }
            if(out == null || inp == null)
            {
                System.out.println("ERROR TRYING TO FIND NODES FOR CONNECTION WHEN CREATING CHILD GENOME");
                System.out.println(c);
                System.out.println("INP: "+inp+"\nOUT: "+out);
                System.exit(0);
            }
           // System.out.println("-----CREATING NEW CONNECTION BETWEEN----- \n"+out+"\n AND \n"+inp+"\n");
            Connection con = new Connection(inp,out,c.getWeight(),c.isEnabled(),c.getInnovation());
            connections.add(con);
        }
    }
    //Constructor for blank genome
    public Genome(InnovationTable innovTable, Random rng, int inputs, int outputs)
    {
        rand = rng;
        if(rand == null) {rand = new Random((long)(Math.random()*Long.MAX_VALUE));}
        table = innovTable;
        ID = table.getNextGenomeID();
        nodes = new ArrayList<Node>();
        connections = new ArrayList<Connection>();
    }
    //Copy constructor
    public Genome(Genome other)
    {
        duplicate(other);
        if(rand == null) {rand = new Random((long)(Math.random()*Long.MAX_VALUE));}
    }
    public boolean addConnection(InnovationTable table)
    {
        double mutationRate = Config.CONNECTION_ADD_CHANCE;
        int maxAttempts = Config.MAX_ATTEMPTS_ADD_CONNECTION;
        if(rand.nextDouble()>mutationRate){return false;}
        Node n1 = null;
        Node n2 = null;
        int inpIdx = -1;
        int outIdx = -1;
        boolean recursive = false;
        boolean found = false;
        while(maxAttempts-->0)
        {
            inpIdx = rand.nextInt(nodes.size());
            if(rand.nextDouble()<Config.RECURSIVE_CONNECTION_CHANCE){outIdx=inpIdx; recursive=true;}
            else{outIdx = rand.nextInt(nodes.size()-inputs) + inputs;}
            n1 = nodes.get(inpIdx);
            n2 = nodes.get(outIdx);
            if(n2.getType() == Node.INPUT_NODE
                    || n2.getType() == Node.BIAS_NEURON 
                    || n1.getType() == Node.OUTPUT_NODE
                    || (inpIdx == outIdx && !recursive) 
                    || ( (n1 instanceof Neuron) && (n2 instanceof FeatureFilter) ))
            {continue;}
            else
            {
                if(duplicateConnection(n1.getID(),n2.getID()) && n1 instanceof Neuron){continue;}
                maxAttempts = 0;
                found = true;
                break;
            }
        }
        if(n1 == null || n2 == null || !found){return false;}
        int id = table.checkInnovation(table.NEW_CONNECTION, n1.getID(),n2.getID());
        if(id==-1){id = table.createConnection(n1.getID(),n2.getID());}
        Connection newCon = null;
        
        //Check if input will be a feature filter. If so, attach connection randomly inside it.
        if(n1.getType() == Node.FEATURE_FILTER)
        {
            int[] filterPos = new int[2];
            int[] availablePositions = ((FeatureFilter)n1).getOutputShape(Config.FEATURE_FILTER_INITIAL_INPUT_SHAPE);
            
            int filterX = rand.nextInt(availablePositions[0]);
            int filterY = rand.nextInt(availablePositions[1]);
            
            filterPos[0] = filterX;
            filterPos[1] = filterY;
            newCon = new Connection(n1, n2, rand.nextGaussian(), true, filterPos, id);
        }
        else
        {
            newCon = new Connection(n1, n2, rand.nextGaussian(), true, id);
        }
        
        connections.add(newCon);
        return true;
    }
    public boolean addNode(InnovationTable table)
    {
        if(nodes.size() >= Config.MAX_ALLOWED_NODES) {return false;}
        double neuronAddChance = Config.NEURON_ADD_CHANCE;
        double filterAddChance = Config.FILTER_ADD_CHANCE;
        double num = rand.nextDouble();
        if(num>neuronAddChance)
        {
            if(num > filterAddChance){return false;}
            return addFilter(table);
        }
        return addNeuron(table);
    }
    public boolean addFilter(InnovationTable table)
    {
        int maxAttempts = Config.MAX_ATTEMPTS_ADD_NODE;

        boolean foundSplit = false;
        int idx = 0;
        int[] kern = new int[] {2,2};
        int[] step = new int[] {3,3};
        int offset = 0;
        if(connections.size()>1) {offset = (int)Math.sqrt(connections.size()-1);}
        Node inp;
        Connection selected;

        while(maxAttempts-->0 && !foundSplit)
        {
            idx = rand.nextInt(connections.size() - offset);
            selected = connections.get(idx);

            //if the input node of this connection is a neuron, we cannot put a filter here.
            if(selected.getInput() instanceof Neuron) {continue;}

            inp = selected.getInput();

            if(selected.isEnabled())
            {foundSplit = true;}
        }

        if(!foundSplit){return false;}

        connections.get(idx).setEnable(false);
        

        double oldWeight = connections.get(idx).getWeight();
        int[] oldPos = connections.get(idx).cloneFeatureFilterPos();

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
            nodeID = table.createNode(inp.getID(), out.getID(),Node.FEATURE_FILTER);
            FeatureFilter n = new FeatureFilter(newWidth, newDepth, Node.FEATURE_FILTER, nodeID, rand, kern, step);
            nodes.add(n);
            int link1ID = table.createConnection(inp.getID(), n.getID());
            int link2ID = table.createConnection(n.getID(), out.getID());
            Connection con1 = new Connection(inp,n,1.0,true,link1ID);
            Connection con2 = new Connection(n,out,oldWeight,true,oldPos,link2ID);
            connections.add(con1);
            connections.add(con2);
        }
        else
        {
            nodeID = table.getNodeID(id);
            FeatureFilter n = new FeatureFilter(newWidth, newDepth, Node.FEATURE_FILTER, nodeID, rand, kern, step);
            nodes.add(n);
            int link1ID = table.checkInnovation(table.NEW_CONNECTION, inp.getID(), n.getID());
            int link2ID = table.checkInnovation(table.NEW_CONNECTION, n.getID(), out.getID());
            Connection con1 = new Connection(inp,n,1.0,true,link1ID);
            Connection con2 = new Connection(n,out,oldWeight,true,oldPos,link2ID);
            connections.add(con1);
            connections.add(con2);
        }
        return true;
    }
    public boolean addNeuron(InnovationTable table)
    {
        int maxAttempts = Config.MAX_ATTEMPTS_ADD_NODE;

        boolean foundSplit = false;
        int idx = 0;
        int offset = 0;
        if(connections.size()>1) {offset = (int)Math.sqrt(connections.size()-1);}
        Node inp;
        Connection selected;

        while(maxAttempts-->0 && !foundSplit)
        {
            idx = rand.nextInt(connections.size() - offset);
            selected = connections.get(idx);

            //if the output node of this connection is a feature filter, we cannot put a neuron here.
            if(selected.getOutput() instanceof FeatureFilter) {continue;}

            inp = selected.getInput();

            if(selected.isEnabled())
            {foundSplit = true;}
        }

        if(!foundSplit){return false;}

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
            nodeID = table.createNode(inp.getID(), out.getID(),Node.HIDDEN_NEURON);
            Neuron n = new Neuron(newWidth, newDepth, Node.HIDDEN_NEURON, nodeID);
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
            Neuron n = new Neuron(newWidth, newDepth, Node.HIDDEN_NEURON, nodeID);
            nodes.add(n);
            int link1ID = table.checkInnovation(table.NEW_CONNECTION, inp.getID(), n.getID());
            int link2ID = table.checkInnovation(table.NEW_CONNECTION, n.getID(), out.getID());
            Connection con1 = new Connection(inp,n,1.0,true,link1ID);
            Connection con2 = new Connection(n,out,oldWeight,true,link2ID);
            connections.add(con1);
            connections.add(con2);
        }
        return true;
    }
    public void mutateWeights()
    {
        if(rand.nextDouble() >= Config.WEIGHT_MUTATION_PROB) {return;}
        double mutationRate = 0.1;
        double replaceProb = 0;
        double maxPerturb = Config.MAX_MUTATION_PERTURBATION;
        int numCons = connections.size();
        int end = (int)(Math.round(numCons*0.8));
        int num = 0;
        boolean severe = rand.nextDouble()>0.5;
        for(Connection con : connections)
        {
            num++;
            if(!con.isEnabled()){continue;}
            if(severe)
            {
                mutationRate=Config.WEIGHT_MUTATION_RATE;
                replaceProb=Config.WEIGHT_REPLACEMENT_RATE;
            }
            else if(num>end && numCons>=10)
            {
                mutationRate=Config.WEIGHT_MUTATION_RATE+0.2;
                replaceProb=Config.WEIGHT_REPLACEMENT_RATE+0.2;
            }
            else
            {
                mutationRate = 1.0;
            }
            if(rand.nextDouble()<mutationRate)
            {
                double mutationValue = rand.nextGaussian();
                con.setWeight(con.getWeight() + mutationValue*maxPerturb);
            }
            else if(rand.nextDouble()<replaceProb)
            {
                double mutationValue = rand.nextGaussian();
                con.setWeight(mutationValue);
            }
        }
    }
    public void mutateConnections()
    {
        double toggleRate = Config.MUTATED_CONNECTION_TOGGLE_RATE;
        double enableRate = Config.MUTATED_CONNECTION_ENABLE_RATE;
        for(Connection c : connections)
        {
            if(rand.nextDouble()<toggleRate)
            {
                c.setEnable(!c.isEnabled());
            }
            else if(rand.nextDouble()<enableRate)
            {
                c.setEnable(true);
            }
        }
    }
    public void mutateNodes()
    {
        double mutationRate = Config.ACTIVATION_RESPONSE_MUTATION_RATE;
        double maxPerturb = Config.MAX_MUTATION_PERTURBATION;
        for(Node n : nodes)
        {
            if(n instanceof FeatureFilter)
            {
                ((FeatureFilter)n).mutate();
            }
            else if(rand.nextDouble()<mutationRate)
            {
                double mutationValue = rand.nextGaussian()*maxPerturb/3;
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
            if(n instanceof FeatureFilter)
            {
                ((FeatureFilter)n).initFilter();
            }
            n.setActivationResponse(rand.nextGaussian());
        }
    }
    public void duplicate(Genome other)
    {
        nodes = new ArrayList<Node>();
        connections = new ArrayList<Connection>();
        for(Connection c : other.getConnections()) {connections.add(new Connection(c));}
        for(Node n : other.getNodes()) 
        {
            if(n instanceof Neuron)
            {
                nodes.add(new Neuron(n));
            }
            else if(n instanceof FeatureFilter)
            {
                nodes.add(new FeatureFilter(n));
            }
        }
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
    public double getAverageConnectionWeight()
    {
        double avg = 0;
        if(connections.size() == 0) {return 0;}
        for(int i=0;i<connections.size();i++)
        {
            avg+=connections.get(i).getWeight();
        }
        return avg/connections.size();
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
