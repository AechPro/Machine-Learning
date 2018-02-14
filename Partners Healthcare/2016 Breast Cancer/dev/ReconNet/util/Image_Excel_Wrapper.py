from util import Image_Processor as imp
import os
import cv2
def load_cell_images(directory):
    samples = []
    discoveredCells = {}
    #print("loading directory",directory)
    for fileName in os.listdir(directory):
        #print("processing file",fileName)
        cell = {}
        cell["470"] = {}
        cell["625"] = {}
        cell["625"]["coordinates"] = [-1, -1]
        cell["470"]["coordinates"] = [-1, -1]
        if "ref" in fileName or ".png" not in fileName or "recon" in fileName:
            continue
        if "multicells" in fileName or "noise" in fileName:
            continue

        cellName = fileName[:fileName.find("_")]
        #print("cell",cellName)
        if "_625" in fileName:
            wavelength = "625"
        else:
            wavelength = "470"
        coordinates = fileName[fileName.find("cells") + len("cells"):fileName.find("_")]
        coordinates = coordinates.split("-")
        x = int(coordinates[0])
        y = int(coordinates[1])
        if cellName not in discoveredCells.keys():
            x = int(coordinates[0])
            y = int(coordinates[1])
            cell[wavelength]["name"] = cellName
            cell[wavelength]["coordinates"] = [x, y]
            cell[wavelength]["intensities"] = [float(fileName[fileName.rfind("_int") + len("_int"):fileName.rfind(".png")])]
            cell[wavelength]["pixels"] = cv2.imread(''.join([directory, '/', fileName]), cv2.IMREAD_ANYDEPTH)

            discoveredCells[cellName] = len(samples)
            #print(cellName,discoveredCells[cellName])
            samples.append(cell)
        else:
            samples[discoveredCells[cellName]][wavelength]["name"] = cellName
            samples[discoveredCells[cellName]][wavelength]["coordinates"] = [x, y]
            samples[discoveredCells[cellName]][wavelength]["intensities"] = [float(fileName[fileName.rfind("_int") + 
                                                                                    len("_int"):fileName.rfind(".png")])]
            samples[discoveredCells[cellName]][wavelength]["pixels"] = cv2.imread(''.join([directory, '/', fileName]), cv2.IMREAD_ANYDEPTH)
    return samples
def load_noise_images(directory):
    samples = []
    for fileName in os.listdir(directory):
        noise = {}
        noise["470"] = {}
        noise["625"] = {}
        noise["625"]["coordinates"] = [-1, -1]
        noise["470"]["coordinates"] = [-1, -1]
        if "ref" in fileName or ".png" not in fileName or "recon" in fileName:
            continue
        if "multicells" in fileName or "cells" in fileName:
            continue

        noiseName = fileName[:fileName.find("_")]
        coordinates = fileName[fileName.find("noise") + len("noise"):fileName.find("_")]
        coordinates = coordinates.split("-")
        x = int(coordinates[0])
        y = int(coordinates[1])
        if "625" in fileName:

            flag = True
            for c in samples:
                if c["470"]["coordinates"][0] == x and c["470"]["coordinates"][1] == y:
                    flag = False
                    c["625"]["name"] = noiseName
                    c["625"]["coordinates"] = [x, y]
                    c["625"]["intensities"] = [0]
                    c["625"]["pixels"] = cv2.imread(''.join([directory, '/', fileName]), cv2.IMREAD_ANYDEPTH)
            if flag:
                noise["625"]["name"] = noiseName
                noise["625"]["coordinates"] = [x, y]
                noise["625"]["intensities"] = [0]
                noise["625"]["pixels"] = cv2.imread(''.join([directory, '/', fileName]), cv2.IMREAD_ANYDEPTH)
                samples.append(noise)

        if "470" in fileName:
            flag = True
            for c in samples:
                if c["625"]["coordinates"][0] == x and c["625"]["coordinates"][1] == y:
                    c["470"]["name"] = noiseName
                    c["470"]["coordinates"] = [x, y]
                    c["470"]["intensities"] = [0]
                    c["470"]["pixels"] = cv2.imread(''.join([directory, '/', fileName]), cv2.IMREAD_ANYDEPTH)
                    flag = False
            if flag:
                noise["470"]["name"] = noiseName
                noise["470"]["coordinates"] = [x, y]
                noise["470"]["intensities"] = [0]
                noise["470"]["pixels"] = cv2.imread(''.join([directory, '/', fileName]), cv2.IMREAD_ANYDEPTH)
                samples.append(noise)
    return samples
