package NEAT.Simulations.FlappySquares;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

import NEAT.Configs.Config;
import NEAT.Display.DisplayObject;
import NEAT.Population.Organism;
import NEAT.Population.Phenotype;

public class NotABird extends DisplayObject
{
	
	private Phenotype phenotype;
	private ArrayList<NotAPole> knownPoles;
	private NotAPole nearestPole;
	private boolean colliding;
	private double[][][] inputVector;
	private double[] outputVector;
	private double x,y;
	private double gravity = 2;
	private double jumpSpeed = 3;
	private double ySpd;
	private double velocityCap;
	private double fitness = 0;
	private int width,height;
	private Color color;
	private int windowWidth,windowHeight;
	
	private BufferedImage texture;
	
	public NotABird(double sx, double sy, int w, int h, int winWidth, int winHeight, ArrayList<NotAPole> poles)
	{
		colliding = false;
		inputVector = new double[3][1][1];
		outputVector = new double[1];
		x = sx;
		y = sy;
		width = w;
		height = h;
		windowWidth = winWidth;
		windowHeight = winHeight;
		knownPoles = poles;
		velocityCap = 10;
		color = Color.RED;
		/*try
		{
			texture = ImageIO.read(new File("resources/NEAT/BirdTexture.png"));
		}
		catch(Exception e) {e.printStackTrace();}*/
	}
	
	public void loadInputVector()
	{
		inputVector[0][0][0] = (double)nearestPole.getX()/(double)windowWidth;
		inputVector[1][0][0] = (double)nearestPole.getGapStart()/(double)windowHeight;
		inputVector[2][0][0] = y/(double)windowHeight;
 	}
	public void jump()
	{
		ySpd -= jumpSpeed;
	}
	@Override
	public void update(double delta) 
	{
		y+=ySpd*delta;
		ySpd+=gravity;
		clamp();
		
		if(colliding || checkVictoryCondition()) {return;}
		if(phenotype == null) {return;}
		
		loadInputVector();
		boolean success = phenotype.activate(inputVector);
		for(int relax = 0,stop = phenotype.getDepth();relax<stop;relax++)
		{
			success = phenotype.activate(inputVector);
		}
		
		if(!success) {return;}
		outputVector = phenotype.readOutputVector();
		phenotype.reset();
		
		if(outputVector[0] >= 0.5) {jump();}
		
		fitness+=0.01;
	}

	@Override
	public void render(Graphics2D g, double frameDelta) 
	{
		if(colliding) {return;}
		//g.drawImage(texture,(int)x,(int)y,null);
		
		g.setColor(color);
		g.fillRect((int)x, (int)y, width, height);
	}
	
	private void clamp()
	{
		//if(x < 0) {x = 0;}
		//else if(x+width > windowWidth) {x = windowWidth - width;}
		if(ySpd > velocityCap) {ySpd = velocityCap;}
		else if(ySpd < -velocityCap) {ySpd = -velocityCap;}
		
		if(y < 0) {y = 0;}
		else if(y+height > windowHeight) {y = windowHeight - height;}
	}
	
	private boolean checkVictoryCondition()
	{
		if(fitness >= 2000) {colliding = true; return true;}
		NotAPole temp = knownPoles.get(0);
		for(NotAPole p : knownPoles)
		{
			if(p.collides(this))
			{
				colliding = true;
				return true;
			}
			
			if(p.getX()+p.getWidth() >= x)
			{
				if(p.getX() < temp.getX())
				{
					temp = p;
				}
			}
		}
		//temp.setColor(Color.GREEN);
		nearestPole = temp;
		return false;
	}
	public int getWidth() {return width;}
	public int getHeight() {return height;}
	public double getFitness() {return fitness;}
	public double getX() {return x;}
	public double getY() {return y;}
	public boolean isColliding() {return colliding;}
	
	public void setColliding(boolean i) {colliding=i;}
	public void setX(double i) {x=i;}
	public void setY(double i) {y=i;}
	public void setFitness(double i) {fitness=i;}
	public void setPhenotype(Organism org) 
	{
		phenotype = org.getPhenotype();
		int cm = org.getColorMarker();
		int offset = 50;
		Random rand = new Random((long)cm);
		int r = (int)Math.round(rand.nextDouble()*255-offset-1)+offset;
		int g = (int)Math.round(rand.nextDouble()*255-offset-1)+offset;
		int b = (int)Math.round(rand.nextDouble()*255-offset-1)+offset;
		color = new Color(r,g,b,255);
		/*Graphics2D g2 = (Graphics2D)texture.getGraphics();
		g2.setColor(color);
		g2.fillOval(1, 1, texture.getWidth()-1, texture.getHeight()-1);
		g2.dispose();*/
		//color = new Color((int)Math.round(offset + (Math.pow(2, 24)-offset-1)*cm/(double)Config.POPULATION_SIZE));
	}

}
