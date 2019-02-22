package NEAT.util.MNIST;

/**
 * Created by kaustubh on 17/11/16.
 */
public class Data {
    private Matrix input; // The input image as 28 X 28 matrix
    private Matrix result; // The value of the number in image as 10 x 1 vector

    Data(Matrix input, Matrix result){
        this.setInput(input);
        this.setResult(result);
    }

    public Matrix getInput()
    {
        return input;
    }

    public void setInput(Matrix input)
    {
        this.input = input;
    }

    public Matrix getResult()
    {
        return result;
    }

    public void setResult(Matrix result)
    {
        this.result = result;
    }
}
