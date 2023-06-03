#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import functools
import math
import sys
import cv2
import numpy as np
import imutils
import logging
from ocr.common.common_tool import *
from ocr.common import image_tool


LOG_FORMAT = "%(asctime)s %(levelname)s %(process)d-%(processName)s-%(thread)d-%(thread)s: %(message)s"
logging.basicConfig(level=logging.INFO, format=LOG_FORMAT)


# 标准霍夫线变换
# https://blog.csdn.net/tengfei461807914/article/details/77466796
def line_detection(image, rho_precision, theta_precision, accumulator_thr, **kwargs):
    """
    参数 示例 1, np.pi/180/5, 600
    :param image: 图片
    :param rho_precision: 极坐标rho的精度
    :param theta_precision:  极坐标角度精度
    :param accumulator_thr: 累计值精度
    :param kwargs: show_log 输出日志  show_imge 输出过程图片
    :return:
    """
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    show_log = kwargs.get('show_log', False)
    show_imge = kwargs.get('show_imge', False)
    if show_log:
        logging.info("{} {} {}".format(rho_precision, theta_precision, accumulator_thr))
    edges = cv2.Canny(gray, 50, 150, apertureSize=3)  # apertureSize参数默认=3
    if show_imge: image_tool.show_img(edges)
    lines = cv2.HoughLines(edges, rho_precision, theta_precision, accumulator_thr)
    lines = sorted(lines, key=functools.cmp_to_key(image_tool.compare))
    if show_log: logging.info("找到直线数量:%d", len(lines))
    result = []
    for line in lines:
        rho, theta = line[0]  # line[0]存储的是点到直线的极径和极角，其中极角是弧度表示的。
        x = np.cos(theta)  # theta是弧度
        y = np.sin(theta)
        x0 = x * rho  # 代表x = r * cos（theta）
        y0 = y * rho  # 代表y = r * sin（theta）
        if show_imge: cv2.circle(image, (int(x0), int(y0)), 10, [0, 255, 0], thickness=5)

        x_h = -y
        y_h = x
        line_len = math.sqrt(gray.shape[0] ** 2 + gray.shape[1] ** 2)
        # line_len = 1000
        x1 = int(x0 + line_len * -x_h)  # 计算直线起点横坐标
        y1 = int(y0 + line_len * -y_h)  # 计算起始起点纵坐标
        x2 = int(x0 + line_len * x_h)  # 计算直线终点横坐标
        y2 = int(y0 + line_len * y_h)  # 计算直线终点纵坐标    注：这里的数值1000给出了画出的线段长度范围大小，数值越小，画出的线段越短，数值越大，画出的线段越长
        if show_imge:cv2.line(image, (x1, y1), (x2, y2), (0, 0, 255), 2)  # 点的坐标必须是元组，不能是列表。
        line_data = {
            "rho": rho,
            "theta": theta,
            "vector": (x, y),
            "line_dir": (x_h, y_h),
            "point":(x0,y0),
            "start": (x1, y1),
            "end": (x2, y2)
        }
        if show_log: logging.info("直线表示:" + str(line_data))
        result.append(line_data)
    if show_imge: image_tool.show_img(image)
    return result


# 统计概率霍夫线变换
def line_detect_possible_demo(image):
    """
    暂未使用
    :param image:
    :return:
    """
    gray = cv2.cvtColor(image, cv2.COLOR_RGB2GRAY)
    edges = cv2.Canny(gray, 50, 100, apertureSize=3)  # apertureSize参数默认其实就是3
    image_tool.show_img(edges)
    lines = cv2.HoughLinesP(edges, 1, np.pi / 180, 60, minLineLength=image.shape[0] / 3, maxLineGap=100)
    for line in lines:
        x1, y1, x2, y2 = line[0]
        cv2.line(image, (x1, y1), (x2, y2), (0, 0, 255), 2)
    image_tool.show_img(image)


def line_merge(lines, **kwargs):
    """
    将直线进行排序并合并相近直线
    :param lines:
    :param kwargs:
    :return:
    """
    show_log = kwargs.get('show_log', False)
    lines = sorted(lines, key=functools.cmp_to_key(lambda x, y: compare(x['rho'], y['rho'])))
    if show_log:
        for line in lines:
            logging.info('排序后直线:' + str(line))
    min_dist = kwargs.get("min_dist", 20)
    result = [lines[0]]
    for i in range(1, len(lines), 1):
        if lines[i]['rho'] - lines[i - 1]['rho'] <= min_dist:
            continue
        result.append(lines[i])
    if show_log:
        for line in result:
            logging.info('合并后直线:' + str(line))
    return result


def line_merge_and_sort_hv(lines, vertical_start=np.pi / 4, vertical_end=np.pi + np.pi / 4, **kwargs):
    """
    将直线分为水平和垂直两类，同时对各类内部相近直线做聚合只取一条
    实测发现 （100,0）是水平方向, (0,100)是垂直方向，
    :param lines: 直线列表，格式如line_detection
    :param vertical_start: 垂直直线判定极角开始值
    :param vertical_end: 垂直直线判定极角结束值
    :param kwargs: show_log 显示日志   min_dist 合并为相近直线的举例阈值
    :return:
    """
    vertical = []
    horizontal = []
    for line in lines:
        if line['theta'] > vertical_start and line['theta'] < vertical_end:
            horizontal.append(line)
        else:
            vertical.append(line)
    vertical = line_merge(vertical, **kwargs)
    horizontal = line_merge(horizontal, **kwargs)
    return {
        "vertical": vertical,
        "horizontal": horizontal
    }

def find_line(img_path,**kwargs):
    """
    解析图片上的表格直线返回横纵两个方向的线
    :param img_path:
    :param kwargs: show_img 是否显示图片  show_log 是否打印日志
    :return:
    """
    show_log = kwargs.get('show_log',False)
    show_img = kwargs.get('show_img',False)
    image = cv2.imread(img_path)
    lines = line_detection(image, 1, np.pi / 180 / 6, int(min(image.shape[0], image.shape[1]) / 3), show_log=False,
                           show_imge=False)
    result = line_merge_and_sort_hv(lines, show_log=show_log)
    if show_img:
        white_img = np.full(image.shape, 255, dtype=np.uint8)
        white_img = cv2.imread(img_path)
        white_img = np.ascontiguousarray(white_img, dtype=np.uint8)
        cv2.circle(white_img, (int(0), int(100)), 10, [0, 255, 0], thickness=5)
        cv2.circle(white_img, (int(100), int(0)), 10, [0, 0, 255], thickness=5)
        for line in result['vertical'] + result['horizontal']:
            cv2.line(white_img, line['start'], line['end'], (0, 0, 255), 2)
        image_tool.show_img(white_img)
    return result
if __name__ == "__main__":
    pre_img_path_format = "/Users/yuwanglin/pycharmProjects/frist/ocr/image/pre_line_daxin_0.png"
    find_line(pre_img_path_format, show_img=True,show_log=True)
    # image = cv2.flip(image,1,dst=None)
    # image = imutils.rotate_bound(image, -10)
    # image_tool.show_img(image)