package NEAT.Simulations.FishMaze.Workers;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
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
	protected double[] velocity;
	protected double[] acceleration;
	protected double[] maxVelocity;
	protected double[] destination;
	protected double[] home;
	protected double[] orientation;
	
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
	public Worker(double[] startPos, double startAngle, double[] accel, double[] dest, GameBoard b)
	{
		board = b;
		destination=dest;
		theta = startAngle;
		
		acceleration = accel;
		orientation = new double[2];
		position = new double[]{startPos[0],startPos[1]};
		velocity = new double[2];
		maxVelocity = new double[]{5.0f,5.0f};
		
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
		    t.rotate(theta, width/2, height/2);
		    
			orientation[0] = (double) Math.cos(theta);
			orientation[1] = (double) Math.sin(theta);
			
			if(!colH){position[0]+=velocity[0];}
			if(!colV){position[1]+=velocity[1];}
			
			for(int i=0;i<2;i++)
			{
				velocity[i]+=acceleration[i]*orientation[i]*delta;
				if(velocity[i]>maxVelocity[i]){velocity[i]=maxVelocity[i];}
				else if(velocity[i]<-maxVelocity[i]){velocity[i]=-maxVelocity[i];}
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
		}
		catch(Exception e){e.printStackTrace();}
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
		double xSpd = velocity[0];
		double ySpd = velocity[1];
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
		velocity[0] = xSpd;
		velocity[1] = ySpd;
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
		if(species == -1 || species > 255) {species = 0;}
		img = null;
		try{img = ImageIO.read(new File("resources/textures/workers/worker_"+species+".png"));}
		catch(Exception e){System.out.println(species);e.printStackTrace();}
		width = img.getWidth();
		height = img.getHeight();
		
		phenotype = org.getPhenotype();
	}
	
	public double[] getPosition() {return new double[]{position[0],position[1]};}
	public double[] getVelocity() {return new double[]{velocity[0],velocity[1]};}
	public double[] getAcceleration() {return new double[]{acceleration[0],acceleration[0]};}
	public double[] getDestination() {return destination;}
	public double[] getOrientation() {return orientation;}
	public boolean isThreadClosed(){return threadClosed;}
	public void setPosition(double[] position) {this.position = new double[]{position[0],position[1]};}
	public void setVelocity(double[] velocity) {this.velocity = new double[]{velocity[0],velocity[1]};}
	public void setAcceleration(double[] acceleration) {this.acceleration = new double[]{acceleration[0],acceleration[1]};}
	public void setDestination(double[] destination) {this.destination = destination;}
}