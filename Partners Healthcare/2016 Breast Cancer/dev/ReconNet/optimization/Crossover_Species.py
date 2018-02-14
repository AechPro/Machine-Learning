import Worker
from copy import copy
import numpy as np
class Species(object):
    def __init__(self,worker,maxAge):
        self.age = 0
        self.populationPool = 0
        self.population = [copy(worker)]
        self.bestWorker = copy(worker)
        self.maxAge = maxAge
        self.maxWorkerAge = max(maxAge/10,2)
        self.bestFitness = self.bestWorker.fitness
        self.bestGenome = self.bestWorker.genome
        self.hasDiverged = False
        self.lastImprovement = 0
        self.numImprovements = 1
        self.mutationRate = 0.2
        self.genomicThreshold = len(self.bestWorker.genome)
        self.completionFlag = False

    def init_population(self,num,target):
        genome = self.population[0].genome
        for i in range(num):
            worker = Worker.Worker(target,genome=genome)
            worker.mutate_genome(self.age,0.2,self.mutationRate)
            self.population.append(worker)
        self.populationPool = num

    def evaluate_workers(self):
        self.age+=1
        self.lastImprovement+=1
        self.mutationRate = max(np.exp(-self.numImprovements),0.1)
        for i in range(len(self.population)):
            self.population[i].evaluate_genome()
            if self.population[i].fitness>=self.bestFitness:
                self.numImprovements+=1
                self.lastImprovement=0
                self.bestWorker = copy(self.population[i])
                self.bestFitness = self.bestWorker.fitness
                self.bestGenome = self.bestWorker.genome


    def speciate(self):
        if self.completionFlag:
            return []
        adjunctWorkers = []
        thresh = int(round(len(self.bestGenome)*(1.0 - self.age/self.maxAge)))
        best = self.bestWorker
        newPopulation = []
        for i in range(len(self.population)):
            if self.population[i] == best:
                newPopulation.append(self.population[i])
                continue
            worker = self.population[i]
            dist = worker.get_genomic_distance(best)
            percentage = best.fitness/worker.fitness
            if best.fitness>0:
                precentage = worker.fitness/best.fitness
            if dist>thresh and percentage>=0.75 and self.age/self.maxAge>=0.5 and not self.hasDiverged:
                #print("adjunct worker ",dist,thresh,percentage,worker.fitness,best.fitness)
                adjunctWorkers.append(worker)
            if not self.hasDiverged and len(adjunctWorkers)>0:
                self.hasDiverged = True
            else:
                newPopulation.append(worker)
        self.population = newPopulation[:]
        if len(self.population)<2 or float(self.lastImprovement) >= 10 + self.age/2 or self.age>=self.maxAge:
            """print("\nSpecies died!\nmutation rate:",self.mutationRate,"\nage:",self.age,"\ncycles since last "
                "improvement:",self.lastImprovement,"\nadjunct workers:",len(adjunctWorkers))"""
            self.completionFlag = True
        return adjunctWorkers

    def repopulate(self):
        assert len(self.population) >= 2, "There must be at least 2 workers in the self.population to repopulate!"
        # print("Initial population of self.population: ",len(self.population))
        # Set up local variables and loop through workers.
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
            minFitness = 10

        # Select part of the population to be parent candidates. Note that this is not guaranteed to select any parents.
        # To combat this, there is a check performed at the end of the loop to ensure there will always be at least
        # 2 parents.
        for worker in self.population:
            fitness = abs((worker.fitness - minFitness) / (maxFitness - minFitness))
            # random() returns a value in the range [0.0,1.0) which means that the higher the fitness value the
            # higher the probability of parent selection.
            #print("fitness:",worker.fitness,"age:",self.age,"max:",self.maxAge)
            if float(fitness) >= self.age/self.maxAge:
                parentWorkers.append(worker)

        if len(parentWorkers) >= len(self.population):
            return

        if len(parentWorkers) == 0:
            print("species died through repopulation\n",self.age,"\n",self.maxAge)
            print("num workers:",len(self.population))
            for worker in self.population:
                print(worker.fitness)
            self.completionFlag = True
            return

        if len(parentWorkers) == 1:
            p1 = parentWorkers[0]
            p2 = Worker.Worker(target=p1.target,genome=p1.genome)
            p2.mutate_genome(self.age,0.2,self.mutationRate)
            parentWorkers.append(p2)
        children = []

        # Loop until we have re-filled the population.
        while len(children) + len(parentWorkers) < self.populationPool:
            p1,idx = self.select_random_worker(parentWorkers,minFitness,maxFitness)
            parentWorkers.pop(idx)
            p2,idx = self.select_random_worker(parentWorkers,minFitness,maxFitness)
            parentWorkers.append(p1)
            newGenome = p1.create_genome(p2)
            child = Worker.Worker(p1.target, genome=newGenome)
            children.append(child)

        self.population = []
        for worker in children:
            worker.mutate_genome(self.age,0.2,self.mutationRate)
            self.population.append(worker)
        for worker in parentWorkers:
            self.population.append(worker)
        #print("population count after repopulation:",len(self.population))
            
    def select_random_worker(self,arr,mi,ma):
        w = None
        arg = 0
        while w == None:
            arg = np.random.randint(len(arr))
            w = arr[arg]
            fitness = abs((w.fitness - mi) / (ma - mi))
            if np.random.uniform(0, 1.0) > fitness:
                w = None
        return w,arg

    def report(self):
        output = "-Species report-\nBest fitness: {}\nBest genome: {}" \
                 "\nPopulation: {}".format(self.bestFitness,self.bestGenome,len(self.population))
        return output