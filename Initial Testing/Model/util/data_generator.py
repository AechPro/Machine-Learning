import XML_Parser as parser
import numpy as np
import string
from PIL import Image
from PIL import ImageOps
from random import randrange
import math
import os
def generate_images(fileName, num, width=2592, height=1944):
    cells, nonCells, dictionary = get_image_gen_data()
    isCell = False;
    w,h = cells[0].size
    mode = cells[0].mode
    for i in range(num):
        bboxData = []
        newImage = Image.new(mode,(width,height))
        cellCount=0
        plx = 0
        ply = 0
        for x in range(int(width/w)):
            for y in range(int(height/h)):
                plx = x*w
                ply = y*h
                isCell=True
                sample = cells[randrange(0,len(cells))]
                if(randrange(0,100)>10):
                    sample = nonCells[randrange(0,len(nonCells))]
                    isCell = False
                name = sample.filename[sample.filename.rfind("/")+1:sample.filename.rfind("png")-1]
                cellCount+=int(name[name.index("*")+1])
                newImage.paste(sample,box=(plx,ply))
                if(isCell):
                    boxes = dictionary[name]
                    for box in boxes:
                        x1,y1,x2,y2 = box
                        x1+=plx
                        y1+=ply
                        x2+=plx
                        y2+=ply
                        
                        #Getting duplicates from somewhere... Not entirely sure how.
                        if([x1,y1,x2,y2] not in bboxData):
                            bboxData.append([x1,y1,x2,y2])
        #Determine average image color and set up padding bars to place on left and right sides of image.
        avgColor = int(get_average_color(newImage))
        widthPad = Image.new(mode, (w,height),color=avgColor)
        heightPad = Image.new(mode,(width,h),color=avgColor)

        #Paint padding bars on image to remove any black bars on sides where image slices would not fit.
        newImage.paste(widthPad,box=(plx+w,0))
        newImage.paste(heightPad,box=(0,ply+h))

        #Set up name of new image and save.
        n = "{}{}_{}.png".format("../../resources/data/images/training_data/generated_training_images/training_image_",fileName,i)
        newImage.save(n)

        #Create PASCAL style XML file representing all bounding box data in
        #the newly generated image.
        parser.create_file("{}{}_{}".format("training_image_",fileName,i),width,height,1,bboxData)

        #Print cell count.
        print("total cells in image: ",cellCount)

def augment_data():
    file = open("../../resources/data/images/training_data/cell_images/augmented_data/Augmented_Data_Labels.txt",'w')
    imgs = load_image_array("../../resources/data/images/training_data/cell_images/non_stained")
    cells = load_image_array("../../resources/data/images/training_data/cell_images/stained")
    bg = load_image_array("../../resources/data/images/training_data/non_cell_images")
    bboxData = load_bbox_data("../../resources/data/image_labels/Snippet_Labels.txt")
    dictionary = {}
    newBBoxData = []
    for im in cells:
        imgs.append(im)
    for im in bg:
        imgs.append(im)
    filePath = imgs[0].filename
    w,h = imgs[0].size
    filePath = filePath[:filePath.rfind("training_data/")+len("training_data/")]
    filePath = ''.join([filePath,"cell_images/augmented_data/"])
    dictionary = build_bbox_dictionary(imgs,bboxData)
    print(usingWindows)
    for im in imgs:
        imageName = im.filename[im.filename.rfind("/")+1:im.filename.rfind("png")-1]
        saveImageName = "{}{}.png".format(filePath,imageName)
        if usingWindows:
            saveImageName = format_for_windows(saveImageName)
        im.save(saveImageName)
        for r in range(90,360,90):
            rImageName = "{}{}_{}.png".format(filePath,r,imageName)
            mirImageName = "{}{}_{}_{}.png".format(filePath,r,"mir",imageName)
            if imageName in dictionary:
                cls = "stained_cell"
                if "cell_" in imageName:
                    cls = "cell"
                for arr in dictionary[imageName]:
                    x1,y1,x2,y2 = arr
                    
                    rx1, ry1 = rotate_point([x1,y1],[w/2,h/2],np.deg2rad(-r))
                    rx2, ry2 = rotate_point([x2,y2],[w/2,h/2],np.deg2rad(-r))
                    
                    rBoxName = rImageName[rImageName.rfind("../")+3:]
                    rBoxName = ''.join(["resources/data/",rBoxName])
                    rBox = pack_bbox(rBoxName,cls,[rx1,ry1,rx2,ry2])
                    newBBoxData.append(rBox)
                    
                    mx1, my1 = mirror_point_horizontal([rx1,ry1],[w/2,h/2])
                    mx2, my2 = mirror_point_horizontal([rx2,ry2],[w/2,h/2])

                    mBoxName = mirImageName[mirImageName.rfind("../")+3:]
                    mBoxName = ''.join(["resources/data/",mBoxName])
                    mBox = pack_bbox(mBoxName,cls,[mx1,my1,mx2,my2])
                    newBBoxData.append(mBox)

                    origBoxName = ''.join(["resources/data/images/training_data/cell_images/augmented_images/",imageName,".png"])
                    origBox = pack_bbox(origBoxName,cls,[x1,y1,x2,y2])
                    newBBoxData.append(origBox)
            if usingWindows:
                rImageName = format_for_windows(rImageName)
                mirImageName = format_for_windows(mirImageName)
            rImage = im.rotate(r)
            mImage = ImageOps.mirror(rImage)
            rImage.save(rImageName)
            mImage.save(mirImageName)
    for dataPack in newBBoxData:
        file.write(dataPack)
        file.write("\n")
    file.close()
            
        
