import cv2
import os
import Region_Filter as network
import Region_Detector as regionFinder
import Data_Loader as dLoader
import numpy as np

def readImages():
    imgs = {}
    path = "blobs/cells"
    for filename in os.listdir(path):
        try:
            img = cv2.imread(''.join([path,"/",filename]))
            imgs[filename] = img
        except:
            print("Error reading image: ",filename)
    imgs = {k:v for k,v in imgs.items() if v is not None}
    return imgs
def createImages(img, sourceImg):
    img = cv2.imread(img)
    sourceImg = cv2.imread(sourceImg)
    imgs = regionFinder.get_regions(img, sourceImg)
    return imgs
def filter_images(imgs,bg,cell,net):
    filepath = "test/"
    confirmationName = "cell"
    nonConfirmationName = "blob"
    thresh = 0.95
    for img in imgs:
        key = 0
        preds = net.classify(img,hardChoice=False)
        if img is not None:
            img*=255.
            img+=111.385
            img = img.astype('uint8')
        if isinstance(preds, int):
            cv2.imwrite(''.join([filepath,nonConfirmationName,"_{}.png"]).format(bg),img)
            bg+=1
            continue
        #print(preds)
        if(preds[0][np.argmax(preds)]<thresh):
            cv2.imshow("Uncertain image", img)
            key = cv2.waitKey(0)
            if(key==101): #101 == E
                cv2.imwrite(''.join([filepath,"cells/",confirmationName,"_{}.png"]).format(cell),img)
                cell+=1
            else:
                cv2.imwrite(''.join([filepath,nonConfirmationName,"_{}.png"]).format(bg),img)
                bg+=1
            cv2.destroyAllWindows()
        else:
            if(np.argmax(preds)==1):
                cv2.imwrite(''.join([filepath,"cells/",confirmationName,"_{}.png"]).format(cell),img)
                cell+=1
            else:
                cv2.imwrite(''.join([filepath,nonConfirmationName,"_{}.png"]).format(bg),img)
                bg+=1
    return bg,cell
cell = 3442
bg = 3405
net = network.filter_CNN()
filepath = "Normalized Images"
regions = []
for filename in os.listdir(filepath):
    if ".png" in filename:
        srcName = filename[filename.rfind("Normalized_")+len("Normalized_"):]
        srcName = ''.join(["../../resources/data/images/originals/",srcName])
        imgName = ''.join([filepath,"/",filename])
        imgs = createImages(imgName,srcName)
        for im in imgs:
            regions.append(im)
bg,cell = filter_images(regions,bg,cell,net)
print(bg,cell)
