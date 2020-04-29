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
### Running the Model Implementation
#### Setting up the repository
Clone the repository and place it in your preferred project space.
#### Downloading the model
To download a sample model to predict and test view this link:
https://drive.google.com/drive/folders/1OD2z9ztrpB-JVjK30fnOF85GQiGl2coc?usp=sharing

Once the model file has been downloaded, place it in the keras_model folder provided in this repository.
#### Downloading the images
To download the image dataset view this link:
https://drive.google.com/open?id=1_aKkNb15ZWnGHbUpcpShYuiCSZleaH6J

Once the \_base.zip file has been downloaded, extract it the keras_model folder provided in this repository. Or you may place it anywhere in your preferred directory. However, be aware you will need to adjust your paths in the python files to acommodate for the different image paths.

#### Running ml_average_dimension.py
To gather the average dimension of all the images in a directory please adjust the following lines:
- Line 6: Change the directory to the absolute directory path containing the \_base folder that you extracted.
- Line 13: Ensure TRAIN_DIR is set to "\_base/training"
- Line 14: Ensure VAL_DIR is set to "\_base/validation"

After running the program it will output the average height and dimension. Pick the smaller of the 2 to use as your training size
#### Running ml_basic_predict.py
To predict images in a directory (if you want to predict just a photo it must also be in the corresponding directory) make sure you have a h5 model available in the same working directory and change the following lines to your needs:
- Line 31: Change PREDICT_DIR to folder relative path containing the images to predict. Ex: "\_base/prediction"
- Lind 34: Set target_dim to the image prediction size for the model
- Line 40: Change the relative path in load_model to a relative path containing the model you want to be tested if it is different.
- Example: "ml_basic_tf_acc5.h5"
- Ensure the model is an h5 file not a tflite file
#### Running ml_basic_test.py
To run a testing session make sure you have a h5 model available in the same working directory and change the following lines to your needs:
- Line 13: Change TEST_DIR to a relative path containing the testing images
- Line 15: Adjust image_dim to the imaage dimensions of the model if needed
- Line 38: Change the relative path in load_model to a relative path containing the model you want to be tested if it is different.
- Example: "ml_basic_tf_acc5.h5"
- Ensure the model is an h5 file not a tflite file

After changing the appropriate lines:
1. Run the program
2. The program will output the lost and accuracy of the testing
#### Running ml_basic_tf.py
To train your own model with different hyperparameters make sure you have an image dataset to train on and change the following lines of code:
- Line 26: Change TRAIN_DIR to a relative path containing the training image data. Example: "\_base/training"
- Line 27: Change VAL_DIR to a relative path containing the validation image data. Example: "\_base/validation"
- Line 29: Change SAVE_DIR to a relative directory or absolute directory you would like to save the model to
- Line 30: Change FILENAME to the filename you want the model to output as
- Line 33: Change image_dim to the preferred image dimensions you want to train the model on
- Line 37: Change epochs to the number of epochs you want the model to iterate over
- Line 38: Change earlystop_patience
- Line 39: Change reducelr_patience

The paths for the directories need to be relative paths not absolute paths. Note that if you change the image dimension (image_dim) and produce your own model then all the files that require loading the model must have their image dimension parameter changed as well in order to work properly.
#### Running ml_h5_to_tflite.py
To convert an h5 model file change the following lines:
- Line 8: Change input_filename to the relative path to the file.
- Line 9: Change output_filename to the relative path of the output file to save as.

Reminder, the paths need to be relative to the working directory and must include the filename at the end of the path so the model being loaded in must also be in the same directory as the program.
### Running the Android App
