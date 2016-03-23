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

import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import bsh.*;
import jbyoshi.robotgame.api.*;
import jbyoshi.robotgame.gui.ScriptStorage;
import jbyoshi.robotgame.util.GameJar;
import jbyoshi.robotgame.util.reflect.ReflectField;
import jbyoshi.robotgame.util.reflect.ReflectMethod;

public final class ScriptLoader {
	private static final File javac = findJavac();

	private static File findJavac() {
		File javaHome = new File(System.getProperty("java.home"));
		String path = "bin" + File.separator + "javac";
		if (System.getProperty("os.name").toLowerCase().contains("win")) path += ".exe";
		File javac = new File(javaHome, path);
		if (!javac.exists() && javaHome.getName().equalsIgnoreCase("jre")) {
			javac = new File(javaHome.getParentFile(), path);
		}
		return javac;
	}

	public static boolean isJavacEnabled() {
		return javac != null && javac.exists();
	}

	public static Script loadScript(ScriptStorage scriptStorage) throws InvocationTargetException, CompilationException,
			IOException {
		if (isJavacEnabled()) {
			return loadJavacScript(scriptStorage, javac);
		}
		return loadBshScript(scriptStorage);
	}

	private static Script loadJavacScript(ScriptStorage scriptStorage, File javac) throws IOException,
			CompilationException, InvocationTargetException {
		File outDir = new File(System.getProperty("java.io.tmpdir"), "RobotGame-script-out-"
				+ scriptStorage.getRootDir().getName() + "-" + UUID.randomUUID());
		if (!outDir.mkdirs()) throw new IOException("Failed to create class file output directory");
		Process p = new ProcessBuilder().command(javac.getAbsolutePath(),
				"-cp", GameJar.getGameLocation().getAbsolutePath(),
				"-d", outDir.getAbsolutePath(),
				"-sourcepath", scriptStorage.getSourceDir().getAbsolutePath(),
				"-source", "1.8",
				"-target", "1.8",
				scriptStorage.getMainFile().getAbsolutePath()).start();
		int result;
		try {
			result = p.waitFor();
		} catch (InterruptedException e) {
			final InterruptedIOException ioe = new InterruptedIOException(e.toString());
			ioe.initCause(e);
			throw ioe;
		}
		if (result == 0) {
			URLClassLoader loader = new URLClassLoader(new URL[] {outDir.toURI().toURL()},
					ScriptLoader.class.getClassLoader());
			try {
				return loadScript(loader.loadClass(scriptStorage.getMainClassName()));
			} catch (ClassNotFoundException e) {
				FileNotFoundException fnfe = new FileNotFoundException(e.toString());
				fnfe.initCause(e);
				throw fnfe;
			}
		}

		StringBuilder sb = new StringBuilder("Compilation failed:\n");
		InputStreamReader in = new InputStreamReader(p.getErrorStream());
		char[] cbuf = new char[1024];
		int read;
		while ((read = in.read(cbuf)) > 0) {
			sb.append(cbuf, 0, read);
		}
		throw new CompilationException(sb.toString());
	}

	private static Script loadBshScript(ScriptStorage scriptStorage) throws IOException, InvocationTargetException,
			CompilationException {
		Interpreter interpreter = new Interpreter();
		NameSpace ns = new SecuredNameSpace(new SecuredBshClassManager(ScriptLoader.class.getClassLoader()), "global");
		ns.loadDefaultImports();
		interpreter.setNameSpace(ns);
		try {
			for (File file : scriptStorage.getAuxiliaryFiles()) {
				loadAndEvaluate(interpreter, ns, file);
			}
			Object main = loadAndEvaluate(interpreter, ns, scriptStorage.getMainFile());
			if (main instanceof Class) {
				Class<?> clazz = (Class<?>) main;
				return loadScript(clazz);
			}
			BshMethod tick = interpreter.getNameSpace().getMethod("tick", new Class[] { Game.class });
			if (tick == null) {
				throw new InvocationTargetException(new Error("Global method tick(Game) not defined"));
			}
			return game -> {
				try {
					tick.invoke(new Object[] { game }, interpreter);
				} catch (TargetError e) {
					reformatException(e.getTarget(), e);
					e.getTarget().printStackTrace();
				} catch (EvalError e) {
					e.printStackTrace();
				}
			};
		} catch (TargetError e) {
			reformatException(e.getTarget(), e);
			throw new InvocationTargetException(e.getTarget());
		} catch (UtilTargetError e) {
			throw new InvocationTargetException(e.t);
		} catch (EvalError | UtilEvalError e) {
			throw new CompilationException(e);
		}
	}

	private static Object loadAndEvaluate(Interpreter interpreter, NameSpace ns, File file) throws IOException, EvalError {
		String contents = String.join(System.lineSeparator(), Files.readAllLines(file.toPath()));
		return interpreter.eval(new StringReader(contents), ns, file.getName());
	}

