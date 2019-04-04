package Evolution_Strategies.Environments.Pong.workers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import Evolution_Strategies.Configs.Config;
import Evolution_Strategies.Environments.EnvironmentAgent;
import core.camera.Camera;
import core.phys.PhysicsObject;
import core.util.TextureHandler;

public class Paddle extends EnvironmentAgent
{
	private double fitness;
	private Ball ball;
	public Paddle(double[] startPos, double startAngle, Camera cam, Ball b) 
	{
		super(startPos, startAngle, cam);
		ball = b;
	}
	
	@Override
	public void handleCollision(PhysicsObject collider)
	{
		if(collider instanceof Ball)
		{
			double x = (position[0] + width/2) - (ball.getX() + ball.getWidth()/2);
			double y = (position[1] + height/2) - (ball.getY() + ball.getHeight()/2);
			double angle = Math.atan2(y,x) + Math.PI;
			fitness++;
		}
	}
	
	@Override
	public double takeAction(int actionNum) 
	{
		if(actionNum == 0)
		{
			velocity[1] = 5;
		}
		else
		{
			velocity[1] = -5;
		}
		return fitness;
	}

	@Override
	public double[][][] getState() 
	{
		if(dead){return null;}
		double[][][] state = new double[Config.FFNN_LAYER_INFO[0]][1][1];
		
		state[0][0][0] = position[0]/camera.getViewPort().getWidth();
		state[1][0][0] = position[1]/camera.getViewPort().getHeight();
		state[2][0][0] = velocity[0]/5;
		state[3][0][0] = velocity[1]/5;
		state[4][0][0] = ball.getPos()[0]/camera.getViewPort().getWidth();
		state[5][0][0] = ball.getPos()[1]/camera.getViewPort().getHeight();
		state[6][0][0] = ball.getVelocity()[0]/ball.getMaxVelocity()[0];
		state[7][0][0] = ball.getVelocity()[1]/ball.getMaxVelocity()[1];
		
		return state;
	}

	@Override
	public void init() 
	{
		fitness = 0;
		width = PADDLE_WIDTH;
		height = PADDLE_HEIGHT;
		setTexture(buildTexture(), scale);
	}

	@Override
	public void eUpdate() 
	{
	}

	@Override
	public void eRender(Graphics2D g, double interp) 
	{
	}
	
	public void reset()
	{
		fitness = 0;
		setPos(origin);
	}
	
	public double getFitness()
	{
		return fitness;
	}
	
	
	
	private BufferedImage buildTexture()
	{
		BufferedImage image = new BufferedImage((int)width,(int)height,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D)image.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, (int)width, (int)height);
        g.dispose();
        return TextureHandler.scaleTexture(image, scale);
	}
	
	public static final int PADDLE_WIDTH = 20;
	public static final int PADDLE_HEIGHT = 100;
}
