#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import cv2
from PIL import Image

dir = '/Users/yuwanglin/project/weixin-little-ant/python/manual_test/地图处理/'
file_path_1 = dir + 'image/map12.jpeg'
output_path = dir + 'image/result12.png'
image = Image.open(file_path_1)
# 8 行 9列
# 1080 * 9 / 8
height = image.size[1]
image = image.resize((int(height * 8.5 / 8),height))
image = image.rotate(45, expand=True)
image.save(output_path)