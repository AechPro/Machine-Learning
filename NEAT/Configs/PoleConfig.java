package NEAT.Configs;

public class PoleConfig 
{
	//Global scope parameters.
	public final int POPULATION_SIZE = 1000;
	public final double MAX_MUTATION_PERTURBATION = 2.5;
	public final double WORST_PERCENT_REMOVED = 0.4;
	
	//Node specific parameters.
	public final double NODE_ADD_CHANCE = 0.03;
	public final double ACTIVATION_RESPONSE_MUTATION_RATE = 0.3;
	public final int MAX_ALLOWED_NODES = 100;
	public final int MAX_ATTEMPTS_ADD_NODE = 20;

	//Connection specific parameters.
	public final double CONNECTION_ADD_CHANCE = 0.05;
	public final double INHERITED_CONNECTION_ENABLE_RATE = 0.25;
	public final double MUTATED_CONNECTION_ENABLE_RATE = 0.00;
	public final double MUTATED_CONNECTION_TOGGLE_RATE = 0.00;
	public final double RECURSIVE_CONNECTION_CHANCE = 0.2;
	public final double WEIGHT_REPLACEMENT_RATE = 0.2;
	public final double WEIGHT_MUTATION_RATE = 0.8;
	public final int MAX_ATTEMPTS_ADD_CONNECTION = 20;

	//Mating specific parameters.
	public final double CROSSOVER_RATE = 0.75;
	public final int MAX_ATTEMPTS_FIND_PARENT = 90;
	public final double MATE_NO_MUTATION_CHANCE = 0.2;

	//Species specific parameters.
	public final double SPECIES_AGE_FITNESS_MODIFIER = 0.0;
	public final int SPECIES_OLD_THRESHOLD = 20;
	public final int SPECIES_YOUNG_THRESHOLD = 15;
	public final int SPECIES_SIZE_FOR_CHAMP_CLONING = 5;
	public final double SPECIES_COMPAT_THRESHOLD = 5.0;
	public final double NUM_POP_CHAMP_MUTATIONS = 3;
	
	//Compatibility specific parameters.
	public final double COMPAT_EXCESS_COEF = 1.0;
	public final double COMPAT_DISJOINT_COEF = 1.0;
	public final double COMPAT_SHARED_COEF = 1.0;
	
	//Stagnation specific parameters.
	public final int MAX_ALLOWED_ORGANISM_AGE = 20;
	public final int MAX_TIME_ORGANISM_STAGNATION = 10;
	public final int MAX_TIME_SPECIES_STAGNATION = 15;
	public final int MAX_TIME_POPULATION_STAGNATION = 5 + MAX_TIME_SPECIES_STAGNATION;

}
