package Evolution_Strategies.Policies.CNN;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import Evolution_Strategies.Configs.Config;
import Evolution_Strategies.Util.NoiseTable;
import Evolution_Strategies.Util.Rand;
public class FeatureFilter
{
    private int numChannels;
    private double[][][] filter;
    private int[] kernel;
    private int[] stepKernel;
    private double activationResponse;
    private int noiseIdx;
    private int numParams;
    private double bias;
    public FeatureFilter(int[] kern, int[] step, int noiseStart)
    {
        kernel = new int[] {kern[0], kern[1]};
        stepKernel = new int[] {step[0], step[1]};
        numChannels = Config.NUM_IMAGE_COLOR_CHANNELS;
        filter = new double[numChannels][kernel[0]][kernel[1]];
        bias = 0;
        noiseIdx = noiseStart;
        initFilter();
        numParams = getFlat().length;
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
    
    public double[][][] convolveTensor(double[][][] image, double[] noise)
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
        int noiseIdx = 0;
        //printOutput(image);
        double[][][] output = new double[numChannels][newWidth][newHeight];
        //System.out.println(image[0].length+" "+image[0][0].length);
        for(int xStep = 0;xStep<image[0].length-kernel[0];xStep+=stepKernel[0])
        {
            for(int yStep = 0; yStep < image[0][0].length-kernel[1];yStep+=stepKernel[1])
            {
                noiseIdx = 0;
                for(int channel=0;channel<numChannels;channel++)
                {
                    for(int x = 0; x < kernel[0]; x++)
                    {
                        for(int y = 0; y < kernel[1]; y++)
                        {
                           // System.out.println(x+","+y+","+xStep+","+yStep+","+xIdx+","+yIdx+","+channel);
                            output[channel][xIdx][yIdx] += (filter[channel][x][y] + noise[noiseIdx++])*image[channel][x+xStep][y+yStep];
                        }
                    }
                    output[channel][xIdx][yIdx] = sigmoid(output[channel][xIdx][yIdx] + bias + noise[noise.length-1],1);
                }
                yIdx++;
            }
            xIdx++;
            yIdx=0;
        }
        //printOutput(output);
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
        int idx = 0;
        bias = NoiseTable.noise[noiseIdx + idx++];
        for(int j=0;j<filter[0].length;j++)
        {
            for(int k=0;k<filter[0][j].length;k++)
            {
                for(int i=0;i<filter.length;i++)
                {
                    //we might set the weight here to enable different features to be detected in different color channels
                    //weight = rand.nextGaussian();
                    filter[i][j][k] = Rand.getRandNorm(0.0, Config.WEIGHT_INIT_STD);
                }
            }
        }
    }
    
    public double[] getFlat()
    {
        double[] flat = new double[filter.length*filter[0].length*filter[0][0].length + 1];
        int idx = 0;
        
        for(int channel=0;channel<numChannels;channel++)
        {
            for(int x = 0; x < kernel[0]; x++)
            {
                for(int y = 0; y < kernel[1]; y++)
                {
                    flat[idx++] = filter[channel][x][y];
                }
            }
        }
        
        flat[flat.length-1] = bias;
        return flat;
    }
    
    public void setFlat(double[] flat)
    {
        int idx = 0;
        for(int channel=0;channel<numChannels;channel++)
        {
            for(int x = 0; x < kernel[0]; x++)
            {
                for(int y = 0; y < kernel[1]; y++)
                {
                    filter[channel][x][y] = flat[idx++];
                }
            }
        }
        bias = flat[flat.length-1];
    }
    
    public int getNumParams()
    {
        return numParams;
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
    
    public int[] getOutputShape(int[] inputShape)
    {
        boolean debug = false;
        if(debug)
        {
            System.out.println("\n!!!FEATURE FILTER GOT OUTPUT SHAPE COMPUTATION REQUEST!!!");
            System.out.println("Initial input shape: ("+inputShape[0]+","+inputShape[1]+")");
        }
        
        double a = (inputShape[0] - kernel[0]) % stepKernel[0];
        double b = (inputShape[1] - kernel[1]) % stepKernel[1];
        int padding = (int)Math.max(a,b);
        int newWidth = 1 + (inputShape[0] - kernel[0] + padding)/stepKernel[0];
        int newHeight = 1 + (inputShape[1] - kernel[1] + padding)/stepKernel[1];
        
        if(debug)
        {
            System.out.println("Got at input: ("+inputShape[0]+","+inputShape[1]+")");
            System.out.println("Returning: ("+newWidth+","+newHeight+")");
        }
        return new int[] {newWidth,newHeight};
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

}
