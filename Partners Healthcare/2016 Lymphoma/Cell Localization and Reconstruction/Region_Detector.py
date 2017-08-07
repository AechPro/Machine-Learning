import cv2
import numpy as np
import time
import Region_Filter as regionFilter

"""
    Function to extract cells from a normalized image taken through holographic microscopy.
    @param imageMapping: The hologram image, normalized by a reference image.
    @param sourceImg: The hologram image.
    @param params: A tuple containing the parameters for the MSER blob detection algorithm.
    @param imName: The name to use when saving the image after cells have been detected.
    @param flter: An instance of a Convolutional Neural Network that has been trained to classify small regions
                    as cells or not cells.
    @param show: Flag for displaying the image in a window after cell extraction.
    @param classify: Flag to decide whether or not the CNN filter should be used on the regions.
"""
def extract_regions(imageMapping, sourceImg, params, imName, flter=None, show=False, classify=False):
    t1 = time.time()

    #Copy normalized image and set it to be dtype uint8 and valued from 0 to 255.
    img = imageMapping.copy()
    img *= 255./np.max(img)
    img = img.astype('uint8')
    if show or classify:
        #Copy of image for display purposes.
        vis = img.copy()
    if classify and flter is None:
        #Create a filter CNN if none has been passed and classification is necessary.
        print("Creating filter.")
        flter = regionFilter.filter_CNN()
    bboxes = []

    #Run MSER algorithm on image and save boundary box data for each contour it finds.
    hulls = MSER_blobs(img, params)

    #For all contours detected by MSER.
    for cnt in hulls:

        #Find and save the minimally sized boundary box around each contour.
        x, y, w, h = cv2.boundingRect(cnt)
        bboxes.append([x, y, x + w, y + h])

    #Perform non-max suppression on boundary boxes with overlap threshold = 0.2.
    bboxes = np.asarray(bboxes)
    bboxes = non_max_suppression_fast(bboxes, 0.2)

    #Expand is the number of pixels in width and height the box will increase before cropping.
    expand = 20
    regions = []
    normalRegions = []
    coordinates = []
    #For each boundary box.
    for box in bboxes:

        #Unpack the box.
        x1, y1, x2, y2 = box

        #Expand boundary box to ensure the entire cell is encompassed.
        y1 = int(y1 - expand / 2.)
        y2 = int(y2 + expand / 2.)
        x1 = int(x1 - expand / 2.)
        x2 = int(x2 + expand / 2.)

        #Map boundary box coordinates on to original hologram, subtract image mean and set to be valued 0<region[x,y]<1
        region = sourceImg[y1:y2, x1:x2]
        region = region.astype('float32')
        region -= 111.385  #Calculated channel mean from the data set.
        region /= 255.

        #Draw the boundary box in green on the normalized image.
        if show:
            if classify:
                #If the region is a cell, draw this region on the visualization image.
                if flter.classify(region):
                    cv2.rectangle(vis, (x1, y1), (x2, y2), (0, 255, 0), 1)
            else:
                #If classification is not requested and displaying is, draw boundary box on visualization image.
                cv2.rectangle(vis, (x1, y1), (x2, y2), (0, 255, 0), 1)
        #If there is a filter available, use it.
        if flter is not None:
            if flter.classify(region):
                regions.append(region)
                normalRegions.append(imageMapping[y1:y2,x1:x2])
        else:
            #If there is not a filter available, simply return all regions.
            regions.append(region)
            normalRegions.append(imageMapping[y1:y2, x1:x2])
        #If classification is requested and displaying is not, classify region and draw to visualization image for saving.
        if classify and not show and flter.classify(region):
            cv2.rectangle(vis, (x1, y1), (x2, y2), (0, 255, 0), 1)
    print(len(regions), "regions extracted in:", time.time() - t1, "seconds.")
    #Save and display normalized image with boundary boxes shown.
    if show:
        cv2.imshow("Bounding Box Data", vis)
        cv2.waitKey(0)
        cv2.destroyAllWindows()
        cv2.imwrite(imName, vis)
    #If displaying is not requested, save image with classified regions shown.
    elif classify:
        cv2.imwrite(imName, vis)
    return np.asarray(normalRegions), np.asarray(regions)

