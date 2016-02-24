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
package jbyoshi.robotgame.graphics;

import java.awt.*;
import java.awt.geom.*;

import jbyoshi.robotgame.model.*;

final class RobotSprite extends AttackableSprite<RobotModel> {
	RobotSprite(GameDraw game, RobotModel model, SpriteState state) {
		super(game, model, state);
	}

	@Override
	public void drawAttackable(Graphics2D g, float renderTicks) {
		g.setColor(model.player.getColor());
		g.fill(new Ellipse2D.Double(0, 0, 1, 1));
	}
}
