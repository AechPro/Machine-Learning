package NEAT.Genes;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

import NEAT.Configs.Config;

public class FeatureFilter extends Node
{
    private int numChannels;
    private ArrayList<double[][][]> inputTensors;
    private double[][][] activeOutput;
    private double[][][] filter;
    private int[] kernel;
    private int[] stepKernel;
    private double activationResponse;
    private Random rand;
    private boolean active;

    public FeatureFilter(double sx, double sy, int nt, int nid, Random rng, int[] kern, int[] step)
    {
        super(sx,sy,nt,nid);
        rand = rng;
        kernel = new int[] {kern[0], kern[1]};
        stepKernel = new int[] {step[0], step[1]};
        numChannels = Config.NUM_IMAGE_COLOR_CHANNELS;
        filter = new double[numChannels][kernel[0]][kernel[1]];
        active = false;

        initFilter();
    }
    public FeatureFilter(Node other)
    {
        super(other);
        try
        {
            rand = ((FeatureFilter)other).getRand();
            kernel = new int[] {((FeatureFilter)other).getKernel()[0],((FeatureFilter)other).getKernel()[1]};
            stepKernel = new int[] {((FeatureFilter)other).getStepKernel()[0],((FeatureFilter)other).getStepKernel()[1]};
            numChannels = Config.NUM_IMAGE_COLOR_CHANNELS;
            double[][][] otherFilter = ((FeatureFilter)other).getFilter();
            filter = new double[numChannels][kernel[0]][kernel[1]];
            for(int i=0;i<otherFilter.length;i++)
            {
                for(int j=0;j<otherFilter[i].length;j++)
                {
                    for(int k=0;k<otherFilter[i][j].length;k++)
                    {
                        filter[i][j][k] = otherFilter[i][j][k];
                    }
                }
            }
            active = false;
            //System.out.println("FEATURE FILTER CREATED: "+kernel.length);
        }
        catch(Exception e)
        {
            System.out.println("FAILED TO CONVERT NODE TO FILTER NODE");
            e.printStackTrace();
            System.exit(1);
        }
        
    }
    
    public double[][][] getOutput()
    {
        if(!active)
        {
            return null;
        }
        return activeOutput;
    }
    
    public void passTensor(double[][][] filter)
    {
        if(inputTensors == null)
        {
            inputTensors = new ArrayList<double[][][]>();
        }
        if(filter == null)
        {
            return;
        }
        inputTensors.add(filter);
    }
    public void reset()
    {
        if(inputTensors != null)
        {
            inputTensors.clear();   
        }
        active = false;
    }
    public void computeActiveOutput()
    {
        if(inputTensors == null || inputTensors.size() == 0)
        {
            return;
        }
        int w=0, h=0, d=0;
        int max = 0;
        for(double[][][] inp : inputTensors)
        {
            if(inp.length*inp[0].length*inp[0][0].length > max)
            {
                max = inp.length*inp[0].length*inp[0][0].length;
                d = inp.length;
                w = inp[0].length;
                h = inp[0][0].length;
            }
        }
        
        double[][][] avg = new double[d][w][h];
        double[][][] divisorMap = new double[d][w][h];
        for(double[][][] inp : inputTensors)
        {
            int depth = Math.min(avg.length, inp.length);
            int width = Math.min(avg[0].length, inp[0].length);
            int height = Math.min(avg[0][0].length, inp[0][0].length);
            
            for(int i=0;i<depth;i++)
            {
                for(int j=0;j<width;j++)
                {
                    for(int k=0;k<height;k++)
                    {
                        avg[i][j][k]+=inp[i][j][k];
                        divisorMap[i][j][k]++;
                    }
                }
            }
        }
        
        for(int i=0;i<avg.length;i++)
        {
            for(int j=0;j<avg[0].length;j++)
            {
                for(int k=0;k<avg[0][0].length;k++)
                {
                    avg[i][j][k]/=divisorMap[i][j][k];
                }
            }
        }
        
        activeOutput = convolveTensor(avg);
    }
    public static double[][][] weighInput(double[][][] inp, double weight)
    {
        if(inp == null)
        {
            return null;
        }
        double[][][] weighted = new double[inp.length][inp[0].length][inp[0][0].length];
        
        for(int i=0;i<inp.length;i++)
        {
            for(int j=0;j<inp[0].length;j++)
            {
                for(int k=0;k<inp[0][0].length;k++)
                {
                    weighted[i][j][k] = weight*inp[i][j][k];
                }
            }
        }
        return weighted;
    }
    
