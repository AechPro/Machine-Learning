package NEAT.Genes;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

import javax.imageio.ImageIO;

import NEAT.Configs.Config;

public class FeatureFilter 
{
	private int numChannels;
	private double[][][] filter;
	private int[] kernel;
	private int[] stepKernel;
	private double activationResponse;
	private Random rand;
	
	public FeatureFilter(Random rng, int[] kern, int[] step, double activationResponse)
	{
		rand = rng;
		kernel[0] = kern[0];
		kernel[1] = kern[1];
		stepKernel[0] = step[0];
		stepKernel[1] = step[1];
		numChannels = Config.NUM_IMAGE_COLOR_CHANNELS;
		filter = new double[numChannels][kernel[0]][kernel[1]];

		initFilter();
	}
	
	public double[][][] convolveTensor(double[][][] image)
	{
		double a = (image[0].length - kernel[0]) % stepKernel[0];
		double b = (image[0][0].length - kernel[1]) % stepKernel[1];
		int padding = (int)Math.max(a,b);
		int newWidth = 1 + (image[0].length - kernel[0] + 2*padding)/stepKernel[0];
		int newHeight = 1 + (image[0][0].length - kernel[1] + 2*padding)/stepKernel[1];
		if(newWidth <= 0 || newHeight<=0) {return image;}
		int xIdx = 0,yIdx = 0;
		if(padding>0)
		{
			image = zeroPad(image,padding);
		}
		
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
							//System.out.println(x+","+y+","+xStep+","+yStep+","+xIdx+","+yIdx+","+channel);
							output[channel][xIdx][yIdx] += filter[channel][x][y]*image[channel][x+xStep][y+yStep];
						}
					}
					output[channel][xIdx][yIdx] = sigmoid(output[channel][xIdx][yIdx],activationResponse);
				}
				yIdx++;
			}
			xIdx++;
			yIdx=0;
		}
		//activateOutput(output);
		printOutput(output);
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
	public void saveAsImage(double[][][] matrix, String name)
	{
		BufferedImage img = new BufferedImage(matrix[0].length, matrix[0][0].length,BufferedImage.TYPE_INT_RGB);
		Color color;
		int[] rgb = new int[3];
		for(int j=0;j<matrix[0].length;j++)
		{
			for(int k=0;k<matrix[0][0].length;k++)
			{
				for(int i=0;i<matrix.length;i++)
				{
					rgb[i] = (int)Math.round(matrix[i][j][k]*255);
				}
				if(matrix.length == 1) {color = new Color(rgb[0],rgb[0],rgb[0]);}
				else {color = new Color(rgb[0],rgb[1],rgb[2]);}
				
				img.setRGB(j, k, color.getRGB());
			}
		}
		try{ImageIO.write(img, "PNG", new File(name));}
		catch(Exception e) {e.printStackTrace();}
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
}
