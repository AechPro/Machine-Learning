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
import time
from copy import copy
import cv2
import numpy as np
import os
import Region_Filter as rFilter


def extract_regions(sourceImg, reference, params, imName, cellName="a0", flter=None, show=False, classify=False):
    """
    Function to extract cells from a normalized image taken through holographic microscopy.
    :param sourceImg: The hologram image.
    :param reference: The reference image taken with no sample.
    :param params: A tuple containing the parameters for the MSER blob detection algorithm.
    :param imName: The name to use when saving the image after cells have been detected.
    :param cellName: The beginning of the naming convention for saving individual regions. Should be left as is unless
                     picking up from a previous detection process.
    :param flter: An instance of a Convolutional Neural Network that has been trained to classify small regions
                  as cells or not cells.
    :param show: Flag for displaying the image in a window after cell extraction.
    :param classify: Flag to decide whether or not the CNN filter should be used on the regions.
    :return: The detected regions & cells as tuples, as well as the amount of time this function took to complete.
    """

    t1 = time.time()
    #Set both input images to the 32-bit floating point data type.
    sourceImg = sourceImg.astype('float32')
    reference = reference.astype('float32')

    #Create a normalized image from the two input images.
    img = np.divide(sourceImg, reference)

    #Transform the normalized image into the 16-bit number space.
    img *= np.power(2, 16) - 1
    img = img.astype('uint16')

    vis = None
    if show or classify:
        #Copy of input hologram for display purposes.
        vis = sourceImg.copy()

        #Convert the copy to 8-bit.
        vis = np.divide(vis, 256)
        vis = vis.astype('uint8')

        #Convert the copy to BGR format.
        vis = cv2.cvtColor(vis, cv2.COLOR_GRAY2BGR)

    if classify and flter is None:
        #Create a filter CNN if none has been passed and classification is necessary.
        print("Creating filter.")
        flter = rFilter.filter_CNN()

    bboxes = []
    print("Detecting contours...")
    #Run the MSER algorithm on the input hologram and show the detected contours if requested.
    if show:
        hulls = MSER_blobs(img, params, display=vis)
    else:
        hulls = MSER_blobs(img,params)

    #For all contours detected by the MSER algorithm.
    for cnt in hulls:
        #Find and save the minimally sized boundary box around each contour.
        x, y, w, h = cv2.boundingRect(cnt)
        bboxes.append([x, y, x + w, y + h])

    print("Suppressing",len(bboxes),"boxes...")
    #Perform non-max suppression on boundary boxes with overlap threshold = 0.2.
    bboxes = np.asarray(bboxes)
    bboxes = non_max_suppression_fast(bboxes, 0.2)

    #Expand is the number of pixels in width and height the box will increase before cropping.
    regions = []
    cells = []
    name = cellName
    print("Classifying regions...")

    #For each boundary box.
    for box in bboxes:
        cellDict = {}
        regionDict = {}
        name = name_region(name)

        #Unpack and reshape the box to be 64x64x1.
        x1, y1, x2, y2, cx, cy = reshapeBox(box, (64, 64), sourceImg.shape)

        regionDict["coordinates"] = [cx, cy]
        regionDict["pixels"] = np.asarray(sourceImg[y1:y2, x1:x2])
        regionDict["ref"] = reference[y1:y2, x1:x2]
        regionDict["name"] = name
        regions.append(regionDict)

        #Map boundary box coordinates back to the original input hologram.
        region = sourceImg[y1:y2, x1:x2]

        if classify:
            if flter.classify(region):

                #Save data to cell dictionary.
                cellDict["pixels"] = np.asarray(sourceImg[y1:y2, x1:x2])
                cellDict["coordinates"] = [cx, cy]
                cellDict["ref"] = reference[y1:y2, x1:x2]
                cellDict["name"] = name

                #Add this dictionary to the list of cells we've found so far.
                cells.append(cellDict)

                #Draw bounding box and center of bounding box on to vis image.
                cv2.rectangle(vis, (x1, y1), (x2, y2), (0, 255, 0), 1)
                cv2.rectangle(vis, (cx, cy), (cx, cy), (0, 0, 255), 5)

        elif show:
            #If classification is not requested, draw bounding boxes and do not save them to the cell dictionary.
            cv2.rectangle(vis, (x1, y1), (x2, y2), (0, 255, 0), 1)
            cv2.rectangle(vis, (cx, cy), (cx, cy), (0, 0, 255), 5)

    print("regions:", len(regions), "cells:", len(cells), "extraction time:", time.time() - t1, "seconds.")

    if show:
        #Save the visualization image.
        cv2.imwrite(imName, vis)

        #Show the image in 1080p and wait for user input if requested.
        cv2.resize(vis,(1920,1080),cv2.INTER_CUBIC)
        cv2.imshow("Bounding Box Data", vis)
        cv2.waitKey(0)
        cv2.destroyAllWindows()

    #If displaying is not requested, save the input hologram with classified regions shown.
    elif classify:
        cv2.imwrite(imName, vis)

    return np.asarray(regions), np.asarray(cells), time.time()-t1

