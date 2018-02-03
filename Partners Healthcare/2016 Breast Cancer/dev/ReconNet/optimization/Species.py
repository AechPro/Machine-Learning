import Worker
import numpy as np
class Species(object):
    def __init__(self,worker,maxAge):
        self.age = 0
        self.population = [worker]
        self.maxAge = maxAge
        self.bestFitness = worker.fitness
        self.bestWorker = worker
        self.bestGenome = worker.genome
        self.genomicThreshold = len(worker.genome)
        self.completionFlag = False

    def init_population(self,num,target):
        genome = self.population[0].genome
        for i in range(num):
            worker = Worker.Worker(target,genome=genome)
            for i in range(len(worker.genome)):
                if worker.genome[i][-1] != 1:
                    worker.genome[i].append(1)
            worker.mutate_genome(0.2,canAdd=target.canAdd, canRemove=target.canRemove)
            self.population.append(worker)

    def evaluate_workers(self,target):
        self.age+=1
        for worker in self.population:
            worker.evaluate_genome(target)
            if worker.fitness>self.bestFitness:
                self.bestWorker = worker
                self.bestFitness = worker.fitness
                self.bestGenome = worker.genome

    def speciate(self):
        adjunctWorkers = []
        thresh = int(round(len(self.bestGenome)*(1.1 - self.age/self.maxAge)))
        best = self.bestWorker
        newPopulation = []
        for i in range(len(self.population)):
            worker = self.population[i]
            dist = worker.get_genomic_distance(best)
            if dist>thresh:
                adjunctWorkers.append(worker)
            else:
                newPopulation.append(worker)
        self.population = newPopulation[:]
        if len(self.population)<2:
            self.completionFlag = True
        return adjunctWorkers

    def repopulate(self,target):
        assert len(self.population) >= 2, "There must be at least 2 workers in the self.population to repopulate!"
        # print("Initial population of self.population: ",len(self.population))
        # Set up local variables and loop through workers.
        pop = len(self.population)
        minFitness = self.population[0].fitness
        maxFitness = self.population[0].fitness
        averageFitness = 0
        parentWorkers = []
        for worker in self.population:
            # Calculate average fitness, store min and max fitness values for normalization later.
            averageFitness += worker.fitness
            if worker.fitness < minFitness:
                minFitness = worker.fitness
            if worker.fitness > maxFitness:
                maxFitness = worker.fitness
        averageFitness /= len(self.population)
        if minFitness - maxFitness == 0:
            minFitness = -1

        #print("Average fitness:",averageFitness,"\nMax fitness:",maxFitness,"\nMin fitness:",minFitness)

        # Select part of the population to be parent candidates. Note that this is not guaranteed to select any parents.
        # To combat this, there is a check performed at the end of the loop to ensure there will always be at least
        # 2 parents.
        for worker in self.population:
            # Normalize current worker fitness.
            if worker.age >= self.maxAge and worker.fitness < self.bestFitness:
                fitness = -1
            else:
                fitness = abs((worker.fitness - minFitness) / (maxFitness - minFitness))
            # random() returns a value in the range [0.0,1.0) which means that the higher the fitness value the
            # higher the probability of parent selection.
            if float(fitness) >= self.age/self.maxAge:
                parentWorkers.append(worker)
        if len(parentWorkers) >= len(self.population):
            return parentWorkers
        # Force parent population to be at least 2
        idx = 0
        while len(parentWorkers) < 2:
            parentWorkers.append(self.population[idx])
            idx += 1
        # print("Selected", len(parentWorkers), "parent candidates.")

        children = []
        # Loop until we have re-filled the population.
        while (len(children) + len(parentWorkers) < pop):
            for i in range(len(parentWorkers)):
                if len(children) + len(parentWorkers) >= pop:
                    break
                # print(len(children),len(parentWorkers))

                # Normalize the current worker fitness again.
                fitness = 0.01 + abs((parentWorkers[i].fitness - minFitness) / (maxFitness - minFitness))
                # print("Rolling with fitness =",fitness)
                # Give each worker a weighted chance to be a parent based on the fitness.
                if np.random.random() < fitness:
                    # Repeat the above process for the second parent.
                    for j in range(len(parentWorkers)):
                        fitness = 0.01 + abs((parentWorkers[j].fitness - minFitness) / (maxFitness - minFitness))
                        if np.random.random() < fitness and i != j:
                            # Create a child from both selected parents and add it to the current population.
                            newGenome = parentWorkers[i].create_genome(parentWorkers[j])
                            child = Worker.Worker(target, genome=newGenome)
                            children.append(child)
                            break
        # print(len(children),"children created.")
        # Empty old population and re-fill it with newly bred population.
        self.population = []
        for worker in children:
            worker.mutate_genome(0.2, canAdd=target.canAdd, canRemove=target.canRemove)
            self.population.append(worker)
        for worker in parentWorkers:
            self.population.append(worker)
    def report(self):
        output = "-Species report-\nBest fitness: {}\nBest genome: {}" \
                 "\nPopulation: {}".format(self.bestFitness,self.bestGenome,len(self.population))
        return output
