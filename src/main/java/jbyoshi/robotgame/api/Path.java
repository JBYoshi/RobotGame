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
package jbyoshi.robotgame.api;

import java.util.Arrays;
import java.util.List;

public final class Path {
    private final Point[] points;
    private final Direction[] directions;

    public Path(Point start, Point...points) {
        this(start, Arrays.asList(points));
    }

    public Path(Point start, List<Point> points) {
        this.points = points.toArray(new Point[points.size()]);
        directions = new Direction[points.size()];
        Point prev = start;
        int i = 0;
        points: for (Point p : points) {
            for (Direction dir : Direction.values()) {
                if (prev.add(dir).equals(p)) {
                    directions[i++] = dir;
                    prev = p;
                    continue points;
                }
            }
            throw new IllegalArgumentException("Non-neighboring pair " + prev + " -> " + p);
        }
    }

    public int getLength() {
        return points.length;
    }

    public Point getPoint(int index) {
        return points[index];
    }

    public Direction getDirectionTo(int index) {
        return directions[index];
    }
}