def get_regions(sourceImage, reference, cellName="a0", imName="test.png", flter=None, show=False, classify=False):
    """
    Wrapper function for extract_regions that uses a pre-determined parameter set for the MSER algorithm.    :param sourceImg: The hologram image.
    :param reference: The reference image taken with no sample.
    :param params: Optional tuple containing the parameters for the MSER blob detection algorithm.
    :param imName: The name to use when saving the image after cells have been detected.
    :param cellName: The beginning of the naming convention for saving individual regions. Should be left as is unless
                     picking up from a previous detection process.
    :param flter: An instance of a Convolutional Neural Network that has been trained to classify small regions
                  as cells or not cells.
    :param show: Flag for displaying the image in a window after cell extraction.
    :param classify: Flag to decide whether or not the CNN filter should be used on the regions.
    :return: The detected regions & cells as tuples, as well as the amount of time this function took to complete.
    """

    #Parameter set for MSER algorithm. These parameters were found via numerical optimization.
    params = [11, 68, 236, 0.46394341374428594, 0.20434550448034017, 200, 1, 0.32948156997486944, 5]
    regions, cells, calcTime = extract_regions(sourceImage, reference, params, imName, cellName=cellName, flter=flter,
                                               show=show, classify=classify)
    return regions, cells, calcTime

def reshapeBox(box, shape, boundaryShape):
    """
    Function to reshape a box defined as (x1,y1,x2,y2) to a desired shape within the boundaries of an image.
    :param box: The box to reshape, must be a tuple or list in the format (x1,y2,x2,y2).
    :param shape: A tuple or list containing the desired shape of the box, I.E. (64,64).
    :param boundaryShape: The shape of the matrix that contains the box to be reshaped, an image in this case.
    :return: Coordinates of the upper left, lower right, and center of the newly shaped bounding box.
    """

    #Unpack the box.
    x1, y1, x2, y2 = box
    #h & w are the maximum x,y coordinates that the box is allowed to attain.
    h, w = boundaryShape
    #dw & dh are the desired width and height of the box.
    dw, dh = shape

    #Calculated the amount that the dimensions of the box will need to be changed
    heightExpansion = dh - abs(y1 - y2)
    widthExpansion = dw - abs(x1 - x2)

    #Force y1 to be the smallest of the y values. Shift y1 by half of the necessary expansion. Bound y1 to a minimum of 0.
    y1 = max(int(round(min(y1, y2) - heightExpansion / 2.)), 0)

    #Force y2 to be the largest of the y values. Shift y2 by half the necessary expansion. Bound y2 to a maximum of h.
    y2 = min(int(round(max(y1, y2) + heightExpansion / 2.)), h)

    #These two lines are a repeat of the above y1,y2 calculations but with x and w instead of y and h.
    x1 = max(int(round(min(x1, x2) - widthExpansion / 2.)), 0)
    x2 = min(int(round(max(x1, x2) + widthExpansion / 2.)), w)

    #Calculate the center point of the newly reshaped box, truncated.
    cx = x1 + abs(x1 - x2) // 2
    cy = y1 + abs(y1 - y2) // 2

    return x1, y1, x2, y2, cx, cy


