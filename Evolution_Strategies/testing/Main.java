package Evolution_Strategies.testing;

import java.util.ArrayList;

import Evolution_Strategies.Policies.FFNN.Network;
import Evolution_Strategies.Util.Maths;
import Evolution_Strategies.Util.Rand;

public class Main
{
    public Main()
    {
        testRanks();
    }
    
    private void testRanks()
    {
        ArrayList<ArrayList<Double>> rewards = new ArrayList<ArrayList<Double>>();
        
        //np.asarray(((5,1,2,3),(3,4,2,1),(4,5,3,1)))
        ArrayList<Double> entry = new ArrayList<Double>();
        entry.add(5d);
        entry.add(1d);
        entry.add(2d);
        entry.add(3d);
        rewards.add(entry);
        
        entry = new ArrayList<Double>();
        entry.add(3d);
        entry.add(4d);
        entry.add(2d);
        entry.add(1d);
        rewards.add(entry);
        
        entry = new ArrayList<Double>();
        entry.add(4d);
        entry.add(5d);
        entry.add(3d);
        entry.add(1d);
        rewards.add(entry);
        
        ArrayList<ArrayList<Double>> centeredRanks = computeCenteredRanks(rewards);
        String out = "";
        for(int i=0;i<centeredRanks.size();i++)
        {
            for(int j=0;j<centeredRanks.get(i).size();j++)
            {
                out+=centeredRanks.get(i).get(j)+",";
            }
            out+="\n";
        }
        System.out.println(out);
    }
    private ArrayList<Integer> computeRanks(ArrayList<ArrayList<Double>> rewards)
    {
        ArrayList<Double> ravel = new ArrayList<Double>();
        ArrayList<Integer> range = new ArrayList<Integer>();
        ArrayList<Integer> ranks = new ArrayList<Integer>();
        int idx = 0;
        for(ArrayList<Double> rewardList : rewards)
        {
            ravel.addAll(rewardList);
            for(int i=0;i<rewardList.size();i++)
            {
                range.add(idx++);
                ranks.add(0);
            }
        }
        int[] args = Maths.argsort(ravel, true);
        for(int i=0;i<args.length;i++)
        {
            ranks.set(args[i],range.get(i));
            
        }
        return ranks;
    }
    private ArrayList<ArrayList<Double>> computeCenteredRanks(ArrayList<ArrayList<Double>> rewards)
    {
        ArrayList<Integer> ranks = computeRanks(rewards);
        ArrayList<ArrayList<Integer>> reshaped = reshape(ranks, rewards);
        ArrayList<ArrayList<Double>> centered = new ArrayList<ArrayList<Double>>();
        
        int size = 0;
        for(int i=0;i<reshaped.size();i++)
        {
            for(int j=0;j<reshaped.get(i).size();j++)
            {
                size++;
            }
        }
        for(int i=0;i<reshaped.size();i++)
        {
            ArrayList<Double> cur = new ArrayList<Double>();
            for(int j=0;j<reshaped.get(i).size();j++)
            {
                double entry = (reshaped.get(i).get(j)/(double)(size-1)) - 0.5;
                cur.add(entry);
            }
            centered.add(cur);
        }
        return centered;
    }
    private ArrayList<ArrayList<Integer>> reshape(ArrayList<Integer> lst, ArrayList<ArrayList<Double>> dest)
    {
        ArrayList<ArrayList<Integer>> out = new ArrayList<ArrayList<Integer>>();
        
        int idx = 0;
        for(int i=0;i<dest.size();i++)
        {
            ArrayList<Integer> arg = new ArrayList<Integer>();
            for(int j=0;j<dest.get(i).size();j++)
            {
                arg.add(lst.get(idx + j));
            }
            idx+=dest.get(i).size();
            out.add(arg);
        }
        
        return out;
    }

