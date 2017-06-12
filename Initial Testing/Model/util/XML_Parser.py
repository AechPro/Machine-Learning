"""
@Author: Matthew Allen
@Date: 06/01/2016
@Title: XML_Parser.py
@Description:
    This file is designed to load and save image data in a PASCAL-VOC style XML
    document.
@TODO: Implement XML reading functions to load image data from XML documents.
"""

import xml.etree.cElementTree as ET
from xml.dom import minidom

def load_file(fileName, classMap={}, classCount={}):
    tree = ET.parse(fileName)
    root = tree.getroot()
    parsedImage = {}
    parsedImage['bboxes'] = []
    parsedImage['width'] = int(root.find('size').find('width').text)
    parsedImage['height'] = int(root.find('size').find('height').text)
    parsedImage['filename'] = root.find('filename').text
    for obj in root.findall('object'):
        bboxData = {}
        className = obj.find('name').text
        bboxData['class'] = (className)
        bboxData['difficulty'] = obj.find('difficult').text
        objectBox = obj.find('bndbox')
        for coordinate in objectBox:
            bboxData[coordinate.tag] = (int(coordinate.text))
        if className not in classCount:
           classCount[className]=1
        else: classCount[className]+=1
        if className not in classMap:
            classMap[className] = len(classMap)
        if bboxData not in parsedImage['bboxes']:
            parsedImage['bboxes'].append(bboxData)
        
    return parsedImage, classMap, classCount

"""Main function to create and save a generated image in an XML file.
    w,h,d are the dimensionality of the image, name is the desired name
    of the file, bboxData is a list of tuples containing bounding-box data
    for the objects in the image.
"""
def create_file(name,w,h,d,bboxData):
    root = ET.Element("annotation")
    folder = ET.SubElement(root,"folder").text="BBox_Data"
    fname = ET.SubElement(root,"filename").text="{}{}.png".format("../resources/data/images/training_data/generated_training_images/",name)
    size = ET.SubElement(root,"size")
    ET.SubElement(size,"width").text=str(w)
    ET.SubElement(size,"height").text=str(h)
    ET.SubElement(size,"depth").text=str(d)
    ET.SubElement(root,"segmented").text="0"
    for arr in bboxData:
        x1,y1,x2,y2 = arr
        save_object_data(root,x1,y1,x2,y2)
    xmlText = minidom.parseString(ET.tostring(root)).toprettyxml(indent="  ")
    fileName = "{}{}{}".format("../../resources/data/images/training_data/generated_training_images/BBox_Data/",name,".xml")
    file = open(fileName,'w')
    file.write(xmlText)
    file.close()

"""Utility function to save an object to a desired XML root element."""
def save_object_data(root,x1,y1,x2,y2):
    obj = ET.SubElement(root,"object")
    ET.SubElement(obj, "name").text = "cell"
    ET.SubElement(obj, "pose").text = "Unspecified"
    ET.SubElement(obj, "truncated").text="0"
    ET.SubElement(obj, "difficult").text="0"
    bbox = ET.SubElement(obj, "bndbox")
    ET.SubElement(bbox,"xmin").text=str(x1)
    ET.SubElement(bbox,"ymin").text=str(y1)
    ET.SubElement(bbox,"xmax").text=str(x2)
    ET.SubElement(bbox,"ymax").text=str(y2)
