package NEAT.Simulations.SnakeSim.workers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import core.camera.Camera;
import core.entities.Entity;
import core.phys.PhysicsObject;
import core.util.TextureHandler;

public class SnakeSegment extends Entity
{
    private SnakeSegment parent;
    private int currentDirection;
    public SnakeSegment(double[] startPos, double startAngle, Camera cam, SnakeSegment p)
    {
        super(startPos, startAngle, cam);
        parent = p;
    }

    @Override
    public void init()
    {
        width = Snake.SEGMENT_SIZE;
        height = Snake.SEGMENT_SIZE;
        collidable = false;
        acceleration[0] = 0;
        acceleration[1] = 0;
        velocity[0] = 0;
        velocity[1] = 0;
        hasSelectiveCollisions = false;
        upperExtension = -1;
        lowerExtension = -1;
        leftExtension = -1;
        rightExtension = -1;

        setTexture(buildImage(),scale);
    }

    @Override
    public void eUpdate()
    {
    }

    @Override
    public void eRender(Graphics2D g, double interp)
    {
    }
    
    @Override
    public void handleCollision(PhysicsObject collider)
    {
    }
    
    public void moveTo(int x, int y)
    {
    }
    
    public double[] getBehindPos()
    {
        double[] pos = new double[2];
        switch(currentDirection)
        {
            case(Snake.UP):
                pos[0] = position[0];
                pos[1] = position[1]+height+1;
                break;
            case(Snake.DOWN):
                pos[0] = position[0];
                pos[1] = position[1]-height-1;
                break;
            case(Snake.LEFT):
                pos[0] = position[0]+width+1;
                pos[1] = position[1];
                break;
            case(Snake.RIGHT):
                pos[0] = position[0]-width-1;
                pos[1] = position[1];
                break;
        }
        return pos;
    }
    public int getDirection()
    {
        return currentDirection;
    }
    
    public void setDirection(int i)
    {
        currentDirection = i;
    }
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
