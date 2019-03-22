package Evolution_Strategies.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Maths
{
    public static double[] vecMulElem(double[] x, double[] y)
    {
        //System.out.println("multiplying vectors of length: "+x.length+" and "+y.length);
        double[] out = new double[x.length];
        for(int i=0;i<x.length;i++)
        {
            out[i] = x[i]*y[i];
        }
        return out;
    }
    
    public static double vecDotVec(double[] x, double[] y)
    {
        double out = 0;
        for(int i=0;i<x.length;i++)
        {
            out+=y[i]*x[i];
        }
        return out;
    }
    
    public static double[][] matMulVec(double[][] mat, double[] vec)
    {
        double[][] out = new double[mat.length][mat[0].length];

        //This scans j to the right and i down. Thus, mat must be mxn and vec must be m.
        for(int i=0;i<out.length;i++)
        {
            for(int j=0;j<out[i].length;j++)
            {
                out[i][j] = mat[i][j] * vec[i];
            }
        }
        return out;
    }
    public static double[][] transpose(double[][] mat)
    {
        double[][] out = new double[mat[0].length][mat.length];
        for(int i=0;i<mat.length;i++)
        {
            for(int j=0;j<mat[i].length;j++)
            {
                out[j][i] = mat[i][j];
            }
        }  
        return out;
    }
    public static double[] matDotVec(double[][] mat, double[] vec)
    {
        //mat = transpose(mat);
        double[] out = new double[mat[0].length];
        double[][] pre = new double[mat.length][mat[0].length];
        for(int i=0;i<mat.length;i++)
        {
            for(int j=0;j<mat[i].length;j++)
            {
                pre[i][j] = mat[i][j]*vec[i];
            }
        }
        for(int i=0;i<mat[0].length;i++)
        {
            for(int j=0;j<mat.length;j++)
            {
                out[i]+=pre[j][i];
            }
        }
        return out;
    }
    public static int argmax(double[] vec)
    {
        double max = vec[0];
        int idx = 0;
        for(int i=0;i<vec.length;i++)
        {
            if(vec[i] > max)
            {
                max = vec[i];
                idx = i;
            }
        }
        return idx;
    }
    
    /*
     * got this off stackoverflow at https://stackoverflow.com/questions/31448608/is-there-an-argsort-function-in-java
     * modified for use with array list and doubles
     */
    public static int[] argsort(final ArrayList<Double> a, final boolean ascending) {
        Integer[] indexes = new Integer[a.size()];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        Arrays.sort(indexes, new Comparator<Integer>() {
            public int compare(final Integer i1, final Integer i2) {
                return (ascending ? 1 : -1) * Double.compare(a.get(i1), a.get(i2));
            }
        });
        return asArray(indexes);
    }


    public static <T extends Number> int[] asArray(final T... a) {
        int[] b = new int[a.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = a[i].intValue();
        }
        return b;
    }
}
