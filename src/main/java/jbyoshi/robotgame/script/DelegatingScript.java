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
package jbyoshi.robotgame.script;

import jbyoshi.robotgame.api.Game;

public class DelegatingScript implements Script {
    private Script currentScript;

    public DelegatingScript(Script currentScript) {
        this.currentScript = currentScript;
    }

    public synchronized void setScript(Script currentScript) {
        this.currentScript = currentScript;
    }

    @Override
    public void tick(Game game) {
        Script scriptSnapshot;
        synchronized (this) {
            scriptSnapshot = currentScript;
        }
        scriptSnapshot.tick(game);
    }
}
