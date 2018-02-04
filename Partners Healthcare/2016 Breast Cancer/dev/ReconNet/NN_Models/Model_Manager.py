from NN_Models import Single_Color_Model
import cv2
import matplotlib
import numpy as np
from keras.preprocessing.image import ImageDataGenerator as augmentor
from util import Data_Loader as dl
import os
matplotlib.use('Agg')
import matplotlib.pyplot as plt

dataGen = augmentor(horizontal_flip=True, vertical_flip=True)

def single_input_data_generator(x, y, batch_size):
    genX = dataGen.flow(x, y, batch_size=batch_size)
    while True:
        out = genX.next()
        yield out[0], out[1]

def multi_input_data_generator(x1, x2, y1, y2, batch_size):
    genX1 = dataGen.flow(x1, y1, batch_size=batch_size)
    genX2 = dataGen.flow(x2, y2, batch_size=batch_size)
    while True:
        out1 = genX1.next()
        out2 = genX2.next()
        yield [out1[0], out2[0]], [out1[1], out2[1]]

class Manager(object):
    def __init__(self):
        self.load_data()
        #inputShape, numClasses, modelNumber
        blueModel = Single_Color_Model.NN_Model(0)
        redModel = Single_Color_Model.NN_Model(1)
        self.models = [blueModel,redModel]
        self.debug_training_data()
    def load_data(self):
        self.trainingSets, self.validationSets, self.inputShapes,\
            self.outputShapes = dl.load_data("../training_data/reconnet")
    def debug_training_data(self):
        for i in range(len(self.trainingSets)):
            xt,yt = self.trainingSets[i]
            binCounts = {}
            for entry in yt:
                val = np.argmax(entry)
                if val in binCounts.keys():
                    binCounts[val] += 1
                else:
                    binCounts[val] = 0
            binCounts = sorted(binCounts.items())
            print("Bins for training set {}:".format(i),binCounts)
    def train_models(self):
        batchSize = 32
        epochs = 150
        for i in range(len(self.models)):
            xTrain, yTrain = self.trainingSets[i]
            xVal, yVal = self.validationSets[i]
            vout = yVal
            vinp = np.asarray([x for x in xVal])
            inp = np.asarray([x for x in xTrain])
            out = yTrain
            trainGen = single_input_data_generator(inp, out, batch_size=batchSize)
            valGen = single_input_data_generator(vinp, vout, batch_size=batchSize)
            epochSteps = len(xTrain) / batchSize
            valSteps = len(xVal)/batchSize
            self.models[i].build_model(self.inputShapes[i],self.outputShapes[i])
            history = self.models[i].train(epochs, batchSize, epochSteps, valSteps, trainGen, valGen)
            self.plot_training_metrics(history,self.models[i].modelNumber)
            self.save_training_metrics(history,self.models[i].modelNumber)
    def plot_training_metrics(self, history, num):
        path = "data/model_{}".format(num)
        # Since Keras starts history logs after the first epoch, we will copy them to an array starting at 0.
        acc = [0]
        accVal = [0]
        loss = [0]
        lossVal = [0]
        for i in range(len(history.history['acc'])):
            acc.append(history.history['acc'][i])
            accVal.append(history.history['val_acc'][i])

            loss.append(history.history['loss'][i])
            lossVal.append(history.history['val_loss'][i])

        # Clear any previous plot and plot training and validation data.
        plt.clf()
        plt.plot(acc)
        plt.plot(accVal)

        plt.title('model accuracy')
        plt.ylabel('accuracy')
        plt.xlabel('epoch')
        plt.legend(['train', 'val'], loc='lower right')
        plt.savefig(''.join([path, '/', "Accuracy.png"]))
        plt.clf()

        # Plot and save training and validation loss data.
        plt.plot(loss)
        plt.plot(lossVal)

        plt.title('model loss')
        plt.ylabel('loss')
        plt.xlabel('epoch')
        plt.legend(['train', 'val'], loc='upper right')
        plt.savefig(''.join([path, '/', "Loss.png"]))


    def save_training_metrics(self, history,num):
        path = "data/model_{}".format(num)
        val_acc = open(''.join([path, '/', 'val_acc_history.txt']), 'w')
        acc = open(''.join([path, '/', 'acc_history.txt']), 'w')
        loss = open(''.join([path, '/', 'loss_history.txt']), 'w')
        val_loss = open(''.join([path, '/', 'val_loss_history.txt']), 'w')

        a = history.history['acc']
        va = history.history['val_acc']
        l = history.history['loss']
        vl = history.history['val_loss']

        # It would definitely be better to re-create the history object as a copy of itself beginning at 0, but I've already
        # done it this way so I can't be bothered to change it.
        acc.write("{}\n".format(0))
        val_acc.write("{}\n".format(0))
        loss.write("{}\n".format(0))
        val_loss.write("{}\n".format(0))

        for accVal, vacc, lossVal, vloss in zip(a, va, l, vl):
            acc.write("{}\n".format(accVal))
            val_acc.write("{}\n".format(vacc))
            loss.write("{}\n".format(lossVal))
            val_loss.write("{}\n".format(vloss))

        val_acc.close()
        loss.close()
        val_loss.close()
        acc.close()
    def set_model_number(self, num):
        self.modelNumber = num
        self.model_file = "data/model_{}/reconnet_model_file_{}.h5".format(num, num)