"""
@Author: Matthew Allen
@Date 05/24/2017
@File: model_builder.py
@Description:
    This file is responsible for handling the models built from the components defined
    in model_structure.py. This file also has the capability to train a model
    by passing the model and a data set to train_model.py, or to load a pre-trained
    model and show its output when given an image input."""
import model_builder
import time
import numpy as np
import os
import keras.backend as K
from util import Loss_Functions as losses
from util import ROI_Helpers as roi_helpers
from util import data_loader as dLoader
from keras.models import Sequential, Model
from keras.optimizers import Adadelta, RMSprop,Adam
from keras.layers.convolutional import Conv2D, MaxPooling2D
from keras.utils import np_utils
from keras.callbacks import ModelCheckpoint
from keras.utils import generic_utils
from util import Config
from util.Pascal_VOC_Parser import get_data
from util import Image_Processor as processor
import pickle
class Network(object):
    def __init__(self):
        """C = Config.Config()
        trainIter,valIter,inputImageShape,allImgs,classCount,classMap = dLoader.load_data("../resources/data/images/training_data/generated_training_images/BBox_Data",C)
        self.models = self.get_models(inputImageShape,C.num_rois,len(C.anchor_box_ratios)*len(C.anchor_box_scales), len(C.class_mapping))
        self.train(trainIter, valIter, classMap)"""
        self.testStep()
    def get_models(self, inputImageDims, numROIs, numAnchors, numClasses):
        optimizer = Adam()
        
        RPN, classifier, full = model_builder.build_models(inputImageDims,numROIs, numAnchors, numClasses)
        
        RPN.compile(optimizer=optimizer, loss=[losses.rpn_loss_cls(numAnchors), losses.rpn_loss_regr(numAnchors)])
        
        classifier.compile(optimizer=optimizer, loss=[losses.class_loss_cls, losses.class_loss_regr(numClasses-1)], metrics={'dense_class_{}'.format(numClasses): 'accuracy'})
        
        full.compile(optimizer='sgd', loss='mae')
        return RPN,classifier,full

    def testStep(self):
        C = Config.Config()
        
        images, classCount, classMap = get_data(os.path.expanduser("~/Desktop/training_data/PASCAL VOC/VOCdevkit"))
        
        if 'bg' not in classCount:
            classCount['bg'] = 0
            classMap['bg'] = len(classMap)
        C.class_mapping = classMap

        with open("config.pickle", 'wb') as config_f:
            pickle.dump(C,config_f)
            print('Config has been written to {}, and can be loaded when testing to ensure correct results'.format("config.pickle"))
        np.random.shuffle(images)
        numImages = len(images)

        
        trainImages = [s for s in images if s['imageset'] == 'trainval']
        valImages = [s for s in images if s['imageset'] == 'test']
        
        print('Num train samples {}'.format(len(trainImages)))
        print('Num val samples {}'.format(len(valImages)))
        
        trainIter = processor.get_anchor_gt(trainImages,classCount,C,K)
        valIter = processor.get_anchor_gt(trainImages,classCount,C,K,mode='val')
        
        numAnchors = len(C.anchor_box_scales)*len(C.anchor_box_ratios)
        
        self.models = self.get_models((3,None,None),C.num_rois,numAnchors,len(classCount))
        
        self.train(trainIter,valIter,classMap)

    def train(self, data_gen_train, data_gen_val, classMap):
        model_rpn,model_classifier,model_all = self.models

        epoch_length = 1000
        num_epochs = 25
        iter_num = 0

        losses = np.zeros((epoch_length, 5))
        rpn_accuracy_rpn_monitor = []
        rpn_accuracy_for_epoch = []
        start_time = time.time()

        best_loss = np.Inf
        for epoch_num in range(num_epochs):
                
            progbar = generic_utils.Progbar(epoch_length)
            print('Epoch {}/{}'.format(epoch_num + 1, num_epochs))

            while True:
                try:
                    if len(rpn_accuracy_rpn_monitor) == epoch_length and C.verbose:
                        mean_overlapping_bboxes = float(sum(rpn_accuracy_rpn_monitor))/len(rpn_accuracy_rpn_monitor)
                        rpn_accuracy_rpn_monitor = []
                        print('Average number of overlapping bounding boxes from RPN = {} for {} previous iterations'.format(mean_overlapping_bboxes, epoch_length))
                        if mean_overlapping_bboxes == 0:
                            print('RPN is not producing bounding boxes that overlap the ground truth boxes. Check RPN settings or keep training.')

                    X, Y, img_data = next(data_gen_train)
                    print("Predicting on batch...")
                    P_rpn = model_rpn.predict_on_batch(X)
                    print("OUTPUT SHAPES:",P_rpn[0].shape,",",P_rpn[1].shape)

                    loss_rpn = model_rpn.train_on_batch(X, Y)

                    

                    R = roi_helpers.rpn_to_roi(P_rpn[0], P_rpn[1], C, K.image_dim_ordering(), use_regr=True, overlap_thresh=0.7, max_boxes=300)

                    # note: calc_iou converts from (x1,y1,x2,y2) to (x,y,w,h) format
                    X2, Y1, Y2 = roi_helpers.calc_iou(R, img_data, C, class_mapping)

                    if X2 is None:
                            rpn_accuracy_rpn_monitor.append(0)
                            rpn_accuracy_for_epoch.append(0)
                            continue

                    neg_samples = np.where(Y1[0, :, -1] == 1)
                    pos_samples = np.where(Y1[0, :, -1] == 0)

                    if len(neg_samples) > 0:
                            neg_samples = neg_samples[0]
                    else:
                            neg_samples = []

                    if len(pos_samples) > 0:
                            pos_samples = pos_samples[0]
                    else:
                            pos_samples = []

                    rpn_accuracy_rpn_monitor.append(len(pos_samples))
                    rpn_accuracy_for_epoch.append((len(pos_samples)))

                    if C.num_rois > 1:
                            if len(pos_samples) < C.num_rois/2:
                                    selected_pos_samples = pos_samples.tolist()
                            else:
                                    selected_pos_samples = np.random.choice(pos_samples, C.num_rois/2, replace=False).tolist()
                            try:
                                    selected_neg_samples = np.random.choice(neg_samples, C.num_rois - len(selected_pos_samples), replace=False).tolist()
                            except:
                                    selected_neg_samples = np.random.choice(neg_samples, C.num_rois - len(selected_pos_samples), replace=True).tolist()

                            sel_samples = selected_pos_samples + selected_neg_samples
                    else:
                            # in the extreme case where num_rois = 1, we pick a random pos or neg sample
                            selected_pos_samples = pos_samples.tolist()
                            selected_neg_samples = neg_samples.tolist()
                            if np.random.randint(0, 2):
                                    sel_samples = random.choice(neg_samples)
                            else:
                                    sel_samples = random.choice(pos_samples)

                    loss_class = model_classifier.train_on_batch([X, X2[:, sel_samples, :]], [Y1[:, sel_samples, :], Y2[:, sel_samples, :]])

                    losses[iter_num, 0] = loss_rpn[1]
                    losses[iter_num, 1] = loss_rpn[2]

                    losses[iter_num, 2] = loss_class[1]
                    losses[iter_num, 3] = loss_class[2]
                    losses[iter_num, 4] = loss_class[3]

                    iter_num += 1

                    progbar.update(iter_num, [('rpn_cls', np.mean(losses[:iter_num, 0])), ('rpn_regr', np.mean(losses[:iter_num, 1])),
                                                                      ('detector_cls', np.mean(losses[:iter_num, 2])), ('detector_regr', np.mean(losses[:iter_num, 0]))])

                    if iter_num == epoch_length:
                            loss_rpn_cls = np.mean(losses[:, 0])
                            loss_rpn_regr = np.mean(losses[:, 1])
                            loss_class_cls = np.mean(losses[:, 2])
                            loss_class_regr = np.mean(losses[:, 3])
                            class_acc = np.mean(losses[:, 4])

                            mean_overlapping_bboxes = float(sum(rpn_accuracy_for_epoch)) / len(rpn_accuracy_for_epoch)
                            rpn_accuracy_for_epoch = []

                            if C.verbose:
                                    print('Mean number of bounding boxes from RPN overlapping ground truth boxes: {}'.format(mean_overlapping_bboxes))
                                    print('Classifier accuracy for bounding boxes from RPN: {}'.format(class_acc))
                                    print('Loss RPN classifier: {}'.format(loss_rpn_cls))
                                    print('Loss RPN regression: {}'.format(loss_rpn_regr))
                                    print('Loss Detector classifier: {}'.format(loss_class_cls))
                                    print('Loss Detector regression: {}'.format(loss_class_regr))
                                    print('Elapsed time: {}'.format(time.time() - start_time))

                            curr_loss = loss_rpn_cls + loss_rpn_regr + loss_class_cls + loss_class_regr
                            iter_num = 0
                            start_time = time.time()

                            if curr_loss < best_loss:
                                    if C.verbose:
                                            print('Total loss decreased from {} to {}, saving weights'.format(best_loss,curr_loss))
                                    best_loss = curr_loss
                                    model_all.save_weights(C.model_path)

                            break

                except Exception as e:
                        print('Exception: {}'.format(e))
                        continue
net = Network()
