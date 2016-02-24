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

import jbyoshi.robotgame.action.Action;
import jbyoshi.robotgame.api.ObjectInGame;
import jbyoshi.robotgame.api.Point;
import jbyoshi.robotgame.model.Model;

abstract class ModelView<T extends Model> implements ObjectInGame {
    final GameView game;
    final T model;

    ModelView(GameView game, T model) {
        this.game = game;
        this.model = model;
    }

    @Override
    public Point getLocation() {
        return model.loc;
    }

    final void addAction(Action<? super T> action) {
        game.addAction(model, action);
    }
}
