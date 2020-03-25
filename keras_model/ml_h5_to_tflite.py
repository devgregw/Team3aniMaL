# Author: Jonah Bui
# March 23, 2020
# Purpose: Load in a h5 Keras model and convert it to a tflite file
import tensorflow as tf
from tensorflow.keras import Model
from tensorflow.keras.models import load_model

input_filename = 'Models/ml_basic_tf_2.h5'
output_filename = 'Models/ml_basic_tf_2.tflite'

model = load_model(input_filename)

converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()
open(output_filename,'wb').write(tflite_model)