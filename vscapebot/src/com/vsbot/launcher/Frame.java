package com.vsbot.launcher;

import scripts.*;

import javax.swing.*;

import com.vsbot.scriptmanager.ScriptManager;

import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

public class Frame extends JFrame implements AppletStub, ActionListener {

    JTabbedPane tabs = new JTabbedPane();


    ///private ArrayList<Loader> loaders = new ArrayList<Loader>();

    public static Loader[] loaders = new Loader[6];

    Script[] scriptArray = new Script[5];

    Thread[] scriptThreads = new Thread[5];


    ScriptManager sm = new ScriptManager();

    String[] debugItems = {"mouse", "objects", "position", "npcs", "inventory"};


    /**
     * All the menu items
     */
    JMenuBar menuBar = new JMenuBar();
    JMenu[] menus = new JMenu[]{new JMenu("File"), new JMenu("Debug"),
            new JMenu("Tabs")};
    JMenuItem start = new JMenuItem("Start script");
    JMenuItem stop = new JMenuItem("Stop script");
    JMenuItem selector = new JMenuItem("Script selector");
    JMenuItem quit = new JMenuItem("Quit");
    JMenuItem mouse = new JMenuItem("Debug mouse");
    JMenuItem objects = new JMenuItem("Debug objects");
    JMenuItem position = new JMenuItem("Debug position");
    JMenuItem npcs = new JMenuItem("Debug npcs");
    JMenuItem inventory = new JMenuItem("Debug inventory");
    JMenuItem newTab = new JMenuItem("New tab");
    JMenuItem closeTab = new JMenuItem("Close tab");

    public static void main(String args[]) {
        new Frame();
    }

    public Frame() {


        this.setTitle("BSBot v 2.0 - Now with tabs!");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(765 + 23, 600);
        this.setJMenuBar(menuBar);
        for (JMenu menu : menus) {
            menuBar.add(menu);
            String text = menu.getText();
            if (text.equals("File")) {
                menu.add(start);
                menu.add(stop);
                menu.add(selector);
                menu.add(quit);
            } else if (text.equals("Debug")) {
                menu.add(mouse);
                menu.add(objects);
                menu.add(position);
                menu.add(npcs);
                menu.add(inventory);
            } else if (text.equals("Tabs")) {
                menu.add(newTab);
                menu.add(closeTab);
            }
            for (int i = 0; i < menu.getItemCount(); i++) {
                JMenuItem item = menu.getItem(i);
                item.addActionListener(this);
            }
        }
        selector.setEnabled(false);
        this.add(tabs);
        setVisible(true);
        addLoader();
        startUp();
        tabs.setSize(40, 40);
        sm.makeDirectories(); // make the bot dirs
        sm.makeCompilers(); // make the compilers for scripts
        sm.writeCompilers();
    }

    public void startUp() {

        for (int i = 0; i < 5; i++) {
            if (loaders[i] != null) {
                tabs.add(loaders[i]);
            }

        }
        setTabNames();
    }

    public void newTab() {
        setTabNames();
    }

    /**
     * Sets the tab names
     */
    public void setTabNames() {
        for (int i = 0; i < tabs.getComponentCount(); i++) {
            tabs.setTitleAt(i, "bot #" + (i + 1));
        }
    }

