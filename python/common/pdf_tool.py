#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import fitz
import logging

LOG_FORMAT = "%(asctime)s %(levelname)s %(process)d-%(processName)s-%(thread)d-%(thread)s: %(message)s"
logging.basicConfig(level=logging.INFO, format=LOG_FORMAT)


def pdf_image(pdfPath, img_dir, file_name, zoom_x, zoom_y, rotation_angle):
    '''
    pdfPath pdf文件的路径
    imgPath 图像要保存的文件夹
    zoom_x x方向的缩放系数
    zoom_y y方向的缩放系数
    rotation_angle 旋转角度
    '''
    # 打开PDF文件
    pdf = fitz.open(pdfPath)
    page_count = pdf.page_count
    # 逐页读取PDF
    for pg in range(0, page_count):
        page = pdf[pg]
        # 设置缩放和旋转系数
        matrix = fitz.Matrix(zoom_x, zoom_y)
        trans = matrix.preRotate(rotation_angle)
        # 创建Pixmap对象
        pm = page.get_pixmap(matrix=trans, alpha=False)
        # 开始写图像
        pm.writePNG("{}{}_{}.png".format(img_dir, file_name, pg))
    pdf.close()
    return page_count


if __name__ == '__main__':
    pdf_path = '/Users/yuwanglin/Desktop/汇丰外币储蓄用例.pdf'
    imgPath = "/Users/yuwanglin/pycharmProjects/frist/ocr/image/"
    pdf_image(pdf_path, imgPath, 'daxin', 3, 3, 0)