    public double[][][] convolveTensor(double[][][] image)
    {
        //This will calculate the least amount of zero padding that would be necessary
        //to get an optimal output volume with our kernel and our step size on the input image.
        //Note that we don't need to do this, and could set the padding to 0 or pass it as a parameter.
        double a = (image[0].length - kernel[0]) % stepKernel[0];
        double b = (image[0][0].length - kernel[1]) % stepKernel[1];
        int padding = (int)Math.max(a,b);

        int newWidth = 1 + (image[0].length - kernel[0] + padding)/stepKernel[0];
        int newHeight = 1 + (image[0][0].length - kernel[1] + padding)/stepKernel[1];
        if(newWidth <= 0 || newHeight<=0) {return image;}
        int xIdx = 0,yIdx = 0;
        if(padding>0)
        {
            image = zeroPad(image,padding);
        }
        //printOutput(image);
        double[][][] output = new double[numChannels][newWidth][newHeight];
        //System.out.println(image[0].length+" "+image[0][0].length);
        for(int xStep = 0;xStep<image[0].length-kernel[0];xStep+=stepKernel[0])
        {
            for(int yStep = 0; yStep < image[0][0].length-kernel[1];yStep+=stepKernel[1])
            {
                for(int channel=0;channel<numChannels;channel++)
                {
                    for(int x = 0; x < kernel[0]; x++)
                    {
                        for(int y = 0; y < kernel[1]; y++)
                        {
                           // System.out.println(x+","+y+","+xStep+","+yStep+","+xIdx+","+yIdx+","+channel);
                            output[channel][xIdx][yIdx] += filter[channel][x][y]*image[channel][x+xStep][y+yStep];
                        }
                    }
                    output[channel][xIdx][yIdx] = sigmoid(output[channel][xIdx][yIdx],1);
                }
                yIdx++;
            }
            xIdx++;
            yIdx=0;
        }
        //printOutput(output);
        return output;
    }
    public double produceGlobalAverage(double[][][] tensor, boolean convolve)
    {
        if(tensor == null)
        {
            return 0;
        }
        
        double[][][] output = null;
        if(convolve)
        {
            output = convolveTensor(tensor);
        }
        else
        {
            output = tensor;
        }
        double avg = 0;
        for(int i=0;i<output.length;i++)
        {
            for(int j=0;j<output[i].length;j++)
            {
                for(int k=0;k<output[i][j].length;k++)
                {
                    avg+=output[i][j][k];
                }
            }
        }
        return avg/output.length*output[0].length*output[0][0].length;
    }
    public double[][][] maxPool(double[][][] tensor, int[] kern, int[] step)
    {
        double max = 0;
        double a = (tensor[0].length - kern[0]) % step[0];
        double b = (tensor[0][0].length - kern[1]) % step[1];
        int padding = (int)Math.max(a,b);

        int newWidth = 1 + (tensor[0].length - kern[0] + padding)/step[0];
        int newHeight = 1 + (tensor[0][0].length - kern[1] + padding)/step[1];
        if(newWidth <= 0 || newHeight<=0) {return tensor;}
        int xIdx = 0,yIdx = 0;
        if(padding>0)
        {
            tensor = zeroPad(tensor,padding);
        }
        //printOutput(tensor);
        double[][][] output = new double[numChannels][newWidth][newHeight];       
        for(int xStep = 0;xStep<tensor[0].length-kern[0];xStep+=step[0])
        {
            for(int yStep = 0; yStep < tensor[0][0].length-kern[1];yStep+=step[1])
            {
                for(int channel=0;channel<numChannels;channel++)
                {
                    max = 0;
                    for(int x = 0; x < kern[0]; x++)
                    {
                        for(int y = 0; y < kern[1]; y++)
                        {
                            if(tensor[channel][x+xStep][y+yStep] > max)
                            {
                                max = tensor[channel][x+xStep][y+yStep];
                            }

                        }
                    }
                    output[channel][xIdx][yIdx] = max;
                }
                yIdx++;
            }
            xIdx++;
            yIdx=0;
        }
        return output;
    }
    public double[][][] averagePool(double[][][] tensor, int[] kern, int[] step)
    {
        double avg = 0;
        double a = (tensor[0].length - kern[0]) % step[0];
        double b = (tensor[0][0].length - kern[1]) % step[1];
        int padding = (int)Math.max(a,b);

        int newWidth = 1 + (tensor[0].length - kern[0] + padding)/step[0];
        int newHeight = 1 + (tensor[0][0].length - kern[1] + padding)/step[1];
        if(newWidth <= 0 || newHeight<=0) {return tensor;}
        int xIdx = 0,yIdx = 0;
        if(padding>0)
        {
            tensor = zeroPad(tensor,padding);
        }
        //printOutput(tensor);
        double[][][] output = new double[numChannels][newWidth][newHeight];       
        for(int xStep = 0;xStep<tensor[0].length-kern[0];xStep+=step[0])
        {
            for(int yStep = 0; yStep < tensor[0][0].length-kern[1];yStep+=step[1])
            {
                for(int channel=0;channel<numChannels;channel++)
                {
                    avg = 0;
                    for(int x = 0; x < kern[0]; x++)
                    {
                        for(int y = 0; y < kern[1]; y++)
                        {
                            avg+= tensor[channel][x+xStep][y+yStep];
                        }
                    }
                    output[channel][xIdx][yIdx] = avg/(kern[0]*kern[1]);
                }
                yIdx++;
            }
            xIdx++;
            yIdx=0;
        }
        return output;
    }
    public void activateOutput(double[][][] output)
    {
        for(int i=0;i<output.length;i++)
        {
            for(int j=0;j<output[i].length;j++)
            {
                for(int k=0;k<output[i][j].length;k++)
                {
                    output[i][j][k] = sigmoid(output[i][j][k],activationResponse);
                }
            }
        }
    }
    public void normalizeTensor(double[][][] tensor, boolean useMean)
    {

        double mean = 0;
        double max = 0;
        for(int i=0;i<tensor.length;i++)
        {
            for(int j=0;j<tensor[i].length;j++)
            {
                for(int k=0;k<tensor[i][j].length;k++)
                {
                    mean+=tensor[i][j][k];
                    if(tensor[i][j][k] > max) {max = tensor[i][j][k];}
                }
            }
        }
        mean/=(tensor.length*tensor[0].length*tensor[0][0].length);
        System.out.println("Channel-wise image mean: "+mean);
        for(int i=0;i<tensor.length;i++)
        {
            for(int j=0;j<tensor[i].length;j++)
            {
                for(int k=0;k<tensor[i][j].length;k++)
                {
                    if(useMean) {tensor[i][j][k]/=mean;}
                    else {tensor[i][j][k]/=max;}
                }
            }
        }
    }
    public double[][][] convertImageToTensor(BufferedImage img, boolean grey)
    {
        int w = img.getWidth();
        int h = img.getHeight();
        Color c;
        int channels = 3;
        if(grey) {channels=1;}
        double[][][] output = new double[channels][w][h];
        for(int i=0;i<w;i++)
        {
            for(int j=0;j<h;j++)
            {
                if(grey)
                {
                    output[0][i][j] = img.getRGB(i,j);
                }
                else
                {
                    c = new Color(img.getRGB(i,j));
                    output[0][i][j] = c.getRed();
                    output[1][i][j] = c.getGreen();
                    output[2][i][j] = c.getBlue();

                }
            }
        }
        return output;
    }
    public void saveAsImage(double[][][] matrix, String name)
    {
        BufferedImage img = new BufferedImage(matrix[0].length, matrix[0][0].length,BufferedImage.TYPE_INT_RGB);
        Color color;
        int[] rgb = new int[3];
        for(int j=0;j<matrix[0].length;j++)
        {
            for(int k=0;k<matrix[0][0].length;k++)
            {
                if(matrix.length == 1)
                {
                    rgb[0] = (int)clampValue(matrix[0][j][k]);
                    color = new Color(rgb[0],rgb[0],rgb[0]);
                }
                else
                {
                    rgb[0] = (int)clampValue(matrix[0][j][k]);
                    rgb[1] = (int)clampValue(matrix[1][j][k]);
                    rgb[2] = (int)clampValue(matrix[2][j][k]);

                    color = new Color(rgb[0],rgb[1],rgb[2]);
                }

                img.setRGB(j, k, color.getRGB());
            }
        }
        try{ImageIO.write(img, "PNG", new File(name));}
        catch(Exception e) {e.printStackTrace();}
    }
    private double clampValue(double val)
    {
        //System.out.println(val);
        if(val >=0 && val<2.0)
        {
            return Math.min(255, Math.max(0, val*255));
        }
        return val;
    }
    public double[][][] zeroPad(double[][][] img, int num)
    {
        double[][][] padded = new double[img.length][img[0].length+num][img[0][0].length+num];
        int left = num/2;
        int right = num - left;
        for(int i=0;i<padded.length;i++)
        {
            for(int j=left;j<padded[i].length-right;j++)
            {
                for(int k=left;k<padded[i][j].length-right;k++)
                {
                    padded[i][j][k] = img[i][j-left][k-left];
                }
            }
        }
        return padded;
    }
    public void initFilter()
    {
        double weight = 0;
        for(int j=0;j<filter[0].length;j++)
        {
            for(int k=0;k<filter[0][j].length;k++)
            {
                weight = rand.nextGaussian();
                for(int i=0;i<filter.length;i++)
                {
                    //we might set the weight here to enable different features to be detected in different color channels
                    //weight = rand.nextGaussian();
                    filter[i][j][k] = weight;
                }

            }
        }
    }
    public void replaceFilter()
    {
        double weight;
        for(int j=0;j<filter[0].length;j++)
        {
            for(int k=0;k<filter[0][j].length;k++)
            {
                weight = rand.nextGaussian();
                for(int i=0;i<filter.length;i++)
                {
                    if(Math.random()<Config.FILTER_MUTATION_RATE)
                    {
                        filter[i][j][k] = weight;
                    }
                }
            }
        }
    }
    public void perturbFilter()
    {
        double weight;
        for(int j=0;j<filter[0].length;j++)
        {
            for(int k=0;k<filter[0][j].length;k++)
            {
                weight = rand.nextGaussian();
                for(int i=0;i<filter.length;i++)
                {
                    if(Math.random()<Config.FILTER_MUTATION_RATE)
                    {
                        filter[i][j][k] += weight*Config.MAX_MUTATION_PERTURBATION;
                    }
                }
            }
        }
    }
    public void mutate()
    {
        double mutationRate;
        double replaceProb;
        boolean severe = false;
        int num = 0;
        int end = 0;
        int numCons = 0;
        num++;
        if(severe)
        {
            mutationRate=Config.FILTER_MUTATION_RATE;
            replaceProb=Config.FILTER_REPLACEMENT_RATE;
        }
        else if(num>end && numCons>=10)
        {
            mutationRate=Config.FILTER_MUTATION_RATE+0.2;
            replaceProb=Config.FILTER_REPLACEMENT_RATE+0.2;
        }
        else
        {
            replaceProb = 0.0;
            mutationRate = 1.0;
        }
        if(rand.nextDouble()<mutationRate)
        {
            perturbFilter();
        }
        else if(rand.nextDouble()<replaceProb)
        {
            replaceFilter();
        }
    }
    
