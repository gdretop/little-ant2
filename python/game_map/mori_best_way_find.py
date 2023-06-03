#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import os
from queue import Queue

import cv2

from common.algorithm import STRICT_DIRECTION
from common.math_tool import Point
from config.mori_map_config import SCALE_SIZE
from game_map.item_map import type_map

"""
位置是Excel上的位置
0  位置 01  01  00B050 [  0, 176,  80] 深绿  草地岩石-fgColor.rgb
1  位置 94  26  92D050 [146, 208,  80] 浅绿  栏栅-fgColor.rgb
2  位置 07  05  F0F0F0 [  0,   0,   0] 黑色  普通地块-fgColor.rgb
3  位置 06  05  7030A0 [112,  48, 160] 紫色  山洞-fgColor.rgb  
4  位置 12  17  F196AB [241, 150, 171] 粉红  坐标牌-fgColor.rgb
5  位置 94  25  FF922B [255, 146,  43] 橘色  建筑-fgColor.theme=5
6  位置 149 152 00B0F0 [  0, 176, 240] 浅蓝  商店-fgColor.rgb
7  位置 151 148 817709 [129, 119,   9] 橙色  医院-fgColor.rgb
8  位置 140 95  FFC000 [255, 192,   0] 浅黄  教堂-fgColor.rgb
9  位置 149 148 FF0000 [255,   0,   0] 大红  加油站-fgColor.rgb
10 位置 144 124 002060 [  0,  32,  96] 深蓝  广场-fgColor.rgb
11 位置 132 94  C00000 [240, 240, 240] 深红  帐篷-fgColor.rgb 
12 位置 162 32  4290FF [ 66, 144, 255] 青蓝  工厂-fgColor.rgb 
13 位置 170 28  A219FF [162,  25, 255] 浅紫  房屋-fgColor.rgb 
14 位置 261 91  598F38 [ 89, 143,  56] 深绿  热带树-fgColor.rgb 
15 位置 264 87  E5F3F1 [229, 243, 241] 白灰  石头-fgColor.rgb 
16 位置 260 87  355522 [ 53,  85,  34] 深绿  深绿树-fgColor.rgb 
17 位置 274 130 BEFF9E [190, 255, 158] 浅绿  带花草-fgColor.rgb 
18 位置 263 99  89FF8B [137, 255, 139] 浅绿  小草-fgColor.rgb 
"""

global_result_map = None


def bfs(map, point, row_n, column_n, points):
    global global_result_map
    result_map = None
    if global_result_map is None:
        global_result_map = [[[-1, 0] for j in range(column_n)] for i in range(row_n)]
        result_map = global_result_map
    else:
        result_map = global_result_map
        for i in range(row_n):
            for j in range(column_n):
                result_map[i][j][0] = -1
                result_map[i][j][1] = 0

    target = [p.x * 400 + p.y for p in points]
    target_count = 0
    point_que = Queue()
    point_que.put(point)
    result_map[point.y][point.x][0] = 0
    while not point_que.empty():
        point = point_que.get()
        if point.x * 400 + point.y in target:
            target_count += 1
            if target_count == 4:
                break
        value = result_map[point.y][point.x][1]
        for dir in STRICT_DIRECTION:
            new_point = Point(x=point.x + dir[0], y=point.y + dir[1])
            if new_point.x <= 0 or new_point.x >= column_n or new_point.y <= 0 or new_point.y >= row_n:
                continue
            position_type = type_map[map[new_point.y][new_point.x]]
            if not position_type['reach']:
                continue
            up_value = 0
            if position_type['useful']:
                up_value = 1
            if result_map[new_point.y][new_point.x][0] >= 0:
                if result_map[new_point.y][new_point.x][0] == result_map[point.y][point.x][0] + 1:
                    result_map[new_point.y][new_point.x][1] = max(result_map[new_point.y][new_point.x][1], up_value + value)
                continue
            else:
                result_map[new_point.y][new_point.x][1] = up_value + value
                result_map[new_point.y][new_point.x][0] = result_map[point.y][point.x][0] + 1
                point_que.put(new_point)
    return result_map


best_result = None


