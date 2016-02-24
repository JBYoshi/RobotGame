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
package jbyoshi.robotgame;

import java.io.File;
import java.net.*;

public final class BootstrapClassLoader extends ClassLoader {
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		final String classFile = name.replace('.', '/') + ".class";
		URL resource = getResource(classFile);
		if (resource == null) {
			return super.loadClass(name, resolve);
		}

		URL source = resource;
		if (resource.getProtocol().equals("jar")) {
			try {
				source = new URL(resource.getPath());
			} catch (MalformedURLException e) {
				throw new ClassNotFoundException(name, e);
			}
		}
		if (source.getProtocol().equals("file")) {
			@SuppressWarnings("deprecation")
			String path = URLDecoder.decode(source.getPath());
			while (path.startsWith("/")) {
				path = path.substring(1);
			}
			if (path.replace('/', File.separatorChar).startsWith(System.getProperty("java.home"))) {
				return super.loadClass(name, resolve);
			}
		}

		// Not from the JRE? Then refuse to load it.
		throw new ClassNotFoundException(name);
	}

	static {
		ClassLoader.registerAsParallelCapable();
	}
}