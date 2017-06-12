from keras.layers import Input, Add, Dense, Activation, Flatten, Conv2D, MaxPooling2D, ZeroPadding2D, AveragePooling2D, TimeDistributed, BatchNormalization
from keras import backend as K
from keras.models import Model
from util.RoiPoolingConv import RoiPoolingConv
from util import Resnet_Model
from keras.utils.data_utils import get_file
WEIGHTS_PATH_NO_TOP = 'https://github.com/fchollet/deep-learning-models/releases/download/v0.2/resnet50_weights_tf_dim_ordering_tf_kernels_notop.h5'

def build_models(inputImageShape,numROIs):
    weightsPath = get_file('resnet50_weights_tf_dim_ordering_tf_kernels_notop.h5',
                                    WEIGHTS_PATH_NO_TOP,
                                    cache_subdir='models',
                                    md5_hash='a268eb855778b3df3c7506639542a6af')
    ROIInput = Input(shape=(numROIs,4))
    imgInput = Input(shape=inputImageShape)
    resnet = Resnet_Model.ResNet50(include_top=False,input_tensor = imgInput)
    rpnLayers = get_RPN_layers(resnet,9)
    classifier = get_classifier(resnet,ROIInput,numROIs)

    rpnModel = Model(inputs=imgInput,outputs=rpnLayers)
    classifierModel = Model(inputs=[imgInput,ROIInput],outputs=classifier)
    fullModel = Model(inputs=[imgInput,ROIInput],outputs=rpnLayers+classifier)
    fullModel.load_weights(weightsPath,by_name=True)
    #print(fullModel.summary())
    return [rpnModel, classifierModel, fullModel]

def get_RPN_layers(preceedingLayers,numAnchors, n=3, lowerVecDim=256):
    convLayer = Conv2D(lowerVecDim,(n,n),padding='same',activation='relu',name='RPN_conv_layer')(preceedingLayers)
    regressionOutput = (Conv2D(numAnchors*4,(1,1),padding='same',name='RPN_regression_layer'))(convLayer)
    classifierOutput = (Conv2D(numAnchors,(1,1),activation='softmax',padding='same',name='RPN_classification_layer'))(convLayer)
    return [classifierOutput,regressionOutput]


def get_classifier(base,roiInputs, numROIs, classes=21, trainable=False):
    ROIRegions = 14
    inpShape = (numROIs,1024,14,14)
    outROIRegions = RoiPoolingConv(ROIRegions,numROIs)([base,roiInputs])
    model = get_classifier_layers(outROIRegions,input_shape=inpShape,trainable=trainable)
    model = TimeDistributed(Flatten())(model)
    classificationModel = TimeDistributed(Dense(classes,activation='softmax'),name='dense_class_{}'.format(classes))(model)
    regressionModel = TimeDistributed(Dense(4*(classes-1),activation='linear'),name='dense_regression_{}'.format(classes))(model)
    return [classificationModel,regressionModel]
def get_classifier_layers(x, input_shape, trainable=False):
    x = conv_block_td(x, 3, [512, 512, 2048], stage=5, block='atd', input_shape=input_shape, strides=(2, 2), trainable=trainable)
    x = identity_block_td(x, 3, [512, 512, 2048], stage=5, block='btd', trainable=trainable)
    x = identity_block_td(x, 3, [512, 512, 2048], stage=5, block='ctd', trainable=trainable)
    x = TimeDistributed(AveragePooling2D((7, 7)), name='avg_pooltd')(x)

    return x

def identity_block_td(input_tensor, kernel_size, filters, stage, block, trainable=True):

    # identity block time distributed

    nb_filter1, nb_filter2, nb_filter3 = filters
    bn_axis = 1

    conv_name_base = 'tdres' + str(stage) + block + '_branch'
    bn_name_base = 'tdbn' + str(stage) + block + '_branch'

    x = TimeDistributed(Conv2D(nb_filter1, (1, 1), trainable=trainable, kernel_initializer='normal'), name=conv_name_base + '2atd')(input_tensor)
    x = TimeDistributed(BatchNormalization(axis=bn_axis), name=bn_name_base + '2atd')(x)
    x = Activation('relu')(x)

    x = TimeDistributed(Conv2D(nb_filter2, (kernel_size, kernel_size), trainable=trainable, kernel_initializer='normal',padding='same'), name=conv_name_base + '2btd')(x)
    x = TimeDistributed(BatchNormalization(axis=bn_axis), name=bn_name_base + '2btd')(x)
    x = Activation('relu')(x)

    x = TimeDistributed(Conv2D(nb_filter3, (1, 1), trainable=trainable, kernel_initializer='normal'), name=conv_name_base + '2ctd')(x)
    x = TimeDistributed(BatchNormalization(axis=bn_axis), name=bn_name_base + '2ctd')(x)

    x = Add()([x, input_tensor])
    x = Activation('relu')(x)

    return x
def conv_block_td(input_tensor, kernel_size, filters, stage, block, input_shape, strides=(2, 2), trainable=True):

    # conv block time distributed

    nb_filter1, nb_filter2, nb_filter3 = filters
    bn_axis = 1

    conv_name_base = 'tdres' + str(stage) + block + '_branch'
    bn_name_base = 'tdbn' + str(stage) + block + '_branch'

    x = TimeDistributed(Conv2D(nb_filter1, (1, 1), strides=strides, trainable=trainable, kernel_initializer='normal'), input_shape=input_shape, name=conv_name_base + '2atd')(input_tensor)
    x = TimeDistributed(BatchNormalization(axis=bn_axis), name=bn_name_base + '2atd')(x)
    x = Activation('relu')(x)

    x = TimeDistributed(Conv2D(nb_filter2, (kernel_size, kernel_size), padding='same', trainable=trainable, kernel_initializer='normal'), name=conv_name_base + '2btd')(x)
    x = TimeDistributed(BatchNormalization(axis=bn_axis), name=bn_name_base + '2btd')(x)
    x = Activation('relu')(x)

    x = TimeDistributed(Conv2D(nb_filter3, (1, 1), kernel_initializer='normal'), name=conv_name_base + '2c', trainable=trainable)(x)
    x = TimeDistributed(BatchNormalization(axis=bn_axis), name=bn_name_base + '2ctd')(x)

    shortcut = TimeDistributed(Conv2D(nb_filter3, (1, 1), strides=strides, trainable=trainable, kernel_initializer='normal'), name=conv_name_base + '1td')(input_tensor)
    shortcut = TimeDistributed(BatchNormalization(axis=bn_axis), name=bn_name_base + '1td')(shortcut)

    x = Add()([x, shortcut])
    x = Activation('relu')(x)
    return x
[a,b,c] = build_models([3,300,300],100)
