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
package jbyoshi.robotgame.api;

import java.util.Objects;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

public final class Point implements Located {
	private final int x, y;

	public Point(int x, int y) {
		x %= Game.WORLD_SIZE;
		y %= Game.WORLD_SIZE;
		if (x < 0) x = Game.WORLD_SIZE + x;
		if (y < 0) y = Game.WORLD_SIZE + y;
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public Point add(Direction dir) {
		return new Point(x + dir.dx, y + dir.dy);
	}

	public Point add(int dx, int dy) {
		return new Point(x + dx, y + dy);
	}

	public Point add(Point other) {
		return new Point(x + other.x, y + other.y);
	}

	public Point unaryOp(IntUnaryOperator transform) {
		return new Point(transform.applyAsInt(x), transform.applyAsInt(y));
	}

	public Point binaryOp(IntBinaryOperator transform, Point other) {
		return new Point(transform.applyAsInt(x, other.x), transform.applyAsInt(y, other.y));
	}

	public int distanceTo(Point other) {
		return Math.max(Math.abs(x - other.x), Math.abs(y - other.y));
	}

	@Override
	public String toString() {
		return "(" + this.x + ", " + this.y + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Point point = (Point) o;
		return x == point.x && y == point.y;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public Point getLocation() {
		return this;
	}
}
