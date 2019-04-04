package Evolution_Strategies.Environments.Pong.engine;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import Evolution_Strategies.Environments.Environment;
import Evolution_Strategies.Environments.EnvironmentAgent;
import Evolution_Strategies.Environments.Pong.workers.Ball;
import Evolution_Strategies.Environments.Pong.workers.Paddle;
import core.camera.Camera;
import core.phys.PhysicsObject;

public class PongLevel extends Environment
{
	private Paddle[] paddles;
	private Ball ball;
	private ArrayList<EnvironmentAgent> agents;
	
	public PongLevel(PhysicsObject p, Camera cam, double[] resScale) 
	{
		super(p, cam, resScale);
	}

	@Override
	public void initEnv() 
	{
		for(Paddle p : paddles)
		{
			p.reset();
		}
		ball.reset();
	}

	@Override
	public ArrayList<EnvironmentAgent> buildAgents(int numAgents) 
	{
		if(agents == null || agents.size() == 0)
		{
			agents = new ArrayList<EnvironmentAgent>();
			agents.add(paddles[0]);
		}
		return null;
	}

	@Override
	public void takeStep() 
	{
		update();
	}

	@Override
	public void keyPressed(KeyEvent e) 
	{
	}

	@Override
	public void keyReleased(KeyEvent e) 
	{
	}

	@Override
	public void keyTyped(KeyEvent e) 
	{
	}

	@Override
	public void loadMap() 
	{
	}

	@Override
	public void loadEntities() 
	{
		agents = new ArrayList<EnvironmentAgent>();
		paddles = new Paddle[2];
		
		double[] leftPaddleSpawn = new double[]{camera.getViewPort().getWidth() - Paddle.PADDLE_WIDTH, 
				  								camera.getViewPort().getHeight()/2};
		
		double[] rightPaddleSpawn = new double[]{Paddle.PADDLE_WIDTH,camera.getViewPort().getHeight()/2};
		
		double[] ballSpawn = new double[]{camera.getViewPort().getWidth()/2, 
										  camera.getViewPort().getHeight()/2};
		ball = new Ball(ballSpawn,0,camera);
		paddles[0] = new Paddle(rightPaddleSpawn,0,camera);
		paddles[1] = new Paddle(leftPaddleSpawn, 0, camera);
		
	}

	@Override
	public void levelUpdate() 
	{
	}

	@Override
	public void levelRender(Graphics2D g, double delta) 
	{
	}
	
}
