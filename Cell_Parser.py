import cv2
import os
import Region_Optimizer as network
import blob_detection_test as regionFinder
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
def createImages(img, normal):
    imgs = regionFinder.get_regions(normal, img)
    return imgs
def filter_images(imgs,bg,cell,net):
    filepath = "test/"
    confirmationName = "cell"
    nonConfirmationName = "blob"
    thresh = 0.4
    for img in imgs:
        key = 0
        preds = net.parseImage(img,hardChoice=False)
        if img is not None:
            img*=255.
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
cell = 0
bg = 0
net = network.filter_CNN()
imgs = createImages("5.png","Normalized Image.png")
bg,cell = filter_images(imgs,bg,cell,net)
imgs = createImages("5stained.png","Normalized Image Stained.png")
bg, cell = filter_images(imgs,bg,cell,net)

