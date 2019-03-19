package NEAT.Simulations.BetterFish;

import java.util.ArrayList;

import NEAT.Display.Window;
import NEAT.Population.Organism;
import NEAT.Simulations.BetterFish.engine.FishLevel;
import NEAT.Simulations.BetterFish.workers.Fish;
import core.camera.Camera;
import core.level.Level;
import editor.CameraFocus;

public class Simulator
{
    private int windowWidth, windowHeight;
    private Window window;
    private FishLevel level;
    private CameraFocus subject;
    private Camera camera;
    private int popSize;
    
    public Simulator(int populationSize)
    {
        popSize = populationSize;
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
        level = new FishLevel(popSize,subject,camera,scale);
        
        window = new Window(windowWidth, windowHeight, 60, level);
    }
    
    public void buildPop(ArrayList<Organism> population)
    {
        level.buildPop(population);
    }
    
    public void runVisible(int numFrames)
    {
        window.buildWindow();
        while(window.getFramesSinceStart() < numFrames);
        window.setRunning(false);
        window.destroy();
    }
    public void runInvisible(int numFrames)
    {
        long t1 = System.nanoTime();
        for(int i=0;i<numFrames;i++)
        {
            level.update();
        }
        long t2 = System.nanoTime() - t1;
        double secondsPassed = t2/1000000000d;
        double fps = numFrames/secondsPassed;
        System.out.println("Sim fps: "+fps);
        
        
    }
    public double[] getTestResults()
    {
        return level.getTestResults();
    }
    
    /*public static void main(String[] args)
    {
        new Simulator(3);
    }*/
}
