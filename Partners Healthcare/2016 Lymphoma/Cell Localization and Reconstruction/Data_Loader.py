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

import os
import cv2
import numpy as np
import keras

def load_data():
    """
    Function to load and process all available training images.
    :return: Training set, Validation set, shape of each sample, & number of classes.
    """
    
    #Get images sized to 64x64x1 and fill xTrain, yTrain tuples with them.
    cells,nonCells = get_images(64,64)
    if len(cells) > len(nonCells):
        cells = trim_random(cells,len(nonCells))
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
    xTrain,yTrain,xVal,yVal = split(xTrain,yTrain)
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
    xTrain = np.divide(xTrain, np.power(2,16)-1)
    xVal = np.divide(xVal, np.power(2,16)-1)
    
    print('xTrain shape:', xTrain.shape)
    print(xTrain.shape[0], 'train samples')
    print(xVal.shape[0], 'test samples')

    #Convert class vectors to binary class matrices.
    yTrain = keras.utils.to_categorical(yTrain, numClasses)
    yVal = keras.utils.to_categorical(yVal, numClasses)
    return (xTrain, yTrain), (xVal, yVal), inputShape, numClasses

def get_images(w,h):
    """
    Function to load, resize and return all available images in our training set.
    :param w: Desired image width.
    :param h: Desired image height.
    :return:
    """

    #File paths for training data.
    nonCellPath = "training_data/background/negative"
    cellPath = "training_data/total/cells"
    cells = []
    nonCells = []

    #For every available cell image.
    for filename in os.listdir(cellPath):
        try:
            #Read grayscale, resize to (w,h) and append to cell list
            img = cv2.imread(''.join([cellPath,"/",filename]),cv2.IMREAD_ANYDEPTH)
            img = cv2.resize(img,(w,h),cv2.INTER_CUBIC)
            cells.append(img)
        except:
            #If an exception is thrown for any reason, we just want to skip over the bad file in the directory.
            continue
    #For every available non-cell image.
    for filename in os.listdir(nonCellPath):
        try:
            #Read grayscale, resize to (w,h) and append to non-cell list.
            img = cv2.imread(''.join([nonCellPath, "/", filename]),cv2.IMREAD_ANYDEPTH)
            img = cv2.resize(img, (w, h), cv2.INTER_CUBIC)
            nonCells.append(img)
        except:
            #If an exception is thrown for any reason, we just want to skip over the bad file in the directory.
            continue
    return cells,nonCells

def split(x,y,splitRatio=1/3):
    """
    Function to split training data into training and validation data sets.
    :param x: Tuple containing all x training data.
    :param y: Tuple containing all y training data.
    :param splitRatio: Ratio of validation data to training data.
    :return: nx & ny are the training sets, while xv & yv are the validation sets.
    """
    val = int(round(len(y)*splitRatio))
    xv = x[:val]
    yv = y[:val]
    nx = x[val:]
    ny = y[val:]
    return nx,ny,xv,yv

def shuffle(x,y):
    """
    Function to shuffle two tuples of equal length.
    :param x: First tuple.
    :param y: Second tuple.
    :return: Both newly shuffled arrays as numpy arrays.
    """
    print("Shuffling training set...")
    #Create new tuples that will contain shuffled data.
    nx = []
    ny = []

    indicesToSelect = [i for i in range(len(y))]
    while(len(indicesToSelect)>0):
        idx = np.random.randint(0,len(indicesToSelect))
        val = indicesToSelect[idx]
        nx.append(x[val])
        ny.append(y[val])
        indicesToSelect.pop(idx)
    return np.asarray(nx),np.asarray(ny)

def trim_random(x, l):
    nx = []
    indices = [i for i in range(len(x))]
    for i in range(0,l):
        idx = np.random.randint(0,len(indices))
        nx.append(x[indices[idx]])
        indices.pop(idx)
    return np.asarray(nx)