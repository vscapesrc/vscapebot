package com.vsbot.scriptmanager;

import scripts.Script;
import scripts.ScriptManifest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class ScriptManager {
    String[] locations = {"~/com.bsbot",/* linux, macos(?) */
            System.getenv("APPDATA") + "\\bsbot"/* windows */};
    String[] files = {"compile.bat", "compile.sh"};

    FileWriter fwStream;

    /**
     * @return Whether or not the directories were made.
     */

    public boolean makeDirectories() {
        for (String location : locations) {
            File file = new File(location);
            File jar = null;
            File script = null;// = new File(location + "/scripts");
            if (file.exists()) {
                String os = System.getProperty("os.name");
                if (os.contains("Windows")) {
                    jar = new File(location + "\\jar");
                    script = new File(location + "\\scripts");
                    System.out.println("Your script dir is " + file
                            + "\\scripts");
                } else {
                    jar = new File(location + "/jar");
                    script = new File(location + "/scripts");
                    System.out.println("Your script dir is " + file
                            + "/scripts");
                    System.out
                            .println("ps non windows untested please let me know if it works");
                }
                if (jar != null && jar.exists() && script != null && script.exists()) {
                    return true;
                }
            }

        }
        String os = System.getProperty("os.name");
        if (os.contains("Windows")) {
            System.out.println("making windows");
            new File(locations[1]).mkdir();
            new File(locations[1] + "\\jar").mkdir();
            new File(locations[1] + "\\scripts").mkdir();
        } else if (os.contains("Linux") || os.contains("Mac")) {
            new File(locations[0]).mkdir();
            new File(locations[0] + "/jar").mkdir();
            new File(locations[0] + "/scripts").mkdir();
        }

        return false;
    }

    public boolean makeCompilers() {
        String os = System.getProperty("os.name");
        if (os.contains("Windows")) {
            File compiler = new File(locations[1] + "\\compile.bat");
            if (compiler.exists()) {
                return true;
            } else {
                try {
                    System.out.println(compiler.getPath());
                    return compiler.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            File compiler = new File(locations[0] + " \\compile.sh");
            if (compiler.exists()) {
                return true;
            } else {
                try {
                    return compiler.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public boolean writeCompilers() {
        String os = System.getProperty("os.name");
        if (os.contains("Windows")) {
            File compiler = new File(locations[1] + "\\compile.bat");
            if (compiler.exists()) {
                if (compiler.length() > 5) {
                    return true;
                } else {
                    try {
                        fwStream = new FileWriter(locations[1]
                                + "\\compile.bat");
                        fwStream.write("@echo off \r\necho Make sure you have the bot jar in the jar directory as BSBot.jar \r\njavac -classpath .;./jar/BSBot.jar ./scripts/*.java \r\npause");
                        fwStream.close();
                        return true;
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        } else {
            File compiler = new File(locations[0] + " \\compile.sh");
            if (compiler.exists()) {
                if (compiler.length() > 5) {
                    return true;
                } else {
                    try {
                        fwStream = new FileWriter(locations[0] + "\\compile.sh");
                        fwStream.write("\r\njavac -cp .;./jar/BSBot.jar ./scripts/*.java");
                        fwStream.close();
                        return true;
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    public ScriptManager() {
    }

    public Class[] getClassesFromFolder() {
        File folder = getFolder();
        String thePath = folder.getPath();
        ArrayList<Class<Script>> classes = new ArrayList<Class<Script>>();
        try {

            URL[] path = {new URL("file://" + thePath + "/scripts/")};
            File scriptFolder = new File(getFolder().getPath() + "/scripts");
            URLClassLoader cl = new URLClassLoader(path);
            for (String script : scriptFolder.list()) {
                if (script.contains(".class") && !script.contains("$")) {
                    String truePath = script.replace(".class", "");
                    try {
                        Class<?> scriptClass = (Class<?>) cl
                                .loadClass(truePath);
                        classes.add((Class<Script>) scriptClass);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("amount: " + classes.size());
        return classes.toArray(new Class<?>[classes.size()]);
    }

    public Script getScriptForClass(Class<Script> theClass) {
        Script scripts = null;
        ArrayList<Class<Script>> list = new ArrayList();
        list.add(theClass);

        try {
            if (theClass.isAnnotationPresent(ScriptManifest.class)) {
                scripts = (Script) list.get(0).newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return scripts;
    }

    public File getFolder() {
        for (String location : locations) {
            File file = new File(location);
            if (file.exists()) {
                return file;
            }
        }
        String os = System.getProperty("os.name");
        if (os.contains("Windows")) {
            new File(locations[1]).mkdir();

        } else if (os.contains("Linux") || os.contains("Mac")) {
            new File(locations[0]).mkdir();
        }
        return getFolder();
    }
}