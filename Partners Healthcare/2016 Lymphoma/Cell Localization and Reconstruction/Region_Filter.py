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

import Data_Loader as dl
from keras.models import Model,load_model
from keras.layers import Dense, Dropout, Flatten, Input
from keras.layers import Conv2D, MaxPooling2D, BatchNormalization,AveragePooling2D
from keras.optimizers import Adam
from keras.callbacks import ModelCheckpoint
from keras.preprocessing.image import ImageDataGenerator as augmentor
import cv2
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

"""Convolutional Neural Network used to filter cells from proposed regions in a hologram."""
class filter_CNN(object):
    def __init__(self):
        self.model_file = "hologram_model.h5"

        self.model = self.build_model(None,None,load=True)
        #plot_model(self.model, to_file='model.png')
        #trainData, valData, inputShape, numClasses = dl.load_data()
        #self.model = self.build_model(inputShape, numClasses)
        #self.train(trainData, valData)

    """
       Function to construct a new model or load trained model from designated file and return it.
       @param inputShape: Dimensions of input image (64x64x1).
       @param numClasses: Number of unique classes in data set.
       @param load: Flag to load a saved model.
    """
    def build_model(self, inputShape, numClasses,load=False):
        if(load):
            model = load_model(self.model_file)
            return model

        #Begin network architecture
        inp = Input(shape=inputShape)
        
        layers = Conv2D(16,kernel_size=(5,5),activation='relu')(inp)
        layers = BatchNormalization(axis=3)(layers)
        
        layers = MaxPooling2D()(layers)
        
        layers = Conv2D(32,kernel_size=(3,3),activation='relu')(layers)
        layers = BatchNormalization(axis=3)(layers)

        layers = Conv2D(32, kernel_size=(3, 3), activation='relu')(layers)
        layers = BatchNormalization(axis=3)(layers)

        layers = Dropout(0.25)(layers)

        layers = Conv2D(64, kernel_size=(3, 3), activation='relu')(layers)
        layers = BatchNormalization(axis=3)(layers)

        layers = Conv2D(64, kernel_size=(3, 3), activation='relu')(layers)
        layers = BatchNormalization(axis=3)(layers)

        layers = Dropout(0.25)(layers)

        layers = Conv2D(128,kernel_size=(1,1),activation='relu')(layers)
        layers = BatchNormalization(axis=3)(layers)
        
        layers = AveragePooling2D()(layers)
        layers = Flatten()(layers)
        
        layers = Dropout(0.5)(layers)
        
        layers = Dense(256,activation='sigmoid')(layers)
        layers = Dense(256, activation='sigmoid')(layers)
        layers = Dense(numClasses,activation='softmax')(layers)
        
        model = Model(inputs=inp,outputs=layers)
        #End network architecture

        #Compile model with categorical cross-entropy loss function and Adam optimizer.
        model.compile(optimizer=Adam(),loss='categorical_crossentropy',metrics=['accuracy'])
        return model

    """
        Function to classify an input image with a trained model.
        @param img: A grayscale input image for classification.
        @param hardChoice: Flag to determine whether or not the returned value should be the
                           full output of the model, or a boolean classification based on the output.
    """
    def classify(self, img, hardChoice=True):
        img = np.asarray(img)

        #Return bad value if the image is 0 in any dimension.
        if 0 in img.shape:
            if(hardChoice):
                return False
            return -1

        #Reshape image to be 64x64x1 and get model prediction
        img = cv2.resize(img, (64,64), interpolation=cv2.INTER_CUBIC)
        preds = self.model.predict(img.reshape(1,64,64,1))
        choice = np.argmax(preds)
        if(hardChoice):
            return choice == 1
        return preds

    """
       Function to train a fresh model architecture.
       @param trainData: A tuple containing the xTrain, yTrain training set for the network.
       @param valData: A tuple containing the xVal, yVal validation set for the network.
    """
    def train(self,trainData, valData):
        batch_size = 32
        epochs = 250
        xTrain, yTrain = trainData
        xTest, yTest = valData

        #Set up generator for data augmentation.
        dataGen = augmentor(rotation_range=359,horizontal_flip=True,vertical_flip=True)
        dataGen.fit(xTrain)

        #Set up callbacks tuple for checkpoints. This will save the model every time validation loss improves.
        callbacks = [ModelCheckpoint("cell_model.h5", monitor='val_loss', verbose=1, save_best_only=True)]

        #Train the model and save its training progress in a history object.
        history = self.model.fit_generator(dataGen.flow(xTrain, yTrain,batch_size=batch_size),
                                           steps_per_epoch=len(xTrain)/batch_size,epochs=epochs,
                                           verbose=2,validation_data=valData,callbacks=callbacks)
        #Plot and save training and validation accuracy data.
        plt.plot(history.history['acc'])
        plt.plot(history.history['val_acc'])
        plt.title('model accuracy')
        plt.ylabel('accuracy')
        plt.xlabel('epoch')
        plt.legend(['train', 'test'], loc='lower right')
        plt.savefig("Accuracy.png")
        plt.clf()


        #Plot and save training and validation loss data.
        plt.plot(history.history['loss'])
        plt.plot(history.history['val_loss'])
        plt.title('model loss')
        plt.ylabel('loss')
        plt.xlabel('epoch')
        plt.legend(['train', 'test'], loc='upper right')
        plt.savefig("Loss.png")
#net = filter_CNN()
