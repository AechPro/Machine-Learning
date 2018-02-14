
class target(object):
    def __init__(self):
        self.encodingString = "1,10 1,10 1,10 1,10 1,10 1.0 p1-1,10 1,10 1,10 1,10 1,10 1.0 p2"
        self.canAdd = False
        self.canRemove = False
        self.initializationType = "sequential"
        self.encodingTable = None
        self.group1 = []
        self.group2 = []
    def build_from_genome(self,genome):
        assert genome != None, "Null genome passed to target!"
        self.group1 = genome[0][1:]
        self.group2 = genome[1][1:]
        #self.params = [delta,minArea,maxArea,maxVariation,minDiversity,maxEvolution,areaThreshold,minMargin,edgeBlurSize]
    def evaluate(self):
        genes = []
        m = 1
        s = 0
        for arg in self.group1:
            s+=arg
            genes.append(arg)
        for arg in self.group2:
            m*=arg
            genes.append(arg)
        duplicateCount = len(genes) - len(set(genes))
        m-=360
        s-=36
        fitness = -(abs(m) + abs(s)) - duplicateCount
        #print("\nFITNESS:",fitness,"\n")
        return fitness
    def validate_genome(self,genome):

        return True