def non_max_suppression_fast(boxes, overlapThresh):
    """
    Fast non-maximum suppression algorithm by Malisiewicz et al.
    :param boxes: Array of boundary boxes to perform non-max suppression on.
    :param overlapThresh: Acceptable overlap threshold. 0<=overlapTresh<=1.
    :return: The remaining boxes.
    """

    #if there are no boxes, return an empty list
    if len(boxes) == 0:
        return []

    #if the bounding boxes are integers, convert them to floats --
    #this is important since we'll be doing a bunch of divisions
    if boxes.dtype.kind == "i":
        boxes = boxes.astype("float")

    #initialize the list of picked indexes
    pick = []

    #grab the coordinates of the bounding boxes
    x1 = boxes[:, 0]
    y1 = boxes[:, 1]
    x2 = boxes[:, 2]
    y2 = boxes[:, 3]

    #compute the area of the bounding boxes and sort the bounding
    #boxes by the bottom-right y-coordinate of the bounding box
    area = (x2 - x1 + 1) * (y2 - y1 + 1)
    idxs = np.argsort(y2)

    #keep looping while some indexes still remain in the indexes
    #list
    while len(idxs) > 0:
        #grab the last index in the indexes list and add the
        #index value to the list of picked indexes
        last = len(idxs) - 1
        i = idxs[last]
        pick.append(i)

        #find the largest (x, y) coordinates for the start of
        #the bounding box and the smallest (x, y) coordinates
        #for the end of the bounding box
        xx1 = np.maximum(x1[i], x1[idxs[:last]])
        yy1 = np.maximum(y1[i], y1[idxs[:last]])
        xx2 = np.minimum(x2[i], x2[idxs[:last]])
        yy2 = np.minimum(y2[i], y2[idxs[:last]])

        #compute the width and height of the bounding box
        w = np.maximum(0, xx2 - xx1 + 1)
        h = np.maximum(0, yy2 - yy1 + 1)

        #compute the ratio of overlap
        overlap = (w * h) / area[idxs[:last]]

        #delete all indexes from the index list that have
        idxs = np.delete(idxs, np.concatenate(([last], np.where(overlap > overlapThresh)[0])))

    #return only the bounding boxes that were picked using the
    #integer data type
    return boxes[pick].astype("int")

def MSER_blobs(img, params, display=None):
    """
    Function to use the MSER algorithm to detect regions of interest in an image.
    :param img: Input image for region detection.
    :param params: Tuple containing all the parameters for the MSER algorithm.
    :param display: Optional image for displaying the contours found by the MSER algorithm.
    :return: Hulls of detected blobs.
    """

    #Unpack MSER parameters and create MSER object with them.
    delta, minArea, maxArea, maxVariation, minDiversity, maxEvolution, areaThreshold, minMargin, edgeBlurSize = params

    mser = cv2.MSER_create(delta, minArea, maxArea, maxVariation, maxEvolution, areaThreshold, minMargin, edgeBlurSize)

    #Use MSER to detect regions within the given image.
    if img.dtype == 'uint16':
        img = np.divide(img, 256)
        img = img.astype('uint8')
    regions, _ = mser.detectRegions(img)

    #Extract the hulls corresponding to the contours found by the MSER algorithm.
    hulls = [cv2.convexHull(p.reshape(-1, 1, 2)) for p in regions]

    #Optionally draw the hulls on a display image.
    if display is not None:
        try:
            display = cv2.cvtColor(display, cv2.COLOR_GRAY2BGR)
        except:
            print("Display image correctly formatted.")
        print(len(hulls))
        cv2.polylines(display, hulls, 1, (0, 65000, 0))
        cv2.imshow("Display Image with MSER hulls", display)
        cv2.waitKey(0)
        cv2.destroyAllWindows()
        cv2.imwrite("MSER Hulls.png", display)
    hulls = np.asarray(hulls)
    return hulls

