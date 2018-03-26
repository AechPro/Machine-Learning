package NEAT.Genes;

public abstract class Gene 
{
	private int innovationID;
	public abstract void mutate(double[] probs);
	public int getInnovationID() {return innovationID;}
}
