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
package jbyoshi.robotgame.impl;

import java.util.*;
import java.util.function.*;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import jbyoshi.robotgame.action.*;
import jbyoshi.robotgame.api.*;
import jbyoshi.robotgame.model.*;
import jbyoshi.robotgame.util.*;

public final class GameView implements Game {
	private final GameModel model;
	final PlayerImpl player;
	private final Function<Model, ModelView<?>> views;
	private final Set<BoundAction> actions = new LinkedHashSet<>();

	public GameView(GameModel game, PlayerImpl player) {
		this.model = game;
		this.player = player;
		views = CacheBuilder.newBuilder().weakValues().<Model, ModelView<?>>build(
				CacheLoader.from(model -> ViewRegistry.wrap(model, this)))::getUnchecked;
	}

	@Override
	public Set<MyRobotView> getMyRobots() {
		return getViews(RobotModel.class, MyRobotView.class);
	}

	@Override
	public Set<MySpawnerView> getMySpawners() {
		return getViews(SpawnerModel.class, MySpawnerView.class);
	}

	@Override
	public Set<? extends Robot> getEnemyRobots() {
		return getEnemyViews(RobotModel.class, MyRobotView.class);
	}

	@Override
	public Set<? extends Spawner> getEnemySpawners() {
		return getEnemyViews(SpawnerModel.class, MySpawnerView.class);
	}

	@Override
	public Set<? extends PowerSource> getPowerSources() {
		return getViews(PowerSourceModel.class, PowerSourceView.class);
	}

	@Override
	public Set<? extends ObjectInGame> getObjectsNear(Point loc, int distance) {
		Set<ModelView<?>> out = new HashSet<>();
		getObjectsNear(loc, distance, new HashSet<>(), out);
		return out;
	}

	private static final int WORLD_SIZE_HALF = WORLD_SIZE / 2;

	@Override
	public Optional<Path> createPath(Point start, Point end, Predicate<Point> isWalkable) {
		Predicate<Point> filter = isWalkable.or(end::equals);
		return new AStarPathFinder<Point>() {
			@Override
			protected Iterable<Point> getNeighbors(Point point) {
				return Arrays.stream(Direction.values()).map(point::add).filter(filter)::iterator;
			}

			@Override
			protected int getResistance(Point from, Point to) {
				return getObjectsAt(to).isEmpty() ? 1 : 1000;
			}

			@Override
			protected int estimateDistanceToEnd(Point point, Point end) {
				return Math.min(Math.min(estimateDistance(point, end),
						estimateDistance(point.add(WORLD_SIZE_HALF, 0), end.add(WORLD_SIZE_HALF, 0))),
						Math.min(estimateDistance(point.add(0, WORLD_SIZE_HALF), end.add(0, WORLD_SIZE_HALF)),
								estimateDistance(point.add(WORLD_SIZE_HALF, WORLD_SIZE_HALF), end.add(50, 50))));
			}

			private int estimateDistance(Point a, Point b) {
				return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
			}
		}.search(start, end).map(points -> new Path(start, points));
	}

	@Override
	public Optional<Path> createPath(Point start, Predicate<Point> end, Predicate<Point> isWalkable) {
		Predicate<Point> filter = isWalkable.or(end);
		return new DijkstraPathFinder<Point>() {
			@Override
			protected Iterable<Point> getNeighbors(Point point) {
				return Arrays.stream(Direction.values()).map(point::add).filter(filter)::iterator;
			}

			@Override
			protected int getResistance(Point from, Point to) {
				return getObjectsAt(to).isEmpty() ? 1 : 1000;
			}
		}.search(start, end).map(points -> new Path(start, points));
	}

	@Override
	public <T extends ObjectInGame> Optional<T> findNearest(Point start, Class<T> type, Predicate<T> acceptTarget,
															Predicate<Point> isWalkable) {
		Map<Point, T> objects = new HashMap<>();
		model.getAllModels().stream().map(views).filter(type::isInstance).map(type::cast)
				.filter(acceptTarget).forEach(view -> objects.put(view.getLocation(), view));

		if (objects.isEmpty()) return Optional.empty();
		if (objects.containsKey(start)) return Optional.of(objects.get(start));
		if (objects.size() == 1) {
			// Optimize using astar.
			return Optional.of(objects.values().iterator().next()).filter(x -> createPath(start, x.getLocation(),
					isWalkable).isPresent());
		}
		return createPath(start, objects::containsKey, isWalkable).map(path -> path.getPoint(path.getLength() - 1))
				.map(objects::get);
	}

	@Override
	public boolean isWall(Point point) {
		return model.map[point.getX()][point.getY()];
	}

	private void getObjectsNear(Point loc, int distance, Set<Point> checked, Set<ModelView<?>> out) {
		if (checked.add(loc)) {
			model.getModelsAt(loc).stream().map(views).forEach(out::add);
		}
		if (distance > 0) {
			for (Direction dir : Direction.values()) {
				getObjectsNear(loc.add(dir), distance - 1, checked, out);
			}
		}
	}

	private <M extends Model, V extends ModelView<M>> Set<V> getViews(Class<M> modelType, Class<V> myViewClass) {
		return model.getModels(modelType).stream().map(views).flatMap(StreamHelpers.casting(myViewClass))
				.collect(StreamHelpers.toImmutableSet());
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private <M extends Model, V extends ModelView<M>> Set<V> getEnemyViews(Class<M> modelType, Class<? extends V> myViewClass) {
		return (Set) model.getModels(modelType).stream().map(views).filter(view -> !myViewClass.isInstance(view))
				.collect(StreamHelpers.toImmutableSet());
	}

	<T extends Model> void addAction(T model, Action<? super T> action) {
		actions.add(new BoundAction(action, model.getId()));
	}

	public Set<BoundAction> popActions() {
		Set<BoundAction> actions = new LinkedHashSet<>(this.actions);
		this.actions.clear();
		return actions;
	}

}
