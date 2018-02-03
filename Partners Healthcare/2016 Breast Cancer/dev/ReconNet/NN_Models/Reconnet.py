import cv2
import matplotlib
import numpy as np
from keras.callbacks import ModelCheckpoint
from keras import backend as K
from keras.layers import Conv2D, MaxPooling2D, BatchNormalization, AveragePooling2D, Activation
from keras.layers import Dense, Dropout, Flatten, Input, Add, Concatenate, Reshape, Average
from keras.models import Model, load_model
from keras.optimizers import Adam, Adadelta
from keras.preprocessing.image import ImageDataGenerator as augmentor
from util import Data_Loader as dl
import os
matplotlib.use('Agg')
import matplotlib.pyplot as plt

dataGen = augmentor(rotation_range=359, horizontal_flip=True, vertical_flip=True)



def multi_input_data_generator(x1, x2, y1, y2, batch_size):
    genX1 = dataGen.flow(x1, y1, batch_size=batch_size)
    genX2 = dataGen.flow(x2, y2, batch_size=batch_size)
    while True:
        out1 = genX1.next()
        out2 = genX2.next()
        yield [out1[0], out2[0]], [out1[1], out2[1]]


class Reconnet(object):
    def __init__(self,modelNumber):
        if not os.path.exists("data/model_{}".format(modelNumber)):
            os.makedirs("data/model_{}".format(modelNumber))
        self.modelNumber = modelNumber
        self.model_file = "data/model_{}/reconnet_model_{}.h5".format(modelNumber,modelNumber)

    def load_data(self):
        self.trainData, self.valData, self.inputShape, \
        self.num470Classes, self.num625Classes = dl.load_data("../training_data/reconnet")

    def build_model(self, load=False):
        if (load):
            self.model = load_model(self.model_file)
            return
        inputShape = self.inputShape
        num470Classes = self.num470Classes
        num625Classes = self.num625Classes

        print(inputShape)

        # Begin network architecture
        input470nm = Input(shape=inputShape)
        input625nm = Input(shape=inputShape)
        inp = Concatenate()([input625nm, input470nm])

        layers = Conv2D(32, kernel_size=(2, 2), activation='relu')(inp)
        layers = BatchNormalization(axis=3)(layers)

        layers = Dropout(0.25)(layers)

        layers = Conv2D(16,kernel_size=(3,3),activation='relu')(layers)
        layers = BatchNormalization(axis=3)(layers)
        layers = self.deepBlock(layers,[64,64,128],[2,2,1])
        shortcut = self.downscale_tensor(inp,layers)
        layers = Concatenate()([shortcut,layers])
        layers = self.deepBlock(layers,[128,128,256],[2,2,1])
        #layers = AveragePooling2D()(layers)
        layers = Flatten()(layers)
        layers = Dropout(0.5)(layers)

        outputRed = Dense(256, activation='sigmoid')(layers)
        outputRed = Dropout(0.5)(outputRed)
        outputRed = Dense(256,activation='sigmoid')(outputRed)
        outputRed = Dropout(0.5)(outputRed)
        outputRed = Dense(num625Classes, name="RedOutput", activation='softmax')(outputRed)

        outputBlue = Dense(256, activation='sigmoid')(layers)
        outputBlue = Dropout(0.5)(outputBlue)
        outputBlue = Dense(256, activation='sigmoid')(outputBlue)
        outputBlue = Dropout(0.5)(outputBlue)
        outputBlue = Dense(num470Classes, name="BlueOutput", activation='softmax')(outputBlue)

        model = Model(inputs=[input470nm, input625nm], outputs=[outputBlue, outputRed])
        # End network architecture

        # Compile model with categorical cross-entropy loss function and Adam optimizer.
        model.compile(optimizer=Adam(), loss='categorical_crossentropy', metrics=['accuracy'])
        self.model = model
        print(model.summary())

    """
        Function to build a new model architecture given a genome. The genome must be a tuple containing tuples that
        represent the type of layer to be added, as well as the necessary parameters for each layer. Ex: [[0,32,2],[0,64,2]]
        would construct a model with layers Conv2D(32,kernel_size=(2,2)), Conv2D(64,kernel_size=(2,2)). Currently there is
        no functionality to determine the architecture of the fully connected layers at the output of the network.

        @param genome: tuple of genes that represent the model to be built.
    """

    def build_model_from_genome(self, genome, encodingTable):
        inputShape = self.inputShape
        numClasses = self.numClasses

        # Begin network architecture
        input470nm = Input(shape=inputShape)
        input625nm = Input(shape=inputShape)
        inp = Concatenate()([input625nm, input470nm])
        layers = Conv2D(32,kernel_size=(2,2),activation='relu')(inp)
        layers = BatchNormalization(axis=3)(layers)
        if genome != None:
            for gene in genome:
                layers = self.build_layer_from_gene(gene, layers, inp, encodingTable)

        layers = AveragePooling2D()(layers)
        layers = Flatten()(layers)
        layers = Dropout(0.5)(layers)

        outputRed = Dense(256, activation='sigmoid')(layers)
        outputRed = Dropout(0.5)(outputRed)
        outputRed = Dense(numClasses, name="RedOutput", activation='softmax')(outputRed)

        outputBlue = Dense(256, activation='sigmoid')(layers)
        outputBlue = Dropout(0.5)(outputBlue)
        outputBlue = Dense(numClasses, name="BlueOutput", activation='softmax')(outputBlue)

        model = Model(inputs=[input470nm, input625nm], outputs=[outputBlue, outputRed])
        # End network architecture

        # Compile model with categorical cross-entropy loss function and Adam optimizer.
        model.compile(optimizer=Adam(), loss='categorical_crossentropy', metrics=['accuracy'])
        self.model = model
        print(model.summary())

    def build_layer_from_gene(self, gene, layers, inp, encodingTable):
        id = gene[0]
        layerName = encodingTable[id][-1]
        print("BUILDING LAYER FROM ID",id,"NAME",layerName)
        layer = None
        print(layerName=="conv",layerName=="dropout",layerName=="max_pool",layerName=="avg_pool",layerName=="input_wrap")
        if layerName == "conv":
            layer = Conv2D(gene[1], kernel_size=(gene[2], gene[2]), activation='relu')(layers)
            layer = BatchNormalization(axis=3)(layer)
        elif layerName == "dropout":
            layer = Dropout(gene[1])(layers)
        elif layerName == "max_pool":
            layer = MaxPooling2D()(layers)
        elif layerName == "avg_pool":
            layer = AveragePooling2D()(layers)
        elif layerName == "input_wrap" and layers != None:
            layer = self.downscale_tensor(inp, layers)
            layer = Concatenate()([layer, layers])
            layer = Activation('relu')(layer)

        return layer

    """
        Function to downscale a tensor to be the same shape as a destination tensor. This is meant to be a helper
        function to easily downscale some early layer to be the same shape as some later layer such that they can
        be concatenated, added, averaged, etc. This is necessary for high level context transferring between layers.
        This function achieves downscaling by first performing a series of average pooling operations to halve the
        dimensions of the input tensor as many times as possible. Following this, a series of linear convolutional layers
        are applied to the remaining tensor to achieve the desired number of filters and final dimensions.

        @param inputTensor: Tensor to be downscaled.
        @param destTensor: Tensor whose shape will be downscaled to.
    """

    def downscale_tensor(self, inputTensor, destTensor):
        # Get the input and dest shapes.
        shape = inputTensor._keras_shape
        destShape = destTensor._keras_shape
        tensor = inputTensor
        if shape == destShape:
            return tensor
        print("DOWNSCALING TENSOR FROM SHAPE",shape,"TO",destShape)
        # Set x = (width, height) of the input tensor and y = (width, height) of the dest tensor.
        x = (shape[1], shape[2])
        y = (destShape[1], destShape[2])

        # Find the number of times the input can be halved before subtraction must begin.
        n = int(np.log2(np.divide(x, y))[0])

        # Loop through all remaining divisions.
        for i in range(n):
            tensor = AveragePooling2D()(tensor)
        # Get the new shape.
        shape = tensor._keras_shape
        print("ALL DIVISIONS RESULTED IN TENSOR SHAPE",shape)
        # Find the number of subtractions will need to be done in order to achieve the destination shape.
        n = shape[1] - y[1]
        if n<0 or n==0:
            print("RETURNING TENSOR BEFORE SUBTRACTIONS WITH SHAPE",tensor._keras_shape)
            return tensor
        print("PERFORMING",n,"SUBTRACTIONS")
        # Perform linear convolutions until the input tensor matches the shape of the destination tensor.
        for i in range(n):
            tensor = Conv2D(destShape[-1], kernel_size=(2, 2), activation=None)(tensor)
        print("FINAL TENSOR SHAPE IS",tensor._keras_shape)

        return tensor

    def shallowBlock(self, inputTensor, filters, kernel, ):
        layers = Conv2D(filters[0], kernel_size=(kernel, kernel), padding='same', activation='relu')(inputTensor)
        layers = BatchNormalization(axis=3)(layers)

        layers = Dropout(0.25)(layers)

        layers = Conv2D(filters[1], kernel_size=(kernel, kernel), padding='same', activation='relu')(layers)
        layers = BatchNormalization(axis=3)(layers)

        layers = Concatenate()([inputTensor, layers])
        layers = Activation('relu')(layers)
        return layers

    def deepBlock(self, inputTensor, filters, kernels):
        layers = Conv2D(filters[0], kernel_size=(kernels[0], kernels[0]), activation='relu')(inputTensor)
        layers = BatchNormalization(axis=3)(layers)
        layers = Conv2D(filters[0], kernel_size=(kernels[0], kernels[0]), activation='relu')(layers)
        layers = BatchNormalization(axis=3)(layers)

        layers = Dropout(0.25)(layers)

        layers = Conv2D(filters[1], kernel_size=(kernels[1], kernels[1]), activation='relu')(layers)
        layers = BatchNormalization(axis=3)(layers)
        layers = Conv2D(filters[1], kernel_size=(kernels[1], kernels[1]), activation='relu')(layers)
        layers = BatchNormalization(axis=3)(layers)

        layers = Dropout(0.25)(layers)

        layers = Conv2D(filters[2], kernel_size=(kernels[2], kernels[2]), activation='relu')(layers)
        layers = BatchNormalization(axis=3)(layers)
        layers = Conv2D(filters[2], kernel_size=(kernels[2], kernels[2]), activation='relu')(layers)
        layers = BatchNormalization(axis=3)(layers)

        layers = AveragePooling2D()(layers)
        return layers

    """
        Function to classify an input image with a trained model.
        @param img: A grayscale input image for classification.
        @param hardChoice: Flag to determine whether or not the returned value should be the
                           full output of the model, or a boolean classification based on the output.
    """

    def classify(self, inputs, certaintyThreshold=0.9, hardChoice=True):
        in1 = np.asarray(inputs[0]).astype('float32')
        in2 = np.asarray(inputs[1]).astype('float32')
        # Return bad value if the image is 0 in any dimension.
        if 0 in in1.shape or 0 in in2.shape:
            if (hardChoice):
                return False
            return -1
        in1 -= 31605.9
        in2 -= 31605.9
        in2 /= np.power(2, 16) - 1
        in1 /= np.power(2, 16) - 1
        in1 = cv2.resize(in1,(64,64),interpolation=cv2.INTER_CUBIC)
        in2 = cv2.resize(in2,(64,64),interpolation=cv2.INTER_CUBIC)
        # calculated channel mean
        #inputImg /= np.power(2, 16) - 1"""

        inp = [in1.reshape(1,64,64,1),in2.reshape(1,64,64,1)]
        preds = self.model.predict(inp)
        return preds

    """
       Function to train a fresh model architecture.
       @param trainData: A tuple containing the xTrain, yTrain training set for the network.
       @param valData: A tuple containing the xVal, yVal validation set for the network.
    """

    def train(self):
        batch_size = 32
        epochs = 150
        xTrain, yTrain = self.trainData
        xVal, yVal = self.valData
        vout1, vout2 = yVal
        vinp1 = np.asarray([x[0] for x in xVal])
        vinp2 = np.asarray([x[1] for x in xVal])
        inp1 = np.asarray([x[0] for x in xTrain])
        inp2 = np.asarray([x[1] for x in xTrain])
        out1, out2 = yTrain
        # Set up generator for data augmentation.
        trainGen = multi_input_data_generator(inp1, inp2, out1, out2, batch_size=batch_size)
        valGen = multi_input_data_generator(vinp1, vinp2, vout1, vout2, batch_size=batch_size)

        # Set up callbacks tuple for checkpoints. This will save the model every time validation loss improves.
        callbacks = [ModelCheckpoint(self.model_file, monitor='val_loss', verbose=1, save_best_only=True)]

        # Train the model and save its training progress in a history object.
        #I should definitely be making each dictionary in the history object start at 0 before passing it off for metric
        #logs but I've implemented it the wrong way already, and can't be bothered to fix it right now.
        history = self.model.fit_generator(trainGen,
                                           steps_per_epoch=len(xTrain) / batch_size, epochs=epochs,
                                           verbose=2, validation_data=valGen,
                                           validation_steps=len(xVal) / batch_size, callbacks=callbacks)
        self.save_training_metrics(history)
        self.plot_training_metrics(history)
        self.modelAccuracy = max(history.history["val_BlueOutput_acc"]) + max(history.history["val_RedOutput_acc"])
        K.clear_session()
    def plot_training_metrics(self,history):
        path = "data/model_{}".format(self.modelNumber)
        #Since Keras starts history logs after the first epoch, we will copy them to an array starting at 0.
        blueAcc = [0]
        blueAccVal = [0]
        redAcc = [0]
        redAccVal = [0]
        blueLoss = [0]
        blueLossVal = [0]
        redLoss = [0]
        redLossVal = [0]
        for i in range(len(history.history['BlueOutput_acc'])):
            blueAcc.append(history.history['BlueOutput_acc'][i])
            blueAccVal.append(history.history['val_BlueOutput_acc'][i])
            redAcc.append(history.history['RedOutput_acc'][i])
            redAccVal.append(history.history['val_RedOutput_acc'][i])

            blueLoss.append(history.history['BlueOutput_loss'][i])
            blueLossVal.append(history.history['val_BlueOutput_loss'][i])
            redLoss.append(history.history['RedOutput_loss'][i])
            redLossVal.append(history.history['val_RedOutput_loss'][i])

        #Clear any previous plot and plot training and validation data.
        plt.clf()
        plt.plot(blueAcc)
        plt.plot(blueAccVal)
        plt.plot(redAcc)
        plt.plot(redAccVal)

        plt.title('model accuracy')
        plt.ylabel('accuracy')
        plt.xlabel('epoch')
        plt.legend(['blue_train', 'blue_val', 'red_train','red_val'], loc='lower right')
        plt.savefig(''.join([path,'/',"Accuracy.png"]))
        plt.clf()

        # Plot and save training and validation loss data.
        plt.plot(blueLoss)
        plt.plot(blueLossVal)
        plt.plot(redLoss)
        plt.plot(redLossVal)

        plt.title('model loss')
        plt.ylabel('loss')
        plt.xlabel('epoch')
        plt.legend(['blue_train', 'blue_val','red_train','red_val'], loc='upper right')
        plt.savefig(''.join([path,'/',"Loss.png"]))

    def save_training_metrics(self,history):
        path = "data/model_{}".format(self.modelNumber)
        bval_acc = open(''.join([path, '/', 'blue_val_acc_history.txt']), 'w')
        bacc = open(''.join([path, '/', 'blue_acc_history.txt']), 'w')
        bloss = open(''.join([path, '/', 'blue_loss_history.txt']), 'w')
        bval_loss = open(''.join([path, '/', 'blue_val_loss_history.txt']), 'w')

        rval_acc = open(''.join([path, '/', 'red_val_acc_history.txt']), 'w')
        racc = open(''.join([path, '/', 'red_acc_history.txt']), 'w')
        rloss = open(''.join([path, '/', 'red_loss_history.txt']), 'w')
        rval_loss = open(''.join([path, '/', 'red_val_loss_history.txt']), 'w')

        a = [history.history['BlueOutput_acc'],history.history['RedOutput_acc']]
        va = [history.history['val_BlueOutput_acc'],history.history['val_RedOutput_acc']]
        l = [history.history['BlueOutput_loss'],history.history['RedOutput_loss']]
        vl = [history.history['val_BlueOutput_loss'],history.history['val_RedOutput_loss']]

        #It would definitely be better to re-create the history object as a copy of itself beginning at 0, but I've already
        #done it this way so I can't be bothered to change it.
        bacc.write("{}\n".format(0))
        bval_acc.write("{}\n".format(0))
        bloss.write("{}\n".format(0))
        bval_loss.write("{}\n".format(0))
        racc.write("{}\n".format(0))
        rval_acc.write("{}\n".format(0))
        rloss.write("{}\n".format(0))
        rval_loss.write("{}\n".format(0))

        for accVal, vacc, lossVal, vloss in zip(a[0], va[0], l[0], vl[0]):
            bacc.write("{}\n".format(accVal))
            bval_acc.write("{}\n".format(vacc))
            bloss.write("{}\n".format(lossVal))
            bval_loss.write("{}\n".format(vloss))

        for accVal, vacc, lossVal, vloss in zip(a[1], va[1], l[1], vl[1]):
            racc.write("{}\n".format(accVal))
            rval_acc.write("{}\n".format(vacc))
            rloss.write("{}\n".format(lossVal))
            rval_loss.write("{}\n".format(vloss))

        bval_acc.close()
        bloss.close()
        bval_loss.close()
        bacc.close()
        rval_acc.close()
        rloss.close()
        rval_loss.close()
        racc.close()
    def set_model_number(self,num):
        self.modelNumber = num
        self.model_file = "data/model_{}/reconnet_model_file_{}.h5".format(num,num)