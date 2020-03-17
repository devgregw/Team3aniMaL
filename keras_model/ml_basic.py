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

import numpy as np
import matplotlib.pyplot as plt
import matplotlib.image as pltimg

# Variables that must be specified (everything else will be automatically inferred from these):
# TRAIN_DIR: where the training data is located
# VAL_DIR: where the validation data is located
# image_dim: the target dimension of the image being processed
# batch_size: the number of photos in a batch

# Directories for images
TRAIN_DIR='_128/train_animals'
VAL_DIR='_128/val_animals'

# Image target dimensions
image_dim = 128
input_shape = (image_dim, image_dim)
batch_size = 128#####

#--------------------------------------------------------------------------------------------------------------------------------
# Image preprocessing
#--------------------------------------------------------------------------------------------------------------------------------
# Generate training data with several transformations
train_gen = ImageDataGenerator(
    rescale= 1./255,
    rotation_range=10,
    #zoom_range=0.1, (Images already blurry, consider not using)
    shear_range=0.1,
    fill_mode='nearest',
    horizontal_flip=True,
)
# Just read in validation data unmodified ( want to validate on regular images )
val_gen = ImageDataGenerator(
    rescale= 1./255,
)
train_data = train_gen.flow_from_directory(
    directory=TRAIN_DIR,
    target_size=input_shape,
    batch_size=batch_size*2,
    class_mode='sparse',
)
val_data = val_gen.flow_from_directory(
    directory=VAL_DIR,
    target_size=input_shape,
    batch_size=batch_size,
    class_mode='sparse',
)

print(train_data.class_indices)

# Set parameters for models
train_size = train_data.n
val_size = val_data.n

#--------------------------------------------------------------------------------------------------------------------------------
# Class Weightings
#--------------------------------------------------------------------------------------------------------------------------------
# Fix unbalanced datasets by assigning weights to each class
# Weights are determined by a ratio between the class with the most datapoint to every other class
class_weights = {}

# Get the number of images in each directory and store to a dictionary
i = 0
for folder in listdir(TRAIN_DIR):
    list = listdir(TRAIN_DIR+'/'+folder)
    class_weights[i] = float(len(list))
    i+=1

i = 0
for folder in listdir(VAL_DIR):
    list = listdir(TRAIN_DIR+'/'+folder)
    class_weights[i] = class_weights[i] + float(len(list))
    i+=1
#--/

# Determine the most amount of data in a directory
max = 0
for j in range(i):
    if class_weights[j] > max:
        max = class_weights[j]

# Divide each dataset amount by the max value to get the ratio between each class
for j in range(i):
    class_weights[j] = max/class_weights[j]

print("Class Weights:")
print(class_weights)

#--------------------------------------------------------------------------------------------------------------------------------
# Callbacks
#--------------------------------------------------------------------------------------------------------------------------------
earlystop = EarlyStopping(
    patience=10,
    monitor='val_loss',
    restore_best_weights=True
)# Stops the training process if the model fails to impprove within 10 epochs
modelcheckpoint = ModelCheckpoint(
    filepath='model_basic.h5',
    monitor='val_loss',
    verbose=1,
    save_best_only=True 
)# Save the file after improvement of an epoch 
csvlogger = CSVLogger(
    filename='model_basic.csv',
    append=False
)# Store epoch information for analysis
reducelr = ReduceLROnPlateau(
    monitor='val_loss',
    patience= 2,
    factor=0.5,
    min_lr=0.00001
)# Reduce the learning rate if the model fails to improve
#--------------------------------------------------------------------------------------------------------------------------------
# Model Architecture
#--------------------------------------------------------------------------------------------------------------------------------
model = Sequential([
    Conv2D(64, kernel_size = (3,3), input_shape = (image_dim, image_dim, 3), activation='relu'),

    MaxPooling2D(pool_size = (2,2), strides=2),
    Dropout(0.3),

    Conv2D(128, kernel_size = (3,3), activation='relu'),
    MaxPooling2D(pool_size = (2,2), strides=2),
    Dropout(0.3),

    Conv2D(256, kernel_size = (5,5), activation='relu'),
    MaxPooling2D(pool_size = (2,2), strides=2),
    Dropout(0.3),

    AveragePooling2D(),
    Flatten(),
    Dense(512, activation = 'relu'),
    Dropout(0.5),
    Dense(train_data.num_classes, activation = 'softmax')
])
model.summary()
plot_model(model, to_file='ml_basic_visual.png')
#--------------------------------------------------------------------------------------------------------------------------------
# Compile Model
#--------------------------------------------------------------------------------------------------------------------------------
model.compile(
    optimizer = 'rmsprop', # Test adam and nadam/ best result rmsprop
    loss = 'sparse_categorical_crossentropy',
    metrics = ['accuracy'],
)
#--------------------------------------------------------------------------------------------------------------------------------
# Train Model
#--------------------------------------------------------------------------------------------------------------------------------
model.fit_generator(
    generator=train_data,
    validation_data = val_data, 
    epochs = 100, 
    steps_per_epoch=train_size*2//batch_size, 
    validation_steps=val_size//batch_size,
    class_weight=class_weights,
    callbacks=[
        modelcheckpoint,
        csvlogger,
        earlystop,
        reducelr
    ]
)