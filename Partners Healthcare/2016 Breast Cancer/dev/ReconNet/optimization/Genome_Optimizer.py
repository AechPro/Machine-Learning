import numpy as np
import Worker
import Species
import os

"""
So basically re-code this so that a population is randomly initialized and then speciated so that
one species represents one random genome, then each species creates some workers that are mutations of
its representative genome. Following this, each species can repopulate in itself so that the random
initial input to the species acts as one random point on the error surface being optimized, and each 
species converges to some local minima on the error surface.

At each iteration, check the genomic distance of each worker inside a species with some representative
genome in that species, and if the genomic distance is not within the current threshold for that species,
remove the worker and have it create its own species.

When a species is created, the worker defining that species will be copied and then mutated so that
the species doesn't immediately die.

Elitism threshold and genomic distance threshold need to get more strict the longer a species is alive,
so each species must have an age associated with it that tightens those thresholds.

Once a species cannot reproduce, it will die off and its representation should be saved for later 
comparison.
"""
"""Parameter optimization class for a neural network"""
class Optimizer(object):
    def __init__(self, target, numSpecies, maxAge, speciesSize):
        for i in range(numSpecies):
            if not os.path.exists("data/genome_{}".format(i)):
                os.makedirs("data/genome_{}".format(i))
                
        self.target = target
        table = self.parse_encoding_table(target)
        self.target.encodingTable = table
        self.species = []
        self.bestFitness = -np.inf
        self.bestGenome = []
        self.maxAge = maxAge
        self.completeGenomes = []
        self.speciesSize = speciesSize
        self.init_species(numSpecies)

    """
        Function to initialize worker population with genomes composed of random parameters taken from the parameter 
        ranges given on instantiation of the optimizer.
    """
    def init_species(self,num):
        self.species = []
        #For all workers we want to make.
        for i in range(num):
            #Create and store a new worker object.
            worker = Worker.Worker(self.target)
            t = self.target.initializationType
            if t == "sequential":
                worker.initialize_sequential_genome()
            elif t == "random":
                worker.initialize_random_genome(self.target.initialSize)
            newSpecies = Species.Species(worker,self.maxAge)
            newSpecies.init_population(self.speciesSize,self.target)
            self.species.append(newSpecies)

    """
        Function to evolve until we have finished the evolution process as defined by some condition. In this example,
        the end condition has been set to 100 iterations of evolution.
    """
    def run(self):
        itr = 0
        while(self.target.runCondition(itr)):
            self.speciate()
            self.evaluate_species()
            self.find_best_worker()
            self.repopulate()

            if itr%1==0:
                print("\n\n****ITERATION REPORT****")
                print("Iterations run: ", itr)
                print("Unique species: ",len(self.species))
                print("Best genome so far: ", self.bestGenome)
                print("Best genome fitness:", self.bestFitness)
                print("Completed genomes:",len(self.completeGenomes))
                avg = 0
                for species in self.species:
                    avg+=len(species.population)
                avg/=len(self.species)
                print("Average species size:",avg)
                """print("Species:")
                for species in self.species:
                    print(species.report())"""
                print("****************\n")
            itr+=1
        print("\n\n****FINAL REPORT****")
        print("Iterations run: ", itr)
        print("Unique species: ", len(self.species))
        print("Best genome: ", self.bestGenome)
        print("Best genome fitness:", self.bestFitness)
        print("Completed genomes:", len(self.completeGenomes))
        avg = 0
        for species in self.species:
            avg += len(species.population)
        avg /= len(self.species)
        print("Average species size:", avg)
        print("****************\n")

    def evaluate_species(self):
        for species in self.species:
            species.evaluate_workers(self.target)

    def find_best_worker(self):
        for species in self.species:
            if species.bestFitness>self.bestFitness:
                self.bestFitness = species.bestFitness
                self.bestGenome = species.bestGenome

    def repopulate(self):
        for species in self.species:
            species.repopulate(self.target)

    def speciate(self):
        for species in self.species:
            adjunctWorkers = species.speciate()
            if len(adjunctWorkers)>=1:
                worker = adjunctWorkers[np.random.randint(len(adjunctWorkers))]
                newSpecies = Species.Species(worker, self.maxAge)
                newSpecies.init_population(self.speciesSize, self.target)
                self.species.append(newSpecies)
        newSpecies = []
        for species in self.species:
            if species.completionFlag:
                self.completeGenomes.append(species.bestGenome)
            else:
                newSpecies.append(species)
        self.species = newSpecies[:]



    def parse_encoding_table(self,target):
        encodingTable = []
        try:
            table = target.encodingString
            lines = table.split("-")
            for line in lines:
                entry = []
                args = line.replace("-"," ").split(" ")
                print(args)
                for i in range(len(args)):
                    try:
                        if len(args[i])>1 and args[i][1] == '.':
                            val = float(args[i])
                        else:
                            val = int(args[i])
                        entry.append(val)
                    except:
                        entry.append(args[i].strip())
                encodingTable.append(entry)
        except Exception as e:
            print("!!!UNABLE TO READ ENCODING TABLE!!!\n\n----EXCEPTION INFORMATION----\nException type:",
                  type(e).__name__,"\nException args:", e.args)
        return encodingTable