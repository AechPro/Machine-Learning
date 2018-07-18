package NEAT.Simulations.PoleBalance;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

import NEAT.Configs.Config;
import NEAT.Display.DisplayObject;
import NEAT.Population.Genome;
import NEAT.Population.Organism;
import NEAT.Population.Phenotype;
import NEAT.TestUnits.FishTester;
import NEAT.TestUnits.PoleTester;
import NEAT.util.InnovationTable;

public class Cart extends DisplayObject
{
	private double[] inputs;
	private double[] outputs;
	private boolean victory;
	private int width,height,poleRadius;
	private int maxSteps;
	private double prevX;
	private int timeSinceMovement;
	private int[] pos, polePos;
	private int fitness;
	private int numTrials;
	private boolean renderPhenotype;
	private int numResets;
	
	private double x,	    /* cart position, meters */
	x_dot,			/* cart velocity */
	theta,			/* pole angle, radians */
	theta_dot;		/* pole angular velocity */
	private int steps,y;
	private Phenotype phenotype;
	private Random rng;
	private boolean random_start;
	private boolean done;
	private double twelve_degrees;
	private double xacc,thetaacc,force,costheta,sintheta,temp;
	private Color color;

	private final double GRAVITY=9.8;
	private final double MASSCART=1.0;
	private final double MASSPOLE=0.1;
	private final double TOTAL_MASS=(MASSPOLE + MASSCART);
	private final double LENGTH=1.0;	  /* actually half the pole's length */
	private final double POLEMASS_LENGTH=(MASSPOLE * LENGTH);
	private final double FORCE_MAG=10.0;
	private final double TAU=0.02;	  /* seconds between state updates */
	private final double FOURTHIRDS=1.3333333333333;
	
	public Cart(int w,int h,int mSteps)
	{
		width=w;
		height=h;
		maxSteps = mSteps;
		random_start = true;
		numTrials=0;
		rng = new Random();
		reset();
	}
	public void reset()
	{
		done = false;
		steps = 0;
		timeSinceMovement = 0;
		twelve_degrees = Math.PI/2;//0.2094384;
		poleRadius = (int)(Math.round(300*LENGTH));
		inputs = new double[4];
		outputs = new double[2];
		pos = new int[] {100,720/2};
		polePos = new int[] {100,720/2};
		y = 0;
		x = 0.0;
		xacc = 0.0;
		theta = 0.1;
		thetaacc = 0.0;
		force = 0.0;
		costheta = 0.0;
		sintheta = 0.0;
		temp = 0.0;
		if (random_start) 
		{
			/*set up random start state*/
			x = ((Integer.MAX_VALUE*Math.random())%4800)/1000.0 - 2.4;
			x_dot = ((Integer.MAX_VALUE*Math.random())%2000)/1000.0 - 1;
			theta = ((Integer.MAX_VALUE*Math.random())%400)/1000.0 - .2;
			theta_dot = ((Integer.MAX_VALUE*Math.random())%3000)/1000.0 - 1.5;
		}
		prevX = x;
	}
	
	@Override
	public void update(double delta) 
	{
		if(phenotype == null) {done=true;}
		if(timeSinceMovement > 100)
		{
			System.out.println("\n!!!FAILURE TO MOVE DETECTED!!!\n"+timeSinceMovement);
			done=true;
		}
		if(done) 
		{
			numTrials++;
			if(numTrials<numResets) {reset();}
			return;
		}
		if(x == prevX) {timeSinceMovement++;}
		else {timeSinceMovement=0;}
		
		inputs[0]=(x + 2.4) / 4.8;
		inputs[1]=(x_dot + .75) / 1.5;
		inputs[2]=(theta + twelve_degrees) / .41;
		inputs[3]=(theta_dot + 1.0) / 2.0;

		if(!phenotype.activate(inputs)) {done=true;}
		pos[0] = (int)(Math.round((1280-width)*(x+2.4)/4.8));
		//System.out.println(x+" "+theta);
		

		outputs = phenotype.readOutputVector();

		if(outputs[0] > outputs[1]) {y = 0;}
		else {y = 1;}
		force = (y>0)? FORCE_MAG : -FORCE_MAG;
		
		costheta = Math.cos(theta);
		sintheta = Math.sin(theta);

		temp = (force + POLEMASS_LENGTH * theta_dot * theta_dot * sintheta)
				/ TOTAL_MASS;

		thetaacc = (GRAVITY * sintheta - costheta * temp)
				   / (LENGTH * (FOURTHIRDS - MASSPOLE * costheta * costheta
				   / TOTAL_MASS));

		xacc  = temp - POLEMASS_LENGTH * thetaacc * costheta / TOTAL_MASS;
		x  += TAU * x_dot;
		x_dot += TAU * xacc;
		theta += TAU * theta_dot;
		if(Math.random()<0.2 && random_start) {thetaacc+=thetaacc*rng.nextGaussian();}
		theta_dot += TAU * thetaacc;
		
		polePos[0] = pos[0] + (int)(Math.round(poleRadius*Math.cos(theta + Math.PI/2)));
		polePos[1] = pos[1] - (int)(Math.round(poleRadius*Math.sin(theta + Math.PI/2)));
		
		
		if (x < -2.4 || x > 2.4  || theta < -twelve_degrees 
				|| theta > twelve_degrees || steps>maxSteps){done=true;}
		
		steps++;
		fitness++;
	}
	
	@Override
	public void render(Graphics2D g) 
	{
		if(done || phenotype == null) {return;}
		if(renderPhenotype) {phenotype.render(g);}
		g.setColor(Color.BLUE);
		g.drawLine(pos[0]+width/2, pos[1]+height/2, polePos[0]+width/2, polePos[1]+height/2);
		g.setColor(color);
		g.fillRect(pos[0],pos[1],width,height);
		g.setColor(Color.RED);
		g.fillOval(polePos[0]+width/2-10, polePos[1]+height/2-10, 20, 20);
		g.setColor(Color.BLACK);
		g.drawString(""+fitness, pos[0]+width/2, pos[1]+height/2);
		
	}
	public void setColor(int i)
	{
		color = new Color((int)((Math.pow(2, 24)-1)*(i+5)/Config.POPULATION_SIZE));
	}
	public void setPhenotype(Phenotype phen) 
	{
		phenotype = phen;
	}
	public Phenotype getPhenotype(){return phenotype;}
	public int getSteps() {return steps;}
	public boolean isDone() {return done;}
	public int getFitness() {return fitness;}
	public void resetFitness() {fitness=0; numTrials=0;}
	public void setRenderPhenotype(boolean i) {renderPhenotype=i;}
	public boolean getRenderPhenotype() {return renderPhenotype;}
	public void setNumResets(int i) {numResets=i;}
}
