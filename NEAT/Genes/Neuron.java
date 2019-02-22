package NEAT.Genes;

public class Neuron extends Node
{
	
	
	public Neuron(double sx, double sy, int nt, int nid)
	{
	    super(sx,sy,nt,nid);
		
	}
	public Neuron(Node other)
	{
	    super(other);
	    try
	    {
	        activeOutput = ((Neuron)other).getActiveOutput();
	        activationCount = ((Neuron)other).getActivationCount();
	        inactiveActiveOutput = ((Neuron)other).getInactiveOutput();
	    }
	    catch(Exception e)
	    {
	        System.out.println("FAILED TO SET NODE TO NEURON IN COPY CONSTRUCTOR");
	        e.printStackTrace();
	        System.exit(1);
	    }
	}
	
	@Override
	public boolean equals(Object other)
	{
		if(other == null){return false;}
		if(!Neuron.class.isAssignableFrom(other.getClass())){return false;}
		final Neuron a = (Neuron)other;
		return a.getID() == getID() && a.getType() == getType();
	}
	@Override
	public String toString()
	{
		String repr = "";
		switch(type)
		{
			case BIAS_NEURON:
				repr+="Bias Node";
				break;
			case INPUT_NODE:
				repr+="Input Node";
				break;
			case OUTPUT_NODE:
				repr+="Output Node";
				break;
			case HIDDEN_NEURON:
				repr+="Hidden Node";
				break;
		}
		repr+=" | ID = "+id+" | Active = "+isActive()+" | Count = "+getActivationCount();
		repr+=" | Raw Value = "+Math.round(getInactiveOutput()*100)/100.0+" | Active Value = "+Math.round(getActiveOutput()*100)/100d;
		repr+=" | Response = "+getActivationResponse();
		return repr;
	}
	
}
