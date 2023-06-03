#!/usr/bin/env python3
# -*- coding: utf-8 -*-
from queue import Queue

import cv2

from common.algorithm import STRICT_DIRECTION
from common.image_tool import show_img
from common.math_tool import Point
from config.mori_map_config import SCALE_SIZE

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
"""
type_map = {
    0: {"name": "草地岩石", "useful": False, "reach": False},
    1: {"name": "栏栅", "useful": False, "reach": False},
    2: {"name": "空地", "useful": False, "reach": True},
    3: {"name": "山洞", "useful": True, "reach": True},
    4: {"name": "坐标牌", "useful": True, "reach": True},
    5: {"name": "建筑", "useful": True, "reach": True},
    6: {"name": "商店", "useful": True, "reach": True},
    7: {"name": "医院", "useful": True, "reach": True},
    8: {"name": "教堂", "useful": True, "reach": True},
    9: {"name": "加油站", "useful": True, "reach": True},
    10: {"name": "广场", "useful": True, "reach": True},
    11: {"name": "帐篷", "useful": True, "reach": True},
    12: {"name": "工厂", "useful": True, "reach": True},
    13: {"name": "房屋", "useful": True, "reach": True},

}


def bfs(map, point, row_n, column_n):
    result_map = [[[-1, 0] for j in range(column_n)] for i in range(row_n)]
    point_que = Queue()
    point_que.put(point)
    result_map[point.y][point.x][0] = 0
    while not point_que.empty():
        point = point_que.get()
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
        print("\n路径信息 {}".format(str(point_way)))
        for i in range(1, len(points)):
            result_map = result_map_list[point_way[i - 1].id]
            result = result_map[point_way[i].y][point_way[i].x]
            if result[0] < 0:
                print("错误路径 {} => {}, 距离结果: {}".format(str(point_way[i - 1]), str(point_way[i]), result))
                raise ValueError("错误路径")
            steps += result[0]
            buildings += result[1]
        way_result = {
            "steps": steps,
            "buildings": buildings,
            "point_way": point_way.copy()
        }
        print("当前路径结果{}".format(str(way_result)))
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


def get_the_way_between_2_point(map, result_map, start_point, end_point, row_n, column_n):
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
    print("\n从当前点{},{}出发,需要走{}步,经过建筑{}".format(path[0].x + 1, path[0].y + 1, target_cell[0], target_cell[1]))
    step = 1
    for i in range(1, len(path)):
        cur_p = path[i]
        pre_p = path[i - 1]
        way_buildings = find_target_from_map(map, pre_p, cur_p)
        msg = ""
        if cur_p.x == pre_p.x:
            if cur_p.y > pre_p.y:
                msg = ("第{}次: 向下走{}步到达({},{})".format(step, cur_p.y - pre_p.y, cur_p.x + 1, cur_p.y + 1))
            if cur_p.y < pre_p.y:
                msg = ("第{}次: 向上走{}步到达({},{})".format(step, pre_p.y - cur_p.y, cur_p.x + 1, cur_p.y + 1))
        if cur_p.y == pre_p.y:
            if cur_p.x > pre_p.x:
                msg = ("第{}次: 向右走{}步到达({},{})".format(step, cur_p.x - pre_p.x, cur_p.x + 1, cur_p.y + 1))
            if cur_p.x < pre_p.x:
                msg = ("第{}次: 向左走{}步到达({},{})".format(step, pre_p.x - cur_p.x, cur_p.x + 1, cur_p.y + 1))
        if way_buildings:
            msg = msg + "\n路上经过{}".format(str(way_buildings))
        print(msg)
        step += 1


def find_nearest_way(points):
    for point in points:
        point.x = point.x - 1
        point.y = point.y - 1
    dir = '/Users/yuwanglin/project/media_process/python/manual_test/地图处理/'
    output_path = dir + '生存之路点图gray.png'
    image = cv2.imread(output_path, cv2.IMREAD_GRAYSCALE)
    for i in range(301):
        for j in range(301):
            image[i][j] = image[i][j] / SCALE_SIZE
    result_map_list = []
    for index, point in enumerate(points):
        result_map = bfs(image, point, 301, 301)
        result_map_list.append(result_map)
        # break
    print('')
    for i in range(1, len(points)):
        print("距离点{}({}):(距离,经过最多建筑数){}:".format(i, str(points[i]), str(result_map_list[0][points[i].y][points[i].x])))
    find_best_way(result_map_list, points, [points[0]])
    global best_result
    print("\n {}".format(str(best_result)))
    for i in range(1, len(points)):
        get_the_way_between_2_point(image, result_map_list[0], points[0].copy(), points[i].copy(), 301, 301)
    # cv2.imshow('x', image)


# 测试
points = [
    Point(x=160, y=152, id=0),
    Point(x=90, y=132, id=1),
    Point(x=176, y=31, id=2),
    Point(x=171, y=265, id=3)
]

points = [
    # Point(x=130, y=147, id=0),
    # Point(x=100, y=129, id=1),
    # Point(x=148, y=82, id=1), # 12.24坐标
    # Point(x=176, y=31, id=0),
    # Point(x=180, y=38, id=0), #12.25
    Point(x=160, y=128, id=0), #12.26
    Point(x=171, y=265, id=1)
]
find_nearest_way(points)
# show_img(image)
