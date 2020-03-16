# Supress tensorflow cuda warning
import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'

import numpy as np
import matplotlib.pyplot as plt
import matplotlib.image as pltimg
from PIL import Image
import tensorflow as tf
from tensorflow import keras
from keras.models import Sequential
from keras.layers import Dense, Conv2D, Dropout, Flatten, MaxPooling2D, BatchNormalization
from keras.preprocessing.image import ImageDataGenerator

#test_gen = ImageDataGenerator(rescale = 1./255)
#test_gen_data = test_gen.flow_from_directory('eval',target_size=(150,150), class_mode='binary',interpolation='bilinear', save_to_dir='new')

labels = ['bird', 'cat', 'cow', 'dog', 'elephant', 'fish']

model = keras.models.load_model('ml_basic_model.h5')

img = pltimg.imread('_0_836260.jpg')
#plt.imshow(img)
#plt.show()

img = img.astype('float')
img /= 255

print(f'Shape of img: {img.shape}')
img = img.reshape(1, 128, 128, 3)
print(f'Shape of img: {img.shape}')

prediction = model.predict(img)
prediction = prediction[0]

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

print(prediction)
maxIndex = findIndex(prediction)
print(labels[maxIndex])


