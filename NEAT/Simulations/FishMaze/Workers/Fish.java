package NEAT.Simulations.FishMaze.Workers;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
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
	private boolean victory;
	
	public Fish(double[] startPos, double startAngle, double accel, double[] dest, GameBoard b) 
	{
		super(startPos, startAngle, accel, dest, b);
		startVector = new double[] {startPos[0],startPos[1]};
		acceleration = 1;
		init();
	}

	@Override
	public void init()
	{
		inputVector = new double[4];
		outputVector = new double[2];
		previousPosition = new double[2];
		TSLI = 0;
		victory=false;
	}

	@Override
	public void executeDecision()
	{
		if(phenotype == null) {return;}
		if(checkVictoryCondition()) 
		{
			acceleration = 0;
			velocity = 0;
			theta = 0;
			return;
		}
		loadInputVector();
		boolean success = phenotype.activate(inputVector);
		for(int relax = 0;relax<phenotype.getDepth();relax++)
		{
			success = phenotype.activate(inputVector);
		}
		outputVector = phenotype.readOutputVector();
		/*
		theta = Math.PI*2*outputVector[2];
		acceleration[0] = (outputVector[0] - 0.5)*2;
		acceleration[1] = (outputVector[1] - 0.5)*2;*/
		//System.out.println("("+outputVector[0]+","+outputVector[1]+","+outputVector[2]+")");
		

		phenotype.reset();
		theta += (outputVector[0] >= outputVector[1]) ? 0.1 : -0.1;
	}

	public void loadInputVector()
	{
		double o1 = Math.cos(theta);
		double o2 = Math.sin(theta);
		int x = 0;
		int y = 0;
		int r = 32;
		int radiusStep = 1;
		Tile t = null;
		int itr = 0;
		int sensorRange = 3;
		double FOV = Math.PI/4;
		boolean found = false;
		//Line2D sensor;
		for(int i=0;i<inputVector.length;i++) {inputVector[i]=-1;}
		
		//center sensor
		found = false;
		for(int i=0;i<sensorRange;i++)
		{
			for(int j=0;j<r;j+=radiusStep)
			{
				x = (int)(o1*(j+1)*(i+1) + position[0] + width/2);
				y = (int)(o2*(j+1)*(i+1) + position[1] + height/2);
				t = board.getTile(new double[] {x,y});
				if(t == null) {inputVector[itr++] = 1; break;}
				if(t.isCollidable())
				{
					inputVector[itr++] = getDistance(t)/(r*sensorRange);
					found = true;
					break;
				}
			}
			if(found) {break;}
		}
		
		//up sensor
		o1 = Math.cos(theta - FOV/2);
		o2 = Math.sin(theta - FOV/2);
		found = false;
		for(int i=0;i<sensorRange;i++)
		{
			for(int j=0;j<r;j+=radiusStep)
			{
				x = (int)(o1*(j+1)*(i+1) + position[0] + width/2);
				y = (int)(o2*(j+1)*(i+1) + position[1] + height/2);
				t = board.getTile(new double[] {x,y});
				if(t == null) {inputVector[itr++] = 1; break;}
				if(t.isCollidable())
				{
					inputVector[itr++] = getDistance(t)/(r*sensorRange);
					found = true;
					break;
				}
			}
			if(found) {break;}
		}
		
		//down sensor
		o1 = Math.cos(theta + FOV/2);
		o2 = Math.sin(theta + FOV/2);
		found = false;
		for(int i=0;i<sensorRange;i++)
		{
			for(int j=0;j<r;j+=radiusStep)
			{
				x = (int)(o1*(j+1)*(i+1) + position[0] + width/2);
				y = (int)(o2*(j+1)*(i+1) + position[1] + height/2);
				t = board.getTile(new double[] {x,y});
				if(t == null) {inputVector[itr++] = 1; break;}
				if(t.isCollidable())
				{
					inputVector[itr++] = getDistance(t)/(r*sensorRange);
					found = true;
					break;
				}
			}
			if(found) {break;}
		}
		inputVector[itr++] = theta;
	}
	public boolean checkVictoryCondition()
	{
		victory = getDistance(board.getDest())<31;
		return victory;
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