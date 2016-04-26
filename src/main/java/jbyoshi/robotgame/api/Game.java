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

import java.util.*;
import java.util.function.Predicate;

public interface Game {
	int WORLD_SIZE = 50;

	default boolean isWalkable(Point loc) {
		return !isWall(loc);
	}

	Set<? extends MyRobot> getMyRobots();

	Set<? extends MySpawner> getMySpawners();

	Set<? extends Robot> getEnemyRobots();

	Set<? extends Spawner> getEnemySpawners();

	default Set<? extends ObjectInGame> getObjectsAt(Point loc) {
		return getObjectsNear(loc, 0);
	}

	Set<? extends ObjectInGame> getObjectsNear(Located loc, int distance);

	default Optional<Path> createPath(Located start, Located end) {
		return createPath(start, end, this::isWalkable);
	}

	Optional<Path> createPath(Located start, Located end, Predicate<Point> isWalkable);

	default Optional<Path> createPath(Located start, Predicate<Point> end) {
		return createPath(start, end, this::isWalkable);
	}

	Optional<Path> createPath(Located start, Predicate<Point> end, Predicate<Point> isWalkable);

	default <T extends ObjectInGame> Optional<T> findNearest(Located start, Class<T> type) {
		return findNearest(start, type, target -> true);
	}

	default <T extends ObjectInGame> Optional<T> findNearest(Located start, Class<T> type, Predicate<T> acceptTarget) {
		return findNearest(start, type, acceptTarget, this::isWalkable);
	}

	<T extends ObjectInGame> Optional<T> findNearest(Located start, Class<T> type, Predicate<T> acceptTarget,
													 Predicate<Point> acceptLocation);

	boolean isWall(Point loc);
}
