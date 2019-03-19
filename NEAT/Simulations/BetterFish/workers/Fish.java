package NEAT.Simulations.BetterFish.workers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import NEAT.Population.Organism;
import NEAT.Population.Phenotype;
import NEAT.Simulations.BetterFish.PhenotypeExecutor;
import NEAT.Simulations.BetterFish.commands.executeDecisionCommand;
import core.camera.Camera;
import core.entities.Entity;
import core.map.TileMap;
import core.map.tiles.Tile;

public class Fish extends Entity
{
    private double[][][] inputVector;
    private double[] outputVector;
    private double[] orientation;
    private double previousScore;
    private double bestScore;
    private double fitness;
    private int TSLI, species;
    private int w,h;
    private boolean victory, champion;
    private Phenotype phenotype;
    private double[] destination;
    private Tile dest;
    private TileMap map;
    private PhenotypeExecutor decisionMaker;
    private boolean readyForDecision;
    
    
    public Fish(TileMap m, double[] destLocation, double[] startPos, double startAngle, Camera cam)
    {
        super(startPos, startAngle, cam);
        map = m;
        destination = destLocation;
        dest = map.findTile((int)destination[0], (int)destination[1]);
        hasSelectiveCollisions = true;
        readyForDecision = true;
        orientation = new double[]{0,0};
    }

    @Override
    public void init()
    {
        loadTexture("resources/textures/workers/worker_2.png");
        
        w = 1;
        h = 4;
        angle = Math.PI/2;
        collidable = true;
        
        inputVector = new double[w*h+1][1][1];
        outputVector = new double[2];
        TSLI = 0;
        victory=false;
        fitness = 0;
        
        //decisionMaker = new PhenotypeExecutor(120);
        //decisionMaker.attachCommand(new executeDecisionCommand(this));
        //decisionMaker.start();
        
        //acceleration[0] = 1.0;
    }

    @Override
    public void eUpdate()
    {
        //readyForDecision = false;
        //executeDecision();
        //setPos(previousPosition);
        executeDecision();
        
        orientation[0] = (double) Math.cos(angle - Math.PI/2);
        orientation[1] = (double) Math.sin(angle - Math.PI/2);
        velocity[0] = maxVelocity[0]*orientation[0];
        velocity[1] = maxVelocity[1]*orientation[1];
        
        //acceleration[0] = 2*Math.cos(angle - Math.PI/2);
        //acceleration[1] = 2*Math.sin(angle - Math.PI/2);
        //readyForDecision = true;
    }

    @Override
    public void eRender(Graphics2D g, double interp)
    {
        //drawSensors(g);
    }
    
    public void executeDecision()
    {
        if(phenotype == null) {return;}
        if(!readyForDecision)
        {
            return;
        }
        if(checkVictoryCondition())
        {
            acceleration[0] = 0;
            acceleration[1] = 0;
            velocity[0] = 0;
            velocity[1] = 1;
            angle = 0;
            victory = true;
            return;
        }
        loadInputVector();
        
        boolean success = phenotype.activate(inputVector);
        for(int relax = 0;relax<phenotype.getDepth();relax++)
        {
            success = phenotype.activate(inputVector);
        }
        outputVector = phenotype.readOutputVector();
        phenotype.reset();
        
        if(outputVector[0] >= outputVector[1]*1.05) {rotate(Math.PI/10);}
        else if(outputVector[0] <= outputVector[1]*0.95){rotate(-Math.PI/10);}
    }
    
    public void loadInputVector()
    {
        for(int i=0;i<inputVector.length;i++) {inputVector[i][0][0]=-1;}
        
        loadSensors();
        double dist = getDistance(dest);
        if(dist < bestScore)
        {
            bestScore = dist;
            fitness += 60;
        }
        inputVector[inputVector.length-2][0][0] = angle - Math.PI/2;
        inputVector[inputVector.length-1][0][0] = dist/(map.getPixelWidth());
    }
    
    public void loadSensors()
    {
        angle-=Math.PI/2;
        double FOV = Math.PI/2;
        int itr = 0;
        
        //center sensor
        double o1 = Math.cos(angle);
        double o2 = Math.sin(angle);
        loadSensor(o1,o2,itr++);

        //up sensor
        o1 = Math.cos(angle - FOV/2);
        o2 = Math.sin(angle - FOV/2);
        loadSensor(o1,o2,itr++);

        //down sensor
        o1 = Math.cos(angle + FOV/2);
        o2 = Math.sin(angle + FOV/2);
        loadSensor(o1,o2,itr++);
        angle+=Math.PI/2;
        
    }
    