def find_best_way(result_map_list, points, point_way=[]):
    if len(point_way) == len(points):
        steps = 0
        buildings = 0
        # print("\n路径信息 {}".format(str(point_way)))
        for i in range(1, len(points)):
            result_map = result_map_list[point_way[i - 1].id]
            result = result_map[point_way[i].id]
            if result[0] < 0:
                # print("错误路径 {} => {}, 距离结果: {}".format(str(point_way[i - 1]), str(point_way[i]), result))
                raise ValueError("错误路径")
            steps += result[0]
            buildings += result[1]
        way_result = {
            "steps": steps,
            "buildings": buildings,
            "point_way": point_way.copy()
        }
        # print("当前路径结果{}".format(str(way_result)))
        global best_result
        if best_result is None or best_result["steps"] > way_result["steps"]:
            best_result = way_result
        return
    for i in range(len(points)):
        point = points[i]
        is_ok = True
        for p in point_way:
            if p.id == point.id:
                is_ok = False
        if is_ok:
            point_way.append(point)
            find_best_way(result_map_list, points, point_way)
            point_way.pop()


def find_target_from_map(map, start_point, end_point):
    dir = [end_point.x - start_point.x, end_point.y - start_point.y]
    if dir[0] != 0: dir[0] = int(dir[0] / abs(dir[0]))
    if dir[1] != 0: dir[1] = int(dir[1] / abs(dir[1]))
    result = []
    start_point = start_point.copy()
    while start_point != end_point:
        start_point.x = start_point.x + dir[0]
        start_point.y = start_point.y + dir[1]
        try:
            type = type_map[map[start_point.y][start_point.x]]
            if type['useful']:
                result.append(type['name'])
        except Exception as e:
            print(e)
    return result


def get_the_way_between_2_point(map, result_map, start_point, end_point, row_n, column_n, steps=100):
    target_p = end_point
    end_point = end_point.copy()
    end_point.value = result_map[end_point.y][end_point.x][1]
    way = [end_point]
    way_map = map.copy()
    way_map[end_point.y][end_point.x] = 255
    pre_dir = 0
    while end_point != start_point:
        pre_point = None
        end_cell = result_map[end_point.y][end_point.x]
        last_pre_dir = pre_dir
        for i in range(4):
            dir = STRICT_DIRECTION[(i + last_pre_dir) % 4]
            new_point = Point(x=end_point.x + dir[0], y=end_point.y + dir[1])
            if new_point.x < 0 or new_point.x >= column_n or new_point.y < 0 or new_point.y >= row_n:
                continue
            cell = result_map[new_point.y][new_point.x]
            new_point.value = cell[1]
            if pre_point is None and cell[0] + 1 == end_cell[0]:
                pre_point = new_point
                pre_dir = (i + last_pre_dir) % 4
            elif cell[0] + 1 == end_cell[0] and new_point.value > pre_point.value:
                pre_point = new_point
                pre_dir = (i + last_pre_dir) % 4
        way.append(pre_point)
        end_point = pre_point
        way_map[end_point.y][end_point.x] = 255
    # show_img(way_map)
    path = [way.pop()]
    pre_p = None
    while len(way) > 0:
        point = way.pop()
        if point.x == path[-1].x or point.y == path[-1].y:
            pre_p = point
        else:
            path.append(pre_p)
            pre_p = point
        if len(way) == 0:
            path.append(point)
            break
    target_cell = result_map[target_p.y][target_p.x]
    response_info = []
    response_info.append("从当前点{},{}出发,需要走{}步,可经过建筑{}".format(path[0].x + 1, path[0].y + 1, target_cell[0], target_cell[1]))
    step = 1
    steps_bak = steps
    for i in range(1, len(path)):
        cur_p = path[i]
        pre_p = path[i - 1]
        way_buildings = find_target_from_map(map, pre_p, cur_p)
        msg = ""
        move = max(abs(cur_p.y - pre_p.y), abs(cur_p.x - pre_p.x))
        if cur_p.x == pre_p.x:
            if cur_p.y > pre_p.y:
                msg = ("第{}次:Y+{}步到达({},{})".format(step, cur_p.y - pre_p.y, cur_p.x + 1, cur_p.y + 1))
                if move >= steps and steps > 0:
                    msg += "\n第{}步你将【停留在{},{}】".format(steps_bak, pre_p.x + 1, pre_p.y + 1 + steps)
            if cur_p.y < pre_p.y:
                msg = ("第{}次:Y-{}步到达({},{})".format(step, pre_p.y - cur_p.y, cur_p.x + 1, cur_p.y + 1))
                if move >= steps and steps > 0:
                    msg += "\n第{}步你将【停留在{},{}】".format(steps_bak, pre_p.x + 1, pre_p.y + 1 - steps)
        if cur_p.y == pre_p.y:
            if cur_p.x > pre_p.x:
                msg = ("第{}次:X+{}步到达({},{})".format(step, cur_p.x - pre_p.x, cur_p.x + 1, cur_p.y + 1))
                if move >= steps and steps > 0:
                    msg += "\n第{}步你将【停留在{},{}】".format(steps_bak, pre_p.x + 1 + steps, pre_p.y + 1)
            if cur_p.x < pre_p.x:
                msg = ("第{}次:X-{}步到达({},{})".format(step, pre_p.x - cur_p.x, cur_p.x + 1, cur_p.y + 1))
                if move >= steps and steps > 0:
                    msg += "\n第{}步你将【停留在{},{}】".format(steps_bak, pre_p.x + 1 - steps, pre_p.y + 1)
        steps -= move
        if way_buildings:
            msg = msg + "\n路上经过{}".format(str(way_buildings))
        response_info.append(msg)
        step += 1
    if steps > 0:
        msg = '\n步数富余,你将停留在{},{}'.format(path[-1].x + 1, path[-1].y + 1)
        response_info.append(msg)
    return response_info


