## Cell Localization with Deep Learning and Region-Based Hologram Reconstruction for Lymphoma Diagnosis.

This project was created by Matthew W. Allen for Partner's Healthcare. We leverage Deep Learning to detect cells in holographic images without the need for object reconstruction. These regions can then be individually reconstructed and used to find diagnostic data. The code is able to process multiple files at once in the event that several images are provided. There are four input directories available, each is meant to contain a unique reference image and any number of hologram images associated with that reference.

## Required packages
The project requires the following (or equivalent) packages:
- Python 3.6.1
- Numpy 1.13.1
- Keras 2.0.6
- TensorFlow 1.2.1
- Scipy 0.19.1
- MatPlotLib 2.0.2
- OpenCV3
- skimage 0.13.0


## Usage
1. Download the repository and unpack it anywhere.
2. Place all of the hologram images that need to be processed in one of the available processing bin directories located in ```resources/holograms```.
3. Place the reference image for the hologram images in ```resources/holograms/processing_bin_x/reference``` where ```processing_bin_x``` is the processing bin you placed the holograms in.
4. Open a Terminal instance.
5. Navigate to the directory that this project was unpacked to.
6. Type ```Python Reconstruction.py```

When all of the images have been processed, the message ```"Processing complete!"``` will appear and the diagnostic results will be found in ```resources/diagnostics/results/processing_bin_x```.
