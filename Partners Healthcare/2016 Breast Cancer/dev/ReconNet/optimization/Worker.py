import numpy as np

class Worker(object):
    def __init__(self,target,genome=None):
        #print("creating new worker")
        self.genome = None
        if genome != None:
            self.genome = genome[:]
        self.fitness = -np.inf
        self.target = target
    def evaluate_genome(self):
        #print("\n****EVALUATING WORKER GENOME****\n", self.genome,"\n")
        self.target.build_from_genome(self.genome)
        self.fitness = self.target.evaluate()
        try:
            self.target.update_encoding_string(self.genome)
        except:
            return
        #print("Fitness =",self.fitness)

    def initialize_sequential_genome(self):
        #print("Initializing sequential genome with encoding table:\n",self.target.encodingTable)
        genome = []
        randf = np.random.uniform
        for i in range(len(self.target.encodingTable)):
            params = self.target.encodingTable[i]
            newGene = []
            newGene.append(i)
            # Randomly initialize every parameter available in the gene parameter list.
            for i in range(1, len(params) - 1):
                val = randf(params[i - 1][0],params[i-1][1])
                if type(params[i - 1][1]) is int:
                    val = int(round(val))
                newGene.append(val)
            genome.append(newGene)
        #print("Finalizing sequential genome initialization with genome:\n",genome)
        self.genome = genome

    def initialize_random_genome(self,size):
        if size == 0:
            self.genome = []
            return
        genome = []
        randf = np.random.uniform
        randint = np.random.randint
        genomeSize = np.random.randint(0,size)

        for i in range(genomeSize):
            newGene = []
            # Select a gene type based on its weight in the encoding table.
            geneID = None
            while geneID == None:
                geneID = np.random.randint(0, len(self.target.encodingTable))
                params = self.target.encodingTable[geneID]
                weight = params[-2]
                if randf(0,weight) >= weight:
                    geneID = None

            # Attach our new gene ID to the gene under construction.
            newGene.append(geneID)
            params = self.target.encodingTable[geneID]

            # Randomly initialize every parameter available in the gene parameter list.
            for i in range(1, len(params) - 1):
                val = randf(params[i-1][0],params[i-1][1])
                if type(params[i - 1][1]) is int:
                    val = int(round(val))
                newGene.append(val)
            genome.append(newGene)
        self.genome = genome

    """
        Worker-specific genome creation function. This will differ for each type of worker.
    """
    def create_genome(self,other):
        """In this case, I define Crossover as the ratio of fitness between the parent genomes.
        A constant value (I.E. 0.2) would work as well. The reason for this selection is to
        favor genes from the most fit parent, without completely removing the chance for the
        less fit parent to pass genes on to the child if the two parents are close in fitness."""
        if other.fitness==0:
            crossover = 1
        elif self.fitness == 0:
            crossover = -1
        else:
            crossover = abs(self.fitness / (other.fitness*2.))

        #print("Creating child...\nCrossover rate:", crossover)
        newGenome = []

        og = other.genome
        g = self.genome
        if g == None and og == None:
            return None
        if g == None and og != None:
            return og
        if g != None and og == None:
            return g

        #Crossover shared genes.
        for i in range(min(len(g),len(og))):
            if g[i][-1] == og[i][-1]:
                if np.random.rand()<crossover: newGene = g[i][:]
                else: newGene = og[i][:]
            else:
                if self.fitness>other.fitness:
                    newGene = g[i][:]
                else:
                    newGene = og[i][:]
            """if np.random.rand() < crossover:
                newGene = g[i][:]
            else:
                newGene = og[i][:]"""
            if g[i] == og[i]: newGene.append(-1)
            newGenome.append(newGene)

        #For all non-shared genes, only include those genes that are most fit.
        if len(g)>len(og) and self.fitness>other.fitness:
            for i in range(len(g)-len(og),len(g)):
                newGene = g[i][:]
                newGenome.append(newGene)

        elif len(og)>len(g) and other.fitness>self.fitness:
            for i in range(len(og)-len(g),len(og)):
                newGene = og[i][:]
                newGenome.append(newGene)

        #print("New genome successfully created!")
        #print("Combined genome\n",g,"\nwith genome\n",og,"\nto create genome\n",newGenome)

        return newGenome
    def mutate_genome(self, age, mutationRate=0.2, variableRate = 0.1):
        canAdd = self.target.canAdd
        canRemove = self.target.canRemove
        muteRate = mutationRate
        randf = np.random.uniform
        mutatedGenome = []
        flag = False
        #print("Starting with genome:",self.genome)
        if self.genome != None:
            if len(self.genome)>=1:
                if randf(0,1.0)<variableRate:
                    flag = True
                    for i in range(len(self.genome)):
                        newGene = self.mutate_gene(i,mutationRate,variableRate)
                        mutatedGenome.append(newGene)
                elif canRemove and randf(0,1.0)<variableRate and len(self.genome)>=1:
                    #print("REMOVE GENE MUTATION")
                    removeIdx = np.random.randint(0, len(self.genome))
                    self.remove_gene(removeIdx)
            if canAdd and randf(0, 1.0) < muteRate:
                newGene = self.add_gene()
                newGene.append(age)
                self.genome.append(newGene)
            #Change parameters of existing genes

        #print("OLD GENOME:",self.genome)
        if flag:
            for i in range(len(mutatedGenome)):
                if mutatedGenome[i] == None:
                    continue
                params = []
                try:
                    params = self.target.encodingTable[mutatedGenome[i][0]]
                except Exception as e:
                    print("!!!UNABLE TO LOCATE GENE ID IN ENCODING TABLE!!!\nGENE ID:", mutatedGenome[i][0], "\nENCODING TABLE:",
                          self.target.encodingTable, "\n\n----EXCEPTION INFORMATION----\nException type:", type(e).__name__,
                          "\nException args:", e.args)
                mutatedGenome[i] = mutatedGenome[i][:len(params)-1]
                mutatedGenome[i].append(age)
            self.genome = mutatedGenome[:]
        #print("NEW GENOME:",self.genome)
    def remove_gene(self,idx):
        self.genome.pop(idx)
    def add_gene(self):
        # Add gene mutation
        newGene = []
        randf = np.random.uniform
        # Select a gene type based on its weight in the encoding table.
        geneID = None
        while geneID == None:
            geneID = np.random.randint(0, len(self.target.encodingTable))
            weight = self.target.encodingTable[geneID][-2]
            # print("ROLLING FOR NEW GENE ID",geneID,"WEIGHT",weight)
            if randf(0, weight) >= weight:
                geneID = None

        # Attach our new gene ID to the gene under construction.
        newGene.append(geneID)
        params = self.target.encodingTable[geneID]

        # Randomly initialize every parameter available in the gene parameter list.
        for i in range(1, len(params) - 1):
            val = randf(params[i - 1][0], params[i - 1][1])
            # print("val",val,"\nidx",i-1,"\nparams",params)
            # print(params[i-1][1])
            if type(params[i - 1][1]) is int:
                val = int(round(val))
            newGene.append(val)
        return newGene
    def mutate_gene(self,idx,mutationRate,variableRate):
        randf = np.random.uniform
        gene = self.genome[idx]
        newGene = gene[:]
        params = []
        muteRate = mutationRate
        try:
            params = self.target.encodingTable[gene[0]]
        except Exception as e:
            print("!!!UNABLE TO LOCATE GENE ID IN ENCODING TABLE!!!\nGENE ID:", gene[0], "\nENCODING TABLE:",
                  self.target.encodingTable, "\n\n----EXCEPTION INFORMATION----\nException type:", type(e).__name__,
                  "\nException args:", e.args)
        if len(newGene) >= len(params) and newGene[-1] == -1:
            muteRate = variableRate
        if randf(0, 1.0) < muteRate:

            """
                Start at 1 because gene[0] is always the encoding ID. Loop to len(params)-1 because we're accessing
                the parameter list at [i-1] every time, thus we will start at 0 and skip params[-1] and params[-2]
                which are always the weight of the gene and the string name of the gene on the encoding table
                respectively.
            """
            for i in range(1, len(params) - 1):
                replacement = self.get_replacement(params,i-1,gene[i])
                newGene[i] = replacement
        return newGene

    def mutate_random_parameter(self,age):
        if len(self.genome) == 0:
            return
        idx = np.random.randint(len(self.genome))
        gene = self.genome[idx]
        newGene = gene[:]
        params = []
        try:
            params = self.target.encodingTable[gene[0]]
        except Exception as e:
            print("!!!UNABLE TO LOCATE GENE ID IN ENCODING TABLE!!!\nGENE ID:", gene[0], "\nENCODING TABLE:",
                  self.target.encodingTable, "\n\n----EXCEPTION INFORMATION----\nException type:", type(e).__name__,
                  "\nException args:", e.args)
        paramIdx = np.random.randint(len(params) - 2)
        geneIdx = paramIdx + 1
        replacement = self.get_replacement(params,paramIdx,newGene[geneIdx])
        newGene[geneIdx] = replacement
        if newGene[:len(params) - 1] != gene[:len(params) - 1]:
            if len(newGene) >= len(params):
                newGene[-1] = age
            else:
                newGene.append(age)
        self.genome[idx] = newGene

    def get_replacement(self,params,idx,orig):
        if params[idx][0] == params[idx][1]:
            return orig
        randf = np.random.uniform
        replacement = randf(params[idx][0], params[idx][1])
        if type(params[idx][1]) is int:
            replacement = int(round(replacement))
        while orig == replacement:
            replacement = randf(params[idx][0], params[idx][1])
            if type(params[idx][1]) is int:
                replacement = int(round(replacement))
        return replacement
    def get_genomic_distance(self,other,similarity=0.5):
        g = self.genome[:]
        og = other.genome[:]
        if og == g:
            return 0
        count = max(len(g),len(og))
        for i in range(min(len(g),len(og))):
            avg = 0.
            for j in range(1,min(len(g[i]),len(og[i]))):
                if max(g[i][j],og[i][j]) != 0:
                    avg += min(g[i][j],og[i][j])/max(g[i][j],og[i][j])
            avg/=max(len(g[i]),len(og[i]))
            if avg>=similarity:
                count-=1
        #print("GENOMIC DISTANCE\n",g,"\n AND \n",og,"\nFOUND TO BE",count)
        return count

    def save(self,name):
        file = open(''.join([name,'.txt']),'w')
        #print("Saving to file: ",name,".txt")
        file.write("Fitness: {}".format(self.fitness))
        for gene in self.genome:
            for entry in gene:
                file.write("{} ".format(entry))
            file.write("\n")
        file.close()

    def __copy__(self):
        newWorker = Worker(self.target,genome=self.genome[:])
        newWorker.fitness = self.fitness
        return newWorker

    def __eq__(self,other):
        if other == None:
            return False
        return self.genome == other.genome

    def __ne__(self,other):
        return not self == other
