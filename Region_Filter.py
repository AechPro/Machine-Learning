import keras
import Data_Loader as dl
from keras.models import Model, Sequential, load_model
from keras.layers import Dense, Dropout, Flatten, Input
from keras.layers import Conv2D, MaxPooling2D, BatchNormalization,AveragePooling2D
from keras.optimizers import Adadelta, Adam
from keras import backend as K
from keras.callbacks import ModelCheckpoint
from keras.preprocessing.image import ImageDataGenerator as augmentor
import cv2
import numpy as np
class filter_CNN(object):
    def __init__(self):
        #trainData, valData, inputShape, numClasses = dl.load_data()
        #self.model = self.build_model(inputShape, numClasses)
        self.model = self.build_model(None,None,load=True)
        #self.train(trainData, valData)
        #self.test(valData[0])
    def build_model(self, inputShape, numClasses,load=False):
        if(load):
            model = load_model("best_model.h5")
            return model
        inp = Input(shape=inputShape)
        layers = Conv2D(16,kernel_size=(5,5),activation='relu')(inp)
        layers = BatchNormalization(axis=3)(layers)
        layers = Dropout(0.25)(layers)
        layers = Conv2D(32,kernel_size=(3,3),activation='relu')(layers)
        layers = BatchNormalization(axis=3)(layers)
        layers = MaxPooling2D()(layers)
        layers = Conv2D(64,kernel_size=(2,2),activation='relu')(layers)
        layers = BatchNormalization(axis=3)(layers)
        layers = Dropout(0.25)(layers)
        layers = Conv2D(128,kernel_size=(1,1),activation='relu')(layers)
        layers = BatchNormalization(axis=3)(layers)
        layers = AveragePooling2D()(layers)
        layers = Dropout(0.25)(layers)
        layers = Flatten()(layers)
        layers = Dropout(0.5)(layers)
        layers = Dense(128,activation='sigmoid')(layers)
        layers = Dense(numClasses,activation='softmax')(layers)
        model = Model(inputs=inp,outputs=layers)
        model.compile(optimizer=Adam(),loss='categorical_crossentropy',metrics=['accuracy'])
        return model
    def classify(self, img, hardChoice=True):
        img = np.asarray(img)
        if 0 in img.shape:
            if(hardChoice):
                return False
            return -1
        try:
            img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        except:
            print("Image already gray.")
        img = cv2.resize(img, (64,64), interpolation=cv2.INTER_CUBIC)
        preds = self.model.predict(img.reshape(1,64,64,1))
        choice = np.argmax(preds)
        if(hardChoice):
            return choice == 1
        return preds
    def score(self,data):
        dataScore = 0
        for example in data:
            if 0 in example.shape:
                continue
            example = np.asarray(example)
            try:
                example = cv2.cvtColor(example, cv2.COLOR_BGR2GRAY)
            except:
                print("Data already grayscale.")
            example = cv2.resize(example, (64,64), interpolation=cv2.INTER_CUBIC)
            pred = self.model.predict(example.reshape(1,64,64,1))
            output = np.argmax(pred)
            if output==1:
                dataScore+=1
        return dataScore
    def train(self,trainData, valData):
        batch_size = 16
        epochs = 10000
        xTrain, yTrain = trainData
        xTest, yTest = valData

        dataGen = augmentor(rotation_range=359,horizontal_flip=True,vertical_flip=True)
        dataGen.fit(xTrain)
        callbacks = [ModelCheckpoint("best_model.h5", monitor='val_loss', verbose=1, save_best_only=True)]
        self.model.fit_generator(dataGen.flow(xTrain, yTrain,batch_size=batch_size),steps_per_epoch=len(xTrain)/batch_size,epochs=epochs,verbose=2,validation_data=valData,callbacks=callbacks)
        score = self.model.evaluate(xTest, yTest, verbose=0)
        print('Test loss:', score[0])
        print('Test accuracy:', score[1])