    public boolean addLoader() {
        if (tabs.getTabCount() < 5) {
            loaders[tabs.getTabCount()] = new Loader();
            loaders[tabs.getTabCount()].setSize(765 + 23, 603);
            loaders[tabs.getTabCount()].setStub(this);
            loaders[tabs.getTabCount()].init();
            loaders[tabs.getTabCount()].start();
            tabs.add(loaders[(tabs.getTabCount())]);
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "You already have 5 bots. That is the maximum amount of connections!");
        }
        return false;
    }

    public int getActiveComponent() {
        return tabs.getSelectedIndex();
    }

    public void removeTab(final int index) {
        System.out.println("index: " + index);
        if (loaders[index] != null) {
            tabs.remove(index);
            System.out.println("removing: " + index);
            loaders[index].destroy();
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    loaders[index].destroy();

                }

            });
            t.run();
            System.gc();


            System.out.println(loaders[index]);

        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        String command = e.getActionCommand();
        int tabNumber = tabs.getSelectedIndex();
        for (String loop : debugItems) {
            if (command.contains(loop)) {
                loaders[tabNumber].setRunningDebug(loop);
            }
        }
        if (command.contains("Stop script")) {
            if (scriptArray[tabNumber] != null) {
                scriptArray[tabNumber].setRunning(false);
                scriptArray[tabNumber] = null;
            }
        }
        if (command.equals("New tab")) {
            if (addLoader()) {
                newTab();
            }
        }
        if (command.equals("Close tab")) {
            removeTab(tabs.getSelectedIndex());
        }
        if (command.equals("Start script") && scriptArray[tabNumber] == null) {
            String a = JOptionPane
                    .showInputDialog(null,
                            "What script do you want to run? (fisher, cooker, aiofighter, alcher)");
            if (scriptArray[tabNumber] == null) {
                if (a != null) {
                    a = a.toLowerCase();
                    if (a.equalsIgnoreCase("fisher")) {
                    scriptArray[tabNumber] = new AIOFisher();
                    } else if (a.equalsIgnoreCase("aiofighter")) {
                        scriptArray[tabNumber] = new KennehFighter();
                    } else if (a.equalsIgnoreCase("alcher")) {
                        scriptArray[tabNumber] = new Alcher();
                    } else if (a.equalsIgnoreCase("cooker")) {
                        scriptArray[tabNumber] = new Cooker();
                    } else if (a.equalsIgnoreCase("interfaceexplorer")) {
                        scriptArray[tabNumber] = new InterfaceExplorer();
                    } else {
                        System.out.println("custom scripts");
                        try{
                        Class<Script>[] scriptClasses = sm
                                .getClassesFromFolder(); // load custom scripts
                        // from folder
                            if(scriptClasses != null){
                                System.out.println(scriptClasses.length);
                            } else{
                                System.out.println("null");
                            }
                            System.out.println("loaded scripts");
                        for (Class<Script> sc : scriptClasses) {
                            System.out.println("for");
                            if ((sc != null) && sc.isAnnotationPresent(ScriptManifest.class)) {
                                System.out.println("found!");
                                String scriptName = getClassAnnotationValue(sc,
                                        ScriptManifest.class, "name")
                                        .toLowerCase();
                                if (a.contains(scriptName)) {
                                    scriptArray[tabNumber] = sm.getScriptForClass(sc);
                                }
                                }
                                }
                            } catch(Exception ex){
                            ex.printStackTrace();
                        }
                    }
                    if(scriptArray[tabNumber] != null){
                    loaders[tabNumber].setRunningScript(scriptArray[tabNumber]);
                    scriptArray[tabNumber].setLoader(loaders[tabNumber]);
                    // scripts.setScript(script, number);
                    scriptThreads[tabNumber] = new Thread(scriptArray[tabNumber], "scriptthread" + tabNumber);
                    scriptThreads[tabNumber].start();
                }
                }
            }
        }
    }

    public String getClassAnnotationValue(Class<Script> classType,
                                          Class<ScriptManifest> annotationType, String attributeName) {
        String value = null;


        Annotation annotation = classType.getAnnotation(annotationType);

        if (annotation != null) {
            try {
                value = (String) annotation.annotationType()
                        .getMethod(attributeName).invoke(annotation);
            } catch (Exception ignored) {
            }
        }

        return value;
    }

    public String getParameter(String s) {
        return a.get(s);
    }

    public URL getDocumentBase() {
        return getCodeBase();
    }

    public URL getCodeBase() {
        try {
            return new URL("http://www.battle-scape.com");
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public void appletResize(int i, int j) {
    }

    public AppletContext getAppletContext() {
        return null;
    }

    public boolean isActive() {
        return true;
    }

    private static Hashtable<String, String> a;

    static {
        a = new Hashtable<String, String>();
        a.put("nodeid", "1");
        a.put("portoff", "0");
        a.put("lowmem", "0");
        a.put("free", "0");
        a.put("version", "474");
        a.put("worldid", "1");
    }


}
