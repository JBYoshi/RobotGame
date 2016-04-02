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

import jbyoshi.robotgame.action.*;
import jbyoshi.robotgame.api.*;
import jbyoshi.robotgame.model.*;

final class MyRobotView extends RobotView implements MyRobot {
	private Path cachedPath;
	private int cachedPathProgress;
	private int cachedPathTicks;

	MyRobotView(GameView game, RobotModel model) {
		super(game, model);
	}

	@Override
	public boolean move(Direction dir) {
		Point newLoc = getLocation().add(dir);
		if (game.getObjectsAt(newLoc).stream().filter(obj -> !(obj instanceof RobotView)).count() == 0) {
			addAction(new RobotMoveAction(dir));
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void attack(Attackable target) {
		addAction(new RobotAttackObjectAction(((ModelView<? extends AttackableModel>) target).model));
	}

	@Override
	public boolean moveTo(Point loc) {
		if (cachedPath != null && cachedPathTicks > 0 && cachedPath.getPoint(cachedPath.getLength() - 1).equals(loc)) {
			cachedPathTicks--;
			return move(cachedPath.getDirectionTo(++cachedPathProgress));
		}
		return game.createPath(getLocation(), loc).filter(p -> p.getLength() > 0).filter(p -> move(p.getDirectionTo(0)))
				.map(p -> {
					cachedPath = p;
					cachedPathProgress = 0;
					cachedPathTicks = 7;
					return p;
		}).isPresent();
	}

}
