
class target(object):
    def __init__(self,numQueens,boardSize):
        boardSize-=1
        positions = boardSize*boardSize - 1
        self.boardSize = boardSize
        self.encodingString = "0,{} 0,{} 1.0 q0-".format(boardSize,boardSize)
        for i in range(1,numQueens):
            self.encodingString = "{}0,{} 0,{} 1.0 q{}-".format(self.encodingString,boardSize,boardSize,i)
        self.encodingString = self.encodingString[:-1]
        self.queens = None
        self.canAdd = False
        self.canRemove = False
        self.initializationType = "sequential"
        self.encodingTable = None
    def build_from_genome(self,genome):
        assert genome != None, "Null genome passed to target!"
        queens = []
        for gene in genome:
            pos = [gene[1], gene[2]]
            #pos = [gene[1]%self.boardSize, gene[1]//self.boardSize]
            queens.append(pos)
        self.queens = queens
        #self.params = [delta,minArea,maxArea,maxVariation,minDiversity,maxEvolution,areaThreshold,minMargin,edgeBlurSize]
    def evaluate(self):

        fitness = 0
        for i in range(len(self.queens)):
            locations = self.find_reachable_locations(self.queens[i])
            for j in range(len(self.queens)):
                if i!=j:
                    if(self.queens[j] in locations):
                        fitness-=1
        #print("\nFITNESS:",fitness,"\n")
        return self.get_fitness()

    def get_fitness(self):
        genes = []
        for queen in self.queens:
            genes.append(queen[0])
            genes.append(queen[1])
        size = self.boardSize + 1
        board = Board(genes, size)
        rowsWithQueens = set()
        colsWithQueens = set()
        northEastDiagonalsWithQueens = set()
        southEastDiagonalsWithQueens = set()
        for row in range(size):
            for col in range(size):
                if board.get(row, col) == 'Q':
                    rowsWithQueens.add(row)
                    colsWithQueens.add(col)
                    northEastDiagonalsWithQueens.add(row + col)
                    southEastDiagonalsWithQueens.add(size - 1 - row + col)
        total = size - len(rowsWithQueens) \
                + size - len(colsWithQueens) \
                + size - len(northEastDiagonalsWithQueens) \
                + size - len(southEastDiagonalsWithQueens)
        return -total
    def validate_genome(self,genome):

        return True
    def find_reachable_locations(self,tile):
        locations = []
        #add horizontal and vertical lines from position
        for i in range(self.boardSize):
            locations.append([i,tile[1]])
            locations.append([tile[0],i])
        x = tile[0]
        y = tile[1]
        #down and right
        while x<self.boardSize and y<self.boardSize:
            locations.append([x, y])
            x+=1
            y+=1
        x = tile[0]
        y = tile[1]
        #up and right
        while x<self.boardSize and y>0:
            x+=1
            y-=1
            locations.append([x,y])
        x = tile[0]
        y = tile[1]
        # down and left
        while x > 0 and y < self.boardSize:
            x -= 1
            y += 1
            locations.append([x, y])
        x = tile[0]
        y = tile[1]
        # up and left
        while x > 0 and y > 0:
            x -= 1
            y -= 1
            locations.append([x, y])
        return locations

    def print_board(self):
        genes = []
        for queen in self.queens:
            genes.append(queen[0])
            genes.append(queen[1])
        size = self.boardSize + 1
        board = Board(genes, size)
        board.print()

class Board:
    def __init__(self, genes, size):
        board = [['.'] * size for _ in range(size)]
        for index in range(0, len(genes), 2):
            row = genes[index]
            column = genes[index + 1]
            board[column][row] = 'Q'
        self._board = board

    def get(self, row, column):
        return self._board[column][row]

    def print(self):
        # 0,0 prints in bottom left corner
        for i in reversed(range(len(self._board))):
            print(' '.join(self._board[i]))