"""Wrapper function for extract_regions that uses a pre-determined parameter set for the MSER algorithm."""
def get_regions(imageMapping, image,imName="test.png", flter=None, show=False, classify=False):
    # Parameter set for MSER algorithm. These parameters were found via numerical optimization.
    params = [11, 68, 236, 0.46394341374428594, 0.20434550448034017, 200, 1, 0.32948156997486944, 5]
    normalRegions, regions = extract_regions(imageMapping, image, params, imName, flter=flter, show=show, classify=classify)
    return normalRegions, regions


"""
    Fast non-maximum suppression algorithm by Malisiewicz et al.
    @param boxes: Array of boundary boxes to perform non-max suppression on.
    @param overlapThresh: Acceptable overlap threshold. 0<=overlapTresh<=1.
"""
def non_max_suppression_fast(boxes, overlapThresh):
    # if there are no boxes, return an empty list
    if len(boxes) == 0:
        return []

    # if the bounding boxes integers, convert them to floats --
    # this is important since we'll be doing a bunch of divisions
    if boxes.dtype.kind == "i":
        boxes = boxes.astype("float")

    # initialize the list of picked indexes
    pick = []

    # grab the coordinates of the bounding boxes
    x1 = boxes[:, 0]
    y1 = boxes[:, 1]
    x2 = boxes[:, 2]
    y2 = boxes[:, 3]

    # compute the area of the bounding boxes and sort the bounding
    # boxes by the bottom-right y-coordinate of the bounding box
    area = (x2 - x1 + 1) * (y2 - y1 + 1)
    idxs = np.argsort(y2)

    # keep looping while some indexes still remain in the indexes
    # list
    while len(idxs) > 0:
        # grab the last index in the indexes list and add the
        # index value to the list of picked indexes
        last = len(idxs) - 1
        i = idxs[last]
        pick.append(i)

        # find the largest (x, y) coordinates for the start of
        # the bounding box and the smallest (x, y) coordinates
        # for the end of the bounding box
        xx1 = np.maximum(x1[i], x1[idxs[:last]])
        yy1 = np.maximum(y1[i], y1[idxs[:last]])
        xx2 = np.minimum(x2[i], x2[idxs[:last]])
        yy2 = np.minimum(y2[i], y2[idxs[:last]])

        # compute the width and height of the bounding box
        w = np.maximum(0, xx2 - xx1 + 1)
        h = np.maximum(0, yy2 - yy1 + 1)

        # compute the ratio of overlap
        overlap = (w * h) / area[idxs[:last]]

        # delete all indexes from the index list that have
        idxs = np.delete(idxs, np.concatenate(([last],
                                               np.where(overlap > overlapThresh)[0])))

    # return only the bounding boxes that were picked using the
    # integer data type
    return boxes[pick].astype("int")


"""
    Function to use the MSER algorithm to detect regions in an image.
    @param img: Input image for region detection.
    @param params: Tuple containing all the parameters for the MSER algorithm.
    @param display: Optional image for displaying the contours found by the MSER algorithm.
"""
def MSER_blobs(img, params, display=None):
    #Unpack MSER parameters and create MSER object with them.
    delta, minArea, maxArea, maxVariation, minDiversity, maxEvolution, areaThreshold, minMargin, edgeBlurSize = params
    mser = cv2.MSER_create(delta, minArea, maxArea, maxVariation, maxEvolution, areaThreshold, minMargin, edgeBlurSize)

    #Use MSER to detect regions within the given image.
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
        cv2.polylines(display, hulls, 1, (0, 255, 0))
        cv2.imshow("Display Image with MSER hulls", display)
        cv2.waitKey(0)
        cv2.destroyAllWindows()
        cv2.imwrite("Test Image.png", display)

    hulls = np.asarray(hulls)
    return hulls
