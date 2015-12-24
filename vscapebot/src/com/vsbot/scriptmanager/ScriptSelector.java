package com.vsbot.scriptmanager;

import scripts.Script;
import scripts.ScriptManifest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.annotation.Annotation;
import java.util.ArrayList;

public class ScriptSelector extends JFrame {

    private JTable jt;

    private ScriptManager sm;

    private String data[][];
    private String fields[] = {"Script name", "Version", "Author"};

    private ArrayList<String[]> list = new ArrayList<String[]>();
    private ArrayList<Script> scripts = new ArrayList<Script>();

    private Script[] allScripts;

    public String[][] getData() {
        list.add(new String[]{"AIOFighter", "1", "Kubko"}); // add default
        // scripts
        list.add(new String[]{"AIOFisher", "1", "Kubko"});
        list.add(new String[]{"AIOCooker", "1", "Kubko"});
        Class<Script>[] scriptClasses = sm.getClassesFromFolder(); // load
        // custom
        // scripts
        // from
        // folder
        for (Class<Script> sc : scriptClasses) {
            if (sc.isAnnotationPresent(ScriptManifest.class)) {
                String scriptName = (String) getClassAnnotationValue(sc,
                        ScriptManifest.class, "name");
                Double vs = (Double) getClassAnnotationValue(sc,
                        ScriptManifest.class, "version");
                String[] authors = (String[]) getClassAnnotationValue(sc,
                        ScriptManifest.class, "authors");
                String author = "";
                for (String s : authors) {
                    if (author.equals("")) {
                        author = s;
                    } else {
                        author = author + "," + s;
                    }
                }
                list.add(new String[]{scriptName, Double.toString(vs), author});
                scripts.add(sm.getScriptForClass(sc));
            }
        }
        allScripts = new Script[scripts.size()];
        for (int i = 0; i < scripts.size(); i++) {
            if (scripts.get(i) != null) {
                allScripts[i] = scripts.get(i);
            }
        }
        return list.toArray(new String[list.size()][]);
    }

    public Object getClassAnnotationValue(Class classType,
                                          Class annotationType, String attributeName) {
        Object value = null;

        Annotation annotation = classType.getAnnotation(annotationType);
        if (annotation != null) {
            try {
                value = annotation.annotationType().getMethod(attributeName)
                        .invoke(annotation);
            } catch (Exception ex) {
            }
        }

        return value;
    }


    public ScriptSelector(String title, ScriptManager manager) {
        super(title);
        this.sm = manager;
        setSize(150, 150);
        data = getData();
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                dispose();
            }
        });
        init();
        pack();

        setVisible(true);
    }

    private void init() {
        DefaultTableModel model = new DefaultTableModel(data, fields);
        jt = new JTable(model) {
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false; //Disallow the editing of any cell
            }
        };
        JScrollPane pane = new JScrollPane(jt);

        getContentPane().add(pane);
        JTableHeader header = jt.getTableHeader();
        JButton start = new JButton("Start script");
        start.setBounds(header.getX(), header.getY(), 30, 40);
        header.setLayout(new BorderLayout());
        header.add(start);

    }
}
