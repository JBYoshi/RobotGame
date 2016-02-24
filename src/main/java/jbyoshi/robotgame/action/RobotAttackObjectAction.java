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

import jbyoshi.robotgame.model.AttackableModel;
import jbyoshi.robotgame.model.RobotModel;
import jbyoshi.robotgame.api.Direction;

import java.util.UUID;

public final class RobotAttackObjectAction implements Action<RobotModel> {
	private final UUID target;

	public RobotAttackObjectAction(AttackableModel target) {
		this.target = target.getId();
	}

	@Override
	public void perform(RobotModel attacker) {
		final AttackableModel targetModel = (AttackableModel) attacker.getGame().getModelById(target);
		if (targetModel != null) {
			for (Direction dir : Direction.values()) {
				if (attacker.loc.add(dir).equals(targetModel.loc)) {
					targetModel.attack(3);
					break;
				}
			}
		}
	}

}