def sort_training_samples(directory):
    trainingDataDir = "../training_data/reconnet"
    if not os.path.exists(trainingDataDir):
        os.makedirs(trainingDataDir)
    samples = load_cell_images(directory)
    saveDir = ''.join([trainingDataDir,'/','cells'])
    for cell in samples:
        #Both of these are supposed to use the 470nm name.
        c625 = "{}/{}_625_int{}.png".format(saveDir, cell["470"]["name"], cell["625"]["intensities"][0])
        c470 = "{}/{}_470_int{}.png".format(saveDir, cell["470"]["name"], cell["470"]["intensities"][0])
        cv2.imwrite(c625,cell["625"]["pixels"])
        cv2.imwrite(c470,cell["470"]["pixels"])
    saveDir = ''.join([trainingDataDir, '/', 'non_cells'])
    noiseSamples = load_noise_images(directory)
    for sample in noiseSamples:
        c625 = "{}/{}_625_int{}.png".format(saveDir, sample["470"]["name"], sample["625"]["intensities"][0])
        c470 = "{}/{}_470_int{}.png".format(saveDir, sample["470"]["name"], sample["470"]["intensities"][0])
        cv2.imwrite(c625, sample["625"]["pixels"])
        cv2.imwrite(c470, sample["470"]["pixels"])

def create_fake_noise_samples(directory):
    noiseDir = '../training_data/reconnet/non_cells'
    x = 0
    y = 0
    flag = 1
    itr = 0
    for fileName in os.listdir(directory):
        if itr>=2:
            itr = 0
            x+=1
            y+=x%15
        if flag == 1:
            WL = "470"
            flag = 0
        else:
            WL = "625"
            flag = 1
        img = cv2.imread(''.join([directory,'/',fileName]),cv2.IMREAD_ANYDEPTH)
        imName = "noise{}-{}_{}_int0.png".format(x,y,WL)
        cv2.imwrite(''.join([noiseDir,'/',imName]),img)
        itr+=1
def cell_images_to_excel(directory):
    samples = load_cell_images(directory)
    workbook, worksheet = imp.get_worksheet(''.join([directory, '/../docs/', 'cell_table.xlsx']),
                                            ['x', 'y', 'int470', 'int625', 'name'])
    row = 1
    for cell in samples:
        try:
            worksheet.write(row, 0, cell["625"]["coordinates"][0])
            worksheet.write(row, 1, cell["625"]["coordinates"][1])
            worksheet.write(row, 2, cell["470"]["intensities"][0])
            worksheet.write(row, 3, cell["625"]["intensities"][0])
            worksheet.write(row, 4, cell["625"]["name"])
            row += 1
        except Exception as e:
            print("\n\n!!!!UNABLE TO WRITE DATA TO EXCEL FILE!!!!.\nException type:", type(e).__name__,
                  "\nException args:", e.args, "\n**625nm Cell Data**\nKEYS:",cell["625"].keys(),"INTENSITY:",
                  cell["625"]["intensities"],"\nCOORDINATES (x,y): ({},{})".format(cell["625"]["coordinates"][0],
                  cell["625"]["coordinates"][1]), "\nSHAPE:",cell["625"]["pixels"].shape,"NAME:",cell["625"]["name"],
                  "\n\n**470nm Cell Data**\nKEYS:",cell["470"].keys(),"INTENSITY:", cell["470"]["intensities"],
                  "\nCOORDINATES (x,y): ({},{})".format(cell["470"]["coordinates"][0], cell["470"]["coordinates"][1]),
                  "\nNAME:",cell["470"]["name"])
    workbook.close()
