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

import jbyoshi.robotgame.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

final class ViewRegistry {
    private static final Map<Class<?>, BiFunction<GameView, ? extends Model, ? extends ModelView<?>>> registry
            = new HashMap<>();

    @SuppressWarnings("unckecked")
    static ModelView<?> wrap(Model model, GameView game) {
        return ((BiFunction<GameView, Model, ? extends ModelView<?>>) registry.get(model.getClass())).apply(game, model);
    }

    private static <M extends Model & OwnedModel, V extends ModelView<M>> void registerOwned(Class<M> modelClass,
            BiFunction<GameView, M, ? extends V> mine, BiFunction<GameView, M, ? extends V> enemy) {
        register(modelClass, (game, model) -> {
            if (model.getPlayer().equals(game.player)) {
                return mine.apply(game, model);
            }
            return enemy.apply(game, model);
        });
    }

    private static <M extends Model, V extends ModelView<M>> void register(Class<M> modelClass,
            BiFunction<GameView, M, V> creator) {
        registry.put(modelClass, creator);
    }

    static {
        registerOwned(RobotModel.class, MyRobotView::new, RobotView::new);
        registerOwned(SpawnerModel.class, MySpawnerView::new, SpawnerView::new);
        register(PowerSourceModel.class, PowerSourceView::new);
    }
}
