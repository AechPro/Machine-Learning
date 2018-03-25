package NEAT.util;

public interface Config
{
	public final double CONNECTION_ADD_CHANCE = 0.07;
	public final double NODE_ADD_CHANCE = 0.03;
	public final double CROSSOVER_RATE = 0.75;
	public final double WEIGHT_MUTATION_RATE = 0.8;
	public final double MAX_MUTATION_PERTURBATION = 0.5;
	public final double WEIGHT_REPLACEMENT_RATE = 0.1;
	public final double ACTIVATION_RESPONSE_MUTATION_RATE = 0.1;
	public final double INHERITED_CONNECTION_ENABLE_RATE = 0.25;
	public final double MUTATED_CONNECTION_ENABLE_RATE = 0.15;
	public final double MUTATED_CONNECTION_DISABLE_RATE = 0.25;
	public final int MAX_ALLOWED_NODES = 100;
	public final int MAX_ATTEMPTS_ADD_CONNECTION = 30;
	public final int MAX_ATTEMPTS_ADD_NODE = 30;
	public final int MAX_ATTEMPTS_FIND_PARENT = 90;
}
