import cv2
import numpy as np
import os
def map_folder_to_image(folderName):
    imageName470 = ''.join([folderName,'_470.png'])
    imageName625 = ''.join([folderName,'_625.png'])
    print("LOADING IMAGE",imageName625)
    img625 = cv2.imread(imageName625,cv2.IMREAD_ANYDEPTH)
    img470 = cv2.imread(imageName470,cv2.IMREAD_ANYDEPTH)
    return img625, img470
def reshapeBox(box, shape, boundaryShape):
    # Unpack the box.
    x1, y1, x2, y2 = box
    ox1, oy1, ox2, oy2 = box
    cx = x1 + abs(x1 - x2) // 2
    cy = y1 + abs(y1 - y2) // 2
    # h & w are the maximum x,y coordinates that the box is allowed to attain.
    h, w = boundaryShape
    # dw & dh are the desired width and height of the box.
    dw, dh = shape

    # Calculated the amount that the dimensions of the box will need to be changed
    heightExpansion = dh - abs(y1 - y2)
    widthExpansion = dw - abs(x1 - x2)

    # Force y1 to be the smallest of the y values. Shift y1 by half of the necessary expansion. Bound y1 to a minimum of 0.
    y1 = max(int(round(min(y1, y2) - heightExpansion / 2.)), 0)

    # Force y2 to be the largest of the y values. Shift y2 by half the necessary expansion. Bound y2 to a maximum of h.
    y2 = min(int(round(max(y1, y2) + heightExpansion / 2.)), h)

    # These two lines are a repeat of the above y1,y2 calculations but with x and w instead of y and h.
    x1 = max(int(round(min(x1, x2) - widthExpansion / 2.)), 0)
    x2 = min(int(round(max(x1, x2) + widthExpansion / 2.)), w)

    if abs(x2-x1) != dw or abs(y1-y2) != dh:
        minVal = min([abs(cx-x1),abs(cx-x2),abs(cy-y1),abs(cy-y1)])
        x1 = cx - minVal
        x2 = cx + minVal
        y1 = cy - minVal
        y2 = cy + minVal
    # Calculate the center point of the newly reshaped box, truncated.
    cx = x1 + abs(x1 - x2) // 2
    cy = y1 + abs(y1 - y2) // 2

    return x1, y1, x2, y2, cx, cy
def process_folder(folder, regionSize, savePath):
    img625, img470 = map_folder_to_image(folder)
    w,h = regionSize
    w = w//2
    h = h//2
    for cellName in os.listdir(''.join([folder,'/training'])):
        if "recon" in cellName:
            continue

        coordinates = cellName[cellName.find("cells") + len("cells"):cellName.find("_")]
        coordinates = coordinates.split("-")
        cx = int(coordinates[0])
        cy = int(coordinates[1])
        x1 = cx-w
        x2 = cx+w
        y1 = cy-h
        y2 = cy+h
        x1,y1,x2,y2,_1, _2 = reshapeBox([x1,y1,x2,y2],regionSize,img625.shape)
        if "_470" in cellName:
            regionOfInterest = img470[y1:y2, x1:x2]
        else:
            regionOfInterest = img625[y1:y2, x1:x2]
        cv2.imwrite(''.join([savePath,'/',cellName]),regionOfInterest)
def loop_folders(workingDirectory):
    savePath = "C:/Users/Matt/Dropbox (Partners HealthCare)/2016 Breast Cancer/2. Experiments/Machine Learning_JM/0. dev/training_data/reconnet/large_cells"
    gen = os.walk(workingDirectory)
    dirs = next(gen)[1]
    for d in dirs:
        gen2 = os.walk(''.join([workingDirectory, '/', d]))
        imageFolders = next(gen2)[1]
        for folder in imageFolders:
            directory = ''.join([workingDirectory, '/', d, '/', folder])
            print(directory)
            process_folder(directory,(50,50),savePath)

directory = "C:/Users/Matt/Dropbox (Partners HealthCare)/2016 Breast Cancer/3. MACHINE LEARNING/Machine Learning/180205_BrCa for DL (Selective Sync Conflict)/re-synced BT474/BT474"
loop_folders(directory)