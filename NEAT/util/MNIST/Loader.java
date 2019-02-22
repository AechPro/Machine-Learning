package NEAT.util.MNIST;

/**
 * Created by kaustubh on 29/11/16.
 * 
 * Edited for use with Convolutional Feature Filters by Matthew Allen 10/22/18
 */
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class Loader 
{
    /* Load the image files separately as training and test set. */
    static String trainingFile = "resources/NEAT/MNIST/train-images";
    static String trainingLabel = "resources/NEAT/MNIST/train-labels";
    static String testFile = "resources/NEAT/MNIST/test-images";
    static String testLabel = "resources/NEAT/MNIST/test-labels";

    int readInt(InputStream in) throws IOException{
        // Data is stored in high endian format so make it low endian.
        int d;
        int[] b = new int[4];
        for(int i = 0; i < 4; i++)
            b[i] = in.read();
        d = b[3] | b[2] << 8 | b[1] << 16 | b[0] << 24;
        return d;
    }

    List<Data> readDataFiles(String imageFile, String labelFile) throws IOException{
        List<Data> dataList = new ArrayList<>();
        int[] imageData;
        int[] labelData;
        int totalRows, totalCols, totalImages, totalLabels;
        try(InputStream in = new FileInputStream(imageFile)){
            int magic = readInt(in);
            totalImages = readInt(in);
            totalRows = readInt(in);
            totalCols = readInt(in);
            
            totalImages=100;
            
            System.out.println("Magic number: " + magic);
            System.out.println("Images: " + totalImages);
            System.out.println("Rows: " + totalRows);
            System.out.println("Cols: " + totalCols);
            imageData = new int[totalImages * totalRows * totalCols];
            
            for(int i = 0; i < totalImages * totalRows * totalCols; i++)
            {
                imageData[i] = in.read();
            }
        }

        try(InputStream in = new FileInputStream(labelFile)){
            int magic = readInt(in);
            
            totalLabels = readInt(in);
            
            totalLabels = 100;
            
            //System.out.println("Magic number: " + magic);
            //System.out.println("Items: " + totalItems);
            labelData = new int[totalLabels];
            for(int i = 0; i < totalLabels; i++)
                labelData[i] = in.read();
        }
        if (totalImages != totalLabels) // file corrupted
            return null;
        int ic = 0; //image data index counter
        int lc = 0; //label data index counter
        while(ic < imageData.length && lc < labelData.length){
           // System.out.println("ic: "+ic+"\nlc: "+lc+"\nlength: "+imageData.length+"\nlabel length: "+labelData.length);
            Matrix input, result;
            input = new Matrix(1, totalRows, totalCols);
            for(int i = 0; i < totalRows; i++)
            {
                for(int j=0;j<totalCols;j++)
                {
                    input.set(0, j, i, imageData[ic++]);
                }
                
            }
            result = new Matrix(1, 10, 1);
            result.applyFunc(p -> 0.0);
            result.set(0,labelData[lc++], 0, 1.0);
            dataList.add(new Data(input, result));
        }
        return dataList;
    }

    List<Data> loadData(String imageFile, String labelFile){
        List<Data> dataList;
        try {
            dataList = readDataFiles(imageFile, labelFile);
        }catch(java.io.IOException e){
            System.out.println(e);
            dataList = null;
        }
        if(dataList == null)
            System.out.println("dataList null");
        return dataList;
    }

    public List<List<Data>> loadAllData(){
        // Return training and test data in a list of Data list.
        List<Data> trainingData = loadData(trainingFile, trainingLabel);
        List<Data> testData = loadData(testFile, testLabel);
        List<List<Data>> data = new ArrayList<>();
        data.add(trainingData);
        data.add(testData);
        return data;
    }

}
