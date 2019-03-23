package Evolution_Strategies.Environments.Flappy;

import java.util.ArrayList;

import Evolution_Strategies.Environments.Environment;
import Evolution_Strategies.Environments.Flappy.Environment.GameWorld;
import NEAT.Display.Window;
import core.camera.Camera;
import editor.CameraFocus;

public class Simulator
{
    private int windowWidth, windowHeight;
    private Window window;
    private GameWorld level;
    private CameraFocus subject;
    private Camera camera;
    
    public Simulator()
    {
        init();
        //runVisible();
    }
    public void init()
    {
        windowWidth = 1280;
        windowHeight = 720;
        
        subject = new CameraFocus(new double[] {0,0});
        double[] scale = new double[] {1.5,1.5};
        camera = new Camera(windowWidth,windowHeight,scale,1000,subject);
        level = new GameWorld(subject,camera,scale);
        
        window = new Window(windowWidth, windowHeight, 60, level);
    }
    public void runVisible(int numFrames)
    {
        level.buildAgents(1);
        window.buildWindow();

        while(!level.isDone() || window.getFramesSinceStart() < numFrames);
        
        window.setRunning(false);
        window.destroy();
    }
    public void runInvisible(int numFrames)
    {
        long t1 = System.nanoTime();
        int framesSinceStart = 0;
        while(!level.isDone())
        {
            framesSinceStart++;
            level.update();
        }
        long t2 = System.nanoTime() - t1;
        double secondsPassed = t2/1000000000d;
        double fps = numFrames/secondsPassed;
        System.out.println("Sim fps: "+fps);
    }
    public Environment getEnv()
    {
        return level;
    }
    public double[] getTestResults()
    {
        return level.getTestResults();
    }
    
    public static void main(String[] args)
    {
        Simulator sim = new Simulator();
        sim.runVisible(10000);
    }
    
}

