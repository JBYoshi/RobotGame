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

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class DijkstraPathFinder<P> extends PathFinder<P> {
    @Override
    public Optional<List<P>> search(P start, Predicate<P> end) {
        return super.search(start, end);
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

    @Override
    protected PathNode createPathNode(P point, PathNode parent, Predicate<P> end) {
        return new PathNode(point, parent, parent == null ? 0 : parent.weight + getResistance(parent.point, point));
    }
}
