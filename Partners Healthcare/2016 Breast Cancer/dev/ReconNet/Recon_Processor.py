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

import cv2
import numpy as np

from util import recon


def process(region,ref,params):
    wavelength,objSupport,dilationSize,dz = params
    rec = recon.Reconstruction()
    rec.Threshold_objsupp=objSupport
    rec.dilation_size=dilationSize
    rec.Dz=dz
    rec.lmbda=wavelength
    norm = normalize(region,ref)
    debugImages = None
    intensities = None
    for i in range(5):
        reconRegion,mask = rec.compute(norm)
        mask = mask.astype('uint8')
        regionMag = np.abs(reconRegion)
        regionMag *= (np.power(2,16)-1) / np.max(regionMag)
        regionMag = regionMag.astype('uint16')
        debugImages, intensities = get_diagnostic_data(regionMag,mask)
        if len(intensities)>0:
            break
        objSupport-=0.02
        rec.Threshold_objsupp=objSupport
    if len(intensities) == 0:
        intensities.append(0)
    return [debugImages,intensities]

"""
    Function to calculate diagnostic data inside a reconstructed region.
    @param region: The region to be used for calculations.
    @param mask: The object support mask that was used to reconstruct this image.
"""

def get_diagnostic_data(region,mask):
    rCopy = region.copy()
    rCopy = np.divide(rCopy,256)
    rCopy = rCopy.astype('uint8')
    vis = rCopy.copy()
    vis = cv2.cvtColor(vis,cv2.COLOR_GRAY2BGR)
    nMask = np.where(mask>0,[1],[0]).astype('uint8')
    rCopy*=nMask
    rCopy = np.where(rCopy>0,[255],[0]).astype('uint8')
    rCopy = cv2.Canny(rCopy,200,255)
    rCopy = cv2.GaussianBlur(rCopy,(1,1),7)

    # Setup SimpleBlobDetector parameters.
    params = cv2.SimpleBlobDetector_Params()

    # Change thresholds
    params.minThreshold = 0
    params.maxThreshold = 255

    # Filter by Area.
    params.filterByArea = True
    params.minArea = 400
    params.maxArea = 2300

    # Filter by Circularity
    params.filterByCircularity = True
    params.minCircularity = 0.5

    # Filter by Convexity
    params.filterByConvexity = True
    params.minConvexity = 0.1

    # Filter by Inertia
    params.filterByInertia = True
    params.minInertiaRatio = 0.01
    detector = cv2.SimpleBlobDetector_create(params)

    # Detect blobs.
    keypoints = detector.detect(rCopy)

    # Draw detected blobs as red circles.
    # cv2.DRAW_MATCHES_FLAGS_DRAW_RICH_KEYPOINTS ensures the size of the circle corresponds to the size of blob
    #vis = cv2.drawKeypoints(vis, keypoints, np.array([]), (0, 255, 0),cv2.DRAW_MATCHES_FLAGS_DRAW_RICH_KEYPOINTS)
    intensities = []
    for keypoint in keypoints:
        cy = int(round(keypoint.pt[1]))
        cx = int(round(keypoint.pt[0]))

        rad = int(round(keypoint.size / 2.))
        rad = int(round(rad*0.75))
        w, h = region.shape

        y, x = np.ogrid[-cx:w - cx, -cy:h - cy]
        mask = x ** 2 + y ** 2 <= rad ** 2
        vis = cv2.circle(vis,(cx,cy),rad,(0,255,0),1)

        colorIntensity = ((np.power(2,16)-1) - np.average(region[mask])) / (np.power(2,16)-1)
        if colorIntensity == np.nan or colorIntensity == np.inf:
            continue
        """The intensity value of each cell is found by first taking the average pixel value of each pixel 
        inside the cell region. This value is then subtracted from 255 so that the final intensity value 
        will increase with the darkness of the cell. Finally, the intensity value is divided by the maximum
        value a pixel can attain, which normalizes the intensity value to be between 0 and 1."""
        intensities.append(colorIntensity)
    debug = [vis,rCopy]
    return debug, intensities

def normalize(image, ref):
    # Normalize the image.
    norm = np.divide(image, ref)
    return norm



