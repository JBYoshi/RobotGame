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
package jbyoshi.robotgame.util.reflect;

import com.google.common.base.Throwables;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class ReflectMethod<T, V> {
    private final Method m;
    public ReflectMethod(Class<? extends T> owner, String name, Class<?>...parameters) {
        try {
            m = owner.getDeclaredMethod(name);
            m.setAccessible(true);
        } catch (NoSuchMethodException exception) {
            NoSuchMethodError error = new NoSuchMethodError(owner.getName() + "." + name + "("
                    + String.join(", ", Arrays.stream(parameters).map(Class::getName).collect(Collectors.toList()))
                    + ")");
            error.initCause(exception);
            throw error;
        }
    }

    @SuppressWarnings("unchecked")
    public V get(T obj, Object...args) {
        if (obj == null) {
            throw new NullPointerException("tried to call null." + m.getName() + "()");
        }
        try {
            return (V) m.invoke(obj, args);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw Throwables.propagate(e);
        }
    }
}
