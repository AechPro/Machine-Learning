import os
import cv2
import numpy as np
import keras
def load_data():
    cells,nonCells = get_images(64,64)
    xTrain = []
    yTrain = []
    for cell in cells:
        xTrain.append(cell)
        yTrain.append(1)
    for im in nonCells:
        xTrain.append(im)
        yTrain.append(0)
    xTrain,yTrain = shuffle(xTrain,yTrain)
    xTrain,yTrain,xTest,yTest=split(xTrain,yTrain)
    xTrain = np.asarray(xTrain)
    yTrain = np.asarray(yTrain)
    xTest = np.asarray(xTest)
    yTest = np.asarray(yTest)
    rows, cols = xTrain.shape[1:]
    numClasses = 2
    xTrain = xTrain.reshape(xTrain.shape[0], rows, cols, 1)
    xTest = xTest.reshape(xTest.shape[0], rows, cols, 1)
    inputShape = (rows, cols, 1)

    xTrain = xTrain.astype('float32')
    xTest = xTest.astype('float32')
    mean = np.mean(xTrain)
    print("CHANNEL-WISE IMAGE MEAN: ",mean)
    xTrain -= mean
    xTest -= mean
    xTrain /= 255
    xTest /= 255
    
    print('xTrain shape:', xTrain.shape)
    print(xTrain.shape[0], 'train samples')
    print(xTest.shape[0], 'test samples')

    # convert class vectors to binary class matrices
    yTrain = keras.utils.to_categorical(yTrain, numClasses)
    yTest = keras.utils.to_categorical(yTest, numClasses)
    return (xTrain, yTrain), (xTest, yTest), inputShape, numClasses
def get_images(w,h):
    cellPath = "../../resources/data/images/training_data/blobs/cells"
    nonCellPath = "../../resources/data/images/training_data/blobs"
    cells = []
    nonCells = []
    for filename in os.listdir(cellPath):
        try:
            img = cv2.imread(''.join([cellPath,"/",filename]),cv2.IMREAD_GRAYSCALE)
            img = cv2.resize(img, (w,h), interpolation=cv2.INTER_CUBIC)
            cells.append(img)
        except:
            continue
    for filename in os.listdir(nonCellPath):
        try:
            img = cv2.imread(''.join([nonCellPath,"/",filename]),cv2.IMREAD_GRAYSCALE)
            img = cv2.resize(img, (w,h), interpolation=cv2.INTER_CUBIC)
            nonCells.append(img)
        except:
            continue
    return cells,nonCells
def split(x,y,splitRatio=0.33):
    val = int(len(y)*splitRatio)
    xv = x[:val]
    yv = y[:val]
    nx = x[val:]
    ny = y[val:]
    return nx,ny,xv,yv
def shuffle(x,y):
    nx = []
    ny = []
    usedVals = []
    val = 0
    for i in range(len(y)):
        while val in usedVals:
            val = np.random.randint(0,len(y))
        usedVals.append(val)
        nx.append(x[val])
        ny.append(y[val])
    return nx,ny
