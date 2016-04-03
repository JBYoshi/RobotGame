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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.*;

public final class RandomHelper {
    public static <T> List<T> choose(int num, Collection<T> values) {
        List<T> list = new ArrayList<>(values);
        Collections.shuffle(list);
        return ImmutableList.copyOf(list.subList(0, num));
    }

    public static <T> T[] choose(int num, T... values) {
        @SuppressWarnings("unchecked")
        final Class<T> componentType = (Class<T>) values.getClass().getComponentType();
        return Iterables.toArray(choose(num, Arrays.<T>asList(values)), componentType);
    }
}