    @Override
    public String toString()
    {
        String repr = "";
        switch(type)
        {
            case BIAS_NEURON:
                repr+="Bias Node";
                break;
            case INPUT_NODE:
                repr+="Input Node";
                break;
            case OUTPUT_NODE:
                repr+="Output Node";
                break;
            case FEATURE_FILTER:
                repr+="Feature Filter";
                break;
        }
        repr+=" | ID = "+id+" | Active = "+isActive()+" | Count = "+getActivationCount();
        repr+=" | Raw Value = "+Math.round(getInactiveOutput()*100)/100.0+" | Active Value = "+Math.round(getActiveOutput()*100)/100d;
        repr+=" | Response = "+getActivationResponse();
        return repr;
    }
    public void printOutput(double[][][] out)
    {
        for(int i=0;i<out.length;i++)
        {
            System.out.println("CHANNEL "+i+"\n\n\n");
            for(int j=0;j<out[i].length;j++)
            {
                for(int k=0;k<out[i][j].length;k++)
                {
                    System.out.print(Math.round(out[i][j][k])+" ");
                }
                System.out.println();
            }
        }
    }
    public double sigmoid(double x, double response)
    {
        //System.out.println("CALCULATING SIGMOID OF "+x+" OUTPUT = "+1.0d/(1.0d+(double)(Math.exp(-x/response))));
        //return 1.0d/(1.0d+(double)(Math.exp(-x/response)));
        return (1 / (1 + Math.exp(-x/1)));
    }
    public double relu(double x, double leak)
    {
        if(leak != 1) {return x/leak;}
        if(x<=0) {return 0;}
        return x;
    }
    
    public void addInput(Connection c) {inputs.add(c);}
    public void addOutput(Connection c)
    {
        outputs.add(c);
    }
    
    public int getNumChannels()
    {
        return numChannels;
    }

    public void setNumChannels(int numChannels)
    {
        this.numChannels = numChannels;
    }

    public double[][][] getFilter()
    {
        return filter;
    }

    public void setFilter(double[][][] filter)
    {
        this.filter = filter;
    }

    public int[] getKernel()
    {
        return kernel;
    }

    public void setKernel(int[] kernel)
    {
        this.kernel = kernel;
    }

    public int[] getStepKernel()
    {
        return stepKernel;
    }

    public void setStepKernel(int[] stepKernel)
    {
        this.stepKernel = stepKernel;
    }

    public double getActivationResponse()
    {
        return activationResponse;
    }

    public void setActivationResponse(double activationResponse)
    {
        this.activationResponse = activationResponse;
    }

    public Random getRand()
    {
        return rand;
    }

    public void setRand(Random rand)
    {
        this.rand = rand;
    }
    public boolean isActive()
    {
        return active;
    }
    public void setActive(boolean active)
    {
        this.active=active;
    }
}
