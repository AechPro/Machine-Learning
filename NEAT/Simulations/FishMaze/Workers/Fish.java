package NEAT.Simulations.FishMaze.Workers;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

import NEAT.Population.Organism;
import NEAT.Simulations.FishMaze.*;


public class Fish extends Worker
{

	private double[][][] inputVector;
	private double[] outputVector;
	private double[] startVector;
	private double previousScore;
	private double bestScore;
	private double fitness;
	private int TSLI;
	private int w,h;
	private boolean victory;

	public Fish(double[] startPos, double startAngle, double accel, double[] dest, GameBoard b) 
	{
		super(startPos, startAngle, accel, dest, b);
		startVector = new double[] {startPos[0],startPos[1]};
		acceleration = 1;
		w = 1;
		h = 4;
		init();
	}

	@Override
	public void init()
	{
		inputVector = new double[w*h+1][1][1];
		outputVector = new double[2];
		TSLI = 0;
		victory=false;
		fitness = 0;
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
			victory = true;
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
		//if(outputVector[0] >= outputVector[1]) {theta-=0.1;}
		//else{theta+=0.1;}
		
		if(outputVector[0] >= outputVector[1]*1.05) {theta += Math.PI/10;}
		else if(outputVector[0] <= outputVector[1]*0.95){theta-=Math.PI/10;}
		
		
	}

	public void loadInputVector()
	{
		Tile t = null;
		int to = 31;
		int itr = 0;
		for(int i=0;i<inputVector.length;i++) {inputVector[i][0][0]=-3;}
		/*for(int i=0;i<w;i++)
		{
			for(int j=0;j<h;j++)
			{
				t = board.getTile(new double[] {position[0]+to*i,position[1]+to*j});
				if(t == null) {inputVector[itr++] = -1;}
				else if(t.isCollidable()) {inputVector[itr++]=1;}//getDistance(t)/(w*32);}
				else {inputVector[itr++]=0;}
			}
		}*/
		loadSensors();
		double dist = getDistance(board.getDest());
		if(dist < bestScore)
		{
			bestScore = dist;
			fitness += 60;
		}
		inputVector[inputVector.length-2][0][0] = theta/Math.PI*2;
		inputVector[inputVector.length-2][0][0] = dist/(32*board.getWidth());
	}

	public void loadSensors()
	{
		double FOV = Math.PI/2;
		int itr = 0;
		
		//center sensor
		double o1 = Math.cos(theta);
		double o2 = Math.sin(theta);
		loadSensor(o1,o2,itr++);

		//up sensor
		o1 = Math.cos(theta - FOV/2);
		o2 = Math.sin(theta - FOV/2);
		loadSensor(o1,o2,itr++);

		//down sensor
		o1 = Math.cos(theta + FOV/2);
		o2 = Math.sin(theta + FOV/2);
		loadSensor(o1,o2,itr++);
		
	}
	
	public void loadSensor(double o1, double o2,int itr)
	{
		int x = 0;
		int y = 0;
		int r = 32;
		Tile t = null;
		int sensorRange = 3;
		Line2D sensorLine = new Line2D.Double(position[0]+width/2,position[1]+height/2,o1*sensorRange*r+position[0]+width/2,o2*sensorRange*r+position[1]+height/2);
		for(int i=0;i<sensorRange;i++)
		{
			x = (int)(o1*r*(i) + position[0] + width/2);
			y = (int)(o2*r*(i) + position[1] + height/2);
			t = board.getTile(new double[] {x,y});
			if(t == null) {inputVector[itr][0][0] = -1; break;}
			if(t.isCollidable())
			{
				Rectangle rect = new Rectangle((int)t.getPosition()[0],(int)t.getPosition()[1],t.getWidth(),t.getHeight());
				if(sensorLine.intersects(rect))
				{
					inputVector[itr][0][0] = getDistance(t)/(r*sensorRange);
					break;
				}
				
			}
			else {inputVector[itr][0][0]=1.1;}
		}
	}
	public boolean checkVictoryCondition()
	{
		return getDistance(board.getDest())<31;
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
		return fitness + 2*dist - fitnessValue;
	}
	public int getTSLI() {return TSLI;}
	public boolean hasFinished() {return victory;}
}