def name_region(name):
    """
    Function to create a unique name for a cell region. This function is primarily useful
    when trying to generate a new training set.
    :param name: The name at which to start.
    :return: The next name in the naming sequence.
    """

    dict = "abcdefghijklmnopqrstuvwxyz"
    if (name[0] == 'z'):
        name = "a{}".format(int(name[1:]) + 1)
    else:
        nextLetter = dict[dict.find(name[0]) + 1]
        name = "{}{}".format(nextLetter, int(name[1:]))
    return name

def process_folders(workingDirectory):
    """
    Function to loop through all the folders in a given directory and process each hologram
    available in the folders.
    :param workingDirectory: Directory to loop through.
    :return: A list containing the results from each processed image.
    """
    gen = os.walk(workingDirectory)
    dirs = next(gen)[1]
    flter = rFilter.filter_CNN()
    processedImages = []
    for d in dirs:
        gen2 = os.walk(''.join([workingDirectory, '/', d]))
        refPath = ''.join([workingDirectory,'/',d,'/reference_image.png'])
        ref = cv2.imread(refPath,cv2.IMREAD_ANYDEPTH)
        for imgName in next(gen2)[2]:
            if ".png" not in imgName or "reference" in imgName:
                continue
            fullFilePath = ''.join([workingDirectory,'/',d,'/',imgName])
            filePath = fullFilePath[:fullFilePath.rfind(".png")]
            print("PROCESSING",fullFilePath)
            img = cv2.imread(fullFilePath,cv2.IMREAD_ANYDEPTH)
            regions, cells, t = get_regions(img,ref,imName="detected.png",flter=flter,classify=True)
            processedImages.append([regions,cells,t,filePath])
    return processedImages
def process_folder(workingDirectory):
    """
    Function to process all holograms in a given directory.
    :param workingDirectory: Directory to process.
    :return: A list containing the results from each processed image.
    """
    processedImages = []
    flter = rFilter.filter_CNN()
    ref = cv2.imread(''.join([workingDirectory,'/',"reference_image.png"]),cv2.IMREAD_ANYDEPTH)
    for imgName in os.listdir(workingDirectory):
        if ".png" not in imgName or "reference" in imgName:
            continue
        fullFilePath = ''.join([workingDirectory,'/',imgName])
        print("PROCESSING", fullFilePath)
        filePath = fullFilePath[:fullFilePath.rfind(".png")]
        if not os.path.exists(filePath):
            os.makedirs(''.join([filePath,'/docs']))
            os.makedirs(''.join([filePath,'/detected_image']))
        img = cv2.imread(fullFilePath, cv2.IMREAD_ANYDEPTH)
        regions, cells, t = get_regions(img, ref, imName=''.join([filePath,'/detected_image/img.png']),flter=flter,classify=True)
        processedImages.append([regions, cells, t, filePath])
    return processedImages

def save_cell_data(data):
    """
    Function to loop through a list of results from processing hologram images and save the results from processing in
    a text file.
    :param data: List of results from processing the holograms.
    :return: Void.
    """
    for entry in data:
        regions, cells, calcTime, filePath = entry
        with open(''.join([filePath,'/docs/cell_counts.txt']), 'w') as file:
            file.write("Total detected regions = {}"
                       "\nNumber of detected cells = {}\n"
                       "Computation Time = {}".format(len(regions), len(cells),calcTime))
        with open(''.join([filePath,'/docs/cell_coordinates.txt']), 'w') as file:
            for cell in cells:
                file.write(" ( {} {} )\n".format(cell["coordinates"][0], cell["coordinates"][1]))
