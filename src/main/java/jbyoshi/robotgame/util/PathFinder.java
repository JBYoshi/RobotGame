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

abstract class PathFinder<P> {
    Optional<List<P>> search(P start, Predicate<P> end) {
        Queue<PathNode> toCheck = new PriorityQueue<>();
        Map<P, PathNode> toCheckMap = new HashMap<>();
        toCheck.add(createPathNode(start, null, end));
        toCheckMap.put(start, toCheck.peek());

        Set<P> checked = new HashSet<>();
        while (!toCheck.isEmpty()) {
            PathNode node = toCheck.remove();

            if (end.test(node.point)) {
                List<P> points = new LinkedList<>();
                while (node.parent != null) {
                    points.add(0, node.point);
                    node = node.parent;
                }
                return Optional.of(points);
            }

            toCheckMap.remove(node.point);
            checked.add(node.point);

            // Stuck? Might need to check if it's blocked.
            if (checked.size() > 10000 && checked.size() % 1000 == 0) {
                System.err.println("Pathfinder warning: I don't think there's a path, but your world seems to be huge.");
                System.err.println("I can't tell you there isn't a path until I've run out of spaces to check.");
                System.err.println("Make sure the pathfinding environment is finite!");
                System.err.println("One of the points I'm looking at right now: " + node.point);
            }

            final Iterable<P> neighborsIterable = getNeighbors(node.point);
            List<P> neighbors;
            if (neighborsIterable instanceof List) {
                neighbors = (List<P>) neighborsIterable;
            } else {
                neighbors = new ArrayList<>();
                neighborsIterable.forEach(neighbors::add);
            }
            Collections.shuffle(neighbors);

            for (P p : neighbors) {
                if (checked.contains(p)) {
                    continue;
                }

                final PathNode next = createPathNode(p, node, end);
                if (toCheckMap.containsKey(p)) {
                    PathNode existing = toCheckMap.get(p);
                    if (existing.compareTo(next) < 0) {
                        continue;
                    }
                    toCheck.remove(existing);
                    // put() below removes it from toCheckMap
                }
                toCheck.add(next);
                toCheckMap.put(p, next);
            }
        }

        return Optional.empty();
    }

    /**
     * Determines the points that can be directly accessed from the given
     * point.
     *
     * @param point The point to search from
     * @return The points that can be directly accessed from the given point
     */
    protected abstract Iterable<P> getNeighbors(P point);

    protected abstract PathNode createPathNode(P point, PathNode parent, Predicate<P> end);

    class PathNode implements Comparable<PathNode> {
        final P point;
        final PathNode parent;
        final int weight;

        PathNode(P point, PathNode parent, int weight) {
            this.point = point;
            this.parent = parent;
            this.weight = weight;
        }

        @Override
        public final int compareTo(PathNode o) {
            return weight - o.weight;
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            @SuppressWarnings("unchecked")
            PathNode pathNode = (PathNode) o;
            return Objects.equals(point, pathNode.point);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(point);
        }
    }
}
