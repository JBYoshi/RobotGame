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
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import jbyoshi.robotgame.api.Game;
import jbyoshi.robotgame.api.Point;
import jbyoshi.robotgame.model.*;

abstract class Sprite<T extends Model> {
	final GameDraw game;
	final T model;
	SpriteState state;
	Point lastLoc;

	Sprite(GameDraw game, T model, SpriteState state) {
		this.game = game;
		this.model = model;
		this.state = state;
	}

	final void draw(Graphics2D g, float renderTicks) {
		if (state == SpriteState.NEW || state == SpriteState.DEAD) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, state == SpriteState.NEW ? renderTicks
					: 1.0f - renderTicks));
			g = new CompoundGraphics(g);
		}

		if (lastLoc == null) {
			g.translate(model.loc.getX(), model.loc.getY());
		} else {
			g.translate(lastLoc.getX() + getChange(lastLoc.getX(), model.loc.getX()) * renderTicks,
						lastLoc.getY() + getChange(lastLoc.getY(), model.loc.getY()) * renderTicks);
		}
		drawSprite(g, renderTicks);
	}

	private static int getChange(int prev, int next) {
		int toLeft = prev - next;
		while (toLeft < 0) toLeft += Game.WORLD_SIZE;
		while (toLeft > Game.WORLD_SIZE) toLeft -= Game.WORLD_SIZE;
		int toRight = next - prev;
		while (toRight < 0) toRight += Game.WORLD_SIZE;
		while (toRight > Game.WORLD_SIZE) toRight -= Game.WORLD_SIZE;
		return toLeft < toRight ? -toLeft : toRight;
	}

	abstract void drawSprite(Graphics2D g, float renderTicks);

	void preTick() {
		lastLoc = model.loc;
	}

	void postTick() {}

	static Color fade(Color start, Color end, double progress) {
		return new Color((int) (start.getRed() + (end.getRed() - start.getRed()) * progress),
				(int) (start.getGreen() + (end.getGreen() - start.getGreen()) * progress),
				(int) (start.getBlue() + (end.getBlue() - start.getBlue()) * progress));
	}

	static Composite createShadeComposite(Color shade) {
		return (srcColorModel, dstColorModel, hints) -> new CompositeContext() {
			final CompositeContext srcOverSrcSrc = AlphaComposite.SrcOver.createContext(srcColorModel, srcColorModel,
					hints);
			final CompositeContext srcOverSrcDst = AlphaComposite.SrcOver.createContext(srcColorModel, dstColorModel,
					hints);

			@Override
			public void dispose() {
				try {
					srcOverSrcSrc.dispose();
				} finally {
					srcOverSrcDst.dispose();
				}
			}

			@Override
			public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
				// Create the shade raster.
				WritableRaster shadeRaster = src.createCompatibleWritableRaster();
				Object shadeData = srcColorModel.getDataElements(shade.getRGB(), null);
				for (int x = src.getMinX(); x < src.getMinX() + src.getWidth(); x++) {
					for (int y = src.getMinY(); y < src.getMinY() + src.getHeight(); y++) {
						shadeRaster.setDataElements(x, y, shadeData);
					}
				}

				WritableRaster temp = src.createCompatibleWritableRaster();
				srcOverSrcSrc.compose(shadeRaster, src, temp);
				srcOverSrcDst.compose(temp, dstIn, dstOut);
			}
		};
	}

}
