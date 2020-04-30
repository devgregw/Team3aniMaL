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

About 66,000 photos were obtained for the classification process. There were 60,000 photos used to train the model and 6,000 used to test the model. The images were supported as much as possible. However there may be some outliers that we were not able to catch due to time constraints.

## Files Included
### keras_model
- ml_basic_predict.py: used to predict images in a directory
- ml_basic_test.py: used to test over labeled images in a directory
- ml_basic_tf.py: trains a model with a tensorflow with Keras
- ml_h5_to_tflite.py: converts a Keras h5 file to a tflite file 
- ml_average_dimensions.py: calculates the average dimension of all images in a directory
## Model
The model implemented within the app is built upon the InceptionResNetV2 pretrained model. It was able to obtain a validation accuracy above 95% and testing accuracy above 90%

## Tools Used
- AdvancedRenamer: useful for renaming large batches of files
- IfranView: also useful for renaming, but also good for image cropping, and converting batches of images to a single file type

## How to Run
### Running the Model Implementation
#### Setting up the workspace
It is recommended to use Visual Studio Code with Python installed for it. Python must also be installed on your machine and be version 3.6+ in order to work with the code provided if you plan to use it through command line.
The following libraries must also be download to work
- tensorflow (Keras should come with tensorflow)
- numpy
- PIL

Please make sure that you all this is properly installed in order to run the code. Also make sure to download the appropriate dataset and a sample model as those are not included in this repository. A sample model is especially needed if you plan on testing or predicting with the model. Also pay close attention to when an absolute or relative path is required.
#### Setting up the repository
Use github bash and clone the repository and place it in your preferred project space. Or you can download the whole repository and extract it from a zip into your preferred project space.

When I mention the current working directory of the project I am refering to the "keras_model" folder not the directory that contains both the android and keras projects.
#### Downloading the model
To download a sample model to predict and test view this link:
https://drive.google.com/drive/folders/1OD2z9ztrpB-JVjK30fnOF85GQiGl2coc?usp=sharing

Once the model file has been downloaded, place it in the keras_model folder provided in this repository. It must be in the same directory as the programs that require it.
Example: keras_model/ml_basic_model.h5
#### Downloading the images
To download the image dataset view this link:
https://drive.google.com/open?id=1_aKkNb15ZWnGHbUpcpShYuiCSZleaH6J

Once the \_base.zip file has been downloaded, extract it the keras_model folder provided in this repository. The images must be in the working directory that contains all the program that require the images. The directories should be structured like this:
keras_model/\_base/..

\_base should contains 4 subdirectories: training, validation, testing, and prediction. For training, validation and testing they include their own subdirectories: bird, butterfly, cat, dog, horse, spider. The "prediction" folder will hold just images without folders that label them.
#### Setting up ml_average_dimension.py
To gather the average dimension of all the images in a directory please adjust the following lines:
- Line 6: Change the directory to the absolute directory path containing the \_base folder that you extracted.
- Line 13: Ensure TRAIN_DIR is set to "\_base/training"
- Line 14: Ensure VAL_DIR is set to "\_base/validation"
#### Running ml_average_dimension.py
1. In visual studio code with python installed you can simply click the run button to execute the file.
2. Program outputs the average height and width dimensions. Pick the smaller of the 2 to use as image size.
#### Setting up ml_basic_predict.py
To predict images in a directory (if you want to predict just a photo it must also be in the corresponding directory) make sure you have a h5 model available in the same working directory and change the following lines to your needs:
- Line 31: Change PREDICT_DIR to folder relative path containing the images to predict. Ex: "\_base/prediction"
- Lind 34: Set target_dim to the image prediction size for the model
- Line 40: Change the relative path in load_model to a relative path containing the model you want to be tested if it is different.
- Example: "ml_basic_tf_acc5.h5"
- Ensure the model is an h5 file not a tflite file
#### Running ml_basic_predict.py
1. In visual studio code with python installed you can simply click the run button to execute the file.
2. The program will output the accuracy of each image file in the directory alongside the prediciton the model made.
#### Setting up ml_basic_test.py
To run a testing session make sure you have a h5 model available in the same working directory and change the following lines to your needs:
- Line 13: Change TEST_DIR to a relative path containing the testing images
- Line 15: Adjust image_dim to the imaage dimensions of the model if needed
- Line 38: Change the relative path in load_model to a relative path containing the model you want to be tested if it is different.
- Example: "ml_basic_tf_acc5.h5"
- Ensure the model is an h5 file not a tflite file
#### Running ml_basic_test.py
1. Prepare an h5 model in the working directory to be loaded into the program.
2. In visual studio code with python installed you can simply click the run button to execute the file.
3. The program will output the loss and accuracy of the model testing session.
#### Setting up ml_basic_tf.py
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
#### Running ml_basic_tf.py
1. Make sure to have the image dataset in the proper directory stucture before feeding it into the model.
2. In visual studio code with python installed you can simply click the run button to execute the file.
3. The program will ouput the summary of the model and info about each epoch.
4. The program will produce a CSV file with all epochs logged, an h5 file, and a tflite file (if all epochs are completed).
#### Setting up ml_h5_to_tflite.py
To convert an h5 model file change the following lines:
- Line 8: Change input_filename to the relative path to the file.
- Line 9: Change output_filename to the relative path of the output file to save as.
Reminder, the paths need to be relative to the working directory and must include the filename at the end of the path so the model being loaded in must also be in the same directory as the program. Also the input file must be a h5 model with the h5 file extension explicitly. Likewise, the output file must have a tflite extension explicitly stated.
#### Running ml_h5_to_tflite.py
1. Prepare an h5 model in the working directory to be loaded into the program.
2. In visual studio code with python installed you can simply click the run button to execute the file.
3. The program will ouput a tflite file from the h5 file.

---

### Running the Android App
#### Importing the Project into Android Studio
1. Open Android Studio.
2. Click "Open an Existing Android Studio project".
3. Navigate to the root folder of the Android project and click "Open".
4. Wait for Gradle sync and indexing to complete.
#### Compiling the App
1. In Android Studio, use the Device menu or click Run > Select Device... to select a connected Android device or emulator running at least Android 7.0 Nougat (API Level 24).
2. Click the Run button to compile and start debugging on the selected device or emulator.
#### Using the App
1. Launch the app on your selected device or emulator.
2. Log in or create an account.
3. On the home screen, tap the large orange button in the center to select a photo from your camera, internal storage, gallery, or a third-party app.
4. After capturing or selecting an image, crop it to ensure that unnecessary details are removed and that the subject is clearly visible.
