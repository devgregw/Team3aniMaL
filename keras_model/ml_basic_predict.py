import os
from os import listdir

# Enable AMD GPU usage w/ Keras
import plaidml
from plaidml import keras
from keras import optimizers
from keras import Sequential
from keras.models import load_model

import numpy as np
from PIL import Image
import matplotlib.pyplot as plt
import matplotlib.image as pltimg

# Used to print result
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

PREDICT_DIR = '_128/predict_animals'
labels = ['bird', 'butterfly','cat', 'dog', 'horse', 'spider']
target_dim = 128
target_size = (target_dim, target_dim)

#--------------------------------------------------------------------------------------------------------------------------------
# Model Predictions
#--------------------------------------------------------------------------------------------------------------------------------
model = load_model('model_basic.h5')
for image in listdir(PREDICT_DIR):
    img = Image.open(PREDICT_DIR+'/'+image)
    img = img.resize(target_size)
    img = np.asarray(img)
    img = img.astype('float')
    img /= 255
    img = img.reshape(1, target_dim, target_dim, 3)
    prediction = model.predict(img)
    prediction = prediction[0]
    maxIndex = findIndex(prediction)
    print(f'{labels[maxIndex]} predicted for file {image}.')






