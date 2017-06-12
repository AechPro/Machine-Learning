from keras.layers import Input
from keras import layers as kerasLayers
from keras.layers import Dense
from keras.layers import Activation
from keras.layers import Flatten
from keras.layers import Conv2D
from keras.layers import MaxPooling2D
from keras.layers import GlobalMaxPooling2D
from keras.layers import ZeroPadding2D
from keras.layers import AveragePooling2D
from keras.layers import GlobalAveragePooling2D
from util.Working_Batch_Normalization import FixedBatchNormalization as BatchNormalization
from keras.models import Model

def conv_block(input_tensor, kernel, filters, conv_name_base, strides=(2,2), trainable=True):
    bn_axis=3
    f1,f2,f3=filters
    bn_name_base = ''.join(["bn_",conv_name_base])
    layers = Conv2D(f1, (1, 1), strides=strides, name=conv_name_base + '2a', trainable=trainable)(input_tensor)
    layers = BatchNormalization(axis=bn_axis, name=bn_name_base + '2a')(layers)
    layers = Activation('relu')(layers)

    layers = Conv2D(f2, kernel, padding='same', name=conv_name_base + '2b', trainable=trainable)(layers)
    layers = BatchNormalization(axis=bn_axis, name=bn_name_base + '2b')(layers)
    layers = Activation('relu')(layers)

    layers = Conv2D(f3, (1, 1), name=conv_name_base + '2c', trainable=trainable)(layers)
    layers = BatchNormalization(axis=bn_axis, name=bn_name_base + '2c')(layers)

    shortcut = Conv2D(f3, (1, 1), strides=strides, name=conv_name_base + '1', trainable=trainable)(input_tensor)
    shortcut = BatchNormalization(axis=bn_axis, name=bn_name_base + '1')(shortcut)

    layers = kerasLayers.add([layers, shortcut])
    layers = Activation('relu')(layers)
    return layers
def identity_block(input_tensor, kernel_size, filters, conv_name_base, trainable=True):
    filters1, filters2, filters3 = filters
    bn_axis = 3
    bn_name_base = ''.join(["bn_",conv_name_base])

    x = Conv2D(filters1, (1, 1), name=conv_name_base + '2a',trainable=trainable)(input_tensor)
    x = BatchNormalization(axis=bn_axis, name=bn_name_base + '2a')(x)
    x = Activation('relu')(x)

    x = Conv2D(filters2, kernel_size, padding='same', name=conv_name_base + '2b', trainable=trainable)(x)
    x = BatchNormalization(axis=bn_axis, name=bn_name_base + '2b')(x)
    x = Activation('relu')(x)

    x = Conv2D(filters3, (1, 1), name=conv_name_base + '2c', trainable=trainable)(x)
    x = BatchNormalization(axis=bn_axis, name=bn_name_base + '2c')(x)

    x = kerasLayers.add([x, input_tensor])
    x = Activation('relu')(x)
    return x

def get_base(input_tensor,trainable=True):
    inp = input_tensor
    layers = ZeroPadding2D((3, 3))(inp)
    
    layers = Conv2D(64, (7, 7), strides=(2,2), name='conv1',trainable=trainable)(layers)
    layers = BatchNormalization(axis=3, name='bn_conv1')(layers)
    layers = Activation('relu')(layers)
    
    layers = MaxPooling2D((3,3),strides=(2,2))(layers)
    layers = conv_block(layers,3,[64,64,256],"conv_block_1_",trainable=trainable)
    layers = identity_block(layers,3,[64,64,256],"identity_block_1_",trainable=trainable)
    layers = AveragePooling2D((7, 7), name='avg_pool')(layers)
    layers = GlobalAveragePooling2D()(layers)
    return layers
