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

import java.lang.reflect.*;

import bsh.classpath.*;

final class SecuredBshClassManager extends ClassManagerImpl {
	SecuredBshClassManager(ClassLoader loader) {
		setClassLoader(loader);
	}

	@Override
	public void cacheResolvedMethod(Class clazz, Class[] parameters, Method method) {
		SecuredNameSpace.filterPackageOrClass(clazz.getName());
		SecuredNameSpace.filterPackageOrClass(method.getDeclaringClass().getName());
		// TODO filter method
		super.cacheResolvedMethod(clazz, parameters, method);
	}
}
