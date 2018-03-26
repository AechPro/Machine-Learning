package NEAT.util;

public interface Config
{
	public final double CONNECTION_ADD_CHANCE = 0.00;
	public final double NODE_ADD_CHANCE = 0.00;
	public final double CROSSOVER_RATE = 0.75;
	public final double WEIGHT_MUTATION_RATE = 0.9;
	public final double MAX_MUTATION_PERTURBATION = 0.5;
	public final double WEIGHT_REPLACEMENT_RATE = 0.1;
	public final double ACTIVATION_RESPONSE_MUTATION_RATE = 0.1;
	public final double INHERITED_CONNECTION_ENABLE_RATE = 0.25;
	public final double MUTATED_CONNECTION_ENABLE_RATE = 0.15;
	public final double MUTATED_CONNECTION_DISABLE_RATE = 0.25;
	public final double SPECIES_AGE_FITNESS_MODIFIER = 0.3;
	public final int SPECIES_OLD_THRESHOLD = 15;
	public final int SPECIES_YOUNG_THRESHOLD = 5;
	public final int SPECIES_SIZE_FOR_CHAMP_CLONING = 5;
	public final int MAX_ALLOWED_NODES = 100;
	public final int MAX_ATTEMPTS_ADD_CONNECTION = 30;
	public final int MAX_ATTEMPTS_ADD_NODE = 30;
	public final int MAX_ATTEMPTS_FIND_PARENT = 90;
	public final double COMPAT_EXCESS_COEF = 1.0;
	public final double COMPAT_DISJOINT_COEF = 1.0;
	public final double COMPAT_SHARED_COEF = 0.4;
	public final int MAX_ALLOWED_ORGANISM_AGE = 20;
	public final int MAX_TIME_ORGANISM_STAGNATION = 10;
	public final int MAX_TIME_SPECIES_STAGNATION = 15;
	public final double SPECIES_COMPAT_THRESHOLD = 0.5;
	public final int POPULATION_SIZE = 150;
	public final double WORST_PERCENT_REMOVED = 0.2;
	public final double MATE_NO_MUTATION_CHANCE = 0.2;
}
