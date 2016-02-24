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

public final class RobotModel extends AttackableModel implements OwnedModel {
	public final PlayerImpl player;

	public RobotModel(PlayerImpl player, Point loc) {
		super(loc, 10);
		this.player = player;
	}

	public void move(Direction dir) {
		Point newLoc = loc.add(dir);
		if (game.getModelsAt(newLoc).isEmpty()) {
			loc = newLoc;
		}
	}

	@Override
	public PlayerImpl getPlayer() {
		return player;
	}
}
