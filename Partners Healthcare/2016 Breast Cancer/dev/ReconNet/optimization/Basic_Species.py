import Worker
from copy import copy
import numpy as np

class Species(object):
    def __init__(self, worker, maxAge):
        self.age = 0
        self.worker = worker
        self.maxAge = maxAge
        self.bestWorker = copy(worker)
        self.bestFitness = self.bestWorker.fitness
        self.bestGenome = self.bestWorker.genome
        self.lastImprovement = 0
        self.mutationRate = 0.2
        self.completionFlag = False
        self.numImprovements = 1
    def evaluate_worker(self):
        self.age += 1
        self.lastImprovement += 1
        self.mutationRate = max(np.exp(-self.numImprovements),0.05)
        self.worker.mutate_genome(self.age,mutationRate=self.mutationRate)
        self.worker.evaluate_genome()
        self.set_best()
        self.worker.mutate_random_parameter(self.age)
        self.worker.evaluate_genome()
        self.set_best()
        #print(self.worker.genome)
    def set_best(self):
        if self.worker.fitness >= self.bestWorker.fitness:
            self.lastImprovement = 0
            self.numImprovements+=1
            self.bestWorker = copy(self.worker)
            self.bestFitness = self.bestWorker.fitness
            self.bestGenome = self.bestWorker.genome
        else:
            del self.worker
            self.worker = copy(self.bestWorker)
    def speciate(self):
        if float(self.lastImprovement)>=self.maxAge*0.25:
            self.numImprovements = 1
        if self.age>=self.maxAge or float(self.lastImprovement) >= 10 + self.age/2:
            self.completionFlag=True
    def report(self):
        output = "-Species report-\nBest fitness: {}\nBest genome: {}".format(self.bestFitness, self.bestGenome)
        return output