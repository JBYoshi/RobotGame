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

import jbyoshi.robotgame.model.AttackableModel;

import java.awt.*;

abstract class AttackableSprite<T extends AttackableModel> extends Sprite<T> {
	private int lastHealth = -1;
	AttackableSprite(GameDraw game, T model, SpriteState state) {
		super(game, model, state);
	}

	@Override
	public void drawSprite(Graphics2D g, float renderTicks) {
		float shade;
		if (state == SpriteState.DEAD) {
			shade = 1.0f;
		} else if (state == SpriteState.NEW) {
			shade = 0.0f;
		} else if (model.health < lastHealth) {
			shade = 1.0f - Math.min(renderTicks, 1);
		} else {
			drawAttackable(g, renderTicks);
			return;
		}
		g.setComposite(createShadeComposite(new Color(1.0f, 0.0f, 0.0f, shade)));
		drawAttackable(g, renderTicks);
	}

	abstract void drawAttackable(Graphics2D g, float renderTicks);

	@Override
	void preTick() {
		super.preTick();
		lastHealth = model.health;
	}
}
