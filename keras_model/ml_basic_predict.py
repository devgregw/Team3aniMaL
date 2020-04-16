# Author: Jonah Bui & Burhanuddin Chinwala
# Date: March 17, 2020
# Description: Used to predict images in a directory
import os
from os import listdir
import numpy as np
from PIL import Image

import tensorflow as tf
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.models import load_model



# Used to find model labels for a prediction by finding the highest number in
# an array holding the models prediction
def findIndex(array):
    print(array)
    max = 0.0
    index = 0
    currentIndex = 0
    for num in array:
        if num > max:
            max = num
            index = currentIndex
        currentIndex+=1
        
    return index

# Directory if images to be processed
PREDICT_DIR = '_128/predict_animals'
labels = ['bird', 'butterfly','cat', 'dog', 'horse', 'spider']

target_dim = 352
target_size = (target_dim, target_dim)

#--------------------------------------------------------------------------------------------------------------------------------
# Model Predictions
#--------------------------------------------------------------------------------------------------------------------------------
model = load_model('ml_basic_tf_acc3.h5')

# Used to predict each image
for image in listdir(PREDICT_DIR):
    # Open and normalize images
    img = Image.open(PREDICT_DIR+'/'+image)
    img = img.resize(target_size)
    img = np.asarray(img)
    img = img.astype('float')
    img /= 255
    img = img.reshape(1, target_dim, target_dim, 3)
    
    # Predict and output result
    prediction = model.predict(img)
    prediction = prediction[0]
    maxIndex = findIndex(prediction)
    print(f'{labels[maxIndex]} predicted for file {image}.')

    i = 0
    for num in prediction:
        print(f'{labels[i]:10s}: {num:2.8f}')
        i+=1






