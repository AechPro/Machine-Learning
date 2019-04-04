package Evolution_Strategies.Environments.Pong.workers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import core.camera.Camera;
import core.entities.Entity;
import core.util.TextureHandler;

public class Ball extends Entity
{
	public Ball(double[] startPos, double startAngle, Camera cam) 
	{
		super(startPos, startAngle, cam);
	}

	@Override
	public void init() 
	{
		
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
		setPos(origin);
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
}
