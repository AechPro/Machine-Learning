import cv2
import numpy as np
from keras.callbacks import ModelCheckpoint
from keras import backend as K
from keras.layers import Conv2D, PReLU, MaxPooling2D, BatchNormalization, AveragePooling2D, Activation
from keras.layers import Dense, Dropout, Flatten, Input, Add, Concatenate, Reshape, Average
from keras.models import Model, load_model
from keras.optimizers import Adam, Adadelta
import os
class NN_Model(object):
    def __init__(self, modelNumber):
        if not os.path.exists("data/model_{}".format(modelNumber)):
            os.makedirs("data/model_{}".format(modelNumber))
        self.modelNumber = modelNumber
        self.model_file = "data/model_{}/single_color_model_{}.h5".format(modelNumber,modelNumber)
        self.inputShape = None
        self.numClasses = None
    def load_model(self):
        self.model = load_model(self.model_file)
        return
    def build_model(self, inputShape, numClasses):
        print("Building model...")
        self.inputShape = inputShape
        self.numClasses = numClasses

        inp = Input(shape=inputShape)

        layers = Conv2D(32, kernel_size=(3, 3), activation="linear")(inp)
        layers = PReLU()(layers)
        layers = BatchNormalization(axis=3)(layers)
        layers = Dropout(0.2)(layers)
        layers = MaxPooling2D()(layers)
        layers = Conv2D(64, kernel_size=(2, 2), activation="linear")(layers)
        layers = PReLU()(layers)
        layers = BatchNormalization(axis=3)(layers)
        layers = Dropout(0.3)(layers)
        layers = AveragePooling2D()(layers)
        layers = Flatten()(layers)
        output = Dense(32, activation='linear')(layers)
        output = PReLU()(output)
        output = Dropout(0.5)(output)
        output = Dense(16, activation='sigmoid')(output)
        output = Dropout(0.5)(output)
        output = Dense(numClasses,name='Model_Output',activation='softmax')(output)
        model = Model(inputs=inp,outputs=output)
        # End network architecture

        # Compile model with categorical cross-entropy loss function and Adam optimizer.
        model.compile(optimizer=Adam(), loss='categorical_crossentropy', metrics=['accuracy'])
        self.model = model
        print(model.summary())

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
        layers = Conv2D(filters[0], kernel_size=(kernels[0], kernels[0]), activation='linear')(inputTensor)
        layers = PReLU()(layers)
        layers = Conv2D(filters[0], kernel_size=(kernels[0], kernels[0]), activation='linear')(layers)
        layers = PReLU()(layers)

        layers = Conv2D(filters[1], kernel_size=(kernels[1], kernels[1]), activation='linear')(layers)
        layers = PReLU()(layers)
        layers = BatchNormalization(axis=3)(layers)
        layers = Conv2D(filters[1], kernel_size=(kernels[1], kernels[1]), activation='linear')(layers)
        layers = PReLU()(layers)

        layers = Conv2D(filters[2], kernel_size=(kernels[2], kernels[2]), activation='linear')(layers)
        layers = PReLU()(layers)
        layers = Conv2D(filters[2], kernel_size=(kernels[2], kernels[2]), activation='linear')(layers)
        layers = PReLU()(layers)
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

    def train(self, epochs, batchSize, epochSteps, valSteps, trainGen, valGen):

        # Set up generator for data augmentation.

        # Set up callbacks tuple for checkpoints. This will save the model every time validation loss improves.
        callbacks = [ModelCheckpoint(self.model_file, monitor='val_loss', verbose=1, save_best_only=True)]

        # Train the model and save its training progress in a history object.
        #I should definitely be making each dictionary in the history object start at 0 before passing it off for metric
        #logs but I've implemented it the wrong way already, and can't be bothered to fix it right now.
        history = self.model.fit_generator(trainGen, steps_per_epoch=epochSteps, epochs=epochs, verbose=2, validation_data=valGen,
                                           validation_steps=valSteps, callbacks=callbacks)

        self.modelAccuracy = max(history.history["val_acc"])
        K.clear_session()
        return history