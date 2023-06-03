package com.ant.little.service.findmap;

import com.alibaba.fastjson.JSONObject;
import com.ant.little.common.constents.MapElement;
import com.ant.little.common.model.Point;
import com.ant.little.common.util.ImageUtil;
import com.ant.little.core.config.EnvConfig;
import com.ant.little.service.model.FindPositionResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: little-ant
 * 描述:
 * @date: 2023/1/25
 * @Version 1.0
 **/
@Component
public class FindMapWayUtil {
    private final static Logger logger = LoggerFactory.getLogger(FindMapWayUtil.class);
    private Cache<Integer, Point[][]> localCache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .initialCapacity(10)
            .expireAfterWrite(2, TimeUnit.DAYS)
            .expireAfterAccess(2, TimeUnit.DAYS)
            .maximumSize(50)
            .build();
    @Value(value = "${mori.wholemap.path}")
    protected String moriWholeMapPath;
    private final int LOCAL_MAP_HEIGHT = 11;
    private final int LOCAL_MAP_WIDTH = 11;
    @Autowired
    private EnvConfig envConfig;
    private int[][] MORI_MAP;
    //        private BufferedImage wholeMap;
    private Map<Integer, BufferedImage> elementImgMap = new HashMap<>();
    private final int ELE_SIZE = 32;
    private final int MAP_SIZE = 300;


    @PostConstruct
    public void setUp() throws IOException {
        MORI_MAP = ImageUtil.readGrayImage(moriWholeMapPath);
//        String fileName2 = String.format("%s/game_map/whole_image.png", envConfig.getPythonCodeDir());
//        wholeMap = ImageUtil.readImage(fileName2);
        for (int i = 0; i < 20; i++) {
            String fileName = String.format("%s/game_map/image/%d.png", envConfig.getPythonCodeDir(), i);
            BufferedImage element = ImageUtil.readImage(fileName);
            elementImgMap.put(i, element);
        }
    }

    public FindPositionResponse searchPosition(int startX, int startY, int[][] match) {
        startX = Math.min(299, Math.max(0, startX - 1));
        startY = Math.min(299, Math.max(0, startY - 1));
        int height = match.length;
        int width = match[0].length;
        FindPositionResponse response = new FindPositionResponse();
        for (int y = startY; y + height < MAP_SIZE; y++) {
            int x = 0;
            if(y == startY) {
                x = startX;
            }
            for (; x + width < MAP_SIZE; x++) {
                boolean result = isMatch(x, y, match);
                if (result) {
                    response.setSearchPosition(new Point(x + 1, y + 1));
                    response.setHasNext(true);
                    response.setFindMatch(true);
                    response.setLocalMap(genLocalMap(new Point(x + LOCAL_MAP_WIDTH / 2, y + LOCAL_MAP_WIDTH / 2)));
                    return response;
                }
            }
        }
        return response;
    }

