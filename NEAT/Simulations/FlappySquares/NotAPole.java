package NEAT.Simulations.FlappySquares;

import java.awt.Color;
import java.awt.Graphics2D;

import NEAT.Display.DisplayObject;

public class NotAPole extends DisplayObject
{
	private int gap;
	private int width;
	private int gapStart;
	private int screenHeight;
	private int minHeight;
	private double x;
	private int xSpd;
	private int screenWidth;
	private Color c;
	private boolean needsReset;
	public NotAPole(int g, int w, int scrh, int scrw, int sx)
	{
		minHeight = 30;
		gap = g;
		width = w;
		screenHeight = scrh;
		gapStart = minHeight + (int)(Math.round(Math.random()*(screenHeight-minHeight*2)));
		x = sx;
		screenWidth = scrw;
		xSpd = -3;
		c = Color.WHITE;
	}
	@Override
	public void update(double delta) 
	{
		if(x+width<0) {needsReset = true;}
		x+=xSpd*delta;
	}
	@Override
	public void render(Graphics2D g)
	{
		g.setColor(c);
		g.fillRect((int)x,0,width,gapStart);
		g.fillRect((int)x, gapStart+gap, width, screenHeight);
	}
	public boolean collides(NotABird bird)
	{
		double bx = bird.getX();
		double by = bird.getY();
		int bw = bird.getWidth();
		int bh = bird.getHeight();
		
		if(bx+bw >= x && bx < x+width)
		{
			if(by < gapStart || by+bh > gap+gapStart)
			{
				return true;
			}
		}
		
		return false;
	}
	public void setColor(Color col) {c = col;}
	public double getX() {return x;}
	public int getWidth() {return width;}
	public int getGapStart() {return gapStart;}
	public int getGapSize() {return gap;}
	public boolean doesNeedReset() {return needsReset;}
	public void reset(double newX) 
	{
		x = newX;
		needsReset = false;
		gapStart = minHeight + (int)(Math.round(Math.random()*(screenHeight-minHeight*2)));
		c = Color.WHITE;
	}
	
}
