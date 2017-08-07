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

import numpy as np
import cv2
import recon
import Region_Detector as detector
import os

#Reconstruction object instantiation for later use.
rec = recon.Reconstruction()

"""
    Function to calculate diagnostic data inside a reconstructed region.
    @param region: The region to be used for calculations.
    @param mask: The object support mask that was used to reconstruct this image.
"""
def get_diagnostic_data(region,mask):
    #MSER parameters to detect cells in region.
    params = [5, 120, 500, 1, 0.2, 200, 5, 0.003, 5]
    bboxes = []
    #Run MSER algorithm on image and save boundary box data for each contour it finds.
    hulls = detector.MSER_blobs(region, params)

    #For every contour detected.
    for cnt in hulls:
        #Get minimum size bounding rectangle for contour.
        x, y, w, h = cv2.boundingRect(cnt)

        #Find what percentage of width that height is, or vice versa.
        per = w/h
        if(w>h):
            per = h/w

        #If width and height are within 20% of each other.
        if(per>=0.8):

            #Add found rectangle to boundary box list.
            bboxes.append([x, y, x + w, y + h])

    # Perform non-max suppression on boundary boxes with overlap threshold = 0.2.
    bboxes = np.asarray(bboxes)
    bboxes = detector.non_max_suppression_fast(bboxes, 0.2)

    #Create a BGR copy of the region to display boundary boxes on.
    vis = region.copy()
    vis = cv2.cvtColor(vis,cv2.COLOR_GRAY2BGR)

    #For all boundary boxes.
    for box in bboxes:
        x1, y1, x2, y2 = box

        #Draw boundary box on visualization image.
        cv2.rectangle(vis, (x1, y1), (x2, y2), (0, 255, 0), 1)
    return vis, len(bboxes)

"""
    Function to normalize a hologram image given a reference image.
    @param image: The image to be normalized by the reference.
    @param ref: The reference image.
"""
def normalize(image,ref):
    #Calculate the normalization factor for the image.
    normFactor = np.mean(ref) / np.multiply(np.mean(image), ref)

    #Normalize the image.
    norm = np.multiply(image, normFactor)
    return norm


"""Function to load and normalize all hologram images in a directory"""
def load_images():
    #Directory from which to load the hologram images.
    defaultDirectory = "../../resources/data/images/originals"

    #Filepath of the reference image.
    refDir = ''.join([defaultDirectory,"/reference_image.png"])

    #Create output tuples.
    images = []
    filenames = []
    normalizedImages = []

    #Load reference image.
    ref = cv2.imread(refDir,0)
    ref = ref.astype('float32')

    #For all files in the directory.
    for filename in os.listdir(defaultDirectory):

        #If the file is an acceptable image.
        if ".png" in filename or ".jpg" in filename:
            filenames.append(filename)

            #Load the image.
            img = cv2.imread(''.join([defaultDirectory,"/",filename]),0)
            img = img.astype('float32')
            images.append(img)

            #Normalize the image.
            norm = normalize(img,ref)
            normalizedImages.append(norm)
    return images,normalizedImages,filenames


#Testing code.
images, normalImages, names = load_images()
filepath = "test/"
confirmationName = "cell"
nonConfirmationName = "blob"
cell = 1664
bg = 559
itr = 0
for img, norm, name in zip(images,normalImages,names):
    if itr < 2:
        itr += 1
        continue
    print("Processing image: ",name)
    normalizedRegions, regions = detector.get_regions(norm, img, imName="Detected Image.png", classify=True)
    numCells = 0
    directory = ''.join(["blobs/",name[:name.rfind(".png")]])
    if not os.path.exists(directory):
        os.makedirs(directory)

    for normalizedRegion, region in zip(normalizedRegions, regions):

        reconRegion, mask = rec.compute(normalizedRegion)
        mask = mask.astype('uint8')
        regionMag = np.abs(reconRegion)
        regionMag *= 255./np.max(regionMag)
        regionMag = regionMag.astype('uint8')
        #blob,cells = get_diagnostic_data(regionMag,mask)

        cv2.imshow("blob", regionMag)
        key = cv2.waitKey(0)
        cv2.destroyAllWindows()
        regionToSave = region.copy()
        regionToSave *=255.
        regionToSave += 111.385
        regionToSave = regionToSave.astype('uint8')
        if key == 101:
            print("Saving cell",cell)
            cv2.imwrite(''.join([filepath, "cells/", confirmationName, "_{}.png"]).format(cell), regionToSave)
            cell += 1
        else:
            cv2.imwrite(''.join([filepath, nonConfirmationName, "_{}.png"]).format(bg), regionToSave)
            bg += 1

    print("CELLS FOR",name,"| ",numCells)
