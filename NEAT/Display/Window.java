package NEAT.Display;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import core.camera.Camera;
import core.level.Level;

public class Window extends JPanel implements Runnable 
{
	private static final long serialVersionUID = 3765213914544786897L;
	private Thread thread;
	private BufferedImage image;
	private boolean running;
	private static int width,height;
	private Graphics2D g;
	private Level level;
	
	private double frameDelta;
	private int fps;
	private long framesSinceStart;
	private double frameRate;
	private Font font;
	private static BufferStrategy frameBuffer;
	
	private static JFrame frame;
	
	//private int delayPeriod;
	private long frameStart;
	public Window(int _width, int _height, int framesPerSecond, Level _level)
	{
		super();
		width = _width;
		height = _height;
		fps = framesPerSecond;
		
		level = _level;
		
		//Force minimum FPS to 1.
		if(fps <= 0) {fps = 1;}
	}
	
	//Tell java we want to start our thread.
	public void addNotify()
	{
	    System.out.println("notify");
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
	    System.out.println("init");
		image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		g = (Graphics2D) image.getGraphics();
		font = new Font("TimesRoman",0,16);
		g.setFont(font);
		
	    addKeyListener(new KeyInputHandler());
	    requestFocus();
		
		running=true;
	}
	//Main display loop.
	public void run()
	{
	    System.out.println("run");
		init();
		long secondTicker = System.nanoTime();
        double frameTick = 0;
        double lag = 0;
        
        double timeStep = 1d/fps;
        frameStart = System.nanoTime();
        
        int count = 0 ;
        framesSinceStart = 0;

        while(running)
        {
            //System.out.println("loop");
            frameStart = System.nanoTime();
            lag+=frameDelta;
            
            count = 0;
            while(lag >= timeStep && count < 5)
            {

                update();
                framesSinceStart++;

                lag -= timeStep;
                count++;
            }

            render(frameDelta/timeStep);

            draw();

            frameDelta = (System.nanoTime() - frameStart)/1000000000d;
            
            frameTick += 1;
            if(System.nanoTime() - secondTicker >= 1000000000) 
            {
                frameRate = (int)frameTick;
                frameTick = 0;
                secondTicker = System.nanoTime();
            }
            
        }
        System.out.println("exit");
        destroy();
	}
	
	public void update()
	{
	    level.update();
	}
	
	public void render(double frameDelta)
	{
		clearCanvas();
		level.render(g, frameDelta);
		g.setColor(Color.GREEN);
		g.drawString("FPS: "+frameRate,2,15);
	}

	public void draw()
    {
	    try
	    {
	        do {
	             // The following loop ensures that the contents of the drawing buffer
	             // are consistent in case the underlying surface was recreated
	             do {
	                 // Get a new graphics context every time through the loop
	                 // to make sure the strategy is validated
	                 Graphics2D g2 = (Graphics2D)frameBuffer.getDrawGraphics();
	                 g2.drawImage(image,0,32,width,height,null);
	                 g2.dispose();

	                 // Repeat the rendering if the drawing buffer contents
	                 // were restored
	             } while (frameBuffer.contentsRestored());

	             // Display the buffer
	             frameBuffer.show();

	             // Repeat the rendering if the drawing buffer was lost
	         } while (frameBuffer.contentsLost());
	        //g2.setClip(0,0,imgWidth,imgHeight);  
	    }
	    catch(Exception e)
	    {
	        System.out.println("I'm pretty sure the frame buffer was just destroyed, oh no!");
	    }
       
    }
	
	public void buildWindow()
	{
	    System.out.println("build");
	    System.setProperty("sun.java2d.opengl", "true");
        frame = new JFrame("Game Engine");
        frame.setContentPane(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setFocusable(true);
        frame.setSize(width,height);
        frame.setVisible(true);
        frame.createBufferStrategy(2);
        frameBuffer = frame.getBufferStrategy();
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
	
	
	public void destroy()
	{
        try
        {
            frameBuffer.dispose();
            frameBuffer = null;
            
            frame.dispose();
            frame = null;
            
            thread.join();
        }
        catch(Exception e)
        {
            //e.printStackTrace();
        }
        thread = null;
        framesSinceStart = 0;
        System.out.println("window exiting");
	}
	
	public void setLevel(Level l)
	{
	    level = l;
	}
	public Level getLevel()
	{
	    return level;
	}
	private class KeyInputHandler extends KeyAdapter
    {
        public void keyPressed(KeyEvent e)
        {
            level.keyPressed(e);
        }
        public void keyReleased(KeyEvent e)
        {
            level.keyReleased(e);
        }
        public void keyTyped(KeyEvent e){}
    }
	public boolean isRunning() {return running;}
	public void setRunning(boolean i) 
	{
	    running=i;
	}
	
	public synchronized long getFramesSinceStart() {return framesSinceStart;}
	public Thread getThread() {return thread;}
}