import multiprocessing as mp
import os

import cv2
import xlsxwriter
import numpy as np

import Recon_Processor as processor
import Region_Detector as detector
from NN_Models import Region_Filter as rFilter

"""
    Debug function to visualize the output of the reconstruction algorithm and its corrosponding holographic regions.
    @param workingDirectory: The directory containing the images and reference to be used for debugging.
"""
def debug_recon(workingDirectory):
    # Create a filter CNN object.
    flter = rFilter.filter_CNN()

    # Create the debug output directory.
    outputDir = ''.join([workingDirectory, '/', "debug"])
    if not os.path.exists(outputDir):
        os.makedirs(outputDir)

    # Set up reconstruction parameters for 470nm and 625nm wavelengths.
    params470 = [470e-9, 0.12, 6, 5e-4]
    params625 = [625e-9, 0.08, 6, 5e-4]

    # For every file in the working directory
    for fileName in os.listdir(workingDirectory):

        # Skip if it's not an image file or if it is the reference image.
        if ".png" not in fileName or "ref" in fileName:
            continue
        print("Processing", fileName)
        processedImage = {}

        # If the wavelength of this image is 470nm.
        if "470" in fileName:
            # Build an object dictionary from the image.
            processedImage = build_object_dict(processedImage, flter, "470", params470, workingDirectory, fileName,
                                               outputDir)
            # For all cells in the image.
            for cell in processedImage["470"]["cells"]:
                # Calculate reconstructed region and cell intensities.
                debug, intensities = processor.process(cell["pixels"], cell["ref"], params470)
                # Append the holographic region that was reconstructed to the debug list.
                debug.append(cell["pixels"])
                print(intensities)
                # Display debug images.
                visualize_regions(debug)
        # Else if the wavelength of this image is 625nm.
        else:
            # Build an object dictionary from the image.
            processedImage = build_object_dict(processedImage, flter, "625", params625, workingDirectory, fileName,
                                               outputDir)
            # For all cells in the image.
            for cell in processedImage["625"]["cells"]:
                # Calculated reconstructed region and cell intensities.
                debug, intensities = processor.process(cell["pixels"], cell["ref"], params625)
                # Append the holographic region that was reconstructed to the debug list.
                debug.append(cell["pixels"])
                print(intensities)
                # Display debug images.
                visualize_regions(debug)


"""
    Function to visualize a list of images.
    @param regions: Tuple or list containing the regions that are to be visualized
"""


def visualize_regions(regions):
    # Set default max width and height to be the shape of the first region in the input.
    maxW = regions[0].shape[1]
    maxH = regions[0].shape[0]

    # Loop through all of the input regions and find the max width and height
    for region in regions:
        if len(region.shape) > 2:
            h, w, d = region.shape
        else:
            w, h = region.shape
        maxW = max(w, maxW)
        maxH = max(h, maxH)

    # Name each region so that we can move the windows around with openCV
    name = "region_"
    # Set starting x,y coordinates for the first window.
    x = 200
    y = 200
    h, w, d = regions[0].shape

    # Show the first window and shift the display coordinates to the right by its width.
    cv2.imshow("region_0", regions[0])
    cv2.moveWindow("region_0", x, y)
    x += w

    # For all regions, skipping the first one.
    for i in range(1, len(regions)):

        # Hard cap on max width, reset x and increment y.
        if (x > 750):
            x = 200
            y += maxH
        # Name window.
        winName = "{}{}".format(name, i)

        # Show window and move to current position on the screen.
        cv2.imshow(winName, regions[i])
        cv2.moveWindow(winName, x, y)
        w, h = regions[i].shape

        # Increment window position by the width of the previously displayed window.
        x += w

    # Wait for and record keyboard input
    key = cv2.waitKey(0)

    # Destroy all windows and return the keyboard input.
    cv2.destroyAllWindows()
    return key


"""
    Function to initialize an empty Excel workbook with one worksheet and column names in place.
    @param name: The name of the workbook to be created.
    @param columnNames: A list or tuple containing the names of the columns to write.
"""


def get_worksheet(name, columnNames):
    # Open an empty workbook and add an empty worksheet to it.
    workbook = xlsxwriter.Workbook(name)
    worksheet = workbook.add_worksheet()
    # For each column name.
    for i in range(len(columnNames)):
        # Write the column name to the correct column.
        worksheet.write(0, i, columnNames[i])
    return workbook, worksheet


"""
    Function to build a dictionary containing all of the necessary data points for an image at a given wavelength.
    @param objDict: The dictionary that will be used to hold the data.
"""

