package Evolution_Strategies.Configs;
public interface Config
{
    public static final int[] POLICY_LAYER_INFO = new int[] {5, 256, 128, 4};
    public static final double L2_COEFFICIENT = 0.005;
    public static final double SGD_MOMENTUM_DEFAULT = 0.9;
    public static final double SGD_STEP_SIZE_DEFAULT = 0.003;
    public static final double ADAM_BETA1_DEFAULT = 0.9;
    public static final double ADAM_BETA2_DEFAULT = 0.999;
    public static final double ADAM_EPSILON_DEFAULT = 1e-08;
    public static final double ADAM_STEP_SIZE_DEFAULT = 0.001;
    public static final double WEIGHT_DECAY_RATE = 0.999d;
    public static final double NOISE_STD_DEV = 0.02;
    public static final double WEIGHT_INIT_STD = 1.0;
    
    public static final int POPULATION_SIZE = 200;
    public static final int NUM_EPOCHS = 10000;
}
