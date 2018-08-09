package NEAT.Simulations.FishMaze.Workers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import NEAT.Population.*;
import NEAT.Display.DisplayObject;
import NEAT.Simulations.FishMaze.*;

public abstract class Worker extends DisplayObject
{
	protected double[] position;
	protected double velocity;
	protected double acceleration;
	protected double maxVelocity;
	protected double[] destination;
	protected double[] home;
	protected double[] orientation;
	protected double vel;
	
	protected int width,height;
	protected int species;
	
	protected double theta;
	
	protected AffineTransform t;
	protected BufferedImage img;
	protected GameBoard board;
	
	protected boolean colH, colV;
	protected ArrayList<Tile> collisions;
	protected ArrayList<Tile> collidableTiles;
	
	protected Thread thread;
	protected boolean threadClosed;
	
	protected double adjustedFitness;
	protected Phenotype phenotype;
	
	public abstract void init();
	public abstract double getFitness();
	public abstract void executeDecision();
	public Worker(double[] startPos, double startAngle, double accel, double[] dest, GameBoard b)
	{
		board = b;
		destination=dest;
		theta = startAngle;
		
		acceleration = accel;
		orientation = new double[2];
		position = new double[]{startPos[0],startPos[1]};
		maxVelocity = 5;
		
		home = new double[]{startPos[0],startPos[1]};
		
		width=height=0;
		
		collisions = new ArrayList<Tile>();
		collidableTiles = board.getCollidableTiles();
	}
	
