from keras.layers import Input, Add, Dense, Activation, Flatten, Conv2D, MaxPooling2D, ZeroPadding2D, AveragePooling2D, TimeDistributed
from util.Working_Batch_Normalization import FixedBatchNormalization as BatchNormalization
from keras import backend as K
from keras.models import Model
from util.RoiPoolingConv import RoiPoolingConv
from util import Base, Resnet_Model
from keras.utils.data_utils import get_file
from keras.utils import layer_utils
WEIGHTS_PATH_NO_TOP = 'https://github.com/fchollet/deep-learning-models/releases/download/v0.2/resnet50_weights_tf_dim_ordering_tf_kernels_notop.h5'

def build_models(inputImageShape,numROIs,numAnchors,numClasses):
    weights_path = get_file('resnet50_weights_tf_dim_ordering_tf_kernels_notop.h5',WEIGHTS_PATH_NO_TOP,cache_subdir='models',md5_hash='a268eb855778b3df3c7506639542a6af')
    ROIInput = Input(shape=(numROIs,4))
    imgInput = Input(shape=(3,None,None))
    resnet = Resnet_Model.ResNet50(include_top = False, input_tensor = imgInput)
    rpnLayers = get_RPN_layers(resnet,numAnchors)
    classifier = get_classifier(resnet,ROIInput,numROIs,nb_classes=numClasses,trainable=True)

    rpnModel = Model(inputs=imgInput,outputs=rpnLayers[:2])
    classifierModel = Model(inputs=[imgInput,ROIInput],outputs=classifier)
    fullModel = Model(inputs=[imgInput,ROIInput],outputs=rpnLayers[:2]+classifier)
    #rpnModel.load_weights(weights_path,by_name=True)
    #classifierModel.load_weights(weights_path,by_name=True)
    #layer_utils.convert_all_kernels_in_model(rpnModel)
    #layer_utils.convert_all_kernels_in_model(classifierModel)
    return [rpnModel, classifierModel, fullModel]

def get_RPN_layers(base_layers,num_anchors):

    x = Conv2D(512, (3, 3), padding='same', activation='relu', kernel_initializer='normal', name='rpn_conv1')(base_layers)

    x_class = Conv2D(num_anchors, (1, 1), activation='sigmoid', kernel_initializer='uniform', name='rpn_out_class')(x)
    x_regr = Conv2D(num_anchors * 4, (1, 1), activation='linear', kernel_initializer='zero', name='rpn_out_regress')(x)

    return [x_class, x_regr, base_layers]


def get_classifier(base_layers, input_rois, num_rois, nb_classes = 10, trainable=False):
    pooling_regions = 7
    input_shape = (num_rois,1024,7,7)
    print("Creating classifier model with",nb_classes,"classes.")
    out_roi_pool = RoiPoolingConv(pooling_regions, num_rois)([base_layers, input_rois])
    out = get_classifier_layers(out_roi_pool, input_shape=input_shape, trainable=True)

    out = TimeDistributed(Flatten())(out)

    out_class = TimeDistributed(Dense(nb_classes, activation='softmax', kernel_initializer='zero'), name='dense_class_{}'.format(nb_classes))(out)
    # note: no regression target for bg class
    out_regr = TimeDistributed(Dense(4 * (nb_classes-1), activation='linear', kernel_initializer='zero'), name='dense_regress_{}'.format(nb_classes))(out)
    return [out_class, out_regr]
def get_classifier_layers(x, input_shape, trainable=False):
    x = conv_block_td(x, 3, [512, 512, 2048], stage=5, block='a', input_shape=input_shape, strides=(1, 1), trainable=trainable)
    x = identity_block_td(x, 3, [512, 512, 2048], stage=5, block='b', trainable=trainable)
    x = identity_block_td(x, 3, [512, 512, 2048], stage=5, block='c', trainable=trainable)
    x = TimeDistributed(AveragePooling2D((7, 7)), name='avg_poolb')(x)

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
