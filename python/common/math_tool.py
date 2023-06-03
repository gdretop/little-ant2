#!/usr/bin/env python3
# -*- coding: utf-8 -*-
class Point:
    def __init__(self, x=0, y=0, list=None, id=None, value=None):
        self.x = x
        self.y = y
        self.id = id
        self.value = value
        try:
            if list is not None:
                self.x = list[0]
                self.y = list[1]
        except BaseException as e:
            print(e)

    def copy(self):
        return self.__copy__()

    def __copy__(self):
        return Point(x=self.x, y=self.y, id=self.id, value=self.value)

    def __eq__(self, other):
        if self.x == other.x and self.y == other.y:
            return True
        return False

    def print_location(self):
        return "{}, {}".format(self.x, self.y)

    def __str__(self):
        str = "[x={}, y={}".format(self.x, self.y)
        if self.id is not None:
            str = '{}, id={}'.format(str, self.id)
        if self.value is not None:
            str = '{}, value={}'.format(str, self.value)
        return str + ']'

    def __repr__(self):
        return self.__str__()

    def __add__(self, other):
        x = self.x + other.x
        y = self.y + other.y
        return Point(x, y)

    def __sub__(self, other):
        x = self.x - other.x
        y = self.y - other.y
        return Point(x, y)

    def __mul__(self, t):
        x = self.x * t
        y = self.y * t
        return Point(x, y)

    def __truediv__(self, t):
        x = self.x / t
        y = self.y / t
        return Point(x, y)


def cross(v1, v2):
    return v1.x * v2.y - v1.y * v2.x


def count_area(p1, p2, p3):
    return cross(p1, p2, p1, p3)


def f_count_area(p1, p2, p3):
    return abs(count_area(p1, p2, p3))


def get_line_intersection(p1, v1, p2, v2):
    t1 = cross(p2 - p1, v2)
    t2 = cross(v1, v2)
    t = t1 / t2
    return p1 + (v1 * t)


EPS = 1e-5


def judge_line_and_segment_intersection(p1, v1, p3, p4):
    int_p = get_line_intersection(p1, v1, p3, p4 - p3)
    if int_p.x < min(p3.x, p4.x) - EPS or int_p.x > max(p3.x, p4.x) + EPS:
        return False
    if int_p.y < min(p3.y, p4.y) - EPS or int_p.y > max(p3.y, p4.y) + EPS:
        return False
    return True


if __name__ == '__main__':
    p1 = Point(0, 20)
    p2 = Point(0, 10)
    p3 = Point(1, 0)
    p4 = Point(2, 0)
    print(cross(p2 - p1, p3 - p4))
    print(judge_line_and_segment_intersection(p1, p2, p3, p4))
    print(get_line_intersection(p1, p2 - p1, p3, p4 - p3))
    p1 = Point(-2, -2)
    p2 = Point(2, 2)
    p3 = Point(0, 2)
    p4 = Point(2, 0)
    v1 = p1 - p2
    print(cross(p2 - p1, p3 - p4))
    print(judge_line_and_segment_intersection(p1, p2, p3, p4))
    print(get_line_intersection(p1, p2 - p1, p3, p4 - p3))
