import os

import cv2
import numpy as np

import Recon_Processor as recon
import Region_Detector as regionFinder
from NN_Models import Region_Filter as network


def read_images():
    imgs = {}
    path = "blobs/cells"
    for filename in os.listdir(path):
        try:
            img = cv2.imread(''.join([path, "/", filename]))
            imgs[filename] = img
        except:
            print("Error reading image: ", filename)
    imgs = {k: v for k, v in imgs.items() if v is not None}
    return imgs


def get_regions(imgPath, refPath):
    img = cv2.imread(imgPath)
    refImg = cv2.imread(refPath)
    img = img.astype('float32')
    refImg = refImg.astype('float32')
    norm_img = recon.normalize(img,refImg)
    regions, coordinates = regionFinder.get_regions(norm_img,img.astype('uint8'),classify=True)
    return regions


def filter_images(imgs, normImgs, bg, cell, net):
    filepath = "test/"
    confirmationName = "cell"
    nonConfirmationName = "blob"
    thresh = 0.98
    for img, norm in zip(imgs,normImgs):
        print(bg, cell)
        key = 0
        preds = net.classify(img, hardChoice=False)
        if isinstance(preds, int):
            cv2.imwrite(''.join([filepath, nonConfirmationName, "_{}.png"]).format(bg), norm)
            bg += 1
            continue
        # print(preds)
        if preds[0][np.argmax(preds)] < thresh:
            cv2.imshow("Uncertain image", norm)
            key = cv2.waitKey(0)
            if key == 101:  # 101 == E
                cv2.imwrite(''.join([filepath, "cells/", confirmationName, "_{}.png"]).format(cell), norm)
                cell += 1
            else:
                cv2.imwrite(''.join([filepath, nonConfirmationName, "_{}.png"]).format(bg), norm)
                bg += 1
            cv2.destroyAllWindows()
        else:
            if (np.argmax(preds) == 1):
                cv2.imwrite(''.join([filepath, "cells/", confirmationName, "_{}.png"]).format(cell), norm)
                cell += 1
            else:
                cv2.imwrite(''.join([filepath, nonConfirmationName, "_{}.png"]).format(bg), norm)
                bg += 1
    return bg, cell
def filter_regions_recon(regions,bg,cell):
    filepath = "../filter_training_samples/"
    confirmationName = "cell"
    nonConfirmationName = "non_cell"
    for region in regions:
        reconRegion = recon.rec.compute(region)
        regionMag = np.abs(reconRegion)
        regionMag *= 255. / np.max(regionMag)
        regionMag = regionMag.astype('uint8')
        blob, cellCountFromRegion = recon.get_diagnostic_data(regionMag, None)
        cv2.imshow("blob",blob)
        key = cv2.waitKey(0)
        cv2.destroyAllWindows()
        if key == 101:
            cv2.imwrite(''.join([filepath, "cells/", confirmationName, "_{}.png"]).format(cell), region)
            cell+=1
        else:
            cv2.imwrite(''.join([filepath, nonConfirmationName, "_{}.png"]).format(bg), region)
            bg+=1
    return bg,cell

cell = 0
bg = 0
net = network.filter_CNN()
filepath = "C:/Users/Matt/Dropbox (Partners HealthCare)/2016 Lymphoma/2. Experiments/Machine learning/Initial Testing/resources/data/images/originals"
refName = ''.join([filepath, "/reference_image.png"])
regions = []
normRegions = []
for filename in os.listdir(filepath):
    if ".png" in filename:
        imgName = ''.join([filepath, "/", filename])
        found_regions = get_regions(imgName, refName)
        for region in found_regions:
            regions.append(region)
bg, cell = filter_regions_recon(regions, bg, cell)
print(bg, cell)
