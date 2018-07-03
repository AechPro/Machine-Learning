package NEAT.TestUnits;

import java.util.ArrayList;
import java.util.Random;

import NEAT.Genes.Connection;
import NEAT.Genes.Node;
import NEAT.Population.Genome;
import NEAT.Population.Organism;
import NEAT.Population.Phenotype;
import NEAT.Simulations.PoleBalance.PoleBalanceWorld;
import NEAT.util.InnovationTable;

public class PoleTester extends TestUnit
{
	private PoleBalanceWorld gameWorld;
	public PoleTester(Random rng, int windowWidth, int windowHeight) 
	{
		super(rng, windowWidth, windowHeight);
		numInputs = 4;
		numOutputs = 2;
		numHiddenNodes = 1;
		numBiasNodes = 1;
		gameWorld = new PoleBalanceWorld();
	}

	@Override
	public Genome buildMinimalStructure(InnovationTable table) 
	{
		ArrayList<Connection> cons = new ArrayList<Connection>();
		ArrayList<Node> nodes = new ArrayList<Node>();
		ArrayList<Node> inputNodes = new ArrayList<Node>();
		ArrayList<Node> outputNodes = new ArrayList<Node>();
		ArrayList<Node> biasNodes = new ArrayList<Node>();
		ArrayList<Node> hiddenNodes = new ArrayList<Node>();
		Random randf = new Random((long)(Math.random()*Long.MAX_VALUE));
		Genome minimalGenome = null;
		
		for(int i=0;i<numBiasNodes;i++)
		{
			int id = table.createNode(-1, -1, Node.BIAS_NODE);
			Node n = new Node(0.25*i,0.0,Node.BIAS_NODE,id);
			biasNodes.add(n);
			nodes.add(n);
		}
		for(int i=0;i<numInputs;i++)
		{
			int id = table.createNode(-1, -1, Node.INPUT_NODE);
			Node n = new Node(biasNodes.get(numBiasNodes-1).getSplitX()+0.25*(i+1),0.0,Node.INPUT_NODE,id);
			inputNodes.add(n);
			nodes.add(n);
		}
		for(int i=0;i<numOutputs;i++)
		{
			int id = table.createNode(-1, -1, Node.OUTPUT_NODE);
			Node n = new Node(inputNodes.get(0).getSplitX()+0.25*i,1.0,Node.OUTPUT_NODE,id);
			outputNodes.add(n);
			nodes.add(n);
		}
		
		int hiddenID = table.createNode(2, 4, Node.HIDDEN_NODE);
		Node node = new Node(inputNodes.get(1).getSplitX(),0.5,Node.HIDDEN_NODE,hiddenID);
		hiddenNodes.add(node);
		nodes.add(node);
		
		for(int i=0;i<numInputs;i++)
		{
			int id = table.createConnection(inputNodes.get(i).getID(), node.getID());
			Connection c = new Connection(inputNodes.get(i),node,randf.nextGaussian(),true,id);
			cons.add(c);
			for(int j=0;j<numOutputs;j++)
			{
				id = table.createConnection(inputNodes.get(i).getID(), outputNodes.get(j).getID());
				c = new Connection(inputNodes.get(i),outputNodes.get(j),randf.nextGaussian(),true,id);
				cons.add(c);
			}
		}
		for(int i=0;i<numBiasNodes;i++)
		{
			int id = table.createConnection(biasNodes.get(i).getID(), node.getID());
			Connection c = new Connection(biasNodes.get(i),node,randf.nextGaussian(),true,id);
			cons.add(c);
			for(int j=0;j<numOutputs;j++)
			{
				id = table.createConnection(biasNodes.get(i).getID(), outputNodes.get(j).getID());
				c = new Connection(biasNodes.get(i),outputNodes.get(j),randf.nextGaussian(),true,id);
				cons.add(c);
			}
		}
		for(int i=0;i<numOutputs;i++)
		{
			int id = table.createConnection(node.getID(),outputNodes.get(i).getID());
			Connection c = new Connection(node,outputNodes.get(i),randf.nextGaussian(),true,id);
			cons.add(c);
		}
		
		minimalGenome = new Genome(cons,nodes,table,randf,numInputs,numOutputs);
		return minimalGenome;
	}

