package NEAT.Simulations.BetterFish.engine;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import NEAT.Population.Organism;
import NEAT.Simulations.BetterFish.workers.Fish;
import core.camera.Camera;
import core.entities.Entity;
import core.level.Level;
import core.map.tiles.Tile;
import core.phys.PhysicsObject;
public class FishLevel extends Level
{
    private int popSize;
    private double[] dest = new double[2];

    public FishLevel(int populationSize, PhysicsObject p, Camera cam, double[] resScale)
    {
        super(p, cam, resScale);
        popSize = populationSize;
        //loadEntities();
        //initCollisionHandler();
    }

    @Override
    public void loadMap()
    {
        map.loadMap("resources/saved_level.txt");
    }

    @Override
    public void loadEntities()
    {
        entities.clear();
        dest = new double[2];
        
        dest[0] = map.getPixelWidth() - Tile.TILE_SIZE - 1;
        dest[1] = Tile.TILE_SIZE*14 - 1;
        
        double[] start = new double[] {32, 720/2};
        
        //
        
        int rows = 10;
        int cols = 100;
        int width = 16;
        int height = 32;
        int offsetX = 32;
        int offsetY = 32;
        int j = 0;
        int k = 0;
        for(int i=0;i<popSize;i++)
        {
            j++;
            if(j > rows)
            {
                j = 0;
                k++;
                if(k > cols)
                {
                    k = 0;
                }
            }
            //double[] pos = new double[] {start};
            Fish f = new Fish(map, dest, start, 0, camera);
            entities.add(f);
        }
        
        System.out.println(entities.size());
    }
    public void buildPop(ArrayList<Organism> population)
    {
        popSize = population.size();
        loadEntities();
        initCollisionHandler();

        for(int i=0;i<population.size();i++)
        {
            ((Fish)entities.get(i)).setPhenotype(population.get(i));
        }
    }
    public double[] getTestResults()
    {
        double[] results = new double[popSize];
        for(int i=0;i<results.length;i++)
        {
            ((Fish)entities.get(i)).stop();
            results[i] = ((Fish)entities.get(i)).getFitness();
            if(((Fish)entities.get(i)).hasFinished()) {results[i] = 30010;}
        }
        return results;
    }

    @Override
    public void levelUpdate()
    {
        double current = 0;
        Fish best = null;
        int idx = 0;
        
        for(int i=0;i<entities.size();i++)
        {
            best = ((Fish)entities.get(i));
            if(best.isChamp())
            {
                double[] pos = best.getPos();
                player.setPos(scale.getScaleX()*pos[0],scale.getScaleY()*pos[1]);
                return;
            }
            if(best.getFitness() > current)
            {
                current = best.getFitness();
                idx = i;
            }
        }
        double[] pos = entities.get(idx).getPos();
        player.setPos(scale.getScaleX()*pos[0],scale.getScaleY()*pos[1]);
        
    }

    @Override
    public void levelRender(Graphics2D g, double delta)
    {
        Tile t = map.findTile((int)dest[0], (int)dest[1]);
        t.setColor(Color.yellow);
        t.render(g, delta);
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
        // TODO Auto-generated method stub
        
    }

}
