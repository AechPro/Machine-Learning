import os
from NN_Models import Reconnet as rnet
from NN_Models import Model_Manager as modelManager
from util import Image_Excel_Wrapper as wrapper
from util import Image_Processor as imp
import numpy as np
def create_excel_files():
    workingDirectory = "C:/Users/Matt/Dropbox (Partners HealthCare)/2016 Breast Cancer/" \
                       "3. MACHINE LEARNING/Machine Learning/Patient samples/171009_BrCa_CSB1"
    dirs = ["0. Unstained", "1. Red", "2. Blue", "3. Dual"]
    colors = ["uns", "red", "blue", "dual"]
    folders = ["BT474_{}_bot left,BT474_{}_bot right,BT474_{}_top left,BT474_{}_top right".format(c, c, c, c).split(',')
               for c in colors]
    gen = os.walk(workingDirectory)
    dirs = next(gen)[1]
    for d in dirs:
        gen2 = os.walk(''.join([workingDirectory,'/',d]))
        imageFolders = next(gen2)[1]
        for folder in imageFolders:
            try:
                directory = ''.join([workingDirectory, '/', d, '/',folder,'/training'])
                print(folder)
                wrapper.cell_images_to_excel(directory)
            except:
                continue
    """
    for i in range(len(dirs)):
        d = dirs[i]
        for folder in folders[i]:
            try:
                directory = ''.join([workingDirectory, '/', d, '/', folder, '/training'])
                wrapper.cell_images_to_excel(directory)
            except:
                continue"""

def sort_training_samples():
    if not os.path.exists("../training_data"):
        os.mkdir("../training_data")
        os.mkdir("../training_data/reconnet")
        os.mkdir("../training_data/reconnet/cells")
        os.mkdir("../training_data/reconnet/non_cells")

    workingDirectory = "C:/Users/Matt/Dropbox (Partners HealthCare)/2016 Breast Cancer/2. Experiments/Machine Learning/dev/images/new samples"
    dirs = ["0. Unstained", "1. Red", "2. Blue", "3. Dual"]
    colors = ["uns", "red", "blue", "dual"]
    folders = ["BT474_{}_bot left,BT474_{}_bot right,BT474_{}_top left,BT474_{}_top right".format(c, c, c, c).split(',')
               for c in colors]

    gen = os.walk(workingDirectory)
    dirs = next(gen)[1]
    for d in dirs:
        gen2 = os.walk(''.join([workingDirectory, '/', d]))
        imageFolders = next(gen2)[1]
        for folder in imageFolders:
            try:
                directory = ''.join([workingDirectory, '/', d, '/', folder, '/training'])
                print(folder)
                wrapper.sort_training_samples(directory)
            except:
                continue

def train_model():
    net = rnet.Reconnet(0)
    net.load_data()
    net.build_model()
    net.train()

def train_single_color_models():
    manager = modelManager.Manager()
    manager.train_models()

def examine_models():
    from keras.models import load_model
    model_file = "reconnet_model_file_"
    for i in range(10):
        print("\n\n****MODEL {}****\n".format(i))
        model = load_model("data/{}{}{}".format(model_file, i, ".h5"))
        print(model.summary())

def test_model(model):
    import numpy as np
    net = rnet.Reconnet(0)
    net.load_data()
    net.model_file = model
    net.build_model(load=True)
    xv, yv = net.valData
    y1, y2 = yv
    acc1 = 0
    acc2 = 0
    print(xv.shape)
    for i in range(xv.shape[0]):
        x = xv[i]
        y = [y1[i], y2[i]]
        preds = np.asarray(net.classify(x))
        if np.argmax(preds[0]) == np.argmax(y[0]):
            acc1 += 1
        if np.argmax(preds[1]) == np.argmax(y[1]):
            acc2 += 1
    acc1 /= xv.shape[0]
    acc2 /= xv.shape[0]
    print("Acc1: ", acc1)
    print("Acc2: ", acc2)

def evaluate_data(workingDirectory):
    net = rnet.Reconnet(0)
    net.load_data()
    net.model_file = "data/model_0/reconnet_model_0.h5"
    net.build_model(load=True)
    gen = os.walk(workingDirectory)
    dirs = next(gen)[1]
    for d in dirs:
        directory = ''.join([workingDirectory,'/',d])
        processedImages = imp.process_dual_wavelength(directory)
        imp.save_processed_images(processedImages,net)

"""
    On Windows platforms there is no fork() routine, so the multiprocessing library is forced to import
    the current module to get access to the worker function. Without the if statement below, the child 
    process from that import would start its own children which would all import multiprocessing and thus all 
    call on themselves to start their own children and so on.
"""
#Basically this line is necessary because windows is shit.
if __name__ == '__main__':
    workingDirectory = "C:/Users/Matt/Dropbox (Partners HealthCare)/2016 Breast Cancer/2. Experiments/Machine Learning_JM/dev/Sample data evalution results"
    #evaluate_data(workingDirectory)
    #test_model("data/model_0/reconnet_model_0.h5")
    #train_model()
    train_single_color_models()
    #sort_training_samples()
    #create_excel_files()
    #wrapper.create_fake_noise_samples('../training_data_method1/background/negative')
