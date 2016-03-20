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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

class ResourceClassLoader extends ClassLoader {
	ResourceClassLoader() {
	}

	ResourceClassLoader(ClassLoader parent) {
		super(parent);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		final String classFile = name.replace('.', '/') + ".class";
		URL resource = getResource(classFile);
		if (resource == null) {
			throw new ClassNotFoundException(name, new FileNotFoundException(classFile));
		}

		final String pkg = name.substring(0, name.lastIndexOf('.'));
		if (getPackage(pkg) == null) {
			definePackage(pkg, null, null, null, null, null, null, null);
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (InputStream in = resource.openStream()) {
			byte[] buf = new byte[65536];
			int read;
			while ((read = in.read(buf)) > 0) {
				out.write(buf, 0, read);
			}
			out.flush();
		} catch (IOException e) {
			throw new ClassNotFoundException(name, e);
		}
		byte[] classBytes = out.toByteArray();
		return defineClass(name, classBytes, 0, classBytes.length);
	}

	static {
		ClassLoader.registerAsParallelCapable();
	}
}