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

import jbyoshi.robotgame.api.Player;

import java.awt.*;

public class PlayerImpl implements Player {
    private final String name;
    private final Color c;

    public PlayerImpl(String name, Color c) {
        this.name = name;
        this.c = c;
    }

    @Override
    public String getName() {
        return name;
    }

    public Color getColor() {
        return this.c;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PlayerImpl && ((PlayerImpl) other).name.equalsIgnoreCase(name);
    }
}
