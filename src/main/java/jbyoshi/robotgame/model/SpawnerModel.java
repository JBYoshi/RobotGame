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

import jbyoshi.robotgame.api.Point;
import jbyoshi.robotgame.impl.PlayerImpl;
import jbyoshi.robotgame.api.Direction;

import java.util.*;

public final class SpawnerModel extends AttackableModel implements OwnedModel {
	public int countdown, totalCountdown;
	public RobotModel spawningRobot;
	public final PlayerImpl player;

	public SpawnerModel(PlayerImpl player, Point loc) {
		super(loc, 100);
		this.player = player;
	}

	public void startSpawn(UUID id) {
		if (spawningRobot != null) {
			throw new IllegalStateException("Already spawning!");
		}
		countdown = totalCountdown = 5; // TODO change the 5
		spawningRobot = new RobotModel(player, loc);
		if (id != null) {
			spawningRobot.setId(id);
		}
	}

	@Override
	void onTickEnd() {
		if (countdown > 0) {
			countdown--;
			if (countdown == 0 && spawningRobot != null) {
				for (Direction dir : Direction.values()) {
					if (game.getModelsAt(loc.add(dir)).isEmpty()) {
						spawningRobot.loc = spawningRobot.loc.add(dir);
						this.game.add(spawningRobot);
						spawningRobot = null;
						totalCountdown = 0;
						return;
					}
				}
				// Can't spawn.
				countdown = 1;
			}
		}
	}

	@Override
	public PlayerImpl getPlayer() {
		return player;
	}
}