	private static Script loadScript(Class<?> clazz) throws InvocationTargetException {
		try {
			Method tick = clazz.getDeclaredMethod("tick", Game.class);
			if (!Modifier.isPublic(tick.getModifiers())) {
				throw new InvocationTargetException(new Error(clazz.getName() + ".tick(Game) must be public"));
			}
			if (!Modifier.isStatic(tick.getModifiers())) {
				throw new InvocationTargetException(new Error(clazz.getName() + ".tick(Game) must be static"));
			}
			return game -> {
				try {
					tick.invoke(null, game);
				} catch (IllegalAccessException e) {
					throw new AssertionError(e);
				} catch (InvocationTargetException e) {
					if (e.getCause() instanceof TargetError) {
						TargetError targetError = (TargetError) e.getCause();
						reformatException(targetError.getTarget(), targetError);
						targetError.getTarget().printStackTrace();
					} else {
						e.printStackTrace();
					}
				}
			};
		} catch (NoSuchMethodException e) {
			throw new InvocationTargetException(new Error(clazz.getName() + ".tick(Game) not defined"));
		}
	}

	private static void reformatException(Throwable t, TargetError eval) {
		StackTraceElement[] oldStackTrace = t.getStackTrace();
		int oldStackTraceIndex = 0;
		List<StackTraceElement> stackTrace = new ArrayList<>();
		while (!oldStackTrace[oldStackTraceIndex].getClassName().startsWith("bsh.")) {
			stackTrace.add(oldStackTrace[oldStackTraceIndex]);
			oldStackTraceIndex++;
		}

		StackTraceElement last = stackTrace.get(stackTrace.size() - 1);
		if ((last.getClassName().equals("java.lang.reflect.Method") && last.getMethodName().equals("invoke"))
				|| (last.getClassName().equals("java.lang.reflect.Constructor") && last.getMethodName().equals("newInstance"))) {
			stackTrace.remove(last);
			last = stackTrace.get(stackTrace.size() - 1);
			while (last.getClassName().startsWith("sun.reflect.")) {
				stackTrace.remove(last);
				last = stackTrace.get(stackTrace.size() - 1);
			}
		}

		CallStack stack = EvalError_callstack.get(eval);
		for (int i = 0; i < stack.depth(); i++) {
			NameSpace ns = stack.get(i);
			if (ns == NameSpace.JAVACODE) continue;

			NameSpace owner = ns;
			while (owner.getName().endsWith("/BlockNameSpace")) {
				owner = owner.getParent();
			}

			if (NameSpace_isClass.get(owner)) {
				continue;
			}

			String className, methodName;
			if (NameSpace_isMethod.get(owner)) {
				methodName = owner.getName();
				NameSpace parent = owner.getParent();
				if (parent != null && NameSpace_isClass.get(parent)) {
					className = parent.getName();
				} else {
					className = "<script file>";
				}
			} else {
				className = "<unknown>";
				methodName = "<unknown>";
			}

			String sourceFile;
			int lineNumber;
			if (i == 0) {
				// Might not have the proper info, so it needs separate code.
				Object node = EvalError_node.get(eval);
				sourceFile = SimpleNode_getSourceFile.get(node);
				lineNumber = Token_beginLine.get(SimpleNode_firstToken.get(node));
			} else {
				Object node = NameSpace_getNode.get(ns);
				if (node != null) {
					sourceFile = SimpleNode_getSourceFile.get(node);
					lineNumber = ns.getInvocationLine();
				} else {
					sourceFile = null;
					lineNumber = -1;
				}
			}
			stackTrace.add(new StackTraceElement(className, methodName, sourceFile, lineNumber));
		}

		while (oldStackTrace[oldStackTraceIndex].getClassName().startsWith("bsh.")
				|| "BeanShell Generated via ASM (www.objectweb.org)"
						.equals(oldStackTrace[oldStackTraceIndex].getFileName())) {
			// stackTrace.add(oldStackTrace[oldStackTraceIndex]);
			oldStackTraceIndex++;
		}

		while (oldStackTraceIndex < oldStackTrace.length) {
			stackTrace.add(oldStackTrace[oldStackTraceIndex]);
			oldStackTraceIndex++;
		}

		t.setStackTrace(stackTrace.toArray(new StackTraceElement[stackTrace.size()]));
	}

	private static final ReflectField<EvalError, CallStack> EvalError_callstack = new ReflectField<>(EvalError.class, "callstack");
	private static final ReflectField<EvalError, Object> EvalError_node = new ReflectField<>(EvalError.class, "node");
	private static final ReflectMethod<NameSpace, Object> NameSpace_getNode = new ReflectMethod<>(NameSpace.class, "getNode");
	private static final ReflectField<NameSpace, Boolean> NameSpace_isClass = new ReflectField<>(NameSpace.class, "isClass");
	private static final ReflectField<NameSpace, Boolean> NameSpace_isMethod = new ReflectField<>(NameSpace.class, "isMethod");
	private static final ReflectMethod<Object, String> SimpleNode_getSourceFile = new ReflectMethod<>(EvalError_node.getType(), "getSourceFile");
	private static final ReflectField<Object, Object> SimpleNode_firstToken = new ReflectField<>(EvalError_node.getType(), "firstToken");
	private static final ReflectField<Object, Integer> Token_beginLine = new ReflectField<>(SimpleNode_firstToken.getType(), "beginLine");
}
