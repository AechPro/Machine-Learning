package NEAT.TestUnits;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import NEAT.Configs.Config;
import NEAT.Genes.Connection;
import NEAT.Genes.FeatureFilter;
import NEAT.Genes.Neuron;
import NEAT.Genes.Node;
import NEAT.Population.Genome;
import NEAT.Population.Organism;
import NEAT.Population.Phenotype;
import NEAT.util.InnovationTable;
import NEAT.util.MNIST.Data;
import NEAT.util.MNIST.Loader;

public class ConvTester extends TestUnit
{


    private Loader loader;
    private List<List<Data>> mnistSet;
    //double[][][] input = new double[numChannels][1280][720];



    public ConvTester(Random rng, int windowWidth, int windowHeight)
    {
        super(rng, windowWidth, windowHeight);
        loader = new Loader();
        mnistSet = loader.loadAllData();
        numBiasNodes = 1;
        numInputs = 1;
        numOutputs = 10;
        normalize();
    }

    @Override
    public Genome buildMinimalStructure(InnovationTable table)
    {
        ArrayList<Connection> cons = new ArrayList<Connection>();
        ArrayList<Node> nodes = new ArrayList<Node>();
        ArrayList<FeatureFilter> inputNodes = new ArrayList<FeatureFilter>();
        ArrayList<Neuron> outputNodes = new ArrayList<Neuron>();
        ArrayList<Neuron> biasNodes = new ArrayList<Neuron>();
        Genome minimalGenome = null;

        for(int i=0;i<numBiasNodes;i++)
        {
            int id = table.createNode(-1, -1, Neuron.BIAS_NEURON);
            Neuron n = new Neuron(0.25*i,0.0,Neuron.BIAS_NEURON,id);
            biasNodes.add(n);
            nodes.add(n);
        }
        for(int i=0;i<numInputs;i++)
        {
            int[] kern = new int[] {(int)Math.round(randf.nextDouble()*9)+1,(int)Math.round(randf.nextDouble()*9)+1};
            int[] step = new int[] {(int)Math.round(randf.nextDouble()*4)+1,(int)Math.round(randf.nextDouble()*4)+1};
            int id = table.createFilter(-1, -1);
            System.out.println(kern[0]+" "+kern[1]);
            FeatureFilter n = new FeatureFilter((biasNodes.get(numBiasNodes-1).getSplitX()+0.25*(i+1)),0.0,Node.INPUT_NODE,id,randf,kern,step);
            inputNodes.add(n);
            nodes.add(n);
        }
        for(int i=0;i<numOutputs;i++)
        {
            int id = table.createNode(-1, -1, Neuron.OUTPUT_NODE);
            Neuron n = new Neuron(inputNodes.get(0).getSplitX()+0.25*i,1.0,Neuron.OUTPUT_NODE,id);
            outputNodes.add(n);
            nodes.add(n);
        }
        for(int i=0;i<numInputs;i++)
        {
            for(int j=0;j<numOutputs;j++)
            {
                
                int[] filterPos = new int[2];
                int[] availablePositions = inputNodes.get(i).getOutputShape(Config.FEATURE_FILTER_INITIAL_INPUT_SHAPE);
                
                int filterX = randf.nextInt(availablePositions[0]);
                int filterY = randf.nextInt(availablePositions[1]);
                
                filterPos[0] = filterX;
                filterPos[1] = filterY;
                
                System.out.println("CHOSE FILTER COORDINATES ("+filterX+","+filterY+")");
                
                int id = table.createConnection(inputNodes.get(i).getID(), outputNodes.get(j).getID());
                Connection c = new Connection(inputNodes.get(i),outputNodes.get(j),randf.nextGaussian(),true,filterPos,id);
                cons.add(c);
            }
        }
        for(int i=0;i<numBiasNodes;i++)
        {
            for(int j=0;j<numOutputs;j++)
            {
                int[] filterPos = new int[2];
                int[] availablePositions = inputNodes.get(i).getOutputShape(Config.FEATURE_FILTER_INITIAL_INPUT_SHAPE);
                
                int filterX = randf.nextInt(availablePositions[0]);
                int filterY = randf.nextInt(availablePositions[1]);
                
                filterPos[0] = filterX;
                filterPos[1] = filterY;
                int id = table.createConnection(biasNodes.get(i).getID(), outputNodes.get(j).getID());
                Connection c = new Connection(biasNodes.get(i),outputNodes.get(j),randf.nextGaussian(),true,filterPos,id);
                cons.add(c);
            }
        }
        minimalGenome = new Genome(cons,nodes,table,randf,numInputs,numOutputs);
        return minimalGenome;
    }