def build_object_dict(objDict, flter, wavelength, params, workingDirectory, name, outputDirName):
    # Load the input image and its reference in 16 bit and 8 bit formats. This is a temporary measure, eventually the image
    # and its reference will be loaded exclusively in 16 bit format.
    source = cv2.imread(''.join([workingDirectory, '/', name]), cv2.IMREAD_ANYDEPTH)
    ref = cv2.imread("{}/ref_{}.png".format(workingDirectory, wavelength), cv2.IMREAD_ANYDEPTH)
    # Detect regions and cells in the image.
    regions, cells = detector.get_regions(source, ref, imName=''.join([outputDirName, '/', wavelength,
                                                                       '/detected image/detected_cells.png']),
                                          flter=flter, classify=True)
    # Initialize the dictionary as empty.
    objDict[wavelength] = {}

    # Add all necessary object data to the dictionary.
    objDict[wavelength]["source"] = source
    objDict[wavelength]["ref"] = ref
    objDict[wavelength]["params"] = params
    objDict[wavelength]["regions"] = regions
    objDict[wavelength]["cells"] = cells
    for cell in objDict[wavelength]["cells"]:
        cell["count"] = 1
        cell["intensities"] = np.asarray([-1,-1])
        cell['recon'] = np.asarray([])
        cell['canny'] = np.asarray([])
    for cell in objDict[wavelength]["regions"]:
        cell["count"] = 1
        cell["intensities"] = np.asarray([-1, -1])
        cell['recon'] = np.asarray([])
        cell['canny'] = np.asarray([])

    return objDict


"""
    Function to make the appropriate directories and sub-directories for an image at the 625nm and 470nm wavelengths.
    @param outputDirName: the path of the directory to create.
"""


def make_directories(outputDirName):
    if not os.path.exists(outputDirName):
        os.makedirs(outputDirName)
        os.makedirs(''.join([outputDirName, '/docs']))
        os.makedirs(''.join([outputDirName, '/470']))
        os.makedirs(''.join([outputDirName, '/625']))

        os.makedirs(''.join([outputDirName, '/470/refs']))
        os.makedirs(''.join([outputDirName, '/470/recons']))
        os.makedirs(''.join([outputDirName, '/470/detected image']))
        os.makedirs(''.join([outputDirName, '/470/cells']))

        os.makedirs(''.join([outputDirName, '/625/refs']))
        os.makedirs(''.join([outputDirName, '/625/recons']))
        os.makedirs(''.join([outputDirName, '/625/detected image']))
        os.makedirs(''.join([outputDirName, '/625/cells']))


def build_cell_recons(cells, params):
    newCells = []
    for cell in cells:
        debug, intensities = processor.process(cell["pixels"], cell["ref"], params)
        cell["count"] = len(intensities)
        cell["intensities"] = intensities
        cell['recon'] = debug[0].copy()
        cell['canny'] = debug[1].copy()
        newCells.append(cell)
    return newCells

def get_cell_prediction(cells,model):
    preds = np.asarray(model.classify(cells))
    #print(preds.shape)
    max1 = np.argmax(preds[0])
    max2 = np.argmax(preds[1])
    arg1 = abs(preds[0][0][max1] + max1 - 1.0)
    arg2 = abs(preds[1][0][max2] + max2 - 1.0)
    return max1,max2
