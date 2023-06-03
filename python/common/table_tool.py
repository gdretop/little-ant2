#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import functools
import logging

from ocr.common import common_tool
import numpy as np
from ocr.common.math_tool import *


def block_compare(x, y):
    """
    文本框排序函数
    :param x:
    :param y:
    :return:
    """
    if x[0][0][1] == y[0][0][1]:
        return common_tool.compare(x[0][0][0], y[0][0][0])
    return common_tool.compare(x[0][0][1], y[0][0][1])


def count_direction(line, point):
    """
    计算点在直线的哪一侧
    :param line:
    :param point:
    :return:
    """
    vector = Point(list=line['line_dir'])
    line_point = Point(list=line['point'])
    new_v = point - line_point
    result = cross(vector, new_v)
    return result


def divide_blocks_by_lines(lines, blocks, filter_fun=None, **kwargs):
    """
    将文本框根据table的线进行行列划分
    :param lines:
    :param blocks:
    :param filter_fun:
    :param kwargs:
    :return:
    """
    show_log = kwargs.get("show_log", False)
    blocks = sorted(blocks, key=functools.cmp_to_key(block_compare))
    new_blocks = []
    for block in blocks:
        column_left = 0
        column_right = 1
        for index, vertical_line in enumerate(lines['vertical']):
            if index == 0: continue
            left_dir = count_direction(vertical_line, Point(list=block[0][0]))
            if left_dir < 0:
                column_left = index
            right_dir = count_direction(vertical_line, Point(list=block[0][1]))
            if right_dir < 0:
                column_right = index + 1
        row_up = 0
        row_down = 1
        for index, vertical_line in enumerate(lines['horizontal']):
            if index == 0: continue
            up_dir = count_direction(vertical_line, (Point(list=block[0][0]) + Point(list=block[0][2])) / 2.0)
            if up_dir < 0:
                row_up = index
            down_dir = count_direction(vertical_line, (Point(list=block[0][0]) + Point(list=block[0][2])) / 2)
            if down_dir < 0:
                row_down = index + 1
        # 判断是否过滤
        if filter_fun is not None:
            result = filter_fun(block, column_left, column_right, row_up, row_down)
            if result == True:
                print(block)
                continue
        block.append({
            "column_left": column_left,
            "column_right": column_right,
            "row_up": row_up,
            "row_down": row_down
        })
        if show_log: logging.info("table位置:", block)
        new_blocks.append(block)
    return new_blocks


def merge_divided_blocks_into_line_table(blocks):
    """
    将行列划分以后的文本框放到指定单元格
    :param blocks:
    :return:
    """
    if not blocks:
        return None
    columns = 0
    rows = 0
    for block in blocks:
        columns = max(columns, block[2]['column_right'])
        rows = max(rows, block[2]['row_down'])
    table = [[[] for j in range(columns)] for i in range(rows)]
    for block in blocks:
        column = block[2]['column_left']
        row = block[2]['row_up']
        table[row][column].append(block)
    return table


def inside_row_blocks_divide_into_table(row, line, index_column):
    """
    将一行中无横线分割的文本框进行划分，得到更细的行划分结果
    :param row:
    :param line:
    :param index_column:
    :return: 划分后的table
    """
    table = []
    sorted_column = sorted(row[index_column], key=functools.cmp_to_key(block_compare))
    for index_block in sorted_column:
        new_row = [[] for i in range(len(row))]
        new_row[index_column].append(index_block)
        mid_point = (Point(list=index_block[0][0]) + Point(list=index_block[0][3])) / 2
        for i in range(len(row)):
            if i == index_column: continue
            column = row[i]
            for block in column:
                if judge_line_and_segment_intersection(mid_point, Point(list=line['line_dir']), Point(list=block[0][0]), Point(list=block[0][3])):
                    new_row[i].append(block)
        table.append(new_row)
    return table


def table_cell_merge(unmerge_table):
    """
    一个单元格内文本进行合并
    :param unmerge_table:
    :return: 合并单元格内文本的新表
    """

    def sorted_by_x(x, y):
        return common_tool.compare(x[0][0][0], y[0][0][0])

    merge_table = []
    for row in unmerge_table:
        new_row = []
        for column in row:
            if not column:
                new_row.append("")
            else:
                sort_column = sorted(column, key=functools.cmp_to_key(sorted_by_x))
                str_value_list = [item[1][0] for item in sort_column]
                new_row.append(' '.join(str_value_list))
        merge_table.append(new_row)
    return merge_table