	@Override
	public Genome buildMinimalSolution(InnovationTable table) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Organism testPhenotypes(ArrayList<Organism> population) 
	{
		int maxSteps = 10000;
		for(Organism org : population)
		{
			org.createPhenotype(width/2,height/2);
			if(!validatePhenotype(org.getPhenotype()))
			{
				org.setPhenotype(null);
			}
		}
		gameWorld.buildPop(population);
		gameWorld.run();
		double[] fitnessList = gameWorld.getTestResults();
		
		for(int i=0;i<fitnessList.length;i++)
		{
			population.get(i).setFitness(fitnessList[i]);
			if(fitnessList[i] > maxSteps) {return population.get(i);}
		}
		return null;
	}
	public int move_cart(Organism org, int max_steps,int thresh)
	{
		double x,			/* cart position, meters */
		x_dot,			/* cart velocity */
		theta,			/* pole angle, radians */
		theta_dot;		/* pole angular velocity */
		int steps=0,y;
		Phenotype phen = org.getPhenotype();
		boolean random_start=true;

		double[] in = new double[4];  //Input loading array
		double[] out = new double[2];
		//		     double one_degree= 0.0174532;	/* 2pi/360 */
		//		     double six_degrees=0.1047192;
		double twelve_degrees=0.2094384;
		//		     double thirty_six_degrees= 0.628329;
		//		     double fifty_degrees=0.87266;


		if (random_start) {
			/*set up random start state*/
			x = ((Integer.MAX_VALUE*Math.random())%4800)/1000.0 - 2.4;
			x_dot = ((Integer.MAX_VALUE*Math.random())%2000)/1000.0 - 1;
			theta = ((Integer.MAX_VALUE*Math.random())%400)/1000.0 - .2;
			theta_dot = ((Integer.MAX_VALUE*Math.random())%3000)/1000.0 - 1.5;
		}
		else 
			x = x_dot = theta = theta_dot = 0.0;
		double[] updates;
		/*--- Iterate through the action-learn loop. ---*/
		while (steps++ < max_steps)
		{

			in[0]=(x + 2.4) / 4.8;;
			in[1]=(x_dot + .75) / 1.5;
			in[2]=(theta + twelve_degrees) / .41;
			in[3]=(theta_dot + 1.0) / 2.0;

			if(!phen.activate(in)) {return 1;}

			out = phen.readOutputVector();

			if(out[0] > out[1]) {y = 0;}
			else {y = 1;}

			updates = cart_pole(y, x, x_dot, theta, theta_dot);
			x  += updates[0] * x_dot;
			x_dot += updates[0] * updates[1];
			theta += updates[0] * theta_dot;
			theta_dot += updates[0] * updates[2];

			/*--- Check for failure.  If so, return steps ---*/
			if (x < -2.4 || x > 2.4  || theta < -twelve_degrees ||
					theta > twelve_degrees)
				return steps;             
		}
		return steps;
	}
	public double[] cart_pole(int action, double x, double x_dot, double theta, double theta_dot)
	{
		double xacc,thetaacc,force,costheta,sintheta,temp;

		double GRAVITY=9.8;
		double MASSCART=1.0;
		double MASSPOLE=0.1;
		double TOTAL_MASS=(MASSPOLE + MASSCART);
		double LENGTH=0.5;	  /* actually half the pole's length */
		double POLEMASS_LENGTH=(MASSPOLE * LENGTH);
		double FORCE_MAG=10.0;
		double TAU=0.02;	  /* seconds between state updates */
		double FOURTHIRDS=1.3333333333333;

		force = (action>0)? FORCE_MAG : -FORCE_MAG;
		costheta = Math.cos(theta);
		sintheta = Math.sin(theta);

		temp = (force + POLEMASS_LENGTH * theta_dot * theta_dot * sintheta)
				/ TOTAL_MASS;

		thetaacc = (GRAVITY * sintheta - costheta* temp)
				/ (LENGTH * (FOURTHIRDS - MASSPOLE * costheta * costheta
						/ TOTAL_MASS));

		xacc  = temp - POLEMASS_LENGTH * thetaacc* costheta / TOTAL_MASS;

		/*** Update the four state variables, using Euler's method. ***/
		return new double[] {TAU,xacc,thetaacc};
		
	}
}
