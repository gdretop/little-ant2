#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import math

import pandas

from common.pdf_tool import *
from common.image_tool import *
from common.table_tool import divide_blocks_by_lines, inside_row_blocks_divide_into_table, table_cell_merge, merge_divided_blocks_into_line_table
from common.find_line import find_line
import cv2
import sys
import logging
from paddleocr import PaddleOCR

# from common.find_line import *

LOG_FORMAT = "%(asctime)s %(levelname)s %(process)d-%(processName)s-%(thread)d-%(thread)s: %(message)s"
logging.basicConfig(level=logging.INFO, format=LOG_FORMAT)
WORK_DIR = '/Users/yuwanglin/Desktop/'
IMG_DIR = "/Users/yuwanglin/pycharmProjects/frist/ocr/image/"
ZOOM = 4
IMG_PATH_FORMAT = IMG_DIR + "{}_{}.png"
PRE_IMG_PATH_FORMAT = IMG_DIR + "pre_{}_{}.png"
DENOISE_IMG_PATH_FORMAT = IMG_DIR + "denoise_{}_{}.png"
PRE_LINE_PATH_FORMAT = IMG_DIR + "pre_line_{}_{}.png"


def pdf_to_png(file_name):
    """
    step: pdf 转成 png 格式
    :param file_name:
    :return:
    """
    pdf_path = WORK_DIR + file_name
    page_count = pdf_image(pdf_path, IMG_DIR, file_name.split('.')[0], ZOOM, ZOOM, 0)
    return page_count


GRAY_THRESHOLD = 170


def pre_process(file_whole_name, page_no):
    """
    切割表格框中内容，将内容和表格框分别保存到图片
    :param file_name:
    :param page_no:
    :return: 保存切割后图片和框线图地址
    """
    file_name = file_whole_name.split('.')[0]

    margin_ub = 10
    margin_lr = 20
    # if page_no == 1: break
    img_path = IMG_PATH_FORMAT.format(file_name, page_no)
    img = cv2.imread(img_path)
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    # show_img(gray)
    # step 找到表格框
    find_map = find_little_dot(gray, GRAY_THRESHOLD, min_size=gray.shape[0] * 6, max_size=sys.maxsize,
                               show_log=True)
    # show_img(find_map)
    min_x = sys.maxsize
    max_x = 0
    min_y = sys.maxsize
    max_y = 0
    for i in range(gray.shape[0]):
        for j in range(gray.shape[1]):
            if find_map[i][j] == 0:
                min_x = min(min_x, i)
                max_x = max(max_x, i)
                min_y = min(min_y, j)
                max_y = max(max_y, j)
    gray = gray[min_x - margin_ub:max_x + margin_ub, min_y - margin_lr:max_y + margin_lr]
    line_find_map = find_map[min_x - margin_ub:max_x + margin_ub, min_y - margin_lr:max_y + margin_lr]
    pre_line_img_path = PRE_LINE_PATH_FORMAT.format(file_name, page_no)
    cv2.imwrite(pre_line_img_path, line_find_map)

    # show_img(gray)
    # show_img(line_find_map)

    pre_img_path = PRE_IMG_PATH_FORMAT.format(file_name, page_no)
    cv2.imwrite(pre_img_path, gray)
    return {
        pre_img_path,
        pre_line_img_path
    }


def denois(file_name, page_no, area_list,**kwargs):
    """
    去除图片上小圆圈噪点
    :param file_name: 文件名
    :param page_no: 页编号
    :param area_list: 需要去噪的区域列表
    :return:
    """

    # step 去除背景噪点
    def other_dot_fun(imag, i, j):
        return imag[i][j]
    show_imge = kwargs.get('show_imge',False)
    img_path = PRE_IMG_PATH_FORMAT.format(file_name, page_no)
    img = cv2.imread(img_path)
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    for rect in area_list:
        args = rect
        rect.update(kwargs)
        if 'max_size' not in args:
            args['max_size'] = math.ceil(ZOOM ** 2 - ZOOM)
        find_map = find_little_dot(gray, GRAY_THRESHOLD,**args)
        if show_imge: show_img(find_map)
        gray = trans_image(gray, find_map, 0, target_value=255, other_dot_fun=other_dot_fun)
    denoise_img_path = DENOISE_IMG_PATH_FORMAT.format(file_name, page_no)
    cv2.imwrite(denoise_img_path, gray)
    return denoise_img_path


def filter_fun(block, x_left, x_right, y_up, y_down):
    if x_left == 0 and x_right > 1 and y_up == 2 and y_down == 3:
        return True
    return False


def process(file_whole_name, **kwargs):
    file_name = file_whole_name.split('.')[0]
    page_count = pdf_to_png(file_whole_name)
    # step: 图片切割，找出表格范围
    start_pg = 0
    end_pg = 3
    pre_process_result = {}
    for page_no in range(start_pg, end_pg):
        logging.info("处理第{}张图片".format(page_no))
        result = pre_process(file_whole_name, page_no)
        pre_process_result[page_no] = result
    # 处理出直线
    show_log = kwargs.get("show_log", False)
    line_result = {}
    for page_no in range(start_pg, end_pg):
        # pre_line_img_path = pre_process_result[i]['pre_line_img_path']
        pre_line_img_path = PRE_LINE_PATH_FORMAT.format(file_name, page_no)
        result = find_line(pre_line_img_path, show_img=False, show_log=True)
        line_result[page_no] = result
    for page_no in range(start_pg, end_pg):
        horizontal = line_result[page_no]['horizontal']
        area_list = []
        react_top = {
            'y_max': int(horizontal[2]['point'][1]),
            'show_imge':False,
            'tilt_dir':True
        }
        area_list.append(react_top)
        react_bottom = {
            'y_min':int(horizontal[3]['point'][1]),
            'y_max':int(horizontal[4]['point'][1]),
            'show_imge': False,
            'tilt_dir': True
        }
        area_list.append(react_bottom)
        denois(file_name,page_no,area_list,show_imge=True)
    # OCR解析
    ocr = PaddleOCR(use_angle_cls=False, lang='ch')
    for page_no in range(start_pg, end_pg):
        pre_img_path = DENOISE_IMG_PATH_FORMAT.format(file_whole_name.split('.')[0], page_no)
        result = ocr.ocr(pre_img_path, cls=True)
        if show_log:
            for idx in range(len(result)):
                res = result[idx]
                for line in res:
                    logging.info(str(line))
        divided_blocks = divide_blocks_by_lines(line_result[page_no], result[0], filter_fun=filter_fun)
        blocks_table = merge_divided_blocks_into_line_table(divided_blocks)
        detail_table = inside_row_blocks_divide_into_table(blocks_table[2], line_result[page_no]['horizontal'][0], 4)
        last_page_flag = True if len(blocks_table) > 4 else False
        unmerge_table = []
        unmerge_table.append(blocks_table[0])
        unmerge_table.append(blocks_table[1])
        for i in range(len(detail_table)):
            unmerge_table.append(detail_table[i])
        for i in range(3, len(blocks_table)):
            unmerge_table.append(blocks_table[i])
        final_table = table_cell_merge(unmerge_table)
        df = pandas.DataFrame(final_table)
        df.to_excel(IMG_DIR + file_name + '_'+str(page_no)+".xlsx")
    # for page_no in range(page_count):
    #     gray = cv2.imread(PRE_IMG_PATH_FORMAT.format(page_no), cv2.IMREAD_GRAYSCALE)
    # line_find_map
    # denoise_1 = cv2.fastNlMeansDenoisingColored(img,None,3,3,7,21)


process('daxin.pdf')
