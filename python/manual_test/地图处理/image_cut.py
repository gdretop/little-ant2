import numpy as np
from skimage.transform import (hough_line, hough_line_peaks, hough_circle,
hough_circle_peaks)
from skimage.draw import circle_perimeter
from skimage.feature import canny
from skimage.data import astronaut
from skimage.io import imread, imsave
from skimage.color import rgb2gray, gray2rgb, label2rgb
from skimage import img_as_float
from skimage.morphology import skeletonize
from skimage import data, img_as_float
import matplotlib.pyplot as pylab
from matplotlib import cm
from skimage.filters import sobel, threshold_otsu
from skimage.feature import canny
from skimage.segmentation import felzenszwalb, slic, quickshift, watershed
from skimage.segmentation import mark_boundaries, find_boundaries
import cv2
image_output_path = "/Users/yuwanglin/project/media_process/python/manual_test/地图处理/image/120.jpg"
# coins = data.coins()
coins = cv2.imread(image_output_path,cv2.IMREAD_GRAYSCALE)

def show_pylab2(data,hist):
    fig, axes = pylab.subplots(1, 2, figsize=(20, 10))
    axes[0].imshow(data, cmap=pylab.cm.gray, interpolation='nearest')
    axes[0].axis('off'), axes[1].plot(hist[1][:-1], hist[0], lw=2)
    axes[1].set_title('imag')
    pylab.show()

def show_pylab(data):
    fig, axes = pylab.subplots(figsize=(10, 6))
    axes.imshow(data, cmap=pylab.cm.gray, interpolation='nearest')
    axes.set_title('Canny detector'), axes.axis('off'), pylab.show()

hist = np.histogram(coins, bins=np.arange(0, 256), normed=True)
show_pylab2(coins,hist)
edges = canny(coins, sigma=2)
# show_pylab(edges)

gray = np.zeros(edges.shape)
for i in range(gray.shape[0]):
    for j in range(gray.shape[1]):
        gray[i][j] = int(255 if edges[i][j] else 0)
kernel = np.ones((8,8), np.uint8)
erosion = cv2.dilate(gray, kernel)
show_pylab(erosion)
