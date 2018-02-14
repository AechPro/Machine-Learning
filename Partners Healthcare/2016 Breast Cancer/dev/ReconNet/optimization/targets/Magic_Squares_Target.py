import numpy as np
class target(object):
    def __init__(self,n):
        #n = the length of the magic square on a diagonal through the center
        self.encodingString = ""
        nSquared = n*n
        self.diagonalLength = n
        for i in range(nSquared):
            self.encodingString = "{}0,{} 0,{} 1.0 index_{}-".format(self.encodingString,n-1,n-1,i)
        self.encodingString = self.encodingString[:-1]
        self.canAdd = False
        self.canRemove = False
        self.initializationType = "sequential"
        self.encodingTable = None
        self.square = []
        self.duplicates = 0
        self.expectedSum = (np.power(self.diagonalLength,3) + self.diagonalLength)/2
        print(self.encodingString)
        self.repeatedValues = []
    def build_from_genome(self,genome):
        assert genome != None, "Null genome passed to target!"
        self.square = [[0 for i in range(self.diagonalLength)] for i in range(self.diagonalLength)]
        #self.repeatedValues = []
        for i in range(len(genome)):
            x = genome[i][1]
            y = genome[i][2]
            self.square[x][y] = i+1
        #print(self.square)

        #self.params = [delta,minArea,maxArea,maxVariation,minDiversity,maxEvolution,areaThreshold,minMargin,edgeBlurSize]
    def evaluate(self):
        self.rowSums = []
        self.duplicates = 0
        self.columnSums = []
        southEastDiag = 0
        northEastDiag = 0
        for i in range(len(self.square)):
            rowSum = 0
            colSum = 0
            northEastDiag += self.square[i][i]
            southEastDiag += self.square[i][-i]
            for j in range(len(self.square[0])):
                if self.square[i][j] == 0:
                    self.duplicates+=1
                rowSum+=self.square[i][j]
                colSum+=self.square[j][i]
            self.columnSums.append(colSum)
            self.rowSums.append(rowSum)
        self.diagonalSums = [northEastDiag,southEastDiag]
        allSums = self.diagonalSums + self.rowSums + self.columnSums
        sumOfDifferences = sum(abs(arg - self.expectedSum) for arg in allSums)
        #self.display()
        return -sumOfDifferences - self.duplicates*self.expectedSum

    """
    current issue: between iteration reports, the board displayed does not change but
    the sums of the values that are calculated do change. find issue.
    
    """

    def display(self):
        print("\n*****MAGIC SQUARE*****")
        board = ""
        for row in self.square:
            board = "{}{}\n".format(board,row)
        print(board)
        print("expected sum:",self.expectedSum)
        print("column sums:",self.columnSums)
        print("row sums:",self.rowSums)
        print("diagonal sums:",self.diagonalSums)
        print("duplicate values:",self.duplicates)
    def validate_genome(self,genome):

        return True