def process_file(workingDirectory, file, outputDirName, reconstruct):
    flter = rFilter.filter_CNN()
    print("PROCESSING FILE", file)

    params470 = [470e-9, 0.12, 3, 5e-4]
    params625 = [625e-9, 0.08, 6, 5e-4]

    objDict = {}
    objDict["directory"] = outputDirName
    objDict["file name"] = file.lower()
    fileName625 = ''.join([file, "625.png"])
    objDict = build_object_dict(objDict, flter, "625", params625, workingDirectory, fileName625, outputDirName)
    fileName470 = ''.join([file, "470.png"])
    objDict = build_object_dict(objDict, flter, "470", params470, workingDirectory, fileName470, outputDirName)

    if len(objDict["625"]["cells"]) > len(objDict["470"]["cells"]):
        objDict["470"]["cells"] = []
        for cell in objDict["625"]["cells"]:
            newCell = {}

            cx, cy = cell["coordinates"]
            h, w = cell["pixels"].shape
            x1 = int(round(cx - w / 2.))
            x2 = int(round(cx + w / 2.))
            y1 = int(round(cy - h / 2.))
            y2 = int(round(cy + h / 2.))
            if x1 == x2 or y1 == y2:
                continue

            region = objDict["470"]["source"][y1:y2, x1:x2]
            ref = objDict["470"]["ref"][y1:y2, x1:x2]
            newCell["count"] = 1
            newCell["intensities"] = np.asarray([-1, -1])
            newCell['recon'] = np.asarray([])
            newCell['canny'] = np.asarray([])
            newCell["coordinates"] = cell["coordinates"]
            newCell["pixels"] = region
            newCell["ref"] = ref
            newCell["name"] = cell["name"]

            objDict["470"]["cells"].append(newCell)

    else:
        objDict["625"]["cells"] = []
        for cell in objDict["470"]["cells"]:
            newCell = {}

            cx, cy = cell["coordinates"]
            h, w = cell["pixels"].shape

            x1 = int(round(cx - w / 2.))
            x2 = int(round(cx + w / 2.))
            y1 = int(round(cy - h / 2.))
            y2 = int(round(cy + h / 2.))
            if x1 == x2 or y1 == y2:
                continue

            region = objDict["625"]["source"][y1:y2, x1:x2]
            ref = objDict["625"]["ref"][y1:y2, x1:x2]
            newCell["count"] = 1
            newCell["intensities"] = np.asarray([-1, -1])
            newCell['recon'] = np.asarray([])
            newCell['canny'] = np.asarray([])
            newCell["coordinates"] = cell["coordinates"]
            newCell["pixels"] = region
            newCell["ref"] = ref
            newCell["name"] = cell["name"]

            objDict["625"]["cells"].append(newCell)

    if reconstruct:
        for wavelength in ["625", "470"]:
            objDict[wavelength]["cells"] = build_cell_recons(objDict[wavelength]["cells"],
                                                             objDict[wavelength]["params"])

    return objDict


def process_dual_wavelength(workingDirectory, reconstruct=False):
    usedFileNames = []
    dirs = []
    workers = []
    pool = mp.Pool(processes=4)
    processedImages = []
    for file in os.listdir(workingDirectory):

        if "ref_" in file.lower() or "_ref" in file.lower() or "reference" in file.lower():
            continue
        if ".jpg" not in file and ".png" not in file:
            continue
        if file.lower()[:file.rfind(".png") - 3] in usedFileNames:
            continue
        else:
            usedFileNames.append(file.lower()[:file.rfind(".png") - 3])

        outputDirName = ''.join([workingDirectory, '/', file[:file.rfind(".png") - 4]])
        print("\n----FILENAME----\n", file)
        print("\n----OUTPUT DIRECTORY----\n", outputDirName)
        dirs.append(outputDirName)
        make_directories(outputDirName)
    for fileName, dirName in zip(usedFileNames, dirs):
        print("\nCreating worker for directory\n", dirName)
        workers.append(pool.apply_async(process_file, args=(workingDirectory, fileName, dirName, reconstruct)))
    print("Waiting for workers to close...")
    itr = 0
    for worker in workers:
        processedImages.append(worker.get())
        print("Worker", itr, "has closed.")
        itr += 1
    return processedImages


