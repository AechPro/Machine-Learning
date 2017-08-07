import os
import cv2
import numpy as np
import keras

"""Function to load and process all available training images."""
def load_data():
    
    #Get images sized to 64x64x1 and fill xTrain, yTrain tuples with them.
    cells,nonCells = get_images(64,64)
    xTrain = []
    yTrain = []
    for cell in cells:
        xTrain.append(cell)
        yTrain.append(1)
    for im in nonCells:
        xTrain.append(im)
        yTrain.append(0)

    #Shuffle training set and split for training and validation.
    xTrain,yTrain = shuffle(xTrain,yTrain)
    xTrain,yTrain,xVal,yVal=split(xTrain,yTrain)
    xTrain = np.asarray(xTrain)
    yTrain = np.asarray(yTrain)
    xVal = np.asarray(xVal)
    yVal = np.asarray(yVal)

    rows, cols = xTrain.shape[1:]

    #Magic number for classes because I'm lazy.
    numClasses = 2

    #Reshape x arrays to (batches,rows,cols,channels).
    xTrain = xTrain.reshape(xTrain.shape[0], rows, cols, 1)
    xVal = xVal.reshape(xVal.shape[0], rows, cols, 1)

    #Determine input shape (64x64x1).
    inputShape = (rows, cols, 1)

    xTrain = xTrain.astype('float32')
    xVal = xVal.astype('float32')

    #Calculate channel=wise image mean and subtract it from x arrays.
    mean = np.mean(xTrain)
    print("CHANNEL-WISE IMAGE MEAN: ",mean)
    xTrain -= mean
    xVal -= mean

    #Normalize x arrays to be within 0,1.
    xTrain /= 255
    xVal /= 255
    
    print('xTrain shape:', xTrain.shape)
    print(xTrain.shape[0], 'train samples')
    print(xVal.shape[0], 'test samples')

    #Convert class vectors to binary class matrices.
    yTrain = keras.utils.to_categorical(yTrain, numClasses)
    yVal = keras.utils.to_categorical(yVal, numClasses)
    return (xTrain, yTrain), (xVal, yVal), inputShape, numClasses

"""
    Function to load, resize and return all available images in the training set.
    @param w: Desired image width.
    @param h: Desired image height.
"""
def get_images(w,h):
    #File paths for training data.
    cellPath = "../../resources/data/images/training_data/blobs/cells"
    nonCellPath = "../../resources/data/images/training_data/blobs"
    cellPath = "test/cells"
    nonCellPath = "test"
    cells = []
    nonCells = []

    #For every available cell image.
    for filename in os.listdir(cellPath):
        try:
            #Read grayscale, resize to (w,h) and append to cell list
            img = cv2.imread(''.join([cellPath,"/",filename]),cv2.IMREAD_GRAYSCALE)
            img = cv2.resize(img, (w,h), interpolation=cv2.INTER_CUBIC)
            cells.append(img)
        except:
            #If an exception is thrown for any reason, we just want to skip over the bad file in the directory.
            continue
    #For every available non-cell image.
    for filename in os.listdir(nonCellPath):
        try:
            #Read grayscale, resize to (w,h) and append to non-cell list.
            img = cv2.imread(''.join([nonCellPath,"/",filename]),cv2.IMREAD_GRAYSCALE)
            img = cv2.resize(img, (w,h), interpolation=cv2.INTER_CUBIC)
            nonCells.append(img)
        except:
            #If an exception is thrown for any reason, we just want to skip over the bad file in the directory.
            continue
    return cells,nonCells

"""
    Function to split training data into training and validation data sets.
    @param x: Tuple containing all x training data.
    @param y: Tuple containing all y training data.
    @param splitRatio: Ratio of validation data to training data
"""
def split(x,y,splitRatio=0.33):
    val = int(len(y)*splitRatio)
    xv = x[:val]
    yv = y[:val]
    nx = x[val:]
    ny = y[val:]
    return nx,ny,xv,yv

"""Function to shuffle two tuples of equal length."""
def shuffle(x,y):
    #Create new tuples that will contain shuffled data.
    nx = []
    ny = []

    #Tuple to contain list of used indices.
    usedVals = []

    #Random index value.
    val = 0

    #For all values in the tuples.
    for i in range(len(y)):

        #While the current value has already been selected.
        while val in usedVals:

            #Select a new value.
            val = np.random.randint(0,len(y))

        #Keep track of used values and add newly selected data points to nx and ny tuples.
        usedVals.append(val)
        nx.append(x[val])
        ny.append(y[val])
    return nx,ny
