import cv2
import numpy as np
import time
import os
import subprocess
import Region_Filter as regionFilter
import matplotlib.pyplot as plt
from subprocess import call
def extract_regions(img, sourceImg, params, imName, flter=None, show=False, classify=False):
    t1 = time.time()
    vis = sourceImg.copy()
    if show or classify:
        vis = sourceImg.copy()
        #vis = cv2.imread("Daudi_Kconcentrated_phase.png")
        #vis = cv2.resize(vis,(sourceImg.shape[1],sourceImg.shape[0]),interpolation=cv2.INTER_CUBIC)
    if classify and flter is None:
        flter = regionFilter.filter_CNN()
    bboxes = []
    #Run MSER algorithm on image and save boundary box data for each contour it finds.
    hulls = MSER_blobs(img,params,display=vis)
    #Run MSER algorithm on the newly masked image with new parameters and save boundary box data.
    for cnt in hulls:
        x,y,w,h = cv2.boundingRect(cnt)
        bboxes.append([x,y,x+w,y+h])

    #Perform non-max suppression on boundary boxes with overlap threshold = 0.25.
    bboxes = np.asarray(bboxes)
    bboxes = non_max_suppression_fast(bboxes,0.2)

    #Expand is the number of pixels in width and height the box will increase before cropping.
    expand = 20
    iterator = 0

    #Read in real image for cropping with boundary boxes.
    regions = []

    #For each boundary box.
    for box in bboxes:

        #Unpack the box.
        x1,y1,x2,y2 = box
        
        #Draw the boundary box in green in the normalized image.
        if show:
            if classify:
                y1=int(y1-expand/2.)
                y2=int(y2+expand/2.)
                x1=int(x1-expand/2.)
                x2=int(x2+expand/2.)

                img = sourceImg[y1:y2,x1:x2]
                img = img.astype('float32')
                img-=111.385
                img/=255.
                probs = flter.classify(img,hardChoice=False)
                if isinstance(probs,int):
                    continue
                if np.argmax(probs[0])==1:
                    cv2.rectangle(vis,(x1,y1),(x2,y2),(0,255,0),1)
                    """textLabel = "{}".format(int(round(probs[0][1]*100)))
                    (retval,baseLine) = cv2.getTextSize(textLabel,cv2.FONT_HERSHEY_COMPLEX,0.5,1)
                    textOrg = (x1, y1)
                    cv2.rectangle(vis, (textOrg[0] - 5, textOrg[1]+baseLine - 5), (textOrg[0]+retval[0] + 5, textOrg[1]-retval[1] - 5), (0, 0, 0), 2)
                    cv2.rectangle(vis, (textOrg[0] - 5,textOrg[1]+baseLine - 5), (textOrg[0]+retval[0] + 5, textOrg[1]-retval[1] - 5), (255, 255, 255), -1)
                    cv2.putText(vis, textLabel, textOrg, cv2.FONT_HERSHEY_DUPLEX, 0.5, (0, 0, 0), 1)"""
            else:
                cv2.rectangle(vis,(x1,y1),(x2,y2),(0,255,0),1)
          
        #Expand boundary box to ensure the entire cell is encompassed.
        y1=int(y1-expand/2.)
        y2=int(y2+expand/2.)
        x1=int(x1-expand/2.)
        x2=int(x2+expand/2.)

        #Crop and save the pixels inside each boundary box.
        #cv2.imwrite("blobs/blob_{}.png".format(iterator),sourceImg[y1:y2,x1:x2])
        img = sourceImg[y1:y2,x1:x2]
        img = img.astype('float32')
        img-=111.385 #Calculated channel mean from the data set.
        img/=255.
        if classify and not show:
            if flter.classify(img):
                cv2.rectangle(vis,(x1,y1),(x2,y2),(0,255,0),1)
        regions.append(img)
        iterator+=1
    print(len(regions),"regions extracted in:",time.time()-t1,"seconds.")
    #Save and display normalized image with boundary boxes shown.
    if(show):
        cv2.imshow("Bounding Box Data",vis)
        cv2.waitKey(0)
        cv2.destroyAllWindows()
        cv2.imwrite(imName,vis)

    elif classify:
        cv2.imwrite(imName,vis)
    return regions
def get_regions(normalImage, sourceImage, imName="test", flter=None, show=False,classify=False):
    #Parameter sets for MSER algorithms. These parameters were found via numerical optimization.
    #pSet1 = [25, 85, 95, 0.3232778456108978, 3.034901370447539, 200, 1, 0.4694207531025012, 10] 
    params = [11, 68, 236, 0.46394341374428594, 0.20434550448034017, 200, 1, 0.32948156997486944, 5]
    print(normalImage.shape, sourceImage.shape)
    regions = extract_regions(normalImage,sourceImage,params, imName, flter=flter, show=show,classify=classify)
    return regions
"""Fast non-maximum suppression algorithm by Malisiewicz et al."""
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
	x1 = boxes[:,0]
	y1 = boxes[:,1]
	x2 = boxes[:,2]
	y2 = boxes[:,3]
 
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
    
