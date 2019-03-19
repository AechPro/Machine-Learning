package Evolution_Strategies.Environments.Snake.Workers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import core.camera.Camera;
import core.entities.Entity;
import core.util.TextureHandler;

public class Pellet extends Entity
{

    public Pellet(double[] startPos, double startAngle, Camera cam)
    {
        super(startPos, startAngle, cam);
        setTexture(buildImage(),scale);
    }

    @Override
    public void init() 
    {
        width = height = 8;
        angle = Math.random()*Math.PI;

        collidable=true;
        projectionPriority = 1;
        maxVelocity = new double[] {0,0};
    }
    private BufferedImage buildImage()
    {
        BufferedImage image = new BufferedImage((int)width,(int)height,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D)image.getGraphics();
        g.setColor(Color.GREEN);
        g.fillOval(0, 0, (int)width, (int)height);
        g.dispose();
        return TextureHandler.scaleTexture(image, scale);
    }
    @Override
    public void eUpdate() 
    {
    }
    @Override
    public void eRender(Graphics2D g, double interp)
    {
    }

}
