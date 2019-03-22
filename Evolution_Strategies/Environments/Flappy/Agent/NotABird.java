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
	private double gravity = 2;
	private double jumpSpeed = 3;
	private double fitness = 0, prevFitness = 0;
	private double rewardPerFrame = 1;
	private int windowHeight, windowWidth;
	private double[] inputVector;
	
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
        fitness = 0;
        prevFitness = 0;
        inputVector = new double[Config.POLICY_INPUT_NODES];
        
    }
	
	@Override
    public void eUpdate()
    {
		prevFitness = fitness;
		//System.out.println("BIRD UPDATING");
	    //System.out.println(position[0]+","+position[1]);
	    if(colliding || checkVictoryCondition()) {return;}
	    //fitness+=rewardPerFrame;
	    clamp();
    }
	
	@Override
    public double takeAction(int actionNum)
    {
	    //System.out.println(getX()+","+getY()+" | "+nearestPole.getX()+" | "+nearestPole.getGapStart()+" "+fitness);
        if(actionNum == 0)
        {
            //System.out.println("JUMP");
            jump();
        }
        
        if(colliding)
        {
            return -rewardPerFrame;
        }
        
        if(checkVictoryCondition())
        {
            return rewardPerFrame;
        }
        
        return fitness - prevFitness;
    }

    @Override
    public double[] getState()
    {
        if(colliding || checkVictoryCondition())
        {
            return null;
        }
        /*
         *  player y position.
			players velocity.
			next pipe distance to player
			next pipe top y position
			next pipe bottom y position
			next next pipe distance to player
			next next pipe top y position
			next next pipe bottom y position
         */
        
        inputVector[0] = position[1];
        inputVector[1] = velocity[1];
        inputVector[2] = nearestPole.getX() - getX();
        inputVector[3] = nearestPole.getGapStart();
        inputVector[4] = nearestPole.getGapStart() + nearestPole.getGapSize();
        return inputVector;
    }
    @Override
    public void eRender(Graphics2D g, double interp)
    {
        
    }
	private void clamp()
	{
		if(position[1] < 0) {position[1] = 0;}
		else if(position[1]+height > windowHeight) {position[1] = windowHeight - height;}
	}
	
	private boolean checkVictoryCondition()
	{
		if(fitness >= 2000) {return true;}
		NotAPole temp = knownPoles.get(0);
		for(NotAPole p : knownPoles)
		{
			if(p.collides(this))
			{
				colliding = true;
				return false;
			}
			
			if(p.getX()+p.getWidth() >= position[0])
			{
				if(p.getX() < temp.getX())
				{
					temp = p;
				}
			}
		}
		//temp.setColor(Color.GREEN);
		if(nearestPole != temp)
		{
			fitness += rewardPerFrame;
		}
		nearestPole = temp;
		return false;
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
