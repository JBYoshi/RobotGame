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

import bsh.*;
import jbyoshi.robotgame.api.*;

import java.lang.reflect.Modifier;
import java.util.HashSet;

final class SecuredNameSpace extends NameSpace {
	private static final long serialVersionUID = -1152079216306105189L;

	SecuredNameSpace(SecuredBshClassManager classManager, String name) {
		super(classManager, name);
	}

	SecuredNameSpace(NameSpace parent, SecuredBshClassManager classManager, String name) {
		super(parent, classManager, name);
	}

	SecuredNameSpace(NameSpace parent, String name) {
		super(parent, name);
	}

	@Override
	public void doSuperImport() {
		throw new SecurityException("Super imports are not allowed!");
	}

	@Override
	public Class<?> getClass(String name) throws UtilEvalError {
		Class<?> clazz = super.getClass(name);
		if (clazz != null) {
			filterPackageOrClass(clazz.getName());
			if (clazz.getPackage() != null && !Modifier.isPublic(clazz.getModifiers())) {
				throw new SecurityException(SecurityException.class.getName() + ": Cannot access " + name);
			}
		}
		return clazz;
	}

	@Override
	public Object getCommand(String name, Class[] argTypes, Interpreter interpreter) {
		return null;
	}

	@Override
	public void importClass(String name) {
		try {
			getClass(name);
		} catch (UtilEvalError utilEvalError) {
			// It'll catch it later.
		}
		super.importClass(name);
	}

	@Override
	public void importCommands(String path) {
	}

	@Override
	public void importPackage(String name) {
		if (name != null) filterPackageOrClass(name);
		super.importPackage(name);
	}

	@Override
	public void importStatic(Class clazz) {
		if (clazz != null) filterPackageOrClass(clazz.getName());
		super.importStatic(clazz);
	}

	@Override
	public void loadDefaultImports() {
		importPackage("java.lang");
		importPackage(Game.class.getPackage().getName());
	}

	private static final HashSet<String> restrictedPackagesAndClasses = new HashSet<>();

	static {
		restrictedPackagesAndClasses.add("bsh");
		restrictedPackagesAndClasses.add("sun");
		restrictedPackagesAndClasses.add("com.oracle");
		restrictedPackagesAndClasses.add("com.sun");
		restrictedPackagesAndClasses.add("java.applet");
		restrictedPackagesAndClasses.add("java.awt");
		restrictedPackagesAndClasses.add("java.io.Console");
		restrictedPackagesAndClasses.add("java.io.File");
		restrictedPackagesAndClasses.add("java.io.FileDescriptor");
		restrictedPackagesAndClasses.add("java.io.FileFilter");
		restrictedPackagesAndClasses.add("java.io.FileInputStream");
		restrictedPackagesAndClasses.add("java.io.FilenameFilter");
		restrictedPackagesAndClasses.add("java.io.FileOutputStream");
		restrictedPackagesAndClasses.add("java.io.FilePermission");
		restrictedPackagesAndClasses.add("java.io.FileReader");
		restrictedPackagesAndClasses.add("java.io.FileWriter");
		restrictedPackagesAndClasses.add("java.lang.instrument");
		restrictedPackagesAndClasses.add("java.lang.management");
		restrictedPackagesAndClasses.add("java.lang.reflect");
		restrictedPackagesAndClasses.add("java.net");
		restrictedPackagesAndClasses.add("java.nio.channel");
		restrictedPackagesAndClasses.add("java.nio.charset");
		restrictedPackagesAndClasses.add("java.nio.file");
		restrictedPackagesAndClasses.add("java.rmi");
		restrictedPackagesAndClasses.add("java.sql");
		restrictedPackagesAndClasses.add("java.util.prefs");
		restrictedPackagesAndClasses.add("javax.accessibility");
		restrictedPackagesAndClasses.add("javax.activation");
		restrictedPackagesAndClasses.add("javax.activity");
		restrictedPackagesAndClasses.add("javax.imageio");
		restrictedPackagesAndClasses.add("javax.lang.model");
		restrictedPackagesAndClasses.add("javax.management");
		restrictedPackagesAndClasses.add("javax.naming");
		restrictedPackagesAndClasses.add("javax.net");
		restrictedPackagesAndClasses.add("javax.print");
		restrictedPackagesAndClasses.add("javax.rmi");
		restrictedPackagesAndClasses.add("javax.script");
		restrictedPackagesAndClasses.add("javax.security");
		restrictedPackagesAndClasses.add("javax.smartcardio");
		restrictedPackagesAndClasses.add("javax.sound");
		restrictedPackagesAndClasses.add("javax.sql");
		restrictedPackagesAndClasses.add("javax.swing");
		restrictedPackagesAndClasses.add("javax.tools");
		restrictedPackagesAndClasses.add("javax.transaction");
		restrictedPackagesAndClasses.add("javax.xml");
		restrictedPackagesAndClasses.add("javafx");
		restrictedPackagesAndClasses.add("javax.crypto");
		restrictedPackagesAndClasses.add("javax.jnlp");
		restrictedPackagesAndClasses.add("jdk");
		restrictedPackagesAndClasses.add("netscape");
		restrictedPackagesAndClasses.add("oracle");
		restrictedPackagesAndClasses.add("org.ietf.jgss");
		restrictedPackagesAndClasses.add("org.jcp.xml.dsig");
		restrictedPackagesAndClasses.add("org.omg");
		restrictedPackagesAndClasses.add("org.w3c.dom");
		restrictedPackagesAndClasses.add("org.xml.sax");
		restrictedPackagesAndClasses.add(Robot.class.getPackage().getName());
		// TODO more filtering
	}

	static void filterPackageOrClass(String name) {
		if (name.equals("")) {
			return;
		}
		if (name.equals(Game.class.getPackage().getName())) {
			return;
		}
		if (restrictedPackagesAndClasses.contains(name)) {
			throw new SecurityException(SecurityException.class.getName() + ": Cannot access " + name);
		}
		int dot = name.lastIndexOf('.');
		if (dot >= 0) {
			filterPackageOrClass(name.substring(0, dot));
		}
	}

}
