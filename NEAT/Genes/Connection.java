package NEAT.Genes;

public class Connection 
{
    public final double WEIGHT_MAX = 12.0;
    public final double WEIGHT_MIN = - WEIGHT_MAX;
    private Node input;
    private Node output;
    
    private int[] featureFilterPosition;

    private double weight;
    private boolean enable;
    private boolean recursive;
    private int innovID;

    public Connection(Node in, Node out, double w, boolean en, int innov)
    {
        weight = w;
        enable = en;
        innovID = innov;
        //System.out.println("!!!!SHITTY CONNECTION!!!!");
        featureFilterPosition = new int[2];
        try
        {
            setOutput(out);
            setInput(in);
        }
        catch(Exception e){e.printStackTrace();}
        recursive = in.equals(out);
    }
    public Connection(Node in, Node out, double w, boolean en, int[] featureFilterPos, int innov)
    {
        weight = w;
        enable = en;
        innovID = innov;
        featureFilterPosition = new int[] {featureFilterPos[0], featureFilterPos[1]};
        try
        {
            setOutput(out);
            setInput(in);
        }
        catch(Exception e){e.printStackTrace();}
        recursive = in.equals(out);
    }
    public Connection(Connection other)
    {
        weight = other.getWeight();
        enable = other.isEnabled();
        innovID = other.getInnovation();
        recursive = other.isRecursive();
        featureFilterPosition = other.cloneFeatureFilterPos();
        try
        {
            if(other.getOutput() instanceof Neuron)
            {
                setOutput(new Neuron(other.getOutput()));
            }
            else
            {
                setOutput(new FeatureFilter(other.getOutput()));
            }
            if(other.getInput() instanceof Neuron)
            {
                setInput(new Neuron(other.getInput()));
            }
            else
            {
                setInput(new FeatureFilter(other.getInput()));
            }
        }
        catch(Exception e){e.printStackTrace();}

    }
    @Override
    public boolean equals(Object other)
    {
        if(other == null){return false;}
        if(!Connection.class.isAssignableFrom(other.getClass())){return false;}
        final Connection a = (Connection)other;
        int id1 = this.getInnovation();
        int id2 = a.getInnovation();
        return id1==id2;
    }
    public Node getInput() {return input;}
    public Node getOutput() {return output;}
    public double getWeight() {return weight;}
    public int getInnovation(){return innovID;}
    public boolean isEnabled() {return enable;}
    public boolean isRecursive() {return recursive;}

    public void setOutput(Node output) throws Exception
    {
        if(output.getType() == Node.INPUT_NODE)
        {
            throw new Exception("Attempted to set output of connection to an input node!\n"+input+" "+output+"\n"+toString());
        }
        if(output.getType() == Node.BIAS_NEURON)
        {
            throw new Exception("Attempted to set output of connection to a bias node!\n"+input+" "+output+"\n"+toString());
        }
        else if(output instanceof FeatureFilter)
        {
            if((input instanceof Neuron))
            {
                throw new Exception("Attempted to connect a neuron to a feature filter!\n"+input+" "+output+"\n"+toString());
            }
        }

        this.output = output;

        if(input != null && output != null)
        {recursive = input.equals(this.output);}
    }
    public void setWeight(double weight)
    {
        this.weight = Math.max(WEIGHT_MIN, Math.min(weight,WEIGHT_MAX));
    }
    public void setEnable(boolean enable) {this.enable = enable;}
    public void setInnovation(int ID){innovID = ID;}
    public void setInput(Node input)
    {
        this.input = input;

        if(this.input != null && output != null) 
        {recursive = this.input.equals(output);}
    }
    public void setRecursive(boolean i) {recursive=i;}
    public int[] cloneFeatureFilterPos()
    {
        return new int[] {featureFilterPosition[0], featureFilterPosition[1]};
    }
    public String toString()
    {
        String repr = "---Connection Gene---\nInnovation: "+getInnovation()+"\nEnabled: "+enable;
        repr+="\nWeight: "+Math.round(weight*100)/100.0+"\nInput: "+this.getInput();
        repr+="\nOutput: "+this.getOutput()+"\n";
        return repr;
    }
}
