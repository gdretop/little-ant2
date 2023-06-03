#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sys
import traceback
import simplejson
from game_map import mori_best_way_find

from common.math_tool import Point

# 173,197#171,265
# /Users/yuwanglin/opt/miniconda3/envs/media_process/bin/python3 /Users/yuwanglin/project/weixin-little-ant/python/service/mori_map_bt_way_service 160,152#90,132#176,31#171#265
if __name__ == '__main__':
    result = {}
    try:
        args = sys.argv[1]
        data = args.split("#")
        points = []
        steps = 100
        for index, d in enumerate(data):
            if index == 2:
                steps = int(d)
                break
            p = d.split(",")
            points.append(Point(x=int(p[0]), y=int(p[1]), id=index))
        response = mori_best_way_find.find_path(points,steps)
        result['resultCode'] = 0
        result['resultString'] = '\n'.join(response)
    except BaseException as e:
        traceback.print_exc()
        result['resultCode'] = 1
        result['resultString'] = str(e)
    json_data = simplejson.dumps(result, ignore_nan=True)
    print(json_data)
