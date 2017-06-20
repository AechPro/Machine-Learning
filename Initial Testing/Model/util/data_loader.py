from util import XML_Parser as parser
from util import Config
import os
import numpy as np
from util import Image_Processor as processor
from keras import backend as K
from keras.layers import Input
def load_data(filePath, C, validation_split=0.0):
    imageData = []
    classCount = {}
    classMap = {}
    for fileName in os.listdir(filePath):
        im, classMap, classCount = parser.load_file(''.join([filePath,"/",fileName]),classMap=classMap,classCount=classCount)
        imageData.append(im)
    if 'bg' not in classCount:
        classCount['bg'] = 0
        classMap['bg'] = len(classMap)
    C.class_mapping = classMap

    np.random.shuffle(imageData)
    trainSet = []
    valSet = []
    for data in imageData:
        val = np.random.random_sample()
        if val>validation_split:
            trainSet.append(data)
        else:
            valSet.append(data)
    print("Training samples:",len(trainSet))
    print("Validation samples:",len(valSet))
    trainIter = processor.get_anchor_gt(trainSet,classCount,C)
    valIter = processor.get_anchor_gt(valSet,classCount,C)
    inputImageShape = (3,None,None)
    return trainIter, valIter, inputImageShape, imageData, classCount, classMap
