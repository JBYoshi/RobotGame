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

import jbyoshi.robotgame.model.PowerSourceModel;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

final class PowerSourceSprite extends AttackableSprite<PowerSourceModel> {
    private static final Path2D SHAPE = new Path2D.Double();

    PowerSourceSprite(GameDraw game, PowerSourceModel model, SpriteState state) {
        super(game, model, state);
    }

    @Override
    void drawAttackable(Graphics2D g, float renderTicks) {
        g.setColor(Color.BLACK);
        g.fill(SHAPE);

        g.setColor(RGColors.POWER_COLOR);
        g.setStroke(new BasicStroke((float) game.component.getPixelSize()));
        g.draw(SHAPE);

        g.setClip(SHAPE);
        double size = model.health;
        size /= model.maxHealth;
        g.fill(new Rectangle2D.Double(0, 1 - size, 1, size));
    }

    static {
        SHAPE.moveTo(0.5, 0);
        SHAPE.lineTo(0, 0.5);
        SHAPE.lineTo(0.5, 1);
        SHAPE.lineTo(1, 0.5);
        SHAPE.closePath();
    }
}
