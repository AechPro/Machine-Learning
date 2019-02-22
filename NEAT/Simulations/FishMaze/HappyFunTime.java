package NEAT.Simulations.FishMaze;

import java.util.ArrayList;

import NEAT.Population.Organism;

public class HappyFunTime
{
    private GameWorld world;
    private ArrayList<Organism> orgs;
    private Organism champ;
    public HappyFunTime()
    {
        world = new GameWorld();
        orgs = new ArrayList<Organism>();
        champ = new Organism(0);
        champ.load("resources/NEAT/debug/victor/genome.txt");
        for(int i=0;i<100;i++)
        {
            Organism org = new Organism(champ);
            org.mutateGenotypeNonStructural(null);
            org.createPhenotype(100, 100);
            orgs.add(org);
        }
        champ.createPhenotype(100, 100);
        orgs.add(champ);
        world.buildPop(orgs);
        world.run(12000);
    }
    public static void main(String[] args)
    {
        new HappyFunTime();
    }
}
