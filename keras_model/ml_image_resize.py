# Author: Jonah Bui & Burhanuddin Chinwala
# Date: March 13th, 2020
# Description: Converts image to RGB and resizes them
import os
from os import listdir
from PIL import Image

target_size = (256,256)
target_directory = 'resize/'

# Resizes images and OVERWRITES the original
for filename in listdir(target_directory):
    image = Image.open(target_directory+filename).convert('RGB')
    image = image.resize(target_size)
    image.save(target_directory+filename)
    print(f'Working on {filename}')