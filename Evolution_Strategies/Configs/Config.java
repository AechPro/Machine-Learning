package Evolution_Strategies.Configs;
public interface Config
{
    public static final int POLICY_INPUT_NODES = 5;
    public static final int POLICY_OUTPUT_NODES = 4;
    public static final int[] POLICY_HIDDEN_NODES = new int[]{32,16};
    public static final double L2_COEFFICIENT = 0.5;
    public static final double GRADIENT_UPDATE_ETA_COEFFICIENT = 1.0;
    public static final double SGD_MOMENTUM_DEFAULT = 0.9;
    public static final double SGD_STEP_SIZE_DEFAULT = 5d;
    public static final double ADAM_BETA1_DEFAULT = 0.9;
    public static final double ADAM_BETA2_DEFAULT = 0.999;
    public static final double ADAM_EPSILON_DEFAULT = 1e-08;
    public static final double ADAM_STEP_SIZE_DEFAULT = 0.1;
    public static final double WEIGHT_DECAY_RATE = 0d;
    public static final double NOISE_STD_DEV = 2.5;
}
