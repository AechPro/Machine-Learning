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
	private double[][][] inputs;
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
	xDot,			/* cart velocity */
	theta1,
	theta2,         /* pole angle, radians */
	thetaDot;		/* pole angular velocity */
	private int steps,y;
	private Phenotype phenotype;
	private Random rng;
	private boolean randomStart;
	private boolean done;
	private double twelve_degrees;
	private double xAcc,thetaAcc,force,costheta,sintheta,temp;
	private Color color;

	private final double GRAVITY=9.8;
	private final double MASSCART=1.0;
	private final double MASSPOLE=0.1;
	private final double TOTAL_MASS=(MASSPOLE + MASSCART);
	private final double LENGTH=1.0;	  /* actually half the pole's length */
	private final double POLEMASS_LENGTH=(MASSPOLE * LENGTH);
	private final double FORCE_MAG=10.0;
	private final double TAU=0.02/4;	  /* seconds between state updates */
	private final double FOURTHIRDS=1.3333333333333;
	
	private Pole pole1,pole2;
	
	public Cart(int w,int h,int mSteps)
	{
		width=w;
		height=h;
		maxSteps = mSteps;
		randomStart = false;
		numTrials=0;
		pole1 = new Pole(w,h,300,1.0,Math.toRadians(1),randomStart);
		//pole2 = new Pole(w,h,300,0.1,0,randomStart);
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
		inputs = new double[4][1][1];
		outputs = new double[2];
		pos = new int[] {100,720/2};
		polePos = new int[] {100,720/2};
		y = 0;
		x = 0.0;
		xAcc = 0.0;
		force = 0.0;
		costheta = 0.0;
		sintheta = 0.0;
		temp = 0.0;
		if (randomStart) 
		{
			/*set up random start state*/
			x = ((Integer.MAX_VALUE*Math.random())%4800)/1000.0 - 2.4;
			xDot = ((Integer.MAX_VALUE*Math.random())%2000)/1000.0 - 1;
		}
		prevX = x;
	}
	
	@Override
	public void update(double delta) 
	{
		if(phenotype == null) {done=true;}
		if (x < -2.4 
				|| x > 2.4  
				|| theta1 < -twelve_degrees 
				|| theta1 > twelve_degrees 
				|| theta2 < -twelve_degrees 
				|| theta2 > twelve_degrees
				|| steps>maxSteps){done=true;}
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
		
		theta1 = pole1.getTheta();
		//theta2 = pole2.getTheta();
		
		
		inputs[0][0][0]=(x + 2.4) / 4.8;
		inputs[1][0][0]=(theta1 + 1.0) / 2.0;
		inputs[2][0][0]=(theta2 + 1.0) / 2.0;

		if(!phenotype.activate(inputs)) {done=true;}
		pos[0] = (int)(Math.round((1280-width)*(x+2.4)/4.8));
		//System.out.println(x+" "+theta);
		

		outputs = phenotype.readOutputVector();

		if(outputs[0] > outputs[1]) {y = 0;}
		else {y = 1;}
		
		xDot = pole1.step(y,pos);
		x  += TAU * 0.75*(xDot);
		
		steps++;
		fitness++;
	}
	
	@Override
	public void render(Graphics2D g) 
	{
		if(done || phenotype == null) {return;}
		if(renderPhenotype) {phenotype.render(g);}
		
		pole1.render(g);
		//pole2.render(g);
		g.setColor(color);
		g.fillRect(pos[0],pos[1],width,height);
		
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
