## Cell Localization with Machine Learning and Region-Based Object Reconstruction for Lymphoma Detection.

This project was created by Matthew W. Allen for Partner's Healthcare.

This project is under the Apache License, see LICENSE for more details.

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
This project finds diagnostic data from holographic images of cells. The project is designed to process multiple files at once. There are four directories available, each is meant to contain a unique reference image and as many images as were taken during a test.

1. Download the repository and unpack it anywhere.
2. Place all of the hologram images that need to be processed in one of the available processing bin directories located in ```resources/holograms```.
3. Place the reference image for the hologram images in ```resources/holograms/processing_bin_x/reference``` where ```processing_bin_x``` is the processing bin you placed the holograms in.
4. Open a Terminal instance.
5. Navigate to the directory that this project was unpacked to.
6. Type ```Python Reconstruction.py``` with arguments -dirx where dirx is the number for the processing bin you placed the holograms in.

The reconstruction file can be run with multiple directories as arguments to process more than one processing bin in a single session. I.E. ```Python Reconstruction.py -dir1 -dir2 -dir3```

When all of the images have been processed, the code will display the message ```"Processing complete!"``` and the diagnostic results will be found in ```resources/diagnostics/results/processing_bin_x```.
