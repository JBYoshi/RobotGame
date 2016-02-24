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
package jbyoshi.robotgame.action;

import java.util.*;

public final class BoundAction {
	public final Action<?> action;
	public final UUID targetId;

	public BoundAction(Action<?> action, UUID targetId) {
		this.action = action;
		this.targetId = targetId;
	}

	@Override
	public int hashCode() {
		return this.targetId.hashCode() ^ action.getClass().hashCode();
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof BoundAction && ((BoundAction) other).targetId.equals(targetId)
				&& ((BoundAction) other).action.getClass().equals(action.getClass());
	}
}
