package NEAT.Simulations.FishMaze.Workers;

import java.util.ArrayList;
import java.util.Random;

import NEAT.Simulations.FishMaze.*;


public class Fish extends Worker
{
	private double[] inputVector;
	private double[] outputVector;
	private double[] startVector;
	private double[] previousPosition;
	private int TSLI;
	public Fish(double[] startPos, double startAngle, double[] accel, double[] dest, GameBoard b)
	{
		super(startPos, startAngle, accel, dest, b);
		startVector = new double[] {startPos[0],startPos[1]};
		init();
	}

	@Override
	public void init()
	{
		inputVector = new double[5];
		outputVector = new double[2];
		previousPosition = new double[2];
		TSLI = 0;
	}
	
	@Override
	public void executeDecision()
	{
		if(phenotype == null) {return;}
		loadInputVector();
		boolean success = phenotype.activate(inputVector);
		for(int relax = 0;relax<phenotype.getDepth();relax++)
		{
			success = phenotype.activate(inputVector);
		}
		outputVector = phenotype.readOutputVector();
		
		velocity[0] = (outputVector[0] - 0.5)*maxVelocity[0];
		velocity[1] = (outputVector[1] - 0.5)*maxVelocity[1];
		phenotype.reset();
		
		if(previousPosition[0] != position[0] ||
		   previousPosition[1] != position[1]) {TSLI=0;}
		else{TSLI++;}
		
		previousPosition[0] = position[0];
		previousPosition[1] = position[1];
	}
	
	public void loadInputVector()
	{
		int xo = 32;
		int yo = 32;
		Tile t = null;
		int itr = 0;
		for(int i=0;i<inputVector.length;i++) {inputVector[i]=0;}
		
		t = board.getTile(new double[]{position[0]+xo,position[1]-yo});
		if(t!=null && t.isCollidable())
		{inputVector[itr++]=1;}
		else {inputVector[itr++]=0;}
		
		t = board.getTile(new double[]{position[0]+xo,position[1]});
		if(t!=null && t.isCollidable())
		{inputVector[itr++]=1;}
		else {inputVector[itr++]=0;}
		
		t = board.getTile(new double[]{position[0]+xo,position[1]+yo});
		if(t!=null && t.isCollidable())
		{inputVector[itr++]=1;}
		else {inputVector[itr++]=0;}
		
		inputVector[itr++]=velocity[0];
		inputVector[itr++]=velocity[1];
		
		
		//top left
		/*t = board.getTile(new double[]{position[0]-xo,position[1]-yo});
		if(t == null) 
		{
			inputVector[itr] = 0;
			itr++;
			inputVector[itr] = 0;
			itr++;
		}
		else 
		{
			inputVector[itr] = getDistance(t);
			itr++;
			if(t.isCollidable()) {inputVector[itr] = 1.0;}
			else {inputVector[itr] = 0.0;}
			itr++;
		}
		
		
		//top middle
		t = board.getTile(new double[]{position[0],position[1]-yo});
		if(t == null) 
		{
			inputVector[itr] = 0;
			itr++;
			inputVector[itr] = 0;
			itr++;
		}
		else 
		{
			inputVector[itr] = getDistance(t);
			itr++;
			if(t.isCollidable()) {inputVector[itr] = 1.0;}
			else {inputVector[itr] = 0.0;}
			itr++;
		}
		
		
		//top right
		t = board.getTile(new double[]{position[0]+xo,position[1]-yo});
		if(t == null) 
		{
			inputVector[itr] = 0;
			itr++;
			inputVector[itr] = 0;
			itr++;
		}
		else 
		{
			inputVector[itr] = getDistance(t);
			itr++;
			if(t.isCollidable()) {inputVector[itr] = 1.0;}
			else {inputVector[itr] = 0.0;}
			itr++;
		}
		
		
		//middle left
		t = board.getTile(new double[]{position[0]-xo,position[1]});
		if(t == null) 
		{
			inputVector[itr] = 0;
			itr++;
			inputVector[itr] = 0;
			itr++;
		}
		else 
		{
			inputVector[itr] = getDistance(t);
			itr++;
			if(t.isCollidable()) {inputVector[itr] = 1.0;}
			else {inputVector[itr] = 0.0;}
			itr++;
		}
		
		
		//center
		t = board.getTile(new double[]{position[0],position[1]});
		if(t == null) 
		{
			inputVector[itr] = 0;
			itr++;
			inputVector[itr] = 0;
			itr++;
		}
		else 
		{
			inputVector[itr] = getDistance(t);
			itr++;
			if(t.isCollidable()) {inputVector[itr] = 1.0;}
			else {inputVector[itr] = 0.0;}
			itr++;
		}
		
		
		//middle right
		t = board.getTile(new double[]{position[0]+xo,position[1]});
		if(t == null) 
		{
			inputVector[itr] = 0;
			itr++;
			inputVector[itr] = 0;
			itr++;
		}
		else 
		{
			inputVector[itr] = getDistance(t);
			itr++;
			if(t.isCollidable()) {inputVector[itr] = 1.0;}
			else {inputVector[itr] = 0.0;}
			itr++;
		}
		
		
		//bottom left
		t = board.getTile(new double[]{position[0]-xo,position[1]+yo});
		if(t == null) 
		{
			inputVector[itr] = 0;
			itr++;
			inputVector[itr] = 0;
			itr++;
		}
		else 
		{
			inputVector[itr] = getDistance(t);
			itr++;
			if(t.isCollidable()) {inputVector[itr] = 1.0;}
			else {inputVector[itr] = 0.0;}
			itr++;
		}
		
		
		//bottom middle
		t = board.getTile(new double[]{position[0],position[1]+yo});
		if(t == null) 
		{
			inputVector[itr] = 0;
			itr++;
			inputVector[itr] = 0;
			itr++;
		}
		else 
		{
			inputVector[itr] = getDistance(t);
			itr++;
			if(t.isCollidable()) {inputVector[itr] = 1.0;}
			else {inputVector[itr] = 0.0;}
			itr++;
		}
		
		
		//bottom right
		t = board.getTile(new double[]{position[0]+xo,position[1]+yo});
		if(t == null) 
		{
			inputVector[itr] = 0;
			itr++;
			inputVector[itr] = 0;
			itr++;
		}
		else 
		{
			inputVector[itr] = getDistance(t);
			itr++;
			if(t.isCollidable()) {inputVector[itr] = 1.0;}
			else {inputVector[itr] = 0.0;}
			itr++;
		}
		*/
	}
	public double getDistance(Tile t)
	{
		if(t == null) {return -1;}
		double x = position[0]+width/2.0 - t.getPosition()[0]-t.getWidth()/2.0;
		double y = position[1]+height/2.0 - t.getPosition()[1]-t.getHeight()/2.0;
		return Math.sqrt(Math.pow(x,2)+Math.pow(y, 2));
	}
	@Override
	public double getFitness()
	{
		if(phenotype == null) {return Math.random()*0.001;}
		
		//Manhattan distance between the fish and the destination.
		double fitnessValue = Math.abs(position[0]-destination[0])+Math.abs(position[1]-destination[1]);
		double dist = Math.abs(startVector[0]-destination[0])+Math.abs(startVector[1]-destination[1]);
		//System.out.println(dist+"   "+fitnessValue+"   "+(2*dist-fitnessValue));
		return 2*dist - fitnessValue;
	}
	public int getTSLI() {return TSLI;}
}