# Author: Jonah Bui
# Date: March 17, 2020
# Purpose: Used to test model by feeding in a directory of images sorted in subdirectories
import os

# Enable AMD GPU usage w/ Keras
import tensorflow as tf
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.models import load_model

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
    class_mode='categorical',
)
print(test_data.class_indices)
#--------------------------------------------------------------------------------------------------------------------------------
# Model Testing
#--------------------------------------------------------------------------------------------------------------------------------
model = load_model('ml_basic_keras_categorical_87.h5')
results =  model.evaluate(test_data)

print(f'{model.metrics_names}{results}')



