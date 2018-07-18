package NEAT.Simulations.FishMaze;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Tile
{
	private double[] position;
	private int width,height;
	private int id;
	private BufferedImage img;
	private boolean collidable;
	private double score;
	private Font font;
	private Color color;
	public Tile(int startX, int startY, int ID)
	{
		position = new double[2];
		position[0] = startX;
		position[1] = startY;
		id = ID;
		switch(id)
		{
			case 1:
				collidable = true;
				break;
			default:
				collidable = false;
				break;
		}
		font = new Font("NewRoman",1,12);
		color = new Color(0,255,0,150);
		loadTexture();
	}
	public void update()
	{
		
	}
	public void render(Graphics2D g)
	{/**
		g.setColor(color);
		g.setFont(font);
		g.drawString(""+(int)score, (int)position[0]+3, (int)position[1]);
		**/
		g.drawImage(img,(int)position[0],(int)position[1],null);
	}
	private void loadTexture()
	{
		try 
		{
			img = ImageIO.read(new File("resources/textures/tiles/tile"+id+".png"));
			height=img.getHeight();
			width=img.getWidth();
		} 
		catch (IOException e) {e.printStackTrace();}
	}
	public boolean intersects(Line2D l)
	{
		Rectangle r = new Rectangle((int)position[0],(int)position[1],width,height);
		return l.intersects(r);
	}
	public void setColor(Color c)
	{
		Graphics2D g = (Graphics2D) img.getGraphics();
		g.setColor(c);
		g.fillRect(0,0,img.getWidth(),img.getHeight());
		g.dispose();
	}
	public double[] getPosition() {return new double[]{position[0],position[1]};}
	public int getId() {return id;}
	public int getHeight(){return height;}
	public int getWidth(){return width;}
	public boolean isCollidable(){return collidable;}
	public void setPosition(double[] position) {this.position = new double[]{position[0],position[1]};}
	public void setId(int id) {this.id = id;}
	public void setCollidable(boolean i){collidable=i;}
	public void setScore(double i){score=i;}
}