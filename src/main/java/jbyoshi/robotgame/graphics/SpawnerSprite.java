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
import java.awt.geom.Rectangle2D;

import jbyoshi.robotgame.model.*;

final class SpawnerSprite extends AttackableSprite<SpawnerModel> {
	private RobotModel spawning;
	private int totalCountdown;

	SpawnerSprite(GameDraw game, SpawnerModel model, SpriteState state) {
		super(game, model, state);
	}

	@Override
	public void drawAttackable(Graphics2D graphics, float renderTicks) {
		Color playerColor = model.player.getColor();
		graphics.setColor(playerColor);
		graphics.fillRect(0, 0, 1, 1);
		if (model.spawningRobot != null || (spawning != null && renderTicks < 0.5)) {
			graphics.setColor(Color.BLACK.brighter());
			if (state == SpriteState.CONTINUOUS) {
				if (spawning == null && renderTicks < 0.5) {
					// Fade in
					graphics.setColor(Sprite.fade(playerColor, graphics.getColor(), renderTicks * 2));
				} else if (model.spawningRobot == null) {
					// Fade out
					graphics.setColor(Sprite.fade(graphics.getColor(), playerColor, renderTicks * 2));
				}
			}
			double pixel = Math.min(game.component.getPixelSize(), 0.125);
			graphics.fill(new Rectangle2D.Double(pixel, pixel, 1 - 2 * pixel, 1 - 2 * pixel));
			graphics.setColor(playerColor);
			double height;
			if (spawning != null && model.spawningRobot == null) {
				height = 1;
			} else {
				height = (totalCountdown - model.countdown - 1 + Math.min(renderTicks, 1)) / (totalCountdown - 1);
			}
			height = Math.min(height, 1);
			height -= 4 * pixel;
			graphics.fill(new Rectangle2D.Double(2 * pixel, 1 - height - 2 * pixel, 1 - 4 * pixel, height));
		}
	}

	@Override
	public void preTick() {
		super.preTick();
		if (spawning == null) totalCountdown = 0;
		spawning = model.spawningRobot;
		if (model.totalCountdown > 0) totalCountdown = model.totalCountdown;
	}

	@Override
	public void postTick() {
		super.postTick();
		if (spawning != null && model.spawningRobot != spawning) {
			RobotSprite robot = game.getSprite(spawning);
			robot.state = SpriteState.CONTINUOUS;
			robot.lastLoc = model.loc;
		}
		if (model.spawningRobot != null) {
			totalCountdown = model.totalCountdown;
		}
	}

}