	@Override
	public void update(double delta)
	{
		try
		{
			t = new AffineTransform();
		    t.translate(position[0], position[1]);
		    t.rotate(theta+Math.PI/2, width/2, height/2);
		    
			orientation[0] = (double) Math.cos(theta);
			orientation[1] = (double) Math.sin(theta);
			
			if(!colH){position[0]+=velocity*orientation[0];}
			if(!colV){position[1]+=velocity*orientation[1];}
			
			for(int i=0;i<2;i++)
			{
				velocity+=acceleration;
				if(velocity>maxVelocity){velocity=maxVelocity;}
				else if(velocity<-maxVelocity){velocity=-maxVelocity;}
			}
			checkCollisions();
			executeDecision();
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	@Override
	public void render(Graphics2D g)
	{
		try
		{
			if(img != null)
			{
				
			    g.transform(t);
			    g.drawImage(img,0,0,null);
			    g.transform(t.createInverse());
			}
			
			//drawSensors(g);
			
			/*if(phenotype!=null)
			{
				phenotype.render(g);
			}*/
		}
		catch(Exception e){e.printStackTrace();}
	}
	public void drawSensors(Graphics2D g)
	{
		double o1,o2 = 0;
		double FOV = Math.PI/4;
		
		o1 = Math.cos(theta - FOV/2);
		o2 = Math.sin(theta - FOV/2);
		drawSensor(o1,o2,g);
		
		o1 = Math.cos(theta + FOV/2);
		o2 = Math.sin(theta + FOV/2);
		drawSensor(o1,o2,g);
		
		o1 = Math.cos(theta);
		o2 = Math.sin(theta);
		drawSensor(o1,o2,g);
	}
	public void drawSensor(double o1, double o2, Graphics2D g)
	{
		Color c1 = new Color(255,0,0,100);
		Color c2 = new Color(0,255,0,100);
		Color collidableTile = Color.RED;
		Color freeTile = Color.GREEN;
		Color nullTile = Color.WHITE;
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
			if(t == null) {g.setColor(nullTile);break;}
			Rectangle rect = new Rectangle((int)t.getPosition()[0],(int)t.getPosition()[1],t.getWidth(),t.getHeight());
			if(sensorLine.intersects(rect))
			{
				if(t.isCollidable())
				{
					g.setColor(Color.YELLOW);
					g.draw(rect);
					
					g.setColor(collidableTile);
					t.setColor(c1);
					
					break;
				}
				else
				{
					g.setColor(freeTile);
					t.setColor(c2);
				}
				
			}
		}
		g.draw(sensorLine);
	}
	public boolean isColliding(int x1, int y1, int w, int h, int x2, int y2, int w2, int h2)
	{
		Rectangle r1 = new Rectangle(x1,y1,w,h);
		Rectangle r2 = new Rectangle(x2,y2,w2,h2);
		return r1.intersects(r2);
	}
	
	public void checkCollisions()
	{
		double x = position[0];
		double y = position[1];
		double xSpd = velocity*orientation[0];
		double ySpd = velocity*orientation[1];
		double prevYSpd = ySpd,prevXSpd=xSpd;
		int hitBox = 0;
		int tw = board.getTileSize()[0];
		int th = board.getTileSize()[1];
		colV=false;
		colH=false;
		
		int px = (int)(x+(width/2)+xSpd)+hitBox;
		int py = (int)(y+(height/2)+ySpd);
		double[] colPos;
		
		collisions.clear();
		
		for(int i=0;i<collidableTiles.size();i++)
		{
			if(isColliding(px,py,width,height,(int)collidableTiles.get(i).getPosition()[0],(int)collidableTiles.get(i).getPosition()[1],tw,th))
			{
				collisions.add(collidableTiles.get(i));
			}
		}
		
		for(int i=0;i<collisions.size();i++)
		{
			double angle=0;
			colPos = new double[]{collisions.get(i).getPosition()[0],collisions.get(i).getPosition()[1]};
			angle = Math.atan2((double)(colPos[1]+th/2)-(py-ySpd+height/2.0),(double)(colPos[0]+tw/2)-(px-xSpd+width/2.0));
			angle*=180.0/-Math.PI;
			if(angle<0){angle=360-Math.abs(angle);}
			int yOffset = (int) (Math.abs((py + height/2) - (colPos[1]+th/2)) - (height/2 + th/2));
			int xOffset = (int) (Math.abs((px + width/2) - (colPos[0]+tw/2)) - (width/2 + tw/2));
			if((angle>46 && angle<134))
			{
				ySpd=0;
				if(!colV){py-=yOffset;}
				colV=true;
			}
			else if((angle<44 || angle>316))
			{
				xSpd=0;
				if(!colH){px+=xOffset;}
				colH=true;
			}
			else if(angle>136 && angle<224)
			{
				xSpd=0;
				if(!colH){px-=xOffset;}
				colH=true;
			}
			else if((angle>226 && angle<314))
			{
				ySpd=0;
				if(!colV){py+=yOffset;}
				colV=true;
			}
			for(int j=0;j<collisions.size();j++)
			{
				if(i!=j)
				{
					if(!isColliding(px,py,width,height,(int)collisions.get(j).getPosition()[0],(int)collisions.get(j).getPosition()[1],tw,th))
					{
						collisions.remove(collisions.get(j));
					}
				}
			}
		}
		colV=false;
		colH=false;
		xSpd=prevXSpd;
		ySpd=prevYSpd;
		px = (int)(x+(width/2)+xSpd)+hitBox;
		py = (int)(y+(height/2)+ySpd);
		for(int i=0;i<collisions.size();i++)
		{
			double angle=0;
			colPos = new double[]{collisions.get(i).getPosition()[0],collisions.get(i).getPosition()[1]};
			angle = Math.atan2((double)(colPos[1]+th/2)-(py-ySpd+height/2.0),(double)(colPos[0]+tw/2)-(px-xSpd+width/2.0));
			angle*=180.0/-Math.PI;
			if(angle<0){angle=360-Math.abs(angle);}
			int yOffset = (int) (Math.abs((py + height/2) - (colPos[1]+th/2)) - (height/2 + th/2));
			int xOffset = (int) (Math.abs((px + width/2) - (colPos[0]+tw/2)) - (width/2 + tw/2));
			if((angle>46 && angle<134))
			{
				ySpd=0;
				if(!colV){y-=yOffset;}
				colV=true;
			}
			else if((angle<44 || angle>316))
			{
				xSpd=0;
				if(!colH){x+=xOffset;}
				colH=true;
			}
			else if(angle>136 && angle<224)
			{
				xSpd=0;
				if(!colH){x-=xOffset;}
				colH=true;
			}
			else if((angle>226 && angle<314))
			{
				ySpd=0;
				if(!colV){y+=yOffset;}
				colV=true;
			}
		}
		
		position[0] = x;
		position[1] = y;
		velocity = Math.sqrt(Math.pow(xSpd, 2)+Math.pow(ySpd, 2));
	}
	
	public void startThread()
	{
		threadClosed = false;
		thread = new Thread()
		{
			int frames = 0;
			public void run()
			{
				while(frames-->-100)
				{
					update(1.0);
				}
				threadClosed = true;
			}
		};
		try{thread.start();}
		catch(Exception e){e.printStackTrace();}
	}
	
	public void setPhenotype(Organism org)
	{
		species = org.getColorMarker();
		if(species == -1 || species > 13000) {species = 0;}
		img = null;
		try{img = ImageIO.read(new File("resources/textures/workers/worker_"+species+".png"));}
		catch(Exception e){System.out.println(species);e.printStackTrace();}
		width = img.getWidth();
		height = img.getHeight();
		
		phenotype = org.getPhenotype();
	}
	
	public double[] getPosition() {return new double[]{position[0],position[1]};}
	public double getVelocity() {return velocity;}
	public double getAngle() {return theta;}
	public double getAcceleration() {return acceleration;}
	public double[] getDestination() {return destination;}
	public double[] getOrientation() {return orientation;}
	public boolean isThreadClosed(){return threadClosed;}
	public void setAngle(double theta) {this.theta=theta;}
	public void setPosition(double[] position) {this.position = new double[]{position[0],position[1]};}
	public void setVelocity(double velocity) {this.velocity = velocity;}
	public void setAcceleration(double acceleration) {this.acceleration = acceleration;}
	public void setDestination(double[] destination) {this.destination = destination;}
}