"""
Copyright 2017 Matthew W. Allen

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""

import cv2
import matplotlib
import os
import numpy as np
from keras.callbacks import ModelCheckpoint
from keras.layers import Conv2D, MaxPooling2D, BatchNormalization, AveragePooling2D
from keras.layers import Dense, Dropout, Flatten, Input
from keras.models import Model, load_model
from keras.optimizers import Adam
from keras.preprocessing.image import ImageDataGenerator as augmentor

from util import Data_Loader as dl

matplotlib.use('Agg')
import matplotlib.pyplot as plt

"""Convolutional Neural Network used to filter cells from proposed regions in a hologram."""


class EmbedNet(object):
    def __init__(self,modelNumber):
        if not os.path.exists("data/embedding_model_{}".format(modelNumber)):
            os.makedirs("data/embedding_model_{}".format(modelNumber))
        self.modelNumber = modelNumber
        self.model_file = "data/model_{}/embedding_model_{}.h5".format(modelNumber,modelNumber)

    def load_data(self):
        self.trainData, self.valData, self.inputShape, \
        self.numClasses = dl.load_data("../training_data/reconnet", numClasses=4)

    def build_model(self, load=False):
        if (load):
            self.model = load_model(self.model_file)
            return
        inputShape = self.inputShape
        numClasses = self.numClasses

        # Begin network architecture
        input470nm = Input(shape=inputShape)
        input625nm = Input(shape=inputShape)
        inp = Concatenate()([input625nm, input470nm])

        layers = Conv2D(32, kernel_size=(2, 2), activation='relu')(inp)
        layers = BatchNormalization(axis=3)(layers)

        layers = Dropout(0.25)(layers)

        layers = Conv2D(16,kernel_size=(3,3),activation='relu')(layers)
        layers = BatchNormalization(axis=3)(layers)

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

    """
        Function to classify an input image with a trained model.
        @param img: A grayscale input image for classification.
        @param hardChoice: Flag to determine whether or not the returned value should be the
                           full output of the model, or a boolean classification based on the output.
    """

    def classify(self, img, certaintyThreshold=0.9, hardChoice=True):
        inputImg = np.asarray(img)
        inputImg = inputImg.astype('float32')
        # Return bad value if the image is 0 in any dimension.
        if 0 in inputImg.shape:
            if (hardChoice):
                return False
            return -1
        # Reshape image to be 64x64x1 using inter-cubic interpolation.
        inputImg = cv2.resize(inputImg, (64, 64), interpolation=cv2.INTER_CUBIC)
        inputImg -= 26925.3  # calculated channel mean
        inputImg /= np.power(2, 16) - 1
        preds = self.model.predict(inputImg.reshape(1, 64, 64, 1))
        choice = np.argmax(preds)
        if (hardChoice):
            return choice == 1 and preds[0][choice] >= certaintyThreshold
        return preds

    """
       Function to train a fresh model architecture.
       @param trainData: A tuple containing the xTrain, yTrain training set for the network.
       @param valData: A tuple containing the xVal, yVal validation set for the network.
    """

    def train(self):
        batch_size = 32
        epochs = 50
        xTrain, yTrain = self.trainData
        valData = self.valData

        # Set up generator for data augmentation.
        dataGen = augmentor(rotation_range=359, horizontal_flip=True, vertical_flip=True)
        dataGen.fit(xTrain)

        # Set up callbacks tuple for checkpoints. This will save the model every time validation loss improves.
        callbacks = [ModelCheckpoint(self.model_file, monitor='val_loss', verbose=1, save_best_only=True)]

        # Train the model and save its training progress in a history object.
        history = self.model.fit_generator(dataGen.flow(xTrain, yTrain, batch_size=batch_size),
                                           steps_per_epoch=len(xTrain) / batch_size, epochs=epochs,
                                           verbose=2, validation_data=valData, callbacks=callbacks)
        val_acc = open("val_acc_history.txt", 'w')
        acc = open("acc_history.txt", 'w')
        loss = open("loss_history.txt", 'w')
        val_loss = open("val_loss_history.txt", 'w')
        a = history.history['acc']
        va = history.history['val_acc']
        l = history.history['loss']
        vl = history.history['val_loss']

        self.modelAccuracy = max(a)

        for accVal, vacc, lossVal, vloss in zip(a, va, l, vl):
            acc.write("{}\n".format(accVal))
            val_acc.write("{}\n".format(vacc))
            loss.write("{}\n".format(lossVal))
            val_loss.write("{}\n".format(vloss))
        val_acc.close()
        loss.close()
        val_loss.close()
        acc.close()

        # Plot and save training and validation accuracy data.
        plt.plot(history.history['acc'])
        plt.plot(history.history['val_acc'])
        plt.title('model accuracy')
        plt.ylabel('accuracy')
        plt.xlabel('epoch')
        plt.legend(['train', 'test'], loc='lower right')
        plt.savefig("Accuracy.png")
        plt.clf()

        # Plot and save training and validation loss data.
        plt.plot(history.history['loss'])
        plt.plot(history.history['val_loss'])
        plt.title('model loss')
        plt.ylabel('loss')
        plt.xlabel('epoch')
        plt.legend(['train', 'test'], loc='upper right')
        plt.savefig("Loss.png")
        # net = filter_CNN()