"""Utility function to use the MSER algorithm with given parameters to detect objects in an image."""
def MSER_blobs(img,params,display=None):
    #Convert to grayscale if necessary.
    try:
        img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    except:
        print("Image already gray.")
    #Unpack MSER parameters and create MSER object with them.
    delta,minArea,maxArea,maxVariation,minDiversity,maxEvolution,areaThreshold,minMargin,edgeBlurSize = params
    mser = cv2.MSER_create(delta,minArea,maxArea,maxVariation,maxEvolution,areaThreshold,minMargin,edgeBlurSize)

    #Use MSER to detect regions within the given image.
    regions, _ = mser.detectRegions(img)

    #Extract the hulls corresponding to the contours found by the MSER algorithm.
    hulls = [cv2.convexHull(p.reshape(-1, 1, 2)) for p in regions]

    #Optionally draw the hulls on a display image.
    if display is not None:
        try:
            display = cv2.cvtColor(display,cv2.COLOR_GRAY2BGR)
        except:
            print("Display image correctly formatted.")
        print(len(hulls))
        cv2.polylines(display, hulls, 1, (0, 255, 0))
        cv2.imshow("Display Image with MSER hulls",display)
        cv2.waitKey(0)
        cv2.destroyAllWindows()
        cv2.imwrite("Test Image.png",display)
    hulls = np.asarray(hulls)
    #Return hulls.
    return hulls

"""Utility function to mask an image given a list of contours."""
def mask_image(img, cnts):
    #Convert image from grayscale if necessary.
    if(len(img.shape)<3):
        img = cv2.cvtColor(img, cv2.COLOR_GRAY2BGR)
    #Calculate the average and standard deviation for the color of the image.
    avg = np.average(img,axis=0)
    std = np.std(img,axis=0)
    std = np.std(std,axis=0)
    std = np.uint8(std)
    avg = np.average(avg,axis=0)
    avg = np.uint8(avg)

    #Create the color mask.
    mask = np.ones(img.shape[:2],dtype="uint8")*255

    #Draw the given contours on the mask.
    for c in cnts:
        cv2.drawContours(mask,[c],-1,0,-1)

    #Perform a bitwise and to remove all pixels inside the mask from the given image.
    img = cv2.bitwise_and(img,img,mask=mask)

    #Set all black pixels to be the average color of the image within one standard deviation.
    for arr in img[:,:,:]:
        mask = np.all(arr==[0,0,0],axis=1)
        val = std*np.random.rand(1)
        if(np.random.randint(2)==1):
            arr[mask] = avg + val
        else:
            arr[mask] = avg - val
            
    #Return newly masked image.
    return img
def normalize_images(filepath):
    for filename in os.listdir(filepath):
        if ".png" in filename:
            abspath = os.path.abspath(''.join([filepath,'/',filename]))
            print(abspath)
            p1 = subprocess.run(["java","-jar", "Test_Filter.jar", abspath,filename])
def normalize_and_process_images(filepath):
    t1 = time.time()
    flter = regionFilter.filter_CNN()
    numImages = 0
    avgTime = 0
    avgRegionCount = 0
    timeStamps = []
    regionStamps = []
    for filename in os.listdir(filepath):
        if ".png" in filename:
            t2 = time.time()
            abspath = os.path.abspath(''.join([filepath,'/',filename]))
            p1 = subprocess.run(["java","-jar", "Test_Filter.jar", abspath,filename])
            img = cv2.imread(''.join(["Normalized Images/Normalized_",filename]))
            sourceImg = cv2.imread(abspath)
            regions = get_regions(img,sourceImg,imName="Filtered Images/{}_{}".format("Filtered_",filename),flter=flter,classify=True)
            avgTime+=time.time()-t2
            timeStamps.append(time.time()-t2)
            regionStamps.append(len(regions))
            avgRegionCount+=len(regions)
            numImages+=1
    avgRegionCount/=numImages
    avgTime/=numImages
    print("Localized and extracted object data for",numImages,"images in",time.time()-t1,"seconds.")
    print("Average processing time:",avgTime,"seconds per image")
    print("Average number of regions:",avgRegionCount,"per image")

    plt.xlabel("Processing Time (seconds)")
    plt.ylabel("Detected Regions")
    plt.title("Detected Regions vs Processing Time")
    plt.plot(timeStamps,regionStamps, 'bo')
    plt.show()
#65 images, avg time = 22.63s, avg regions = 1348.63
#normalize_and_process_images("../../resources/data/images/originals")
filename = "1.png"
sourceImg = cv2.imread("../../resources/data/images/originals/1.png")
abspath = os.path.abspath(''.join(["../../resources/data/images/originals",'/',"1.png"]))
p1 = subprocess.run(["java","-jar", "Test_Filter.jar", abspath,filename])
img = cv2.imread("Normalized Images/Normalized_1.png")
regions = get_regions(img,sourceImg,imName="Test Image.png")