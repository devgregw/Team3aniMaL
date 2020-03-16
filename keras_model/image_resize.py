# Author: Jonah Bui
# Date: March 13th, 2020
# Description: Converts image to RGB and resizes them
import os
from os import listdir
from PIL import Image

target_size = (128,128)
target_directory = 'Spider/'
for filename in listdir(target_directory):
    image = Image.open(target_directory+filename).convert('RGB')
    image = image.resize(target_size)
    image.save(target_directory+filename)
    print(f'Working on {filename}')