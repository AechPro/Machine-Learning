import numpy as np

class Worker(object):
    def __init__(self,target,genome=None):
        self.genome = None
        if genome != None:
            self.genome = genome[:]
        self.fitness = -np.inf
        self.encodingTable = target.encodingTable
        self.age = 0

    def evaluate_genome(self,target):
        #print("\n****EVALUATING WORKER GENOME****\n", self.genome,"\n")
        target.build_from_genome(self.genome)
        self.fitness = target.evaluate()
        self.age+=1

    def initialize_sequential_genome(self):
        #print("Initializing sequential genome with encoding table:\n",self.encodingTable)
        genome = []
        rand = np.random.random
        for i in range(len(self.encodingTable)):
            params = self.encodingTable[i]
            newGene = []
            newGene.append(i)
            # Randomly initialize every parameter available in the gene parameter list.
            for i in range(1, len(params) - 1):
                val = 1 + rand() * (params[i - 1] - 1)
                if params[i - 1] < 1 and params[i - 1] > 0:
                    val = rand() * params[i - 1]
                if type(params[i - 1]) is int:
                    val = int(round(val))
                newGene.append(val)
            genome.append(newGene)
        #print("Finalizing sequential genome initialization with genome:\n",genome)
        self.genome = genome

    def initialize_random_genome(self,size):
        genome = []
        rand = np.random.random
        genomeSize = np.random.randint(0,size)

        for i in range(genomeSize):
            newGene = []
            # Select a gene type based on its weight in the encoding table.
            geneID = None
            while geneID == None:
                geneID = np.random.randint(0, len(self.encodingTable))
                params = self.encodingTable[geneID]
                weight = params[-2]
                if rand() >= weight:
                    geneID = None

            # Attach our new gene ID to the gene under construction.
            newGene.append(geneID)
            params = self.encodingTable[geneID]

            # Randomly initialize every parameter available in the gene parameter list.
            for i in range(1, len(params) - 1):
                val = 1 + rand() * (params[i - 1] - 1)
                if params[i - 1] < 1 and params[i - 1] > 0:
                    val = rand() * params[i - 1]
                if type(params[i - 1]) is int:
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

            if np.random.rand()<crossover:
                newGene = g[i][:]
                if newGene != og[i] and newGene[-1] != 1:
                    newGene.append(1)
                newGenome.append(newGene)
            else:
                newGene = og[i][:]
                if newGene != g[i] and newGene[-1] != 1:
                    newGene.append(1)
                newGenome.append(newGene)

        #For all non-shared genes, only include those genes that are most fit.
        if len(g)>len(og) and self.fitness>other.fitness:
            for i in range(len(g)-len(og),len(g)):
                newGene = g[i][:]
                if newGene != 1:
                    newGene.append(1)
                newGenome.append(newGene)

        elif len(og)>len(g) and other.fitness>self.fitness:
            for i in range(len(og)-len(g),len(og)):
                newGene = og[i][:]
                if newGene[-1] != 1:
                    newGene.append(1)
                newGenome.append(newGene)

        #print("New genome successfully created!")
        #print("Combined genome\n",g,"\nwith genome\n",og,"\nto create genome\n",newGenome)

        return newGenome
    def mutate_genome(self,mutationRate,canAdd=True,canRemove=True):
        muteRate = mutationRate
        rand = np.random.random
        mutatedGenome = []
        #print("Starting with genome:",self.genome)
        removeIdx = -1
        if self.genome != None:
            #Remove gene mutation
            removeIdx = -1
            if canRemove and rand()<muteRate and len(self.genome)>=1:
                print("REMOVE GENE MUTATION")
                removeIdx = np.random.randint(0,len(self.genome))

            #Change parameters of existing genes
            for i in range(len(self.genome)):
                #Instead of actively removing a gene, we simply skip it when creating the new genome.
                if i == removeIdx:
                    continue

                #Access the current gene for modification.
                gene = self.genome[i]
                if gene[-1] != 1:
                    muteRate = -1
                newGene = gene[:]
                if rand()<muteRate:

                    #If this gene represents a convoluational layer, either perturb or replace both available
                    #convolutional layer gene values.
                    try:
                        params = self.encodingTable[gene[0]]
                    except Exception as e:
                        print("!!!UNABLE TO LOCATE GENE ID IN ENCODING TABLE!!!\nGENE ID:",gene[0],"\nENCODING TABLE:",
                              self.encodingTable,"\n\n----EXCEPTION INFORMATION----\nException type:", type(e).__name__,
                              "\nException args:", e.args)
                    """
                        Start at 1 because gene[0] is always the encoding ID. Loop to len(params)-1 because we're accessing
                        the parameter list at [i-1] every time, thus we will start at 0 and skip params[-1] and params[-2]
                        which are always the weight of the gene and the string name of the gene on the encoding table
                        respectively.
                    """
                    for i in range(1,len(params)-1):
                        if rand()<=0.5:
                            newGene[i] = 1 + (rand() * (params[i - 1] - 1))
                            if params[i-1]<1 and params[i-1]>0:
                                newGene[i] = rand()*params[i-1]
                        else:
                            #increase size of newGene
                            newGene[i] += rand()*(params[i-1]-newGene[i])

                            #reduce size of newGene
                            if rand()<=0.5:
                                newGene[i] = rand()*newGene[i]
                        if type(params[i-1]) is int:
                            newGene[i] = int(round(newGene[i]))
                mutatedGenome.append(newGene)
                muteRate = mutationRate

        #Add gene mutation
        if canAdd and rand()<muteRate:
            print("ADD GENE MUTATION")
            newGene = []

            #Select a gene type based on its weight in the encoding table.
            geneID = None
            while geneID == None:
                geneID = np.random.randint(0,len(self.encodingTable))
                weight = self.encodingTable[geneID][-2]
                print("ROLLING FOR NEW GENE ID",geneID,"WEIGHT",weight)
                if rand() >= weight:
                    geneID = None

            #Attach our new gene ID to the gene under construction.
            newGene.append(geneID)
            params = self.encodingTable[geneID]

            #Randomly initialize every parameter available in the gene parameter list.
            for i in range(1,len(params)-1):
                val = 1+rand()*(params[i-1]-1)
                if params[i-1]<1 and params[i-1]>0:
                    val = rand()*params[i-1]
                if type(params[i-1]) is int:
                    val = int(round(val))
                newGene.append(val)
            mutatedGenome.append(newGene)
        #print("OLD GENOME:",self.genome)
        self.genome = []
        for gene in mutatedGenome:
            if gene[-1] == 1:
                params = self.encodingTable[gene[0]]
                gene = gene[:len(params)-1]
            self.genome.append(gene)
        #print("NEW GENOME:",self.genome)

    def get_genomic_distance(self,other,similarity=0.5):
        g = self.genome[:]
        og = other.genome[:]
        count = min(len(g),len(og)) + abs(len(g)-len(og))
        for i in range(min(len(g),len(og))):
            avg = 0.
            for j in range(1,min(len(g[i]),len(og[i]))):
                if max(g[i][j],og[i][j]) != 0:
                    avg += min(g[i][j],og[i][j])/max(g[i][j],og[i][j])
            avg/=max(len(g[i]),len(og[i]))
            if avg>=similarity:
                count-=1
        return count

    def save(self,name):
        file = open(''.join([name,'.txt']),'w')
        #print("Saving to file: ",name,".txt")
        for gene in self.genome:
            for entry in gene:
                file.write("{} ".format(entry))
            file.write("\n")
        file.close()

    def __eq__(self,other):
        return self.genome == other.genome

    def __ne__(self,other):
        return not self == other
