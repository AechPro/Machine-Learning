"""
Basic communication script for ease-of-access with the Amazon AWS servers. Please note that the -download command currently only downloads from the remote path to the models upload path.
"""


import sys
from subprocess import call
options = list(sys.argv[1:])

#Base file path to branch out from for models and resources.
baseFilePath = "~/Dropbox\ \(Partners\ HealthCare\)/2016\ Lymphoma/2.\ Experiments/Machine\ learning/Initial\ Testing/"

#File path of models to upload, this will be appended to the base file path.
modelFilePath = "models/Image\ Processing"

#File path of resources to upload (training data, util scripts, etc), this will be appended to the base file path.
resourcesFilePath = "resources"

#Create the full file path to upload from.
uploadFilePath = ''.join([baseFilePath,modelFilePath])

#Address to SSH into.
AWSIP = "ubuntu@ec2-13-59-146-239.us-east-2.compute.amazonaws.com"

#Remote file path to place uploaded folders in.
remoteFilePath = "~/src/workspace/MachineLearning/"

#File path of AWS key file.
keyFilePath = "~/Dropbox\ \(Partners\ HealthCare\)/2016\ Lymphoma/2.\ Experiments/Machine\ learning/Initial\ Testing/Models/MachineLearningKey.pem"

#Default command if invalid args are received.
command = "echo 'No command given.'"

#Set AWS IP address by passing "-awsip 'ip' " to args.
if "-awsip" in options[0].lower():
    AWSIP = options[1]

#Uplad files by either passing "-upload" or "-uploadtd" to args.
if "-upload" in options[0]:
    if "td" in options[0]:
        uploadFilePath = ''.join([baseFilePath,resourcesFilePath])
    command = "{} {} {} {}".format("scp -r -i",keyFilePath,uploadFilePath,"{}:{}".format(AWSIP,remoteFilePath))

#Download files from the remote directory to the same directory they were uploaded from.
if "-download" in options[0] or "-get" in options[0]:
    command = "{} {} {} {}".format("scp -r -i",keyFilePath,"{}:{}".format(AWSIP,remoteFilePath),uploadFilePath)

#Execute SSH command.
call(command,shell=True)