    @Override
    public Genome buildMinimalSolution(InnovationTable table)
    {
        return null;
    }
    private void normalize()
    {
        double mean = 0;
        double max = 0;
        int imWidth=0, imHeight=0, imDepth = 0;
        for(int i=0;i<mnistSet.get(0).size();i++)
        {
            double[][][] img = mnistSet.get(0).get(i).getInput().getMat();
            for(int depth = 0;depth<img.length;depth++)
            {
                imDepth = img.length;
                for(int w = 0; w < img[0].length; w++)
                {
                    imWidth = img[0].length;
                    for(int h = 0; h < img[0][0].length; h++)
                    {
                        imHeight = img[0][0].length;
                        if(img[depth][w][h] > max)
                        {
                            max = img[depth][w][h];
                        }
                        mean += img[depth][w][h];
                    }
                }
            }
        }
        mean/=mnistSet.get(0).size()*imWidth*imHeight*imDepth;
        System.out.println("CHANNEL-WISE IMAGE MEAN: "+mean);
        
        
        //subtract mean and normalize to between 0 and 1
        for(int i=0;i<mnistSet.get(0).size();i++)
        {
            double[][][] img = mnistSet.get(0).get(i).getInput().getMat();
            for(int depth = 0;depth<img.length;depth++)
            {
                for(int w = 0; w < img[0].length; w++)
                {
                    for(int h = 0; h < img[0][0].length; h++)
                    {
                        img[depth][w][h]-=mean;
                        img[depth][w][h]/=max;
                    }
                }
            }
        }
        
        for(int i=0;i<mnistSet.get(1).size();i++)
        {
            double[][][] img = mnistSet.get(1).get(i).getInput().getMat();
            for(int depth = 0;depth<img.length;depth++)
            {
                for(int w = 0; w < img[0].length; w++)
                {
                    for(int h = 0; h < img[0][0].length; h++)
                    {
                        img[depth][w][h]-=mean;
                        img[depth][w][h]/=max;
                    }
                }
            }
        }
    }
    @Override
    public Organism testPhenotypes(ArrayList<Organism> population)
    {
        Organism victor = null;
        Phenotype phen = null;
        int n =0;
        for(Organism org : population)
        {
            org.createPhenotype(width/2, height/2);
            double score = 0;
            phen = org.getPhenotype();
            phen.calculateDepth();
            for(int i=0;i<mnistSet.get(0).size();i++)
            {
                //System.out.println("Testing image #"+i+" of "+mnistSet.get(1).size()+" on organism "+n);
                double[][][] input = mnistSet.get(1).get(i).getInput().getMat();
                for(int j=0;j<phen.getDepth() && !phen.activate(input);j++)
                {

                }

                double[] vec = phen.readOutputVector();
                double[][][] res = mnistSet.get(1).get(i).getResult().getMat();
                
                int arg = argMax(vec);
                int label = argMax(res[0]);
                double val = squareLoss(vec,res);
                //System.out.println("Pred: "+arg+" Conf: "+vec[arg]+" Label: "+label);
                //System.out.println("Organism scored "+val+" on this image");
                score += val;
            }
            score/=mnistSet.get(0).size();
            score = 1.0/score;
            //System.out.println("ORG "+n+" SCORED: "+score);
            org.setFitness(score);
            
            
            double accuracy = 0;
            for(int i=0;i<mnistSet.get(1).size();i++)
            {
                double[][][] input = mnistSet.get(1).get(i).getInput().getMat();
                for(int j=0;j<phen.getDepth() && !phen.activate(input);j++)
                {

                }

                double[] vec = phen.readOutputVector();
                int arg = argMax(vec);
                double[][][] res = mnistSet.get(1).get(i).getResult().getMat();
                int label = argMax(res[0]);
                //System.out.println("Pred: "+arg+" Conf: "+vec[arg]+" Label: "+label);
                if(label == arg)
                {
                    accuracy++;
                }
            }
            accuracy/=mnistSet.get(1).size();
            System.out.println("ORG "+n+" SCORED: "+score+" WITH ACCURACY: "+accuracy);
            if(accuracy >= 0.9)
            {
                victor = org;
            }
            n++;
        }
        return victor;
    }
    
    public double squareLoss(double[] vec, double[][][] res)
    {
        double sum = 0;
        for(int i=0;i<vec.length;i++)
        {
            sum+=Math.pow(vec[i] - res[0][i][0],2);
        }
        return sum/vec.length;
    }
    
    public double logLoss(double[][][] label, double[] pred)
    {
        double val = 0;
        for(int i=0;i<pred.length;i++)
        {
            val-=label[0][i][0]*Math.log(pred[i]+0.00000001);
        }
        return val;
    }
    public int argMax(double[] vec)
    {
        int arg = 0;
        double m = vec[0];
        for(int i=0;i<vec.length;i++)
        {
            if(vec[i] > m)
            {
                arg = i;
                m = vec[i];
            }
        }
        return arg;
    }

    public int argMax(double[][] vec)
    {
        int arg = 0;
        double m = vec[0][0];
        for(int i=0;i<vec.length;i++)
        {
            if(vec[i][0] > m)
            {
                arg = i;
                m = vec[i][0];
            }
        }
        return arg;
    }
}