    public boolean isMatch(int x, int y, int[][] match) {
        int height = match.length;
        int width = match[0].length;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int toMatch = match[i][j];
                // 未探索
                if (toMatch == 0) {
                    continue;
                }
                int target = MORI_MAP[y + i][x + j];
                if (toMatch == 2 && target == 19) {
                    continue;
                }
                // 相等继续
                if (target == toMatch) {
                    continue;
                }
                // 岩石必须相等
                if (toMatch == 15 || target == 15) {
                    return false;
                }
                // 栅栏必须相等
                if (toMatch == 1 || target == 1) {
                    return false;
                }

                //空地
                JSONObject toJson = MapElement.getElement("" + toMatch);
                JSONObject targetJson = MapElement.getElement("" + target);
                if (!toJson.getBoolean("reach") && !targetJson.getBoolean("reach")) {
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    public String genLocalMap(Point point) {
//        int eleHeight = wholeMap.getHeight() / 300;
        int eleHeight = ELE_SIZE;
//        int eleWidth = wholeMap.getWidth() / 300;
        int eleWidth = ELE_SIZE;
        Point p = point.copy();
        p.x -= LOCAL_MAP_WIDTH / 2 + 1;
        p.y -= LOCAL_MAP_HEIGHT / 2 + 1;
        if (p.x <= 0) {
            p.x = 0;
        }
        if (p.x >= 300 - LOCAL_MAP_WIDTH) {
            p.x = 300 - LOCAL_MAP_WIDTH;
        }
        if (p.y <= 0) {
            p.y = 0;
        }
        if (p.y >= 300 - LOCAL_MAP_HEIGHT) {
            p.y = 300 - LOCAL_MAP_HEIGHT;
        }
        BufferedImage bufferedImage = new BufferedImage(LOCAL_MAP_WIDTH * eleWidth, LOCAL_MAP_WIDTH * eleHeight, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < LOCAL_MAP_HEIGHT; i++) {
            for (int j = 0; j < LOCAL_MAP_WIDTH; j++) {
                Point np = new Point(p.x + j, p.y + i);
                int type = MORI_MAP[np.y][np.x];
                BufferedImage ele = elementImgMap.get(type);
                BufferedImage newEle = new BufferedImage(ELE_SIZE, ELE_SIZE, BufferedImage.TYPE_INT_RGB);
                for (int l = 0; l < ELE_SIZE; l++) {
                    for (int k = 0; k < ELE_SIZE; k++) {
                        newEle.setRGB(l, k, ele.getRGB(l, k));
                    }
                }
                if (type == 2 || type == 19) {
                    Graphics2D pen = newEle.createGraphics();
                    pen.setColor(new Color(255, 255, 255, 255));
                    pen.setFont(new Font(null, Font.BOLD, 10));
                    pen.drawString(String.format("x %d", np.x + 1), 0, 11);
                    pen.drawString(String.format("y %d", np.y + 1), 0, 22);
                }
//                pen.drawImage(newEle,0,0,ELE_SIZE,ELE_SIZE,null);
                for (int l = 0; l < ELE_SIZE; l++) {
                    for (int k = 0; k < ELE_SIZE; k++) {
                        bufferedImage.setRGB(j * ELE_SIZE + l, i * ELE_SIZE + k, newEle.getRGB(l, k));
                    }
                }
            }
        }
//        BufferedImage bufferedImage = wholeMap.getSubimage(p.x * eleWidth, p.y * eleHeight, LOCAL_MAP_WIDTH * eleWidth, LOCAL_MAP_HEIGHT * eleHeight);
        return ImageUtil.GetBase64FromImage(bufferedImage);
    }

    private Point getDistInfo(Point start, Point target) {
        Point[][] map = bfs(MORI_MAP, start, 300, 300);
        return map[target.y][target.x];
    }

    /**
     * 计算一点到地图上任意点的距离
     *
     * @param map
     * @param point
     * @param row_n
     * @param column_n
     * @return
     */
    private Point[][] bfs(int[][] map, Point point, int row_n, int column_n) {
        Integer key = point.x * 400 + point.y;
        Point[][] resultMap = localCache.getIfPresent(key);
        if (resultMap != null) {
            return resultMap;
        }
        resultMap = new Point[row_n][];
        for (int i = 0; i < row_n; i++) {
            resultMap[i] = new Point[column_n];
        }
        Point start = new Point(point.getX(), point.getY());
        start.setDist(0);
        start.setValue(0);
        start.setBuildings(0);
        Queue<Point> queue = new LinkedList<>();
        queue.add(start);
        JSONObject eleType = MapElement.getElement(map[start.y][start.x]);
        if (eleType.getBoolean("useful")) {
            start.buildings += 1;
        }
        resultMap[start.getY()][start.getX()] = start;
        while (queue.size() > 0) {
            start = queue.poll();
            start = resultMap[start.y][start.x];
            for (int[] d : MapElement.DIR) {
                Point newP = new Point(start.x + d[0], start.y + d[1]);
                if (newP.x < 0 || newP.x >= 300 || newP.y < 0 || newP.y >= 300) {
                    continue;
                }
                newP.dist = start.dist + 1;
                newP.value = map[newP.y][newP.x];
                newP.buildings = start.buildings;
                eleType = MapElement.getElement(map[newP.y][newP.x]);
                if (!eleType.getBoolean("reach")) {
                    continue;
                }
                if (eleType.getBoolean("useful")) {
                    newP.buildings += 1;
                }
                if (resultMap[newP.y][newP.x] == null) {
                    queue.add(newP);
                    resultMap[newP.y][newP.x] = newP;
                } else if (resultMap[newP.y][newP.x].dist > newP.dist || (
                        resultMap[newP.y][newP.x].dist == newP.dist && resultMap[newP.y][newP.x].buildings < newP.buildings
                )) {
                    resultMap[newP.y][newP.x] = newP;
                }

            }
        }
        localCache.put(key, resultMap);
        return resultMap;
    }

    private static class BestResult {
        int steps = 0;
        int buildings = 0;
        List<Point> bestWay = new ArrayList<>();
    }

    /**
     * 枚举所有路线寻找最短路线
     *
     * @param points
     * @param pointWay
     * @param bestResult
     */
    private void findBestWay(List<Point> points, List<Point> pointWay, BestResult bestResult) {
        if (points.size() == pointWay.size()) {
            BestResult bestResult1 = new BestResult();
            for (int i = 1; i < pointWay.size(); i++) {
                Point distInfo = getDistInfo(pointWay.get(i), pointWay.get(i - 1));
                bestResult1.steps += distInfo.dist;
                bestResult1.buildings += distInfo.buildings;
            }
            bestResult1.bestWay.addAll(pointWay);
            if (bestResult.steps == -1) {
                bestResult.steps = bestResult1.steps;
                bestResult.buildings = bestResult1.buildings;
                bestResult.bestWay = bestResult1.bestWay;
            } else if (bestResult1.steps < bestResult.steps) {
                bestResult.steps = bestResult1.steps;
                bestResult.buildings = bestResult1.buildings;
                bestResult.bestWay = bestResult1.bestWay;
            } else if (bestResult1.steps == bestResult.steps && bestResult1.buildings > bestResult.buildings) {
                bestResult.buildings = bestResult1.buildings;
                bestResult.bestWay = bestResult1.bestWay;
            }
            return;
        }
        for (int i = 1; i < points.size(); i++) {
            Point p = points.get(i);
            boolean isOk = true;
            for (Point x : pointWay) {
                if (x.id == p.id) {
                    isOk = false;
                }
            }
            if (isOk) {
                pointWay.add(p);
                findBestWay(points, pointWay, bestResult);
                pointWay.remove(pointWay.size() - 1);
            }
        }
    }

    /**
     * 找到路径上的建筑名
     *
     * @param startPoint
     * @param endPoint
     * @return
     */
    private List<String> findTargetFromMap(Point startPoint, Point endPoint) {
        startPoint = startPoint.copy();
        endPoint = endPoint.copy();
        int[] dir = {endPoint.x - startPoint.x, endPoint.y - startPoint.y};
        if (dir[0] != 0) {
            dir[0] = (int) (dir[0] / Math.abs(dir[0]));
        }
        if (dir[1] != 0) {
            dir[1] = (int) (dir[1] / Math.abs(dir[1]));
        }
        List<String> result = new ArrayList<>();
        while (startPoint.getKey() != endPoint.getKey()) {
            startPoint.x += dir[0];
            startPoint.y += dir[1];
            int value = MORI_MAP[startPoint.y][startPoint.x];
            JSONObject json = MapElement.getElement("" + value);
            if (json.getBoolean("useful")) {
                result.add(json.getString("name"));
            }
        }
        return result;
    }

    public List<String> getWayBetween2Point(Point startPoint, Point endPoint, int row_n, int column_n, int steps) {
        startPoint = getDistInfo(endPoint, startPoint);
        endPoint = getDistInfo(endPoint, endPoint);
        Point targetP = endPoint.copy();
        List<Point> way = new ArrayList<>();
        way.add(startPoint.copy());
        int preDir = 0;
        while (startPoint.getKey() != endPoint.getKey()) {
            Point prePoint = null;
            int lastPreDir = preDir;
            for (int i = 0; i < 4; i++) {
                int[] dir = MapElement.DIR[(i + lastPreDir) % 4];
                Point newPoint = new Point(startPoint.x + dir[0], startPoint.y + dir[1]);
                if (newPoint.x < 0 || newPoint.x >= column_n || newPoint.y < 0 || newPoint.y >= row_n) {
                    continue;
                }
                newPoint = getDistInfo(endPoint, newPoint);
                if (newPoint == null) {
                    continue;
                }
                if (newPoint.dist + 1 != startPoint.dist) {
                    continue;
                }
                if (prePoint == null) {
                    prePoint = newPoint;
                    preDir = (i + lastPreDir) % 4;
                } else if (newPoint.buildings > prePoint.buildings) {
                    prePoint = newPoint;
                    preDir = (i + lastPreDir) % 4;
                }
            }
            way.add(prePoint.copy());
            startPoint = prePoint;
        }
        List<Point> path = new ArrayList<>();
        path.add(way.get(0));
        Point preP = way.get(0);
        int index = 1;
        while (index < way.size()) {
            Point point = way.get(index++);
            int lastIndex = path.size() - 1;
            if (point.x == path.get(lastIndex).x || point.y == path.get(lastIndex).y) {
                preP = point;
            } else {
                path.add(preP);
                preP = point;
            }
            if (index == way.size()) {
                path.add(point);
                break;
            }
        }
        List<String> responseInfo = new ArrayList<>();
        JSONObject eleType = MapElement.getElement(MORI_MAP[path.get(0).y][path.get(0).x]);
        int startBuildingSub = 0;
        if (eleType.getBoolean("useful")) {
            startBuildingSub = 1;
        }
        responseInfo.add(String.format("从当前点%d,%d出发,需要走%d步,可经过建筑%d",
                path.get(0).x + 1, path.get(0).y + 1, path.get(0).dist, path.get(0).buildings - startBuildingSub));
        int stepBak = steps;
        int step = 1;
        for (int i = 1; i < path.size(); i++) {
            Point curP = path.get(i);
            preP = path.get(i - 1);
            List<String> wayBuildings = findTargetFromMap(preP, curP);
            String msg = "";
            int move = Math.max(Math.abs(curP.y - preP.y), Math.abs(curP.x - preP.x));
            if (curP.x == preP.x) {
                if (curP.y > preP.y) {
                    msg = String.format("第%d次:左下[Y+%d]步到达(%d,%d)", step, curP.y - preP.y, curP.x + 1, curP.y + 1);
                    if (move >= steps && steps > 0) {
                        msg += String.format("\n第%d步你将【停留在%d,%d】", stepBak, preP.x + 1, preP.y + 1 + steps);
                    }
                }
                if (curP.y < preP.y) {
                    msg = String.format("第%d次:右上[Y-%d]步到达(%d,%d)", step, preP.y - curP.y, curP.x + 1, curP.y + 1);
                    if (move >= steps && steps > 0) {
                        msg += String.format("\n第%d步你将【停留在%d,%d】", stepBak, preP.x + 1, preP.y + 1 - steps);
                    }
                }
            }
            if (curP.y == preP.y) {
                if (curP.x > preP.x) {
                    msg = String.format("第%d次:右下[X+%d]步到达(%d,%d)", step, curP.x - preP.x, curP.x + 1, curP.y + 1);
                    if (move >= steps && steps > 0) {
                        msg += String.format("\n第%d步你将【停留在%d,%d】", stepBak, preP.x + 1 + steps, preP.y + 1);
                    }
                }
                if (curP.x < preP.x) {
                    msg = String.format("第%d次:左上[X-%d]步到达(%d,%d)", step, preP.x - curP.x, curP.x + 1, curP.y + 1);
                    if (move >= steps && steps > 0) {
                        msg += String.format("\n第%d步你将【停留在%d,%d】", stepBak, preP.x + 1 - steps, preP.y + 1);
                    }
                }
            }
            step++;
            steps -= move;
            if (wayBuildings.size() > 0) {
                msg = msg + String.format("\n路上经过%s", wayBuildings.toString());
            }
            responseInfo.add(msg);
            if (i % 5 == 0) {
                responseInfo.add("");
            }
        }
        if (steps > 0) {
            String msg = String.format("\n步数富余,你将停留在%d,%d", targetP.x + 1, targetP.y + 1);
            responseInfo.add(msg);
        }
        return responseInfo;
    }

    public List<String> init(List<Point> points) {
        List<String> responseStr = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            p.x--;
            p.y--;
            p.id = i;
            if (p.x < 0 || p.x >= 300 || p.y < 0 || p.y >= 300) {
                responseStr.add(String.format("坐标 %d,%d 必须在1到300之间", p.x + 1, p.y + 1));
            } else {
                int value = MORI_MAP[p.y][p.x];
                JSONObject json = MapElement.getElement(value + "");
                if (!json.getBoolean("reach")) {
                    responseStr.add(String.format("坐标%d,%d处是一个障碍物,不可到达", p.x + 1, p.y + 1));
                }
            }
        }
        return responseStr;
    }

    public List<String> countBestWay(List<Point> points) {
        List<String> responseStr = init(points);
        if (responseStr.size() > 0) {
            return responseStr;
        }
        Point[][] distMap = new Point[points.size()][];
        for (int i = 0; i < points.size(); i++) {
            distMap[i] = new Point[points.size()];
        }
        for (int i = 1; i < points.size(); i++) {
            bfs(MORI_MAP, points.get(i), 300, 300);
            for (int j = 0; j < points.size(); j++) {
                distMap[j][i] = getDistInfo(points.get(i), points.get(j));
            }
            distMap[i][0] = getDistInfo(points.get(i), points.get(0));
        }
        for (int i = 1; i < points.size(); i++) {
            Point info = distMap[0][i];
            Point target = points.get(i);
            responseStr.add(String.format("从起点到宝箱%d(%d,%d),最短距离%d,最多可经过建筑%d", i, target.x + 1, target.y + 1, info.dist, info.buildings));
        }
        List<Point> pointWay = new ArrayList<>();
        BestResult bestResult = new BestResult();
        bestResult.steps = -1;
        pointWay.add(points.get(0));
        findBestWay(points, pointWay, bestResult);
        responseStr.add(String.format("\n最佳路线最少需要%d步,经过%d个建筑物,取宝箱顺序如下", bestResult.steps, bestResult.buildings));
        for (int i = 0; i < bestResult.bestWay.size(); i++) {
            Point p = bestResult.bestWay.get(i);
            String start = "从起点出发";
            if (i == 1) {
                start = "首先去宝箱";
            }
            if (i == 2) {
                start = "第二去宝箱";
            }
            if (i == 3) {
                start = "最后去宝箱";
            }
            String msg = String.format("%s%d 坐标 %d,%d", start, p.id, p.x + 1, p.y + 1);
            responseStr.add(msg);
        }
        return responseStr;
    }

    public List<String> findPath(List<Point> points, int steps) {
        List<String> responseStr = init(points);
        if (responseStr.size() > 0) {
            return responseStr;
        }
        bfs(MORI_MAP, points.get(1), 300, 300);
        responseStr = getWayBetween2Point(points.get(0), points.get(1), 300, 300, steps);
        return responseStr;
    }

    public static void main(String[] args) throws IOException {
        FindMapWayUtil findMapWayUtil = new FindMapWayUtil();
        findMapWayUtil.moriWholeMapPath = "/Users/yuwanglin/project/weixin-little-ant/python/game_map/mori_game_map_gray.png";
        findMapWayUtil.setUp();
        List<String> response = findMapWayUtil.findPath(Arrays.asList(new Point(1, 3011), new Point(240, 10)), 100);
        for (String x : response) {
            System.out.println(x);
        }
//        List<String> response2 = findMapWayUtil.countBestWay(Arrays.asList(new Point(278, 73), new Point(272, 236), new Point(91, 26), new Point(171, 126)));
//        for (String x : response2) {
//            System.out.println(x);
//        }

    }

}

