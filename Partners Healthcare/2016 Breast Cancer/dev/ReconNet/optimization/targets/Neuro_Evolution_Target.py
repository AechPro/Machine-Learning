import numpy as np
from util import encoding_table_utils as parser
def sigmoid(x):
    output = 1/1.0 + np.exp(-x)
    return output
def softmax(xi,x):
    val = 0
    for inp in x:
        val+=np.exp(inp)
    return np.exp(xi)/val
def binary_cross_entropy(yHat, y):
    if yHat == 1:
      return -np.log(y)
    else:
      return -np.log(1 - y)

def MSE(yHat, y):
    return np.sum((yHat - y)**2) / y.size
class target(object):
    def __init__(self, inputs, outputs, maxHiddenNeurons):
        maxNodes = int(inputs)
        self.inputs = inputs
        self.outputs = outputs*2
        self.maxHiddenNeurons = maxHiddenNeurons
        #topology node needs inputID, outputID, input bias, output bias, weight
        self.encodingString = self.encodingString = "0,{} {},{} -2.0,2.0 1.0 connection_0,{} 0,{} -2.0,2.0 1.0 outputConnection" \
                                                    "_{},{} -2.0,2.0 0.5 node".format(maxNodes, self.inputs,maxNodes, maxNodes,
                                                                                   outputs-1, inputs, maxNodes+outputs)
        self.initialSize = 0
        self.canAdd = True
        self.canRemove = False
        self.initializationType = "random"
        self.encodingTable = parser.parse_encoding_string(self.encodingString)
        self.nodes = {}
        self.connections = {}
        self.hiddenNodes = 0
        self.outputNodes = {}
        self.repeatedNodes = 0
        self.repeatedConnections = 0
        self.outputConnections = 0
        for i in range(outputs*2):
            self.outputNodes[i] = Node(i,0)

    def build_from_genome(self, genome):
        assert genome != None, "Null genome passed to target!"
        #print("building")
        self.hiddenNodes = 0
        self.nodes = {}
        self.connections = []
        self.repeatedNodes = 0
        self.repeatedConnections = 0
        self.outputConnections = 0
        #if len(genome)>=1: print(genome)
        for i in range(self.inputs):
            self.nodes[i] = Node(i,0)
        for i in range(len(genome)):
            gene = genome[i]
            entry = gene[1:]
            #print(gene)
            if gene[0] == 0:
                #print(gene)
                connection = Connection(entry[0],entry[1],entry[2],0)
                self.connections.append(connection)
            elif gene[0] == 1:
                connection = Connection(entry[0],entry[1],entry[2],1)
                self.connections.append(connection)
                self.outputConnections+=1
            elif gene[0] == 2:
                if entry[0] not in self.nodes.keys():
                    self.nodes[entry[0]] = Node(entry[0],entry[1])
                    self.hiddenNodes += 1
                else:
                    self.repeatedNodes+=1

        for i in range(len(self.connections)):
            for j in range(len(self.connections)):
                if i!=j:
                    if self.connections[i] == self.connections[j]:
                        self.repeatedConnections+=1
        #print("found",self.hiddenNodes,"hidden nodes")
        #print(nodes)
        #append output nodes to appropriate locations following hidden and inputs nodes

    def evaluate(self):
        fitness = 0
        inputTable = [[0,0],[0,1],[1,0],[1,1]]
        outputTable = [[0],[1],[1],[0]]
        for i in range(len(inputTable)):
            inp = inputTable[i]
            out = self.predict(inp)
            fitness -= binary_cross_entropy(outputTable[i][0],out[np.argmax(out)])
            #if len(self.connections)>0: print("MODEL PREDICTION\nINPUT =",inp,"\nOUTPUT =",out)
        return round(fitness,4) - self.repeatedNodes - self.repeatedConnections

    """remember to reset all node values after prediction!"""
    def predict(self,input):
        #load input into model
        for x in input:
            for i in range(self.inputs):
                self.nodes[i].compute(x)
        #print("Inputs loaded to neural network")
        for connection in self.connections:
            #print(connection)
            if connection.inputNode not in self.nodes.keys():
                continue
            if connection.outputType == 1:
                try:
                    val = sigmoid(self.nodes[connection.inputNode].value * connection.weight)
                    self.outputNodes[connection.outputNode].compute(val)
                except Exception as e:
                    print(connection.inputNode,connection.outputNode,self.outputNodes.keys(),self.nodes.keys())
                    print("Exception in propagation of output:","\nName:",type(e).__name__,"\nArgs:",e.args)
                    continue
            else:
                if connection.outputNode not in self.nodes.keys():
                    continue
                try:
                    val = sigmoid(self.nodes[connection.inputNode].value*connection.weight)
                    self.nodes[connection.outputNode].compute(val)
                except Exception as e:
                    print(connection.inputNode,connection.outputNode,self.nodes.keys())
                    print("Exception in propagation of hidden nodes:","\nName:",type(e).__name__,"\nArgs:",e.args)
                    continue
        #print("All network connections propagated")
        output = [self.outputNodes[key].value for key in self.outputNodes.keys()]
        dist = []
        for i in range(len(output)):
            dist.append(softmax(output[i],output))
        #print("Outputs loaded")
        for key in self.nodes.keys():
            self.nodes[key].value = 0
        for key in self.outputNodes.keys():
            self.outputNodes[key].value = 0
        #print("Network values reset")
        return np.asarray(dist)
    def display(self):
        print(len(self.nodes.keys()),self.inputs,self.hiddenNodes,self.outputs)
        print("Connections:",len(self.connections),"\nOutput connections:",self.outputConnections,"\nInput nodes:",
              len(self.nodes.keys())-self.hiddenNodes,"\nHidden nodes:",self.hiddenNodes,"\nOutput nodes:",
              len(self.outputNodes.keys()),"\nRepeated Connections:",self.repeatedConnections)
        inputTable = [[0, 0], [0, 1], [1, 0], [1, 1]]
        outputTable = [[0], [1], [1], [0]]
        accuracy = 0
        for i in range(len(inputTable)):
            inp = inputTable[i]
            out = self.predict(inp)
            if np.argmax(out) == outputTable[i][0]:
                accuracy+=1
            print("MODEL PREDICTION\nINPUT =",inp,"\nOUTPUT =",out)
        print("Model accuracy {:.2f}%:".format(accuracy*100/len(outputTable)))

    def validate_genome(self, genome):

        return True
    def update_encoding_string(self,genome):
        conCount = 0
        nodeCount = 0
        nodeProb = 1.0
        for gene in genome:
            if gene[1] == 0 or gene[1] == 1:
                conCount+=1
            else:
                nodeCount+=1
        #if nodeCount>=1:
            #nodeProb = 0.5
        maxNodes = self.inputs+self.hiddenNodes
        self.encodingString = "0,{} {},{} -2.0,2.0 1.0 connection_0,{} 0,{} -2.0,2.0 1.0 outputConnection_{},{} " \
                              "-2.0,2.0 {} node".format(maxNodes, self.inputs,maxNodes, maxNodes,self.outputs-1,
                              self.inputs+self.hiddenNodes, maxNodes, nodeProb)
        self.encodingTable = parser.parse_encoding_string(self.encodingString)


class Connection(object):
    def __init__(self,inID,outID,weight,outputType):
        self.inputNode = inID
        self.outputNode = outID
        self.outputType = outputType
        self.weight = round(weight,4)
    def __eq__(self,other):
        return self.inputNode == other.inputNode and self.outputNode == other.inputNode and self.outputType == other.outputType
class Node(object):
    def __init__(self,ID,bias):
        self.ID = ID
        self.bias = round(bias,4)
        self.value = 0
    def compute(self,value):
        self.value += value + self.bias

