package Evolution_Strategies.Environments.Snake.engine;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import Evolution_Strategies.Environments.Environment;
import Evolution_Strategies.Environments.EnvironmentAgent;
import Evolution_Strategies.Environments.Snake.Workers.Pellet;
import Evolution_Strategies.Environments.Snake.Workers.Snake;
import Evolution_Strategies.Policies.FFNN.Network;
import Evolution_Strategies.Population.Worker;
import core.camera.Camera;
import core.entities.Entity;
import core.level.Level;
import core.map.tiles.Tile;
import core.phys.PhysicsObject;

public class SnakeLevel extends Environment
{
    private int popSize;
    private double[] dest = new double[2];
    private ArrayList<Snake> orgs;
    private ArrayList<EnvironmentAgent> agents;
    private ArrayList<double[]> freeSpawnPoints;
    private Snake best;
    private int pelletSpawnIndex;
    private boolean done;

    public SnakeLevel(int populationSize, PhysicsObject p, Camera cam, double[] resScale)
    {
        super(p, cam, resScale);
        popSize = populationSize;
        //fillPop();
        //locateSpawnPoints();
    }
    
    @Override
    public void loadMap()
    {
        map.loadMap("resources/snake_level.txt");
        mapWidth = map.getPixelWidth();
        mapHeight = map.getPixelHeight();
    }

    @Override
    public void loadEntities()
    {
        locateSpawnPoints();
        orgs = new ArrayList<Snake>();
        agents = new ArrayList<EnvironmentAgent>();
        entities.clear();
        
        //double[] start = new double[] {Tile.TILE_SIZE*20, Tile.TILE_SIZE*30};
        
    }
    private void fillPop()
    {
        //int spawnIdx = freeSpawnPoints.size()/2;
        for(int i=0;i<popSize;i++)
        {
            int spawnIdx = (int)(Math.random()*freeSpawnPoints.size());
            double[] spawn = freeSpawnPoints.get(spawnIdx);
            Snake snake = new Snake(spawn,0,camera, this);
            
            orgs.add(snake);
            entities.add(snake);
            collidableObjects.add(snake);
            agents.add(snake);
        }
    }
    @Override
    public void initEnv()
    {
        pelletSpawnIndex = 0;
    }

    @Override
    public ArrayList<EnvironmentAgent> buildAgents(int numAgents)
    {
        popSize = numAgents;
        loadEntities();
        fillPop();
        initCollisionHandler();
        return agents;
    }

    @Override
    public void takeStep()
    {
        update();
    }
    public double[] getTestResults()
    {
        double[] results = new double[popSize];
        for(int i=0;i<results.length;i++)
        {
            //(orgs.get(i)).stop();
            results[i] = orgs.get(i).getFitness();
        }
        return results;
    }

    @Override
    public void levelUpdate()
    {
        //double[] pos = best.getPos();
        //player.setPos((int)(scale.getScaleX()*pos[0]),(int)(scale.getScaleY()*pos[1]));
        
    }
    public void freeSpawnPoint(double[] pt)
    {
        freeSpawnPoints.add(pt.clone());
    }
    public Pellet spawnPellet()
    {
        int spawnIdx = (int)(Math.random()*freeSpawnPoints.size());
        if(pelletSpawnIndex >= freeSpawnPoints.size())
        {
            pelletSpawnIndex = 0;
        }
        //double[] spawn = freeSpawnPoints.get(pelletSpawnIndex++);
        double[] spawn = freeSpawnPoints.get(spawnIdx);
        //freeSpawnPoints.remove(spawnIdx);
        Pellet p = new Pellet(spawn,0,camera);
        return p;
    }
    
    private void locateSpawnPoints()
    {
        freeSpawnPoints = new ArrayList<double[]>();
        ArrayList<Tile> tiles = map.getTiles();
        double[] pos = new double[] {0,0};
        for(Tile t : tiles)
        {
            if(!t.isCollidable())
            {
                pos = t.getPos().clone();
                pos[0]+=t.getWidth()/2;
                pos[1]+=t.getHeight()/2;
                freeSpawnPoints.add(pos);
            }
        }
    }
    public void setPolicy(Network policy)
    {
        int spawnIdx = (int)(Math.random()*freeSpawnPoints.size());
        double[] spawn = freeSpawnPoints.get(spawnIdx);
        Snake snake = new Snake(spawn,0,camera, this);
        snake.setPolicy(policy);
        popSize = 0;
        
        loadEntities();
        fillPop();
        
        entities.add(snake);
        collidableObjects.add(snake);
        orgs.add(snake);
        initCollisionHandler();
    }
    
    public void addEntity(Entity ent)
    {
        entities.add(ent);
    }

    @Override
    public void levelRender(Graphics2D g, double interp)
    {
        //best.render(g, interp);
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        (orgs.get(0)).keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }
    
    public synchronized boolean isDone()
    {
        for(Snake s : orgs)
        {
            if(!s.isDead())
            {
                return false;
            }
        }
        return true;
    }

    

}
