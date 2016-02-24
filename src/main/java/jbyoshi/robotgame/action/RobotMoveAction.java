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
package jbyoshi.robotgame.action;

import jbyoshi.robotgame.model.*;
import jbyoshi.robotgame.api.Direction;

public final class RobotMoveAction implements Action<RobotModel> {
	private final Direction dir;

	public RobotMoveAction(Direction dir) {
		this.dir = dir;
	}

	@Override
	public void perform(RobotModel target) {
		if (dir != null) {
			target.move(dir);
		}
	}

	@Override
	public int getPriority() {
		return Integer.MAX_VALUE;
	}

}
