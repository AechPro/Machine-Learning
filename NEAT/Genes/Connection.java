package NEAT.Genes;

public class Connection 
{
	public final double WEIGHT_MAX = 200.0;
	public final double WEIGHT_MIN = - WEIGHT_MAX;
	private Node input;
	private Node output;
	private double weight;
	private boolean enable;
	private int innovID;
	
	public Connection(Node in, Node out, double w, boolean en, int innov)
	{
		weight = w;
		enable = en;
		innovID = innov;
		try
		{
			setOutput(out);
			setInput(in);
		}
		catch(Exception e){e.printStackTrace();}
		
	}
	public Connection(Connection other)
	{
		weight = other.getWeight();
		enable = other.isEnabled();
		innovID = other.getInnovation();
		try
		{
			setOutput(new Node(other.getOutput()));
			setInput(new Node(other.getInput()));
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
	
	public void setOutput(Node output) throws Exception
	{
		if(output.getType() == Node.INPUT_NODE)
		{
			throw new Exception("Attempted to set output of connection to an input node!\n"+input+" "+output+"\n"+toString());
		}
		if(output.getType() == Node.BIAS_NODE)
		{
			throw new Exception("Attempted to set output of connection to a bias node!\n"+input+" "+output+"\n"+toString());
		}
		this.output = output;
	}
	public void setWeight(double weight)
	{
		this.weight = Math.max(WEIGHT_MIN, Math.min(weight,WEIGHT_MAX));
	}
	public void setEnable(boolean enable) {this.enable = enable;}
	public void setInnovation(int ID){innovID = ID;}
	public void setInput(Node input) throws Exception
	{
		if(input.getID() == output.getID())
		{
			throw new Exception("A node cannot be connected to itself! "+input+" "+output);
		}
		this.input = input;
	}
	
	
	public String toString()
	{
		String repr = "---Connection Gene---\nInnovation: "+getInnovation()+"\nEnabled: "+enable;
		repr+="\nWeight: "+Math.round(weight*100)/100.0+"\nInput: "+this.getInput();
		repr+="\nOutput: "+this.getOutput()+"\n";
		return repr;
	}
}
