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

final class MySpawnerView extends SpawnerView implements MySpawner {
	MySpawnerView(GameView game, SpawnerModel spawner) {
		super(game, spawner);
	}

	@Override
	public void startSpawn() {
		if (model.countdown > 0) {
			throw new IllegalStateException("Spawning in progress!");
		}
		addAction(new SpawnerStartSpawnAction(new RobotModel(model.player, model.loc)));
	}

}