def save_processed_images(processedImages,model):
    for dictionary in processedImages:
        workingDirectory = dictionary["directory"]
        dualRow = 1
        row470 = 1
        row625 = 1
        workbook470, worksheet470 = get_worksheet(''.join([workingDirectory, '/docs/', 'cell_table_470.xlsx']),
                                                  ['x', 'y', 'int', 'name'])

        workbook625, worksheet625 = get_worksheet(''.join([workingDirectory, '/docs/', 'cell_table_625.xlsx']),
                                                  ['x', 'y', 'int', 'name'])

        dualWorkbook, dualWorksheet = get_worksheet(''.join([workingDirectory, '/docs/', 'cell_table_dual.xlsx']),
                                                    ['x', 'y', 'int470', 'int625', '470 stained count', '470 unstained count',
                                                    '625 stained count','625 unstained count','name'])
        stain470 = 0
        stain625 = 0
        unstain470 = 0
        unstain625 = 0
        for c470, c625 in zip(dictionary["470"]["cells"], dictionary["625"]["cells"]):
            try:
                cv2.imwrite(''.join([workingDirectory, '/470/cells/', c470["name"], '.png']), c470["pixels"])
                cv2.imwrite(''.join([workingDirectory, '/470/refs/', c470['name'], '.png']), c470['ref'])
                #cv2.imwrite(''.join([workingDirectory, '/470/recons/', c470["name"], '.png']), c470['recon'])

                cv2.imwrite(''.join([workingDirectory, '/625/cells/', c625["name"], '.png']), c625["pixels"])
                cv2.imwrite(''.join([workingDirectory, '/625/refs/', c625['name'], '.png']), c625['ref'])
                #cv2.imwrite(''.join([workingDirectory, '/625/recons/', c625["name"], '.png']), c625['recon'])

                if c470["count"] == 1:
                    worksheet470.write(row470, 0, c470["coordinates"][0])
                    worksheet470.write(row470, 1, c470["coordinates"][1])
                    worksheet470.write(row470, 2, c470["intensities"][0])
                    worksheet470.write(row470, 3, c470["name"])
                    row470 += 1

                if c625["count"] == 1:
                    worksheet625.write(row625, 0, c625["coordinates"][0])
                    worksheet625.write(row625, 1, c625["coordinates"][1])
                    worksheet625.write(row625, 2, c625["intensities"][0])
                    worksheet625.write(row625, 3, c625["name"])
                    row625 += 1

                if c625["count"] == 1 and c470["count"] == 1:
                    dualWorksheet.write(dualRow, 0, c625["coordinates"][0])
                    dualWorksheet.write(dualRow, 1, c625["coordinates"][1])
                    preds = get_cell_prediction([c470["pixels"],c625["pixels"]],model)
                    dualWorksheet.write(dualRow, 2, preds[0])
                    dualWorksheet.write(dualRow, 3, preds[1])
                    dualWorksheet.write(dualRow, 8, c470["name"])
                    if preds[0] == 0:
                        unstain470+=1
                    else:
                        stain470+=1
                    if preds[1] == 0:
                        unstain625+=1
                    else:
                        stain625+=1
                    dualRow += 1
            except Exception as e:
                print(c625.keys(),c470.keys())
                print("\n\n!!!!UNABLE TO WRITE DATA TO EXCEL FILE!!!!.\nException type:", type(e).__name__,
                      "\nException args:", e.args, "\n**625nm Cell Data**\nINTENSITY:", c625["intensities"],
                      "\nCOORDINATES (x,y): ({},{})".format(c625["coordinates"][0], c625["coordinates"][1]), "\nSHAPE:",
                      c625["pixels"].shape, "\nREF SHAPE:", c625["ref"].shape, "\nRECON SHAPE:", c625["recon"].shape,
                      "NAME:",
                      c625["name"], "\n\n**470nm Cell Data**\nINTENSITY:", c470["intensities"],
                      "\nCOORDINATES (x,y): ({},{})".format(c470["coordinates"][0], c470["coordinates"][1]), "\nSHAPE:",
                      c470["pixels"].shape, "\nREF SHAPE:", c470["ref"].shape, "\nRECON SHAPE:", c470["recon"].shape,
                      "NAME:",
                      c470["name"])
        dualWorksheet.write(1, 4, stain470)
        dualWorksheet.write(1, 5, unstain470)
        dualWorksheet.write(1, 6, stain625)
        dualWorksheet.write(1, 7, unstain625)
        dualWorkbook.close()
        workbook470.close()
        workbook625.close()


def save_processed_images_txt(processedImages):
    for dictionary in processedImages:
        workingDirectory = dictionary["directory"]
        coordinateFile = open(''.join([workingDirectory, '/docs/', 'dual_wavelength_coordinates.txt']), 'w')
        print("Writing data to",workingDirectory)
        for c470, c625 in zip(dictionary["470"]["cells"], dictionary["625"]["cells"]):
            try:
                """cv2.imwrite(''.join([workingDirectory, '/470/cells/', c470["name"], '.png']), c470["pixels"])
                cv2.imwrite(''.join([workingDirectory, '/470/refs/', c470['name'], '.png']), c470['ref'])

                cv2.imwrite(''.join([workingDirectory, '/625/cells/', c625["name"], '.png']), c625["pixels"])
                cv2.imwrite(''.join([workingDirectory, '/625/refs/', c625['name'], '.png']), c625['ref'])
                """
                coordinateFile.write(" ( {} {} )\n".format(c625["coordinates"][0], c625["coordinates"][1]))
            except:
                print("!!!!UNABLE TO WRITE DATA TO TEXT FILE!!!!.\nSHAPES:", c470["pixels"].shape, c470["ref"].shape,
                      "\n", c625["pixels"].shape, c625["ref"].shape, "\nCOORDINATES (x,y): ({},{})".format(
                        c625["coordinates"][0], c625["coordinates"][1]))
        coordinateFile.close()
