package NEAT.util.MNIST;

/**
 * Created by kaustubh on 9/11/16.
 * 
 * Edited to remove cern library usage by Matthew Allen on 10/22/18.
 */

import java.util.function.Function;


public class Matrix {
    /* Wrapper for the Colt library.
       Makes doing Matrix operations more clear and less verbose.
    */
    private int rows;
    private int cols;
    private int channels;
    private double[][][] mat;

    Matrix(int k, int r, int c){
        channels = k;
        rows = r;
        cols = c;
        mat = new double[k][r][c];
    }
    
    Matrix(double[][][] a)
    {
        channels = a.length;
        rows = a[0].length;
        cols = a[0][0].length;
        double[][][] mat = new double[channels][rows][cols];
        for(int k=0;k<channels;k++)
        {
            for(int i=0;i<rows;i++)
            {
                for(int j=0;j<cols;j++)
                {
                    mat[k][i][j] = a[k][i][j];
                }
            } 
        }
        
    }

    Matrix(Matrix a){
        mat = new double[a.channels][a.rows][a.cols];
        rows = a.rows;
        cols = a.cols;
        for(int k=0;k<channels;k++)
        {
            for(int i = 0; i < rows; i++){
                for(int j = 0; j < cols; j++)
                    mat[k][i][j] = a.get(k, i, j);
            } 
        }
        
    }

    private double[][][] cloneInternalObject()
    {
        double[][][] cpy = new double[channels][rows][cols];
        for(int k=0;k<channels;k++)
        {
            for(int i=0;i<rows;i++)
            {
                for(int j=0;j<cols;j++)
                {
                    cpy[k][i][j] = mat[k][i][j];
                }
            }
        }
        
        return cpy;
    }
    void set(int k, int r, int c, double v){
        mat[k][r][c] = v;
    }

    double get(int k, int r, int c){
        return mat[k][r][c];
    }

    int get_rows(){
        return rows;
    }

    int get_cols() {
        return cols;
    }
    int get_channels() {return channels;}

    void printMatrix(){
        for(int k=0;k<channels;k++)
        {
            for(int i=0; i<rows; i++){
                for(int j=0; j<cols; j++) {
                    System.out.print(mat[k][i][j]);
                    System.out.print(" ");
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    Matrix applyFunc(Function<Double, Double> fn){
        double[][][] newMat = cloneInternalObject();
        for(int k=0;k<channels;k++)
        {
            for(int i = 0; i < rows; i++){
                for(int j = 0; j < cols; j++){
                    double v = fn.apply(mat[k][i][j]);
                    newMat[k][i][j] = v;
                }
            }
        }
        return new Matrix(newMat);
    }
    
    public double[][][] getMat(){return mat;}
}
