## Cell Localization with Machine Learning and Region-Based Object Reconstruction for Lymphoma Detection.

This project was created by Matthew W. Allen for Partner's Healthcare.
This project is under the Apache License. See LICENSE for more details.

## Required packages
This project requires the following packages:
- Python 3.6.1
- Numpy 1.13.1
- Keras 2.0.6
- TensorFlow 1.2.1
- Scipy 0.19.1
- MatPlotLib 2.0.2
- OpenCV3
- skimage 0.13.0


## USAGE
1. To use this project, download the repository and unpack it anywhere. Place all of the hologram images that need to be processed in resources/holograms/processing_bin_1.
2. Place the reference image for the hologram images in resources/holograms/processing_bin_1/reference.
3. Open a Terminal instance.
4. Navigate to the directory that this project was unpacked in.
5. Type "Python Reconstruction.py -dir1"
When all of the images have been processed, the code will display the message "Processing complete!" and the diagnostic results will be found in resources/diagnostics/results/processing_bin_1.
