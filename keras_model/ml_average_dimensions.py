# Author: Jonah Bui & Burhanuddin Chinwala
# Date: April 14, 2020
# Description: Used to determine the average sizes of images in the training and validation directory
import os
from os import listdir
os.chdir('C:\\Users\\jonah_000\\Dropbox')
path = os.getcwd()
print(path)

import numpy as np
from PIL import Image
# Directories for images
TRAIN_DIR='_base/train_animals'
VAL_DIR='_base/val_animals'

count = 0
width = 0
height = 0

print("Determining average image dimensions...")

# Get the total count and dimension of training directory
for folder in listdir(TRAIN_DIR):
    print(f"Working on training folder {folder}")
    list = listdir(TRAIN_DIR+'/'+folder)
    count+= int(len(list))
    for image in listdir(TRAIN_DIR+'/'+folder):
        img = Image.open(TRAIN_DIR+'/'+folder+'/'+image)
        width+=img.width
        height+=img.height

# Get the total count and dimension of training directory
for folder in listdir(VAL_DIR):
    print(f"Working on validation folder {folder}")
    list = listdir(VAL_DIR+'/'+folder)
    count+= int(len(list))
    for image in listdir(VAL_DIR+'/'+folder):
        img = Image.open(VAL_DIR+'/'+folder+'/'+image)
        width+=img.width
        height+=img.height

# Get the average width and height
width/=count
width = int(width)
height/=count
height = int(height)

# Output
print(f"Average width = {width}")
print(f"Average height = {height}")


