package NEAT.Simulations.Pinball;

import java.awt.Graphics2D;

import NEAT.Display.DisplayObject;

public class Machine extends DisplayObject
{
	private double ballPos[];
	private double ballVel;
	private double ballAccel;
	private double ballMass;
	private double ballForceTheta;
	
	private double gravity;
	
	private double pinMass;
	
	private double leftPinTheta;
	private double rightPinTheta;
	private double leftPinPos[];
	private double rightPinPos[];
	
	private double sin,cos;
	public Machine()
	{
	}

	@Override
	public void update(double delta) 
	
	{
		sin = Math.sin(ballForceTheta);
	}

	@Override
	public void render(Graphics2D g) 
	{
	}
}
