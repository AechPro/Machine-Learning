# -*- coding: utf-8 -*-
'''ResNet50 model for Keras.
# Reference:
- [Deep Residual Learning for Image Recognition](https://arxiv.org/abs/1512.03385)
Adapted from code contributed by BigMoyan.
'''
from __future__ import print_function

from keras.utils.data_utils import get_file
from keras.layers import Input
from keras import layers
from keras.layers import Dense
from keras.layers import Flatten
from keras.callbacks import ModelCheckpoint
from keras.layers import Conv2D, Dropout
from keras.layers import MaxPooling2D,PReLU
from keras.layers import ZeroPadding2D
from keras.layers import AveragePooling2D
from util.Working_Batch_Normalization import FixedBatchNormalization as BatchNormalization
from keras.models import Model
from keras.optimizers import Adam, Adadelta
import os
import keras.backend as K
WEIGHTS_PATH_NO_TOP = 'https://github.com/fchollet/deep-learning-models/releases/download/v0.2/resnet50_weights_tf_dim_ordering_tf_kernels_notop.h5'

class NN_Model(object):
    def __init__(self, modelNumber):
        if not os.path.exists("data/model_{}".format(modelNumber)):
            os.makedirs("data/model_{}".format(modelNumber))
        self.modelNumber = modelNumber
        self.model_file = "data/model_{}/resnet_model_{}.h5".format(modelNumber, modelNumber)
        self.inputShape = None
        self.numClasses = None
    def identity_block(self, input_tensor, kernel_size, filters, stage, block, trainable=True):
        """The identity block is the block that has no conv layer at shortcut.
        # Arguments
            input_tensor: input tensor
            kernel_size: defualt 3, the kernel size of middle conv layer at main path
            filters: list of integers, the filterss of 3 conv layer at main path
            stage: integer, current stage label, used for generating layer names
            block: 'a','b'..., current block label, used for generating layer names
        # Returns
            Output tensor for the block.
        """
        filters1, filters2, filters3 = filters
        if K.image_data_format() == 'channels_last':
            bn_axis = 3
        else:
            bn_axis = 1
        conv_name_base = 'res' + str(stage) + block + '_branch'
        bn_name_base = 'bn' + str(stage) + block + '_branch'
    
        x = Conv2D(filters1, (1, 1), name=conv_name_base + '2a',trainable=trainable)(input_tensor)
        x = BatchNormalization(axis=bn_axis, name=bn_name_base + '2a')(x)
        x = PReLU()(x)
    
        x = Conv2D(filters2, kernel_size, padding='same', name=conv_name_base + '2b', trainable=trainable)(x)
        x = BatchNormalization(axis=bn_axis, name=bn_name_base + '2b')(x)
        x = PReLU()(x)
    
        x = Conv2D(filters3, (1, 1), name=conv_name_base + '2c', trainable=trainable)(x)
        x = BatchNormalization(axis=bn_axis, name=bn_name_base + '2c')(x)
    
        x = layers.add([x, input_tensor])
        x = PReLU()(x)
        return x
    
    
    def conv_block(self, input_tensor, kernel_size, filters, stage, block, strides=(2, 2),trainable=True):
        """conv_block is the block that has a conv layer at shortcut
        # Arguments
            input_tensor: input tensor
            kernel_size: defualt 3, the kernel size of middle conv layer at main path
            filters: list of integers, the filterss of 3 conv layer at main path
            stage: integer, current stage label, used for generating layer names
            block: 'a','b'..., current block label, used for generating layer names
        # Returns
            Output tensor for the block.
        Note that from stage 3, the first conv layer at main path is with strides=(2,2)
        And the shortcut should have strides=(2,2) as well
        """
        filters1, filters2, filters3 = filters
        if K.image_data_format() == 'channels_last':
            bn_axis = 3
        else:
            bn_axis = 1
        conv_name_base = 'res' + str(stage) + block + '_branch'
        bn_name_base = 'bn' + str(stage) + block + '_branch'
    
        x = Conv2D(filters1, (1, 1), strides=strides, name=conv_name_base + '2a', trainable=trainable)(input_tensor)
        x = BatchNormalization(axis=bn_axis, name=bn_name_base + '2a')(x)
        x = PReLU()(x)
    
        x = Conv2D(filters2, kernel_size, padding='same', name=conv_name_base + '2b', trainable=trainable)(x)
        x = BatchNormalization(axis=bn_axis, name=bn_name_base + '2b')(x)
        x = PReLU()(x)
    
        x = Conv2D(filters3, (1, 1), name=conv_name_base + '2c', trainable=trainable)(x)
        x = BatchNormalization(axis=bn_axis, name=bn_name_base + '2c')(x)
    
        shortcut = Conv2D(filters3, (1, 1), strides=strides, name=conv_name_base + '1', trainable=trainable)(input_tensor)
        shortcut = BatchNormalization(axis=bn_axis, name=bn_name_base + '1')(shortcut)
    
        x = layers.add([x, shortcut])
        x = PReLU()(x)
        return x
    
    
    def build_model(self, inputShape, numClasses):
        print("Building model...")
        self.inputShape = inputShape
        self.numClasses = numClasses
        trainable = True

        inp = Input(shape=inputShape)
        bn_axis=1
        img_input = inp
        x = ZeroPadding2D((3, 3))(img_input)
        x = Conv2D(64, (7, 7), strides=(2, 2), name='conv1')(x)
        x = BatchNormalization(axis=bn_axis, name='bn_conv1')(x)
        x = PReLU()(x)
        x = MaxPooling2D((3, 3), strides=(2, 2))(x)

        x = self.conv_block(x, 3, [64, 64, 256], stage=2, block='a', strides=(1, 1), trainable=trainable)
        #x = Dropout(0.2)(x)
        x = self.identity_block(x, 3, [64, 64, 256], stage=2, block='b', trainable=trainable)
        #x = Dropout(0.2)(x)
        x = self.identity_block(x, 3, [64, 64, 256], stage=2, block='c', trainable=trainable)
        #x = Dropout(0.2)(x)
        x = self.conv_block(x, 3, [128, 128, 512], stage=3, block='a', trainable=trainable)
        #x = Dropout(0.2)(x)
        x = self.identity_block(x, 3, [128, 128, 512], stage=3, block='b', trainable=trainable)
        #x = Dropout(0.2)(x)
        x = self.identity_block(x, 3, [128, 128, 512], stage=3, block='c', trainable=trainable)
        #x = Dropout(0.2)(x)
        x = self.identity_block(x, 3, [128, 128, 512], stage=3, block='d', trainable=trainable)

        x = self.conv_block(x, 3, [256, 256, 1024], stage=4, block='a', trainable=trainable)
        #x = Dropout(0.25)(x)
        x = self.identity_block(x, 3, [256, 256, 1024], stage=4, block='b', trainable=trainable)
        #x = Dropout(0.25)(x)
        x = self.identity_block(x, 3, [256, 256, 1024], stage=4, block='c', trainable=trainable)
        #x = Dropout(0.25)(x)
        x = self.identity_block(x, 3, [256, 256, 1024], stage=4, block='d', trainable=trainable)
        #x = Dropout(0.25)(x)
        x = self.identity_block(x, 3, [256, 256, 1024], stage=4, block='e', trainable=trainable)
        x = Dropout(0.25)(x)
        x = self.identity_block(x, 3, [256, 256, 1024], stage=4, block='f', trainable=trainable)
        x = Dropout(0.25)(x)

        x = self.conv_block(x, 1, [512, 512, 2048], stage=5, block='a')
        x = Dropout(0.25)(x)
        x = self.identity_block(x, 1, [512, 512, 2048], stage=5, block='b')
        x = Dropout(0.25)(x)
        x = self.identity_block(x, 1, [512, 512, 2048], stage=5, block='c')
        x = AveragePooling2D(name='avg_pool')(x)
        #x = GlobalAveragePooling2D()(x)

        output = Flatten()(x)
        output = Dropout(0.5)(output)
        output = Dense(256,activation='sigmoid')(output)
        output = Dropout(0.5)(output)
        output = Dense(numClasses,activation='softmax')(output)
        model = Model(inputs=inp,outputs=output)
        model.compile(optimizer=Adadelta(), loss='categorical_crossentropy', metrics=['accuracy'])
        self.model = model
        print(model.summary())

    def build_for_fine_tuning(self,inputShape, numClasses):
        self.inputShape = inputShape
        self.numClasses = numClasses
        trainable = False

        inp = Input(shape=inputShape)
        bn_axis = 1
        img_input = inp
        print(inp._keras_shape)
        x = Conv2D(64, (7, 7), strides=(2, 2),data_format="channels_last", name='conv1')(img_input)
        print(x._keras_shape)
        x = BatchNormalization(axis=bn_axis, name='bn_conv1')(x)
        print(x._keras_shape)
        x = PReLU()(x)
        print(x._keras_shape)
        x = MaxPooling2D((3, 3), strides=(2, 2))(x)
        print(x._keras_shape)
        x = self.conv_block(x, 3, [64, 64, 256], stage=2, block='a', strides=(1, 1), trainable=trainable)
        x = self.identity_block(x, 3, [64, 64, 256], stage=2, block='b', trainable=trainable)
        x = self.identity_block(x, 3, [64, 64, 256], stage=2, block='c')
        output = Flatten()(x)
        #output = Dropout(0.45)(output)
        output = Dense(256, activation='sigmoid')(output)
        #output = Dropout(0.45)(output)
        output = Dense(numClasses, activation='softmax')(output)
        model = Model(inputs=inp, outputs=output)
        weights_path = get_file('resnet50_weights_tf_dim_ordering_tf_kernels_notop.h5',
                                WEIGHTS_PATH_NO_TOP,
                                cache_subdir='models',
                                md5_hash='a268eb855778b3df3c7506639542a6af')
        model.load_weights(weights_path,by_name=True)
        model.compile(optimizer=Adadelta(), loss='categorical_crossentropy', metrics=['accuracy'])
        self.model = model
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
    
