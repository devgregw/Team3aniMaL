# Author: Jonah Bui
# Date: March 17, 2020
# Purpose: Used to test model
import os
from os import listdir

# Enable AMD GPU usage w/ Keras
import plaidml
from plaidml import keras
from keras.layers import Conv2D, Dense, Flatten, MaxPooling2D, Dropout, BatchNormalization, GlobalAveragePooling2D, AveragePooling2D
from keras.preprocessing.image import ImageDataGenerator, img_to_array, load_img
from keras.callbacks import CSVLogger, ModelCheckpoint, EarlyStopping, ReduceLROnPlateau
from keras.utils import plot_model
from keras import optimizers
from keras import Sequential
from keras.models import load_model

import numpy as np
import matplotlib.pyplot as plt
import matplotlib.image as pltimg

labels = ['bird', 'butterfly','cat', 'dog', 'horse', 'spider']

# Directories for images
TEST_DIR='_128/test_animals'

# Image target dimensions
image_dim = 128
input_shape = (image_dim, image_dim)
batch_size = 128

#--------------------------------------------------------------------------------------------------------------------------------
# Image preprocessing
#--------------------------------------------------------------------------------------------------------------------------------

# Just read in validation data unmodified ( want to validate on regular images )
test_gen = ImageDataGenerator(
    rescale= 1./255,
)
test_data = test_gen.flow_from_directory(
    directory=TEST_DIR,
    target_size=input_shape,
    batch_size=batch_size,
    class_mode='sparse',
)
print(test_data.class_indices)
#--------------------------------------------------------------------------------------------------------------------------------
# Model Testing
#--------------------------------------------------------------------------------------------------------------------------------
model = load_model('model_basic.h5')
results =  model.evaluate_generator(test_data)

print(f'{model.metrics_names}{results}')