def init(points):
    for point in points:
        point.x = point.x - 1
        point.y = point.y - 1
    # dir = '/Users/yuwanglin/project/weixin-little-ant/python/game_map/'
    dir = os.path.dirname(__file__)
    output_path = dir + os.sep + 'mori_game_map_gray.png'
    image = cv2.imread(output_path, cv2.IMREAD_GRAYSCALE)
    for i in range(301):
        for j in range(301):
            image[i][j] = image[i][j] / SCALE_SIZE
    response_str = []
    for index, point in enumerate(points):
        value = image[point.y][point.x]
        item = type_map[value]
        if not item['reach']:
            response_str.append("坐标{},{}处是一个障碍物,不可到达".format(point.x + 1, point.y + 1))
    return points, image, response_str


def count_best_way(points=[]):
    points, image, response_str = init(points)
    if response_str:
        return response_str
    result_map_list = []
    response_info = []
    for index, point in enumerate(points):
        if index == len(points) - 1:
            break
        result_map = bfs(image, point, 301, 301, points)
        result_m = [[0, 0] for i in range(len(points))]
        for i in range(len(points)):
            position = result_map[points[i].y][points[i].x]
            result_m[i][0] = position[0]
            result_m[i][1] = position[1]
        result_map_list.append(result_m)
    # 最后一个点不需要计算
    result_m = [[0, 0] for i in range(len(points))]
    result_map_list.append(result_m)
    for i in range(len(points)):
        result_m[i] = result_map_list[i][len(points) - 1]

    for i in range(1, len(points)):
        location_info = result_map_list[0][points[i].id]
        response_info.append("从起点到点{}({},{}),最短距离{},最多可经过建筑{}".format(i, points[i].x + 1, points[i].y + 1, location_info[0], location_info[1]))
    response_info.append('')
    find_best_way(result_map_list, points, [points[0]])
    global best_result
    response_info.append("最佳路线最少需要{}步,经过{}个建筑物,顺序如下".format(best_result['steps'], best_result['buildings']))
    for p in best_result['point_way']:
        response_info.append("点{} 坐标{},{}".format(p.id, p.x + 1, p.y + 1))
    return response_info


def find_path(points, steps=100):
    points, image, response_str = init(points)
    if response_str:
        return response_str
    result_map = bfs(image, points[0], 301, 301, points)
    response_info = get_the_way_between_2_point(image, result_map, points[0].copy(), points[1].copy(), 301, 301, steps)
    return response_info


def find_nearest_way(points):
    points, image, response_str = init(points)
    result_map_list = []
    for index, point in enumerate(points):
        result_map = bfs(image, point, 301, 301, points)
        result_map_list.append(result_map)
        # break
    print('')
    for i in range(1, len(points)):
        print("距离点{}({}):(距离,经过最多建筑数){}:".format(i, str(points[i]), str(result_map_list[0][points[i].y][points[i].x])))
    find_best_way(result_map_list, points, [points[0]])
    global best_result
    print("\n {}".format(str(best_result)))
    for i in range(1, len(points)):
        response_info = get_the_way_between_2_point(image, result_map_list[0], points[0].copy(), points[i].copy(), 301, 301)
        print('\n'.join(response_info))
    # cv2.imshow('x', image)


if __name__ == '__main__':
    # 测试
    points1 = [
        Point(x=203, y=213, id=0),
        Point(x=59, y=189, id=1),
        Point(x=86, y=229, id=2),
        Point(x=114, y=166, id=3)
    ]

    points2 = [
        Point(x=203, y=213, id=0),  # 12.27
        Point(x=114, y=166, id=1)
    ]
    # find_nearest_way(points)
    result = count_best_way(points1)
    print('\n'.join(result))
    print('\n')
    result = find_path(points2)
    print('\n'.join(result))
    # show_img(image)
