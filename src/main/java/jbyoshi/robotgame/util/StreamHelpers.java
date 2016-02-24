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
package jbyoshi.robotgame.util;

import com.google.common.collect.ImmutableSet;

import java.util.function.*;
import java.util.stream.*;

public final class StreamHelpers {
	public static <E1, E2> Function<E1, Stream<E2>> casting(Class<E2> clazz) {
		return in -> {
			if (clazz.isInstance(in)) {
				return Stream.of(clazz.cast(in));
			}
			return Stream.of();
		};
	}

	public static <E> Collector<E, ?, ImmutableSet<E>> toImmutableSet() {
		return  Collector.<E, ImmutableSet.Builder<E>, ImmutableSet<E>> of(ImmutableSet::builder,
				ImmutableSet.Builder::add, (one, two) -> one.addAll(two.build()), ImmutableSet.Builder::build);
	}
}
