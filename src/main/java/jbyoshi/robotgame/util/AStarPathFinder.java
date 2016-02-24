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

import java.util.*;
import java.util.function.Predicate;

public abstract class AStarPathFinder<P> extends PathFinder<P> {
    public final Optional<List<P>> search(P start, P end) {
        return search(start, new AStarPredicate(end));
    }

    /**
     * Determines the amount of resistance of a path between the given two
     * points. The given points are adjacent; {@link #getNeighbors(Object)
     * getNeighbors}{@code (from)} will have returned {@code to}. Larger
     * resistance values indicate a slower/longer/etc. path between the points.
     *
     * @param from The point that occurs earlier in the path.
     * @param to The point that occurs later in the path.
     * @return The resistance between the two points.
     */
    protected abstract int getResistance(P from, P to);

    /**
     * Estimates the amount of {@linkplain #getResistance(Object, Object)
     * resistance} from a point to the end. This is used to optimize
     * pathfinding and bias it towards the ending point.
     *
     * @param point The point that occurs earlier in the path.
     * @param end The end point in the path.
     * @return The estimated resistance between the two points.
     */
    protected abstract int estimateDistanceToEnd(P point, P end);

    @Override
    protected final PathFinder<P>.PathNode createPathNode(P point, PathNode parent, Predicate<P> end) {
        return new AStarPathNode(point, parent, parent == null ? 0 : ((AStarPathNode) parent).weightToStart
                + getResistance(parent.point, point), estimateDistanceToEnd(point, ((AStarPredicate) end).point));
    }

    private final class AStarPathNode extends PathNode {
        final int weightToStart;

        AStarPathNode(P point, PathNode parent, int weightToStart, int weightToEnd) {
            super(point, parent, weightToStart + weightToEnd);
            this.weightToStart = weightToStart;
        }
    }

    private final class AStarPredicate implements Predicate<P> {
        final P point;

        AStarPredicate(P point) {
            this.point = point;
        }

        @Override
        public boolean test(P p) {
            return p.equals(point);
        }
    }
}
