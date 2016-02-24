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
package jbyoshi.robotgame.model;

import jbyoshi.robotgame.api.Point;

import java.util.*;

public abstract class Model implements Cloneable {
	private UUID id = UUID.randomUUID();
	GameModel game;
	public Point loc;

	protected Model(Point loc) {
		this.loc = loc;
	}

	public GameModel getGame() {
		return this.game;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getId() {
		return id;
	}

	@Override
	public final int hashCode() {
		return id.hashCode() ^ Model.class.hashCode();
	}

	@Override
	public final boolean equals(Object other) {
		return other != null && other.getClass().equals(getClass()) && ((Model) other).getId().equals(id);
	}

	@Override
	public String toString() {
		return getClass().getName() + "@" + id;
	}

	void onTickStart() {
	}

	void onTickEnd() {
	}

	@Override
	public Model clone() {
		Model m;
		try {
			m = (Model) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
		m.game = null;
		return m;
	}
}
