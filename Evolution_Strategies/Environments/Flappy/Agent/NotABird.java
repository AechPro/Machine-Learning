package Evolution_Strategies.Environments.Flappy.Agent;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import Evolution_Strategies.Configs.Config;
import Evolution_Strategies.Environments.EnvironmentAgent;
import Evolution_Strategies.Environments.Flappy.Environment.NotAPole;
import core.camera.Camera;
import core.util.TextureHandler;

public class NotABird extends EnvironmentAgent
{
    private ArrayList<NotAPole> knownPoles;
	private NotAPole nearestPole;
	private boolean colliding;
	private double gravity = 0.5;
	private double jumpSpeed = 10;
	private double fitness = 0, prevFitness = 0;
	private double rewardPerFrame = 1;
	private int windowHeight, windowWidth;
	private double[][][] inputVector;
	
	public NotABird(double[] startPos, double startAngle, Camera cam, int winWidth, int winHeight, ArrayList<NotAPole> poles)
    {
        super(startPos, startAngle, cam);
        windowWidth = winWidth;
        windowHeight = winHeight;
        knownPoles = poles;
        nearestPole = knownPoles.get(0);
        colliding = false;
        collidable = false;
        width = 20;
        height = 20;
        setTexture(buildImage(),scale);
    }
	
	public void jump()
	{
		velocity[1] = -jumpSpeed;		
	}
	@Override
    public void init()
    {   
        acceleration[0] = 0;
        acceleration[1] = gravity;
        width = 20;
        height = 20;
        colliding = false;
        fitness = -3;
        prevFitness = 0;
        inputVector = new double[Config.FFNN_LAYER_INFO[0]][1][1];
    }
	
	@Override
    public void eUpdate()
    {
	    if(colliding || dead) {return;}
	    NotAPole temp = knownPoles.get(0);
		for(NotAPole p : knownPoles)
		{
			if(p.collides(this))
			{
			    kill();
				colliding = true;
			}
			
			if(p.getX() > position[0]+width)
			{
				if(p.getX() < temp.getX())
				{
					temp = p;
				}
			}
		}
		
		if(nearestPole != temp)
		{
		    //System.out.println("GOT A REWARD");
		    if(fitness < 0){fitness=0;}
			fitness += rewardPerFrame;
		}
		nearestPole = temp;
	    clamp();
    }
	
	@Override
    public double takeAction(int actionNum)
    {
	    //System.out.println(velocity[1]+" | "+getY()+" | "+nearestPole.getX()+" | "+nearestPole.getGapStart()+" | "+fitness+" | "+actionNum);
        if(actionNum == 0)
        {
            //System.out.println("JUMP");
            jump();
        }
        return fitness;
    }

    @Override
    public double[][][] getState()
    {
        if(dead || colliding)
        {
            return null;
        }
        
        inputVector[0][0][0] = position[1]/camera.getViewPort().getHeight();
        inputVector[1][0][0] = velocity[1]/maxVelocity[1];
        inputVector[2][0][0] = nearestPole.getX()/camera.getViewPort().getWidth();
        inputVector[3][0][0] = nearestPole.getGapStart()/camera.getViewPort().getHeight();
        inputVector[4][0][0] = (nearestPole.getGapStart() + nearestPole.getGapSize())/camera.getViewPort().getHeight();
        return inputVector;
    }
    @Override
    public void eRender(Graphics2D g, double interp)
    {
        
    }
	private void clamp()
	{
		if(position[1] < 0) {kill();}
		else if(position[1]+height > windowHeight) {kill();}
	}
	
	
	@Override
	public void kill()
	{
        fitness-=rewardPerFrame;
        dead = true;
        colliding = true;
	}
	
	public double getFitness() {return fitness;}
	public boolean isColliding() {return colliding;}
	
	public void setColliding(boolean i) {colliding=i;}
	public void setFitness(double i) {fitness=i;}

	private BufferedImage buildImage()
    {
        BufferedImage image = new BufferedImage((int)width,(int)height,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D)image.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, (int)width, (int)height);
        g.dispose();
        return TextureHandler.scaleTexture(image, scale);
    }

    

}
