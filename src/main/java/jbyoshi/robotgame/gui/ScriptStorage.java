package jbyoshi.robotgame.gui;

import jbyoshi.robotgame.RobotGame;
import jbyoshi.robotgame.util.GameJar;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public final class ScriptStorage implements Comparable<ScriptStorage> {
    private final File root, srcDir;
    private final String mainClassName;

    ScriptStorage(String line) throws IOException {
        this(new File(line.substring(0, line.lastIndexOf(' '))), line.substring(line.lastIndexOf(' ') + 1));
    }

    ScriptStorage(File root, String mainClassName) throws IOException {
        this.root = root;
        srcDir = new File(root, "src");

        this.mainClassName = mainClassName;
        if (!getMainFile().exists()) throw new FileNotFoundException(getMainFile().toString());

        if (GameJar.getGameJar() != null) {
            generateIdeFiles();
        } else {
            System.out.println("Running outside of JAR, cannot setup IDE projects.");
            System.out.println("To test the creation of IDE projects, please use gradlew runShadow.");
        }
    }

    String getMainClassName() {
        return mainClassName;
    }

    public File getRootDir() {
        return root;
    }

    public File getMainFile() {
        return new File(srcDir, mainClassName.replace('.', '/') + ".java");
    }

    public List<File> getAuxiliaryFiles() {
        List<File> files = new LinkedList<>();
        listRecursive(srcDir, files);
        files.remove(new File(srcDir, mainClassName.replace('.', File.separatorChar) + ".java"));
        return files;
    }

    private void generateIdeFiles() throws IOException {
        // Eclipse
        generateIdeFile(".project");
        generateIdeFile(".classpath");
        if (!new File(root, ".settings").mkdir()) throw new IOException("Could not create .settings/");
        generateIdeFile(".settings/org.eclipse.jdt.core.prefs");

        // IntelliJ
        generateIdeFile("$PROJECT_NAME$.ipr");
        generateIdeFile("$PROJECT_NAME$.iml");
        generateIdeFile("$PROJECT_NAME$.iws");
    }

    private void generateIdeFile(String name) throws IOException {
        StringBuilder data = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                RobotGame.class.getResourceAsStream("idetemplates/" + name)))) {
            String line;
            while ((line = in.readLine()) != null) data.append(line).append("\n");
        }
        try (FileWriter out = new FileWriter(new File(root, name.replace("$PROJECT_NAME$", root.getName())))) {
            out.write(data.toString().replace("$PROJECT_NAME$", xmlEscape(root.getName()))
                    .replace("$GAME$", xmlEscape(GameJar.getGameJar().getAbsolutePath())));
            out.flush();
        }
    }

    private static String xmlEscape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&apos;")
                .replace("\"", "&quot;");
    }

    @Override
    public String toString() {
        return root + " " + mainClassName;
    }

    private static void listRecursive(File dir, List<File> out) {
        File[] children = dir.listFiles();
        if (children == null) {
            if (dir.exists()) {
                out.add(dir);
            }
        } else {
            for (File f : children) {
                if (f.isDirectory()) {
                    listRecursive(f, out);
                } else {
                    out.add(f);
                }
            }
        }
    }

    @Override
    public int compareTo(ScriptStorage o) {
        return this.mainClassName.compareTo(o.mainClassName);
    }
}
