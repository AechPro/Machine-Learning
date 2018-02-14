import numpy as np
import Worker
import Crossover_Species
import Basic_Species
from util import encoding_table_utils as parser
from copy import copy
import os
import time

"""Parameter optimization class for an abstract genome from any problem space"""


class Optimizer(object):
    def __init__(self, target, desiredPopulation, speciesSize):
        self.target = target
        if self.target.encodingTable == None:
            self.target.encodingTable = parser.parse_encoding_string(self.target.encodingString)
        self.species = []
        self.basicSpecies = []
        self.bestFitness = -np.inf
        self.bestGenome = []
        self.maxAge = 1000
        self.maxWorkerAge = self.maxAge
        self.completeGenomes = []
        self.speciesSize = speciesSize
        self.speciesPool = desiredPopulation // speciesSize
        self.init_species(self.speciesPool)
        self.bestSpecies = None

    """
        Function to initialize worker population with genomes composed of random parameters taken from the parameter 
        ranges given on instantiation of the optimizer.
    """

    def init_species(self, num):
        print("initializing...")
        self.species = []
        # For all workers we want to make.
        for i in range(0):
            # Create and store a new worker object.
            worker = Worker.Worker(self.target)
            t = self.target.initializationType
            if t == "sequential":
                worker.initialize_sequential_genome()
            elif t == "random":
                worker.initialize_random_genome(self.target.initialSize)
            newSpecies = Crossover_Species.Species(worker, self.maxAge)
            newSpecies.init_population(self.speciesSize, self.target)
            self.species.append(newSpecies)
        for i in range(self.speciesSize * 2):
            worker = Worker.Worker(self.target)
            t = self.target.initializationType
            if t == "sequential":
                worker.initialize_sequential_genome()
            elif t == "random":
                worker.initialize_random_genome(self.target.initialSize)
            newSpecies = Basic_Species.Species(worker, self.maxAge)
            self.basicSpecies.append(newSpecies)

    """
        Function to evolve until we have finished the evolution process as defined by some condition. In this example,
        the end condition has been set to 100 iterations of evolution.
    """

    def run(self):
        running = True
        itr = 0
        while (running):
            # print("1")
            self.evaluate_species()
            # print("2")
            self.find_best_worker()
            if self.bestFitness == 0:
                break
            # print("3")
            self.repopulate()
            # print("4")
            self.speciate()

            if itr % 2 == 0:
                print("\n\n****ITERATION REPORT****")
                print("Iterations run: ", itr)
                print("Max age: ", self.maxAge)
                print("Unique species: ", len(self.species))
                print("Best genome report:")
                print(self.bestGenome)
                print("fitness:", self.bestFitness)
                self.target.build_from_genome(self.bestGenome)
                self.target.evaluate()
                self.target.display()
                print("Basic genomes:", len(self.basicSpecies))
                print("Completed genomes:", len(self.completeGenomes))
                print("****************\n")
            itr += 1
            running = itr < 10000
            # running = itr<10
        print("\n\n****FINAL REPORT****")
        print("Iterations run: ", itr)
        print("Max age: ", self.maxAge)
        print("Unique species: ", len(self.species))
        print("Best genome report:")
        print("fitness:", self.bestFitness)
        self.target.build_from_genome(self.bestGenome)
        self.target.evaluate()
        self.target.display()
        print("Basic genomes:", len(self.basicSpecies))
        print("Completed genomes:", len(self.completeGenomes))
        print("****************\n")

    def evaluate_species(self):
        for species in self.species:
            species.evaluate_workers()
        for species in self.basicSpecies:
            species.evaluate_worker()
        if self.bestSpecies != None:
            self.bestSpecies.evaluate_worker()

    def find_best_worker(self):
        for species in self.species:
            if species.bestFitness >= self.bestFitness:
                self.setBest(species)

        for species in self.basicSpecies:

            if species.bestFitness >= self.bestFitness:
                self.setBest(species)

        if self.bestSpecies != None and self.bestSpecies.bestFitness > self.bestFitness:
            self.setBest(self.bestSpecies)

    def repopulate(self):
        for species in self.species:
            species.repopulate()
        if len(self.species) == 0 and len(self.basicSpecies) == 0:
            self.init_species(self.speciesPool)

    def speciate(self):
        newSpecies = []
        newBasicSpecies = []
        for species in self.species:
            adjunctWorkers = species.speciate()
            if species.completionFlag:
                # print("Creating basic species from complete species...")
                # maxAge = max(10,self.maxAge/max(1,(len(self.basicSpecies) - self.speciesPool*self.speciesSize)))
                basicSpecies = Basic_Species.Species(species.bestWorker, self.maxAge)
                newBasicSpecies.append(basicSpecies)
            else:
                newSpecies.append(species)

            if len(adjunctWorkers) >= 1:
                bestWorker = adjunctWorkers[0]
                for worker in adjunctWorkers:

                    if worker.fitness > bestWorker.fitness:
                        bestWorker = worker

                if bestWorker.fitness / self.bestFitness >= 0.8:
                    crossoverSpecies = Crossover_Species.Species(bestWorker, self.maxAge)
                    crossoverSpecies.init_population(self.speciesSize, self.target)
                    newSpecies.append(crossoverSpecies)

        for species in self.basicSpecies:
            species.speciate()
            if species.completionFlag:
                if species.bestFitness > self.bestFitness:
                    self.setBest(species)
                self.completeGenomes.append(species.bestGenome[:])
            else:
                newBasicSpecies.append(species)
        """
        for i in range(len(newSpecies), self.speciesPool):
            worker = Worker.Worker(self.target)
            t = self.target.initializationType
            if t == "sequential":
                worker.initialize_sequential_genome()
            elif t == "random":
                worker.initialize_random_genome(self.target.initialSize)
            addition = Crossover_Species.Species(worker, self.maxAge)
            addition.init_population(self.speciesSize, self.target)
            newSpecies.append(addition)"""

        self.basicSpecies = newBasicSpecies[:]
        self.species = newSpecies[:]

    def setBest(self, species):
        self.bestGenome = species.bestGenome
        self.bestFitness = species.bestFitness
        worker = Worker.Worker(self.target, genome=self.bestGenome)
        self.bestSpecies = Basic_Species.Species(worker, self.maxAge)