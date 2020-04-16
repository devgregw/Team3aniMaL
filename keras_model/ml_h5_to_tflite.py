# Author: Jonah Bui & Burhanuddin Chinwala
# March 23, 2020
# Purpose: Load in a h5 Keras model and convert it to a tflite file
import tensorflow as tf
from tensorflow.keras import Model
from tensorflow.keras.models import load_model

input_filename = 'ml_basic_tf_acc.h5'
output_filename = 'ml_basic_tf_acc.tflite'

model = load_model(input_filename)

converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()
open(output_filename,'wb').write(tflite_model)