def build_bbox_dictionary(images,bboxLabels):
    bboxNames = []
    imageNames = []
    for im in images:
        name = im.filename[im.filename.rfind("/")+1:im.filename.rfind("png")-1]
        if "bg_" not in name:
            imageNames.append(name)
    imageNames = set(list(imageNames))
    dictionary = {}
    for name in imageNames:
        dictionary[name] = []
    for dataPack in bboxLabels:
        try:
            name, cls, x1,y1,x2,y2 = dataPack
            coords = [int(float(x1)),int(float(y1)),int(float(x2)),int(float(y2))]
            name = name[name.rfind("/")+1:name.rfind("png")-1]
            dictionary[name].append(coords)
        except Exception as e:
            print("EXCEPTION UNPACKING BBOX |",type(e).__name__,"|",e.args)
    return dictionary
def get_image_gen_data():
    bboxLabels = load_bbox_data("../../resources/data/images/training_data/cell_images/augmented_data/Augmented_Data_Labels.txt")
    images = load_image_array("../../resources/data/images/training_data/cell_images/augmented_data")
    dictionary = build_bbox_dictionary(images,bboxLabels)
    cells = []
    nonCells = []
    for im in images:
        if "bg_" in im.filename:
            nonCells.append(im)
        else:
            cells.append(im)
    return cells,nonCells,dictionary
def pack_bbox(name,classification,coords):
    x1,y1,x2,y2 = coords
    return "{},{},{},{},{},{}".format(name,classification,x1,y1,x2,y2)
def load_image_array(filePath):
    files = []
    global usingWindows
    #Loop through all files in specified file path.
    for fileName in os.listdir(filePath):

        #Try to fill files array with each file found in "path/fileName" directory.
        pathString = "{}/{}".format(filePath,fileName)
        try:
            #Copy image because Image.open() is a memory leak.
            img = Image.open(pathString)
            imageCopy = img.copy()
            imageCopy.filename = img.filename
            files.append(imageCopy)
            img.close()
        except Exception as e:
            print("EXCEPTION LOADING IMAGE |",type(e).__name__,"|",e.args)

    for im in files:
        if '*' not in im.filename:
            usingWindows = True
            name = im.filename[im.filename.rfind("/")+1:im.filename.rfind("png")-1]
            name = ''.join([name[:-2],name[-2].replace('_','*'),name[-1:]])
            imname = ''.join([im.filename[:im.filename.rfind("/")+1],name,im.filename[im.filename.rfind("png")-1:]])
            im.filename = imname
    return files
def load_bbox_data(filePath):
    #Open the file in read only mode and read it into a list.
    file = open(filePath,'r')
    lines = file.readlines()
    data = []

    #Loop through all the lines from the data file, remove new lines, split by ',' delimiter, append to new data tuple.
    for line in lines:
        data.append(line.replace('\n','').split(','))
    file.close()
    return data
def format_for_windows(filename):
    name =filename[filename.rfind("/")+1:filename.rfind("png")-1]
    name = ''.join([name[:-2],name[-2].replace('*','_'),name[-1:]])
    newName = ''.join([filename[:filename.rfind("/")+1],name,filename[filename.rfind("png")-1:]])
    return newName
def mirror_point_horizontal(point, center):
    x,y = point
    cx,cy = center
    nx = 2.*cx-x
    return [nx,y]
def rotate_point(point,center,angle):
    x,y = point
    cx,cy = center
    newX = cx + (x-cx)*math.cos(angle) - (y-cy)*math.sin(angle);
    newY = cy + (x-cx)*math.sin(angle) + (y-cy)*math.cos(angle);
    return [int(round(newX)),int(round(newY))]
def get_average_color(image):
    avg = 0

    #Load pixel array from given image.
    pixels = image.load()
    w,h = image.size

    #Loop through all pixels.
    for x in range(w):
        for y in range(h):
            avg+=pixels[x,y] #Add each pixel value to avg.
    avg/=w*h #Divide avg by area of image (the total number of pixels).
    return avg
