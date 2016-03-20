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
package jbyoshi.robotgame.model;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import jbyoshi.robotgame.action.*;
import jbyoshi.robotgame.api.Point;
import jbyoshi.robotgame.impl.PlayerImpl;
import jbyoshi.robotgame.util.MapGen;

public final class GameModel {
	private final Multimap<Class<? extends Model>, Model> modelsByType = HashMultimap.create();
	private final Map<UUID, Model> modelsById = new HashMap<>();
	private boolean ended;
	private PlayerImpl winner = null;
	public final boolean[][] map;
	public int ticks;
	public static final int MAX_TICKS = 15 * 60;

	public GameModel() {
		map = MapGen.createMap();
	}

	public GameModel(GameModel other) {
		map = new boolean[other.map.length][other.map[0].length];
		for (int x = 0; x < map.length; x++) {
			System.arraycopy(other.map[x], 0, map[x], 0, map[0].length);
		}

		other.modelsById.values().stream().map(Model::clone).forEach(this::add);
		ended = other.ended;
		winner = other.winner;
	}

	public <T extends Model> Set<T> getModels(Class<T> type) {
		return ImmutableSet.copyOf((Set<T>) modelsByType.get(type));
	}

	public void add(Model model) {
		model.game = this;
		modelsByType.put(model.getClass(), model);
		modelsById.put(model.getId(), model);
	}

	public void remove(Model model) {
		modelsByType.remove(model.getClass(), model);
		modelsById.remove(model.getId(), model);
	}

	public Model getModelById(UUID id) {
		return this.modelsById.get(id);
	}

	public void preTick() {
		new LinkedList<>(modelsById.values()).forEach(Model::onTickStart);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized void postTick(List<BoundAction> actions) {
		actions = new ArrayList<>(actions);
		Collections.sort(actions, Comparator.comparing(action -> action.action.getPriority()));
		for (BoundAction action : actions) {
			final Model model = getModelById(action.targetId);
			if (model != null) {
				((Action) action.action).perform(model);
			}
		}
		new LinkedList<>(modelsById.values()).forEach(Model::onTickEnd);

		ticks++;

		boolean ended = true;
		PlayerImpl player = null;
		if (ticks < MAX_TICKS) {
			for (Model model : modelsById.values()) {
				if (model instanceof OwnedModel) {
					if (player == null) {
						player = ((OwnedModel) model).getPlayer();
					} else if (!player.equals(((OwnedModel) model).getPlayer())) {
						ended = false;
						break;
					}
				}
			}
		}
		if (ended) {
			this.ended = true;
			winner = player;
		}
	}

	public Set<Model> getModelsAt(Point loc) {
		return modelsById.values().stream().filter(model -> model.loc.equals(loc)).collect(Collectors.toSet());
	}

	public Set<Model> getAllModels() {
		return ImmutableSet.copyOf(modelsById.values());
	}

	public boolean isRunning() {
		return !ended;
	}

	public PlayerImpl getWinner() {
		return winner;
	}
}
