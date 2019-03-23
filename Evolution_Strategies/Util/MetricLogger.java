package Evolution_Strategies.Util;

import java.util.ArrayList;
import java.io.*;
public class MetricLogger
{
    private ArrayList<Double> mean, max, std;
    private ArrayList<Double> thetaMean, thetaMax, thetaStd;
    private ArrayList<Double> gradMean, gradMax, gradStd;
    
    private String path;
    
    public MetricLogger(String filePath)
    {
        path = filePath;
        max = new ArrayList<Double>();
        mean = new ArrayList<Double>();
        std = new ArrayList<Double>();
        
        thetaMax = new ArrayList<Double>();
        thetaMean = new ArrayList<Double>();
        thetaStd = new ArrayList<Double>();
        
        gradMax = new ArrayList<Double>();
        gradMean = new ArrayList<Double>();
        gradStd = new ArrayList<Double>();
    }
    
    public void update(double[] fitnessList, double[] theta, double[] gradient)
    {
        double meanFit = 0;
        double maxFit = Integer.MIN_VALUE;
        double stdFit = 0;
        
        for(int i=0;i<fitnessList.length;i++)
        {
            meanFit+=fitnessList[i];
            if(fitnessList[i] > maxFit)
            {
                maxFit = fitnessList[i];
            }
        }
        meanFit /= fitnessList.length;
        
        for(int i=0;i<fitnessList.length;i++)
        {
            stdFit += Math.pow(fitnessList[i] - meanFit, 2);
        }
        stdFit /= fitnessList.length;
        
        max.add(maxFit);
        mean.add(meanFit);
        std.add(stdFit);
        
        updateTheta(theta);
        updateGrad(gradient);
    }
    public void updateTheta(double[] theta)
    {
        double meanTheta = 0;
        double maxTheta = Integer.MIN_VALUE;
        double stdTheta = 0;
        
        for(int i=0;i<theta.length;i++)
        {
            meanTheta+=theta[i];
            if(theta[i] > maxTheta)
            {
                maxTheta = theta[i];
            }
        }
        meanTheta /= theta.length;
        
        for(int i=0;i<theta.length;i++)
        {
            stdTheta += Math.pow(theta[i] - meanTheta, 2);
        }
        stdTheta /= theta.length;
        
        thetaMax.add(maxTheta);
        thetaMean.add(meanTheta);
        thetaStd.add(stdTheta);
    }
    public void updateGrad(double[] grad)
    {
        double meanGrad = 0;
        double maxGrad = Integer.MIN_VALUE;
        double stdGrad = 0;
        
        for(int i=0;i<grad.length;i++)
        {
            meanGrad+=grad[i];
            if(grad[i] > maxGrad)
            {
                maxGrad = grad[i];
            }
        }
        meanGrad /= grad.length;
        
        for(int i=0;i<grad.length;i++)
        {
            stdGrad += Math.pow(grad[i] - meanGrad, 2);
        }
        stdGrad /= grad.length;
        
        gradMax.add(maxGrad);
        gradMean.add(meanGrad);
        gradStd.add(stdGrad);
    }
    
    public void finalize()
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)));
            
            String line = "";
            for(Double d : mean)
            {
                line += d+",";
            }
            line+="\n";
            writer.write(line);
            line = "";
            
            for(Double d : max)
            {
                line += d+",";
            }
            line+="\n";
            writer.write(line);
            line = "";
            
            for(Double d : std)
            {
                line += d+",";
            }
            line+="\n";
            writer.write(line);
            line = "";
            
            for(Double d : thetaMean)
            {
                line += d+",";
            }
            line+="\n";
            writer.write(line);
            line = "";
            
            for(Double d : thetaMax)
            {
                line += d+",";
            }
            line+="\n";
            writer.write(line);
            line = "";
            
            for(Double d : thetaStd)
            {
                line += d+",";
            }
            line+="\n";
            writer.write(line);
            line = "";
            
            for(Double d : gradMean)
            {
                line += d+",";
            }
            line+="\n";
            writer.write(line);
            line = "";
            
            for(Double d : gradMax)
            {
                line += d+",";
            }
            line+="\n";
            writer.write(line);
            line = "";
            
            for(Double d : gradStd)
            {
                line += d+",";
            }
            line+="\n";
            writer.write(line);
            
            writer.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