    public void loadSensor(double o1, double o2,int itr)
    {
        int x = 0;
        int y = 0;
        int r = 32;
        Tile t = null;
        int sensorRange = 3;
        double sx = (position[0]+width/2);
        double sy = (position[1]+height/2);
        double sw = o1*sensorRange*r;
        double sh = o2*sensorRange*r;
        Line2D sensorLine = new Line2D.Double(sx,sy,sw+sx,sh+sy);    
        
        synchronized(map)
        {
            for(double i=0;i<sensorRange;i+=0.5)
            {
                x = (int)(o1*r*(i) + position[0] + width/2);
                y = (int)(o2*r*(i) + position[1] + height/2);
                t = map.findTile(x,y);
                if(t == null) {continue;}
                if(t.isCollidable())
                {
                    Rectangle rect = new Rectangle((int)t.getPos()[0],(int)t.getPos()[1],(int)t.getWidth(),(int)t.getHeight());
                    if(sensorLine.intersects(rect))
                    {
                        inputVector[itr][0][0] = getDistance(t)/(r*sensorRange);
                        break;
                    }   
                }
                else {inputVector[itr][0][0]=1.1;}
            }
        }
        
    }
    public boolean checkVictoryCondition()
    {
        return getDistance(dest)<32;
    }
    
    public double getDistance(Tile t)
    {
        if(t == null) {return -1;}
        double x = position[0]+width/2.0 - t.getPos()[0]-t.getWidth()/2.0;
        double y = position[1]+height/2.0 - t.getPos()[1]-t.getHeight()/2.0;
        return Math.sqrt(Math.pow(x,2)+Math.pow(y, 2));
    }
    
    public void drawSensors(Graphics2D g)
    {
        angle-=Math.PI/2;
        double o1,o2 = 0;
        double FOV = Math.PI/4;
        
        o1 = Math.cos(angle - FOV/2);
        o2 = Math.sin(angle - FOV/2);
        drawSensor(o1,o2,g);
        
        o1 = Math.cos(angle + FOV/2);
        o2 = Math.sin(angle + FOV/2);
        drawSensor(o1,o2,g);
        
        o1 = Math.cos(angle);
        o2 = Math.sin(angle);
        drawSensor(o1,o2,g);
        angle+=Math.PI/2;
    }
    public void drawSensor(double o1, double o2, Graphics2D g)
    {
        Color c1 = new Color(255,0,0,100);
        Color c2 = new Color(0,255,0,100);
        Color collidableTile = Color.RED;
        Color freeTile = Color.GREEN;
        Color nullTile = Color.WHITE;
        int x = 0;
        int y = 0;
        int r = 32;
        Tile t = null;
        int sensorRange = 3;
        
        double sx = (position[0]+width/2);
        double sy = (position[1]+height/2);
        double sw = o1*sensorRange*r;
        double sh = o2*sensorRange*r;
        Line2D sensorLine = new Line2D.Double(sx,sy,sw+sx,sh+sy);
        
        /*Rectangle2D sensorRect = new Rectangle2D.Double(sx,sy,sw,sh);
        AffineTransform tf = new AffineTransform();
        tf.rotate(angle,(position[0]+width/2),(position[1]+height/2));
        Shape sensorLine = tf.createTransformedShape(sensorRect);
        */
        
        
        //System.out.println("\nloop");
        for(double i=0;i<sensorRange;i+=0.5)
        {
            x = (int)(o1*r*(i) + position[0] + width/2);
            y = (int)(o2*r*(i) + position[1] + height/2);
            //System.out.println(x+","+y);
            t = map.findTile(x,y);
            if(t == null) {g.setColor(nullTile);continue;}
            Rectangle rect = new Rectangle((int)t.getPos()[0],(int)t.getPos()[1],(int)t.getWidth(),(int)t.getHeight());
            if(sensorLine.intersects(rect))
            {
                if(t.isCollidable())
                {
                    g.setColor(Color.YELLOW);
                    g.draw(rect);
                    
                    g.setColor(collidableTile);
                    t.setColor(c1);
                    
                    break;
                }
                else
                {
                    g.setColor(freeTile);
                    t.setColor(c2);
                }
                
            }
        }
        g.draw(sensorLine);
    }
    
    public void stop()
    {
        //decisionMaker.stop();
    }
    
    public void setPhenotype(Organism org)
    {
        champion = false;
        if(org.isPopChamp())
        {
            champion = true;
        }
        species = org.getColorMarker();
        if(species == -1 || species > 13000) {species = 0;}
        loadTexture("resources/textures/workers/worker_"+species+".png");
        phenotype = org.getPhenotype();
    }
    public boolean isChamp()
    {
        return champion;
    }
    public double getFitness()
    {
        if(phenotype == null) {return Math.random()*0.001;}

        //Manhattan distance between the fish and the destination.
        double fitnessValue = Math.abs(position[0]-destination[0])+Math.abs(position[1]-destination[1]);
        double dist = Math.abs(origin[0]-destination[0])+Math.abs(origin[1]-destination[1]);
        //System.out.println(dist+"   "+fitnessValue+"   "+(2*dist-fitnessValue));
        return fitness + 2*dist - fitnessValue;
    }
    public int getTSLI() {return TSLI;}
    public boolean hasFinished() {return victory;}
}
