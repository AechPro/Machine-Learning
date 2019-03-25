package Evolution_Strategies.Configs;
public interface Config
{
    public static final int[] POLICY_LAYER_INFO = new int[] {9,128,128,4};
    public static final double L2_COEFFICIENT = 0.005;
    public static final double SGD_MOMENTUM_DEFAULT = 0.9;
    public static final double SGD_STEP_SIZE_DEFAULT = 0.03;
    public static final double ADAM_BETA1_DEFAULT = 0.9;
    public static final double ADAM_BETA2_DEFAULT = 0.999;
    public static final double ADAM_EPSILON_DEFAULT = 1e-08;
    public static final double ADAM_STEP_SIZE_DEFAULT = 0.01;
    public static final double WEIGHT_DECAY_RATE = 0.9999d;
    public static final double NOISE_STD_DEV = 0.07;
    public static final double WEIGHT_INIT_STD = 1.0;
    public static final double ACTION_NOISE_STD = 0.01;
    public static final int POPULATION_SIZE = 500;
    public static final int NUM_EPOCHS = 100000;
}
