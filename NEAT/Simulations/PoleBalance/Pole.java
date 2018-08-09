package NEAT.Simulations.PoleBalance;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class Pole 
{
	private final double GRAVITY=9.8;
	private final double MASSCART=1.0;
	private final double MASSPOLE=0.1;
	private final double TOTAL_MASS=(MASSPOLE + MASSCART);
	private double LENGTH=1.0;	  /* actually half the pole's length */
	private final double POLEMASS_LENGTH=(MASSPOLE * LENGTH);
	private final double FORCE_MAG=10.0;
	private final double TAU=0.02/4;	  /* seconds between state updates */
	private final double FOURTHIRDS=1.3333333333333;
	private double theta,thetaAcc,thetaDot,xDot,xAcc;
	private int[] polePos;
	private int radius;
	private int cartHeight, cartWidth;
	private int cx,cy;
	private Random rng;
	public Pole(int cw, int ch, int r, double l, double t, boolean randomStart)
	{
		cartHeight = ch;
		cartWidth = cw;
		polePos = new int[2];
		thetaDot = 0;
		LENGTH = l;
		theta = t;
		if(randomStart)
		{
			theta = ((Integer.MAX_VALUE*Math.random())%400)/1000.0 - .2;
			thetaDot = ((Integer.MAX_VALUE*Math.random())%3000)/1000.0 - 1.5;
		}
		rng = new Random((long)(Math.random()*Long.MAX_VALUE));
		radius = (int)(Math.round(r*LENGTH));
		
	}
	public double step(int y, int[] pos)
	{
		double force = (y>0)? FORCE_MAG : -FORCE_MAG;
		
		double costheta = Math.cos(theta);
		double sintheta = Math.sin(theta);

		double temp = (force + POLEMASS_LENGTH * thetaDot * thetaDot * sintheta)
				/ TOTAL_MASS;

		thetaAcc = (GRAVITY * sintheta - costheta * temp)
				   / (LENGTH * (FOURTHIRDS - MASSPOLE * costheta * costheta
				   / TOTAL_MASS));
		xAcc  = temp - POLEMASS_LENGTH * thetaAcc * costheta / TOTAL_MASS;
		xDot += TAU * xAcc;
		theta += TAU * thetaDot;
		//if(Math.random()<0.2) {thetaAcc+=thetaAcc*rng.nextGaussian();}
		thetaDot += TAU * thetaAcc;
		
		cx = pos[0];
		cy = pos[1];
		
		polePos[0] = cx + (int)(Math.round(radius*Math.cos(theta + Math.PI/2)))+cartWidth/2;
		polePos[1] = cy - (int)(Math.round(radius*Math.sin(theta + Math.PI/2)))+cartHeight/2;
		
		return xDot;
	}
	public void render(Graphics2D g)
	{
		g.setColor(Color.BLUE);
		g.drawLine(cx+cartWidth/2, cy+cartHeight/2, polePos[0], polePos[1]);		
		g.setColor(Color.RED);
		g.fillOval(polePos[0]-10, polePos[1]-10, 20, 20);
		
	}
	public double getTheta() {return theta;}
}
