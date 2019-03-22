package Evolution_Strategies.Environments.Flappy.Environment;

import java.awt.Graphics2D;

import Evolution_Strategies.Environments.Flappy.Agent.NotABird;
import core.camera.Camera;
import core.entities.Entity;

public class NotAPole extends Entity
{	
    private int gap;
    private int width;
    private int gapStart;
    private int screenHeight;
    private int minHeight;
    private boolean needsReset;

    public NotAPole(double[] startPos, double startAngle, Camera cam, int g, int scrh)
    {
        super(startPos, startAngle, cam);
        gap = g;
        screenHeight = scrh;
        gapStart = minHeight + (int)(Math.round(Math.random()*(screenHeight-minHeight*2)));
        
    }

    @Override
    public void init()
    {
        velocity[0] = -3;
        needsReset = false;
        minHeight = 30;
        width = 75;
        collidable = false;
    }
    @Override
    public void eUpdate()
    {
    	//System.out.println("POLE UPDATE "+position[0]+" "+velocity[0]);
    	velocity[0] = -3;
    	
        if(position[0]+width<0) {needsReset = true;}
    }
    @Override
    public void eRender(Graphics2D g, double interp)
    {
        g.setColor(color);
        g.fillRect((int)(position[0]*scale[0]), 0, (int)(scale[0]*width),(int)(scale[1]*gapStart));
        g.fillRect((int)(position[0]*scale[0]), (int)(scale[1]*(gapStart+gap)), (int)(scale[0]*width), (int)(scale[1]*screenHeight));
    }

    public void reset(double newX) 
    {
        position[0] = newX;
        needsReset = false;
        gapStart = minHeight + (int)(Math.round(Math.random()*(screenHeight-minHeight*2)));
    }
    
    public boolean collides(NotABird bird)
    {
        double bx = bird.getX();
        double by = bird.getY();
        double bw = bird.getWidth();
        double bh = bird.getHeight();
        if(bx+bw >= position[0] && bx < position[0]+width)
        {
            if(by < gapStart || by+bh > gap+gapStart)
            {
                return true;
            }
        }

        return false;
    }
    public int getGapStart() {return gapStart;}
    public int getGapSize() {return gap;}
    public boolean doesNeedReset() {return needsReset;}



}
