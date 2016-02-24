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

import java.lang.reflect.Field;

public final class ReflectField<T, V> {
    private final Field f;
    public ReflectField(Class<? extends T> owner, String name) {
        try {
            f = owner.getDeclaredField(name);
            f.setAccessible(true);
        } catch (NoSuchFieldException exception) {
            NoSuchFieldError error = new NoSuchFieldError(owner.getName() + "." + name);
            error.initCause(exception);
            throw error;
        }
    }

    @SuppressWarnings("unchecked")
    public V get(T obj) {
        if (obj == null) {
            throw new NullPointerException("tried to get null." + f.getName());
        }
        try {
            Object val;
            if (f.getType() == Boolean.TYPE) {
                val = f.getBoolean(obj);
            } else if (f.getType() == Character.TYPE) {
                val = f.getChar(obj);
            } else if (f.getType() == Byte.TYPE) {
                val = f.getByte(obj);
            } else if (f.getType() == Short.TYPE) {
                val = f.getShort(obj);
            } else if (f.getType() == Integer.TYPE) {
                val = f.getInt(obj);
            } else if (f.getType() == Long.TYPE) {
                val = f.getLong(obj);
            } else if (f.getType() == Float.TYPE) {
                val = f.getFloat(obj);
            } else if (f.getType() == Double.TYPE) {
                val = f.getDouble(obj);
            } else {
                val = f.get(obj);
            }
            return (V) val;
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Class<? extends V> getType() {
        return (Class<? extends V>) f.getType();
    }
}