    private void testNN()
    {
        int inputs = 4;
        int[] hidden = new int[]{2,4,10};
        int outputs = 3;
        Network n = new Network(inputs,hidden,outputs);

        double[] input = new double[]{1,1,1,1};
        double[] output = new double[]{1,1,1};
        ArrayList<ArrayList> gradients = n.backprop(input, output);

        ArrayList<double[][]> weightGradient = gradients.get(0);
        ArrayList<double[]> biasGradient = gradients.get(1);
    }
    private void backprop()
    {
        double[][] inputSet = new double[][]
                {{0,0},
                 {0,1},
                 {1,0},
                 {1,1}
                };
                
        double[][] outputSet = new double[][]
                {{1,0},
                 {0,1},
                 {0,1},
                 {1,0}
                };      
        int inputs = 2;
        int[] hidden = new int[]{3};
        int outputs = 2;
        Network net = new Network(inputs,hidden,outputs);
        System.out.println("FIRST:");
        testNetworkAccuracy(net);
        ArrayList<double[][]> weightGrad = new ArrayList<double[][]>();
        ArrayList<double[]> biasGrad = new ArrayList<double[]>();
        for(int i=0;i<1000000;i++)
        {
            weightGrad.clear();
            biasGrad.clear();
            for(int j=0;j<inputSet.length;j++)
            {
                double[] input = inputSet[j];
                double[] output = outputSet[j];
                ArrayList<ArrayList> gradients = net.backprop(input, output);

                ArrayList<double[][]> weightGradient = gradients.get(0);
                ArrayList<double[]> biasGradient = gradients.get(1);
                
                for(int k=0;k<weightGradient.size();k++)
                {
                    if(k >= weightGrad.size())
                    {
                        weightGrad.add(weightGradient.get(k));
                        continue;
                    }
                    double[][] grad = weightGradient.get(k);
                    for(int m=0;m<grad.length;m++)
                    {
                        for(int n=0;n<grad[m].length;n++)
                        {
                            weightGrad.get(k)[m][n] += grad[m][n];
                        }
                    }
                }
                
                for(int k=0;k<biasGradient.size();k++)
                {
                    if(k >= biasGrad.size())
                    {
                        biasGrad.add(biasGradient.get(k));
                        continue;
                    }
                    
                    double[] grad = biasGradient.get(k);
                    
                    for(int m=0;m<grad.length;m++)
                    {
                        biasGrad.get(k)[m]+=grad[m];
                    }
                }
                
            }
            net.updateWeights(weightGrad,biasGrad);
            testNetworkAccuracy(net);
        }
        
        testNetworkAccuracy(net);
        
    }
    
    private void testNetworkAccuracy(Network n)
    {
        double[][] inputSet = new double[][]
                {{0,0},
                 {0,1},
                 {1,0},
                 {1,1}
                };
                
        double[][] outputSet = new double[][]
                {{1,0},
                {0,1},
                {0,1},
                {1,0}
                };
        double acc = 0;
        double loss = 0;
        for(int i=0;i<inputSet.length;i++)
        {
            n.activate(inputSet[i]);
            double[] preds = n.readOutputVector();
            int arg = 0;
            double max = preds[0];
            for(int j=0;j<preds.length;j++)
            {
                if(preds[j] > max)
                {
                    max = preds[j];
                    arg = j;
                }
            }
            
            int label = 0;
            max = 0;
            for(int j=0;j<outputSet[i].length;j++)
            {
                if(outputSet[i][j] > max)
                {
                    label = j;
                    max = outputSet[i][j];
                }
            }
            //System.out.println("LABEL: "+label+" ARG: "+arg+" CONF: "+preds[arg]);
            if(label == arg)
            {
                acc++;
            }
            loss+=Math.pow(preds[arg]-outputSet[i][label],2);
        }
        acc/=4.0;
        System.out.println(loss/2.0+" | "+acc);
    }
    private void testDot()
    {
        double[] a = new double[]{1,2};
        double[][] b = new double[2][2];
        b[0] = new double[]{3,4};
        b[1] = new double[]{5,6};

        double[] c = Maths.matDotVec(b,a);
        for(int i=0;i<c.length;i++)
        {
            System.out.println(c[i]);
        }
    }
    public static void main(String[] args)
    {
        new Main();
    }
}
