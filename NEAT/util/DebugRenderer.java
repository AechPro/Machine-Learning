package NEAT.util;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import NEAT.Genes.Node;
import NEAT.Population.Phenotype;

public class DebugRenderer
{
	private XORTester tester;
	private Random rand;
	public DebugRenderer()
	{
		rand = new Random((long)(Math.random()*Long.MAX_VALUE));
		tester = new XORTester(rand);
	}
	public void renderPhenotype(Graphics2D g, Phenotype target)
	{
		ArrayList<Node> targetNodes = target.getNodes();
	}
	
}
