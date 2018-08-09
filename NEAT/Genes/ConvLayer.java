package NEAT.Genes;

import java.util.ArrayList;

public class ConvLayer 
{
	private int numFilters;
	private double[][][][] filters;
	private int[] kernel;
	private int[] stepKernel;
	
	public ConvLayer(int[] kern, int[] step, int numFeatureFilters)
	{
		kernel[0] = kern[0];
		kernel[1] = kern[1];
		stepKernel[0] = step[0];
		stepKernel[1] = step[1];
		numFilters = numFeatureFilters;
		filters = new double[3][numFeatureFilters][][];
	}
	public double conv2d(int[][][] input)
	{
		double output = 0;
		//This is looping over the number of color channels in the input.
		for(int channel=0;channel<filters.length;channel++)
		{
			//This is looping over each filter in the tensor.
			for(int filterNum = 0;filterNum<filters[channel].length;filterNum++)
			{
				//This is looping over the width of the image.
				for(int x = 0;x<input[channel].length;x++)
				{
					//This is looping over the height of the image.
					for(int y = 0;y<input[channel][x].length;y++)
					{
						
					}
				}
			}
		}
		return output;
	}
}
