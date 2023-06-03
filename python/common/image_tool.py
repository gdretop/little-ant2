#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import logging

import matplotlib.pyplot as plt
import numpy as np

LOG_FORMAT = "%(asctime)s %(levelname)s %(process)d-%(processName)s-%(thread)d-%(thread)s: %(message)s"
logging.basicConfig(level=logging.INFO, format=LOG_FORMAT)


def show_img(data, **kwargs):
    dpi = plt.figure().dpi
    plt.figure(figsize=(data.shape[1] / dpi, data.shape[0] / dpi))
    plt.imshow(data, cmap='Greys_r')
    plt.title(kwargs.get("title", ""))
    plt.xlabel(kwargs.get("xlabel", ""))
    plt.ylabel(kwargs.get("ylabel", ""))
    plt.show()


def find_little_dot(gray, value_limit, **kwargs):
    """
    在图中找到小于阈值的像素联通量集合
    :param gray: 灰度图
    :param value_limit:  灰度阈值 小于该阈值的纳入计算
    :param min_size: 联通量最小像素点
    :param max_size: 联通量最大像素点
    :param x_mix: 检测区域 x 轴横坐标
    :param y_mix: 检测区域
    :param x_max: 检测区域 x 轴横坐标
    :param y_max: 检测区域
    :param judge_fun: 检查函数,返回true则处理该联通量
    :param tilt_dir: 是否通过对角方向扩散
    :return:
    """
    min_size = kwargs.get('min_size', 0)
    max_size = kwargs.get('max_size', 50)
    x_min = kwargs.get('x_min', 0)
    x_max = kwargs.get('x_max', gray.shape[1])
    y_min = kwargs.get('y_min', 0)
    y_max = kwargs.get('y_max', gray.shape[0])
    target_value = kwargs.get('target_value', 0)
    judge_fun = kwargs.get('judge_fun', None)
    tilt_dir = kwargs.get('tilt_dir', False)
    show_log = kwargs.get('show_log', False)

    flag = np.full(gray.shape, 0)
    find_map = np.full(gray.shape, 255)
    dir = [[0, 1], [0, -1], [-1, 0], [1, 0]]
    if tilt_dir:
        dir += [[1, -1], [1, 1], [-1, 1], [-1, -1]]

    def deep_find(i, j, list):
        if flag[i][j] == 1:
            return []
        deque = [(i, j)]
        while len(deque) > 0:
            i, j = deque.pop()
            if (flag[i][j] == 1):
                continue
            flag[i][j] = 1
            if (gray[i][j] > value_limit):
                continue
            list.append([i, j])
            for item in dir:
                y = i + item[0]
                x = j + item[1]
                if (x < 0 or y < 0 or x >= x_max or y >= y_max or flag[y][x] == 1):
                    continue
                deque.append((y, x))

    list = []
    for i in range(y_min, y_max):
        for j in range(x_min, x_max):
            if show_log and i % 1000 == 0 and j == 0:
                logging.info("i = %d", i)
            if flag[i][j] == 1:
                continue
            if gray[i][j] > value_limit:
                flag[i][j] = 1
                continue
            list.clear()
            deep_find(i, j, list)
            if judge_fun is not None:
                if (judge_fun(list)):
                    for item in list:
                        find_map[item[0]][item[1]] = target_value
                    continue
            elif len(list) >= min_size and len(list) <= max_size:
                # print(i,j,len(list))
                for item in list:
                    find_map[item[0]][item[1]] = target_value
                continue
    return find_map


def trans_image(image, find_map, value, **kwargs):
    """
    对图片矩阵进行转换，根据是否命中find_map的点做差异化处理
    :param image: 原始图像
    :param find_map:  命中像素点和image二维一致
    :param target_dot_fun: 命中点处理方法
    :param other_dot_fun: 非命中点处理方法
    :param target_value: 命中点替换值
    :param other_value: 非命中点替换值
    :return:
    """
    target_dot_fun = kwargs.get('target_dot_fun', None)
    if target_dot_fun is None:
        target_value = kwargs.get('target_value', None)

        def target_fun(imag, i, j, **kwargs):
            return target_value

        target_dot_fun = target_fun
    other_dot_fun = kwargs.get('other_dot_fun', None)
    if other_dot_fun is None:
        other_value = kwargs.get('other_value', None)

        def other_fun(imag, i, j, **kwargs):
            return other_value

        other_dot_fun = other_fun
    new_imag = image
    for i in range(image.shape[0]):
        for j in range(image.shape[1]):
            fun = target_dot_fun
            if (find_map[i][j] != value):
                fun = other_dot_fun
            if type(image[i][j]) == np.ndarray:
                for z in range(len(image[i][j])):
                    new_imag[i][j][z] = fun(image, i, j, z=z)
            else:
                new_imag[i][j] = fun(image, i, j)
    return new_imag


def compare(x, y):
    x = x[0][1]
    y = y[0][1]
    if x < y:
        return -1
    if x > y:
        return 1
    return 0


def str_2_rgb(str):
    return (int(str[0:2], 16), int(str[2:4], 16), int(str[4:6], 16))

# print(str_2_rgb('00B0F0'))
# print(str_2_rgb('817709'))
# print(str_2_rgb('FFC000'))
# print(str_2_rgb('FF0000'))
# print(str_2_rgb('002060'))
# print(str_2_rgb('002060'))
# print(str_2_rgb('F0F0F0'))
