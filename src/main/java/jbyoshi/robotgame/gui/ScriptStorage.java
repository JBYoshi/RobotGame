package jbyoshi.robotgame.gui;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public final class ScriptStorage implements Comparable<ScriptStorage> {
    public final File root;
    final String mainClassName;

    ScriptStorage(String line) {
        int split = line.lastIndexOf(" ");
        root = new File(line.substring(0, split));
        mainClassName = line.substring(split + 1);
    }

    public File getMainFile() {
        return new File(root, mainClassName.replace('.', '/') + ".java");
    }

    public List<File> getAuxiliaryFiles() {
        List<File> files = new LinkedList<>();
        listRecursive(root, files);
        files.remove(new File(root, mainClassName.replace('.', File.separatorChar) + ".java"));
        return files;
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
