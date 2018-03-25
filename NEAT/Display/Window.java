package NEAT.Display;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

public class Window extends JPanel implements Runnable 
{
	private static final long serialVersionUID = 3765213914544786897L;
	private Thread thread;
	private BufferedImage image;
	private boolean running;
	private final int width,height;
	private Graphics2D g;
	private int[] renderPriorityList;
	private int[] updatePriorityList;
	private ArrayList<DisplayObject> objects;
	private double frameDelta;
	private int fps;
	private double frameRate;
	private Font font;
	//private int delayPeriod;
	private double framePeriod;
	private long frameStart;
	public Window(int _width, int _height, int framesPerSecond, ArrayList<DisplayObject> _objects)
	{
		super();
		width = _width;
		height = _height;
		objects = _objects;
		fps = framesPerSecond;
		//If user forgot to initialize object list, initialize it here.
		if(objects == null) {objects = new ArrayList<DisplayObject>();}
		
		//Force minimum FPS to 1.
		if(fps <= 0) {fps = 1;}
		//delayPeriod = (int)(Math.round(1000.0/(double)fps));
		framePeriod = 1000.0/(double)fps;
		
		//Get render and update priority orders.
		renderPriorityList = getRenderPriorityList();
		updatePriorityList = getUpdatePriorityList();
	}
	
	//Tell java we want to start our thread.
	public void addNotify()
	{
		super.addNotify();
		if(thread==null)
		{
			thread = new Thread(this);
			try{thread.start();}
			catch(Exception e){}
		}
	}
	
	//Initialize everything here.
	public void init()
	{
		image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		g = (Graphics2D) image.getGraphics();
		font = new Font("TimesRoman",0,16);
		g.setFont(font);
		running=true;
	}
	//Main display loop.
	public void run()
	{
		init();
		long secondTicker = System.nanoTime();
		double frameTick = 0;
		while(running)
		{
			//Frame start measurement .
			frameStart = System.nanoTime();
			update();
			render();
			draw();
			//Frame delta = (actual frame time in ms) / (desired frame time in ms)
			//Multiply frame period by 100,000 to set units of numerator from ns to ms.
			frameDelta = (System.nanoTime() - frameStart) / (framePeriod * 1000000);
			frameTick += frameDelta;
			if(System.nanoTime() - secondTicker >= 1000000000) 
			{
				frameRate = (int)frameTick;
				frameTick = 0;
				secondTicker = System.nanoTime();
			}
		}
	}
	public void update()
	{
		int stop = objects.size();
		//Update all display objects in order of priority.
		for(int i=updatePriorityList.length-1;i>=0;i--)
		{
			for(int j=0;j<stop;j++)
			{
				if(objects.get(j).getUpdatePriority() == updatePriorityList[i])
				{
					objects.get(j).update(frameDelta);
				}
			}
		}
	}
	public void render()
	{
		clearCanvas();
		int stop = objects.size();
		//Render all display objects in order of priority.
		for(int i=renderPriorityList.length-1;i>=0;i--)
		{
			for(int j=0;j<stop;j++)
			{
				if(objects.get(j).getRenderPriority() == renderPriorityList[i])
				{
					objects.get(j).render(g);
				}
			}
		}
		g.setColor(Color.GREEN);
		g.drawString("FPS: "+frameRate,2,15);
	}

	public void draw()
	{
		Graphics g2 = getGraphics();
		g2.drawImage(image,0,0,null);
		g2.dispose();
	}
	public void clearCanvas()
	{
		g.clearRect(0,0,width,height);
		g.setColor(Color.BLACK);
		g.fillRect(0,0,width,height);
		g.setColor(Color.WHITE);
	}
	
	public void delay(int millis)
	{
		try{Thread.sleep(millis);}
		catch(Exception e){e.printStackTrace();}
	}
	public int[] getRenderPriorityList()
	{
		int[] list = new int[DisplayObject.RENDER_IN_BACK+1];
		for(int i=0;i<=DisplayObject.RENDER_IN_BACK;i++)
		{
			list[i] = i;
		}
		return list;
	}
	public int[] getUpdatePriorityList()
	{
		int[] list = new int[DisplayObject.UPDATE_FIRST+1];
		for(int i=0;i<=DisplayObject.UPDATE_FIRST;i++)
		{
			list[i] = i;
		}
		return list;
	}
	public void addDisplayObject(DisplayObject obj)
	{
		objects.add(obj);
	}
	public void removeDisplayObject(int i) {objects.remove(i);}
	public void removeDisplayObject(DisplayObject obj) {objects.remove(obj);}
}