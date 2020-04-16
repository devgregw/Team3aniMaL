# Author: Jonah Bui
# Date: March 6, 2020
# Description: Used to train and produce model with a tensorflow backend
import os
from os import listdir
IMAGE_DIR ='C:\\Users\\jonah_000\\Dropbox'
os.chdir(IMAGE_DIR)
path = os.getcwd()

print(path)
# Enable AMD GPU usage w/ Keras
#import plaidml
#from plaidml import keras

import tensorflow as tf
from tensorflow.keras.layers import BatchNormalization, Conv2D, MaxPooling2D, Dropout, Activation, Flatten, Dense
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras import Sequential
from tensorflow.keras import Model
from tensorflow.keras.applications.vgg16 import VGG16
from tensorflow.keras.callbacks import CSVLogger, ModelCheckpoint, EarlyStopping, ReduceLROnPlateau
from tensorflow.keras.optimizers import SGD
from tensorflow.keras.losses import binary_crossentropy, categorical_crossentropy

# Variables that must be specified (everything else will be automatically inferred from these):
# TRAIN_DIR: where the training data is located
# VAL_DIR: where the validation data is located
# image_dim: the target dimension of the image being processed
# batch_size: the number of photos in a batch

# Directories for images
TRAIN_DIR='_base/train_animals'
VAL_DIR='_base/val_animals'

# Image target dimensions
image_dim = 256#352
input_shape = (image_dim, image_dim)
batch_size = 32
#--------------------------------------------------------------------------------------------------------------------------------
# Image preprocessing
#--------------------------------------------------------------------------------------------------------------------------------
# Generate training data with several transformations
train_gen = ImageDataGenerator(
    rescale= 1./255,
    rotation_range=30,
    shear_range=0.2,
    fill_mode='nearest',
    horizontal_flip=True,
)
# Just read in validation data unmodified ( want to validate on regular images )
val_gen = ImageDataGenerator(
    rescale= 1./255,
    horizontal_flip=True,
)
train_data = train_gen.flow_from_directory(
    directory=TRAIN_DIR,
    target_size=input_shape,
    batch_size=batch_size,
    class_mode='categorical',
)
val_data = val_gen.flow_from_directory(
    directory=VAL_DIR,
    target_size=input_shape,
    batch_size=batch_size,
    class_mode='categorical',
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
    patience=5,
    monitor='val_loss',
    restore_best_weights=True
)# Stops the training process if the model fails to impprove within 10 epochs
modelcheckpoint = ModelCheckpoint(
    filepath='ml_basic_tf_acc.h5',
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
base_model = VGG16(
    weights='imagenet',
    include_top=False,
    input_shape=(image_dim, image_dim, 3)
)
add_model = Sequential([
    Conv2D(128, kernel_size = (3,3), padding='same'),
    MaxPooling2D(pool_size = (2,2)),
    Flatten(),
    Dense(256),
    Activation(activation='relu'),
    Dropout(0.2),
    Dense(train_data.num_classes, activation = 'softmax')
])
model = Model(inputs=base_model.input, outputs=add_model(base_model.output))
model.summary()
#--------------------------------------------------------------------------------------------------------------------------------
# Optimizers
#--------------------------------------------------------------------------------------------------------------------------------
sgd = SGD(
    lr=1e-4,
    momentum=0.9
)
#--------------------------------------------------------------------------------------------------------------------------------
# Compile Model
#--------------------------------------------------------------------------------------------------------------------------------
model.compile(
    optimizer = sgd, # Test adam and nadam/ best result rmsprop
    loss = categorical_crossentropy,
    metrics = ['accuracy'],
)
#--------------------------------------------------------------------------------------------------------------------------------
# Train Model
#--------------------------------------------------------------------------------------------------------------------------------
model.fit(
    (item for item in train_data),
    validation_data = val_data, 
    epochs = 20, 
    steps_per_epoch=train_size//batch_size, 
    validation_steps=val_size//batch_size,
    class_weight=class_weights,
    callbacks=[
        modelcheckpoint,
        csvlogger,
        earlystop,
        reducelr
    ],
    #workers=8
)

# Convert and save model to a tflite file
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()
open('ml_basic_tf_acc.tflite','wb').write(tflite_model)