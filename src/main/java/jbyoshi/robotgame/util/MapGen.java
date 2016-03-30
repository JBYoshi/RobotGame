/*
 * Copyright (C) 2016 JBYoshi.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package jbyoshi.robotgame.util;

import jbyoshi.robotgame.api.Direction;
import jbyoshi.robotgame.api.Game;
import jbyoshi.robotgame.api.Point;
import jbyoshi.robotgame.model.GameModel;
import jbyoshi.robotgame.model.PowerSourceModel;

import java.util.Arrays;
import java.util.List;

public final class MapGen {
    private static final int HALF_PASSAGE_SIZE = 2;
    private static final int HALF_PASSAGE_VARIATION = 8;
    private static final DijkstraPathFinder<Point> PATH_FINDER = new DijkstraPathFinder<Point>() {
        @Override
        protected int getResistance(Point from, Point to) {
            return 1;
        }

        @Override
        protected Iterable<Point> getNeighbors(Point point) {
            return Arrays.stream(Direction.values()).map(point::add).map(p -> p.unaryOp(MapGen::wrap))::iterator;
        }
    };

    public static boolean[][] createMap(GameModel model) {
        boolean[][] map = new boolean[Game.WORLD_SIZE][Game.WORLD_SIZE];
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                map[x][y] = (x < 10 || (x >= 15 && x < 35) || x >= 40) || (y < 10 || (y >= 15 && y < 35) || y >= 40);
            }
        }
        buildHorizontalPath(map, Game.WORLD_SIZE / 4, Game.WORLD_SIZE * 3 / 4, Game.WORLD_SIZE / 4);
        buildHorizontalPath(map, Game.WORLD_SIZE / 4, Game.WORLD_SIZE * 3 / 4, Game.WORLD_SIZE * 3 / 4);
        buildHorizontalPath(map, Game.WORLD_SIZE * 3 / 4, Game.WORLD_SIZE / 4, Game.WORLD_SIZE / 4);
        buildHorizontalPath(map, Game.WORLD_SIZE * 3 / 4, Game.WORLD_SIZE / 4, Game.WORLD_SIZE * 3 / 4);
        buildVerticalPath(map, Game.WORLD_SIZE / 4, Game.WORLD_SIZE / 4, Game.WORLD_SIZE * 3 / 4);
        buildVerticalPath(map, Game.WORLD_SIZE / 4, Game.WORLD_SIZE * 3 / 4, Game.WORLD_SIZE / 4);
        buildVerticalPath(map, Game.WORLD_SIZE * 3 / 4, Game.WORLD_SIZE / 4, Game.WORLD_SIZE * 3 / 4);
        buildVerticalPath(map, Game.WORLD_SIZE * 3 / 4, Game.WORLD_SIZE * 3 / 4, Game.WORLD_SIZE / 4);

        map = runPasses(map, 1, (m, x, y) -> m[x][y] && Math.random() > 0.35);
        map = runPasses(map, 6, (m, x, y) -> countWalls(m, x, y, 1) > 4);
        map = removeInaccessibleCaverns(map, Game.WORLD_SIZE / 4, Game.WORLD_SIZE / 4);

        Integer[] spots = RandomHelper.choose(4, 0, 1, 2, 3, 4, 5, 6, 7);
        final boolean[][] map1 = map;
        for (int spot : spots) {
            int x, y;
            switch (spot) {
                case 0:
                    x = 0;
                    y = Game.WORLD_SIZE / 4;
                    break;
                case 1:
                    x = Game.WORLD_SIZE / 2;
                    y = Game.WORLD_SIZE / 4;
                    break;
                case 2:
                    x = 0;
                    y = Game.WORLD_SIZE * 3 / 4;
                    break;
                case 3:
                    x = Game.WORLD_SIZE / 2;
                    y = Game.WORLD_SIZE * 3 / 4;
                    break;
                case 4:
                    x = Game.WORLD_SIZE / 4;
                    y = 0;
                    break;
                case 5:
                    x = Game.WORLD_SIZE / 4;
                    y = Game.WORLD_SIZE / 2;
                    break;
                case 6:
                    x = Game.WORLD_SIZE * 3 / 4;
                    y = 0;
                    break;
                case 7:
                    x = Game.WORLD_SIZE * 3 / 4;
                    y = Game.WORLD_SIZE / 2;
                    break;
                default:
                    throw new AssertionError("case " + spot);
            }
            Point p = new Point(x, y);
            if (map[x][y]) {
                List<Point> path = PATH_FINDER.search(p, p1 -> !map1[p1.getX()][p1.getY()])
                        .orElseThrow(() -> new AssertionError("Map is full!"));
                p = path.get(path.size() - 1);
            }
            List<Point> path = PATH_FINDER.search(p, p1 -> map1[p1.getX()][p1.getY()])
                    .orElseThrow(() -> new AssertionError("Map is empty!"));
            model.add(new PowerSourceModel(path.get(path.size() - 1)));
        }

        return map;
    }

    private static boolean[][] removeInaccessibleCaverns(boolean[][] map, int x, int y) {
        boolean[][] out = new boolean[map.length][map[0].length];
        for (boolean[] row : out) Arrays.fill(row, true);
        removeInaccessibleCaverns(map, out, x, y);
        return out;
    }

    private static void removeInaccessibleCaverns(boolean[][] in, boolean[][] out, int x, int y) {
        if (!in[x][y] && out[x][y]) {
            out[x][y] = false;
            removeInaccessibleCaverns(in, out, wrap(x - 1), wrap(y - 1));
            removeInaccessibleCaverns(in, out, wrap(x - 1), y);
            removeInaccessibleCaverns(in, out, wrap(x - 1), wrap(y + 1));
            removeInaccessibleCaverns(in, out, x, wrap(y - 1));
            removeInaccessibleCaverns(in, out, x, wrap(y + 1));
            removeInaccessibleCaverns(in, out, wrap(x + 1), wrap(y - 1));
            removeInaccessibleCaverns(in, out, wrap(x + 1), y);
            removeInaccessibleCaverns(in, out, wrap(x + 1), wrap(y + 1));
        }
    }

    private static void buildHorizontalPath(boolean[][] map, int minX, int maxX, int y) {
        int minY = y - HALF_PASSAGE_VARIATION, maxY = y + HALF_PASSAGE_VARIATION;
        int destY = (minY + maxY) / 2, currentX = minX, currentY = destY;
        while (currentX != maxX) {
            for (int ptY = currentY - HALF_PASSAGE_SIZE; ptY < currentY + HALF_PASSAGE_SIZE; ptY++) {
                map[currentX][wrap(ptY)] = false;
            }
            if (currentY == minY && currentY != maxY) currentY++;
            else if (currentY == maxY && currentY != minY) currentY--;
            else switch ((int) (Math.random() * 3)) {
                case 0:
                    if (currentY == minY + 1) {
                        if (currentY != maxY - 1) currentY++;
                    } else currentY--;
                    break;
                case 1:
                    if (currentY == maxY - 1) {
                        if (currentY != minY + 1) currentY--;
                    } else currentY++;
                    break;
            }
            for (int ptY = currentY - HALF_PASSAGE_SIZE; ptY < currentY + HALF_PASSAGE_SIZE; ptY++) {
                map[currentX][wrap(ptY)] = false;
            }

            if (minY < destY - wrap(maxX - currentX)) minY++;
            if (maxY > destY + wrap(maxX - currentX)) maxY--;
            currentX = wrap(currentX + 1);
        }
    }

    private static void buildVerticalPath(boolean[][] map, int x, int minY, int maxY) {
        int minX = x - HALF_PASSAGE_VARIATION, maxX = x + HALF_PASSAGE_VARIATION;
        int destX = (minX + maxX) / 2, currentX = destX, currentY = minY;
        while (currentY != maxY) {
            for (int ptX = currentX - HALF_PASSAGE_SIZE; ptX < currentX + HALF_PASSAGE_SIZE; ptX++) {
                map[wrap(ptX)][currentY] = false;
            }
            if (currentX == wrap(minX - 1) && currentX != maxX) currentX++;
            else if (currentX == maxX && currentX != minX) currentX--;
            else switch ((int) (Math.random() * 3)) {
                    case 0:
                        if (currentX == minX + 1) {
                            if (currentX != maxX - 1) currentX++;
                        } else currentX--;
                        break;
                    case 1:
                        if (currentX == maxX - 1) {
                            if (currentX != minX + 1) currentX--;
                        } else currentX++;
                        break;
                }
            for (int ptX = currentX - HALF_PASSAGE_SIZE; ptX < currentX + HALF_PASSAGE_SIZE; ptX++) {
                map[wrap(ptX)][currentY] = false;
            }

            if (minX < destX - wrap(maxY - currentY)) minX++;
            if (maxX > destX + wrap(maxY - currentY)) maxX--;
            currentY = wrap(currentY + 1);
        }
    }

    private static boolean[][] runPasses(boolean[][] map, int num, MapFilter filter) {
        for (int i = 0; i < num; i++) {
            boolean[][] newMap = new boolean[map.length][map[0].length];
            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[0].length; y++) {
                    newMap[x][y] = filter.test(map, x, y);
                }
            }
            map = newMap;
        }
        return map;
    }

    private static int countWalls(boolean[][] map, int x, int y, int dist) {
        int num = 0;
        for (int dx = x - dist; dx <= x + dist; dx++) {
            for (int dy = y - dist; dy <= y + dist; dy++) {
                if (map[wrap(dx)][wrap(dy)]) {
                    num++;
                }
            }
        }
        return num;
    }

    private static int wrap(int val) {
        val %= Game.WORLD_SIZE;
        if (val < 0) val += Game.WORLD_SIZE;
        return val;
    }

    private interface MapFilter {
        boolean test(boolean[][] map, int x, int y);
    }
}
