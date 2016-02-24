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
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import jbyoshi.robotgame.action.BoundAction;
import jbyoshi.robotgame.model.*;

final class GameDraw {
	private final GameModel game;
	private final Map<Model, Sprite<?>> sprites = new TreeMap<>(
			Comparator.<Model, Boolean>comparing(model -> !(model instanceof RobotModel))
			.thenComparing(Model::getId));
	private long paintStart = System.nanoTime();
	final GameComponent component = new GameComponent(this);

	GameDraw(GameModel basedOn) {
		game = basedOn.clone();
		game.getAllModels().forEach(model -> this.createSprite(model, SpriteState.CONTINUOUS));
		sprites.values().forEach(Sprite::preTick);
		sprites.values().forEach(Sprite::postTick);
	}

	void tick(List<BoundAction> actions) {
		if (game == null) {
			throw new IllegalStateException("setup() has not been called!");
		}

		sprites.values().forEach(Sprite::preTick);

		// Keep this synchronized block fast.
		synchronized (game) {
			game.preTick();
			game.postTick(actions);

			Set<Model> models = new HashSet<>(game.getAllModels());
			for (Iterator<Sprite<?>> it = sprites.values().iterator(); it.hasNext(); ) {
				Sprite<?> sprite = it.next();
				if (models.remove(sprite.model)) {
					sprite.state = SpriteState.CONTINUOUS;
				} else if (sprite.state == SpriteState.DEAD) {
					it.remove();
				} else {
					sprite.state = SpriteState.DEAD;
				}
			}
			models.forEach(model -> this.createSprite(model, SpriteState.NEW));
		}

		sprites.values().forEach(Sprite::postTick);
		paintStart = System.nanoTime();
		component.doRepaint();
	}

	private void createSprite(Model model, SpriteState state) {
		@SuppressWarnings("rawtypes")
		Sprite sprite;
		if (model instanceof RobotModel) {
            sprite = new RobotSprite(this, (RobotModel) model, state);
        } else if (model instanceof SpawnerModel) {
            sprite = new SpawnerSprite(this, (SpawnerModel) model, state);
        } else {
            throw new AssertionError("Unknown model " + model.getClass().getName());
        }

		sprites.put(model, sprite);
		sprite.postTick();
	}

	BufferedImage[] updateBuffer() {
		float renderTicks = Math.min((System.nanoTime() - paintStart) / (float) TimeUnit.MILLISECONDS.toNanos(500), 1);

		synchronized (game) {
			if (renderTicks == 1 && !game.isRunning()) {
				if (game.getWinner() == null) {
					component.setMessage(Color.WHITE, "It's a tie!");
				} else {
					component.setMessage(game.getWinner().getColor(), game.getWinner().getName() + " wins!");
				}
			}

			return new BufferedImage[]{component.createLayer(g -> {
				g.setColor(new Color(75, 75, 75));
				g.fill(new Rectangle2D.Double(0, 0, component.getGameSize(), component.getGameSize()));
			}), component.createLayer(g -> {
				g.scale(component.getGridSpotSize(), component.getGridSpotSize());
				sprites.values().forEach(sprite -> sprite.draw((Graphics2D) g.create(), renderTicks));
			}), component.createLayer(g -> {
				g.setColor(Color.BLUE);
				g.draw(new Rectangle2D.Double(0, 0, component.getGameSize(), component.getGameSize()));
			})};
		}
	}

	@SuppressWarnings("unchecked")
	<M extends Model, S extends Sprite<M>> S getSprite(M model) {
		return (S) sprites.get(model);
	}
}
