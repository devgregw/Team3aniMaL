# Team3aniMaL
## Team 3 - CSE 3310 - Spring 2020

## Developers
- Greg Whatley
- Jonah Bui
- Ian Klobe
- Burhanuddin Chinwala
## Description
The focus of this project is to build a model that classifies six different animals which include:
- Birds
- Butterflies
- Cats
- Dogs
- Horses
- Spiders

The model will also feature a front-end built for android so that users may either upload/ take photos to be classified by the model. 

The images used in this project for classification are obtained mainly through google images using a web crawler and from the following sources:
- https://www.kaggle.com/alessiocorrado99/animals10
- https://dl.allaboutbirds.org/merlin---computer-vision--terms-of-use?submissionGuid=e49e295c-a9ca-465b-9810-97c0f3a10b4e
- https://www.kaggle.com/c/dogs-vs-cats
- http://www.vision.caltech.edu/visipedia/CUB-200-2011.html

About 60,000 photos were obtained (unbalanced) for the classification process. For the balanced dataset, there were 36,000 images meaning 6,000 for each animal. The data was split 80/20 for validation.

## Files Included
### keras_model
- ml_basic_predict.py: used to predict images in a directory
- ml_basic_test.py: used to test over labeled images in a directory
- ml_basic_tf.py: trains a model with a tensorflow with Keras
- ml_h5_to_tflite.py: converts a Keras h5 file to a tflite file 
- ml_average_dimensions.py: calculates the average dimension of all images in a directory
## Model
The model implemented within the app is built upon the VGG16 pretrained model. It was able to obtain a validation accuracy above 95% and testing accuracy above 85%

## Tools Used
- AdvancedRenamer: useful for renaming large batches of files
- IfranView: also useful for renaming, but also good for image cropping, and converting batches of images to a single file type

## How to Run
# Running the Model

# Running the Android App
