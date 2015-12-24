package scripts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.*;

import com.vsbot.api.Calculations;
import com.vsbot.api.TilePath;
import com.vsbot.input.Keyboard;
import com.vsbot.input.Mouse;
import com.vsbot.wrappers.RSGroundItem;
import com.vsbot.wrappers.RSItem;
import com.vsbot.wrappers.RSNPC;
import com.vsbot.wrappers.RSTile;

import scripts.Script;
import scripts.ScriptManifest;

/**
 * An all-in-one fighter. Supports attacking,looting,eating,relogging,bone burying and casket opening.
 * @author Kenneh - http://www.rune-server.org/members/kenneh/
 */

@ScriptManifest(authors = { "Kenneh" }, name = "kennehfighter", version = 1, description = "Fights anything")
public class KennehFighter extends Script implements MouseListener {

    public RSTile startTile = null;
    public RSGroundItem loot = null;
    public String foodName = "";
    private int startLvl = 0;
    private int startXp = 0;
    private int xpGained = 0;
    private long startTime = 0;
    public String USER = "z", PASS = "z";

    int strXpS;
    int defXpS;
    int rngXpS;
    int mageXpS;
    int attXpS;
    int strLvlS;
    int defLvlS;
    int rngLvlS;
    int mageLvlS;
    int attLvlS;

    private final Color color1 = new Color(255, 255, 255);
    private final Color color2 = new Color(0, 0, 0);
    private final Color color3 = new Color(0, 0, 0, 145);

    MousePaint mt = new MousePaint();

    private final BasicStroke stroke1 = new BasicStroke(1);

    private final Font font1 = new Font("Arial", 0, 9);

    Rectangle rect = new Rectangle(456, 312, 59, 25);

    ArrayList<String> npcList = new ArrayList<String>();
    ArrayList<String> lootList = new ArrayList<String>();

    AutoFighterGUI i;

    public String[] lootItems() {
        return lootList.toArray(new String[lootList.size()]);
    }

    public String[] mobNames() {
        return npcList.toArray(new String[npcList.size()]);
    }

    public String getSkillName() {
        if(getSkill() == skills.STRENGTH) {
            return "Strength Lvl: ";
        }
        if(getSkill() == skills.DEFENSE) {
            return "Defense Lvl: ";
        }
        if(getSkill() == skills.RANGED) {
            return "Ranged Lvl: ";
        }
        if(getSkill() == skills.MAGIC){
            return "Magic Lvl: ";
        }
        if(getSkill() == skills.ATTACK){
            return "Attack Lvl: ";
        }
        return "NaN";
    }

    public int getSkill() {
        int strXp = skills.getExperience(skills.STRENGTH);
        int defXp = skills.getExperience(skills.DEFENSE);
        int rngXp = skills.getExperience(skills.RANGED);
        int mageXp = skills.getExperience(skills.MAGIC);
        int attXp = skills.getExperience(skills.ATTACK);

        if(strXp > strXpS) {
            startXp = strXpS;
            startLvl = strLvlS;
            return skills.STRENGTH;
        }

        if(defXp > defXpS) {
            startXp = defXpS;
            startLvl = defLvlS;
            return skills.DEFENSE;
        }

        if(rngXp > rngXpS) {
            startXp = rngXpS;
            startLvl = rngLvlS;
            return skills.RANGED;
        }

        if(mageXp > mageXpS) {
            startXp = mageXpS;
            startLvl = mageLvlS;
            return skills.MAGIC;
        }

        if(attXp > attXpS) {
            startXp = attXpS;
            startLvl = attLvlS;
            return skills.ATTACK;
        }

        return 0;
    }

    public enum State {
        Fight, Eat, Loot, Sleep, OpenCasket, Walkback, Bury, Login
    }

    public void doLogin() {
        if (USER != null && PASS != null) {
            mouse.clickMouse(new Point(286, 191), true);
            keyboard.sendKeys(USER);
            sleep(500);
            mouse.clickMouse(new Point(286, 247), true);
            keyboard.sendKeys(PASS);
            sleep(7000);
            mouse.clickMouse(new Point(383, 341), true);
            sleep(2500);
        }
        return;
    }

    public State getStage() {
        if(isLoggedIn() && startTile == null){
            startTile = getMyPlayer().getLocation();
        }
        if(!isLoggedIn() && !USER.equals("z") && !PASS.equals("z")) {
            return State.Login;
        }
        if(Calculations.distanceBetween(getMyPlayer().getLocation(), startTile) > 15) {
            return State.Walkback;
        }
        if(inventory.containsItem("Casket")) {
            return State.OpenCasket;
        }
        if(inventory.containsItem("Bones") || inventory.containsItem("Big bones") && inventory.isFull()) {
            return State.Bury;
        }
        if(getMyPlayer().getHpPercent() <= 40) {
            return State.Eat;
        }
        for(String i : lootItems()) {
            loot = grounditems.getNearest(i);
            if(loot != null) {
                if(calc.distanceTo(loot.getLocation()) <= 15) {
                    return State.Loot;
                }
            }
        }
        if(getMyPlayer().getAnimation() == -1) {
            if(!getMyPlayer().isInCombat()) {
                if(getMyPlayer().getHpPercent() >= 40) {
                    return State.Fight;
                }
            }
        }
        return State.Sleep;
    }

    @Override
    public int loop() {
        switch(getStage()) {
            case Login:
                doLogin();
                break;
            case Fight:
                RSNPC mob = null;
                for(String i : mobNames()) {
                    mob = getNearestNpc(i);
                }
                if(mob != null) {
                    if(!mob.isInCombat()) {
                        if(mob.isOnScreen()) {
                            mob.interact("Attack");
                            return 500;
                        } else {
                            camera.turnTo(mob);
                            return 500;
                        }
                    }
                }
                break;
            case Eat:
                if(inventory.containsItem(foodName)) {
                    RSItem food = inventory.getItem(foodName);
                    if(food != null) {
                        food.interact("Eat");
                        return 500;
                    }
                } else {
                    System.out.println("No food, stopping script!");
                    stop();
                }
                break;
            case Walkback:
                TilePath a = createTilePath(startTile);
                a.traverse();
                break;
            case Loot:
                if(!inventory.isFull()) {
                    TilePath i = createTilePath(loot.getLocation());
                    if(Calculations.distanceBetween(getMyPlayer().getLocation(), loot.getLocation()) > 5) {
                        i.traverse();
                    }
                    loot.interact("Take");
                    return 500;
                } else {
                    if(inventory.containsItem(foodName)) {
                        RSItem food = inventory.getItem(foodName);
                        if(food != null) {
                            food.interact("Eat");
                            return 500;
                        }
                    }
                }
                break;
            case OpenCasket:
                RSItem i = inventory.getItem("Casket");
                i.interact("Open");
                break;
            case Bury:
                RSItem[] i2 = inventory.getItems();
                for(RSItem i3 : i2) {
                    if(i3.getName().toLowerCase().contains("bones")) {
                        i3.interact("Bury");
                    }
                }
            case Sleep:
                return 1000;
        }
        return 0;
    }

    @Override
    public void onBegin(){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (InstantiationException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IllegalAccessException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (UnsupportedLookAndFeelException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                i = new AutoFighterGUI();
                i.setVisible(true);
            }
        });
        while(i != null){
            sleep(500);
        }

        startTime = System.currentTimeMillis();

        strXpS = skills.getExperience(skills.STRENGTH);
        defXpS = skills.getExperience(skills.DEFENSE);
        rngXpS = skills.getExperience(skills.RANGED);
        mageXpS = skills.getExperience(skills.MAGIC);
        attXpS = skills.getExperience(skills.ATTACK);

        strLvlS = skills.getLevel(skills.STRENGTH);
        defLvlS = skills.getLevel(skills.DEFENSE);
        rngLvlS = skills.getLevel(skills.RANGED);
        mageLvlS = skills.getLevel(skills.MAGIC);
        attLvlS = skills.getLevel(skills.ATTACK);

    }

    @Override
    public void onFinish() {

    }

    public String runtime(long start) {
        long millis = System.currentTimeMillis() - start;
        long hours = millis / (1000 * 60 * 60);
        millis -= hours * (1000 * 60 * 60);
        long minutes = millis / (1000 * 60);
        millis -= minutes * (1000 * 60);
        long  seconds = millis / 1000;
        return "Time Running: "+ hours + ":" + minutes + ":" + seconds;
    }

    public String formatNumber(int start) {
        DecimalFormat nf = new DecimalFormat("0.0");
        double i = start;
        if(i >= 1000000) {
            return nf.format((i / 1000000)) + "m";
        }
        if(i >=  1000) {
            return nf.format((i / 1000)) + "k";
        }
        return ""+start;
    }

    public String perHour(int gained) {
        return formatNumber((int) ((gained) * 3600000D / (System.currentTimeMillis() - startTime)));
    }

    public void drawCharInfo(Graphics g, int y, Graphics2D g1) {
        if(getMyPlayer() != null) {
            g.setColor(Color.WHITE);
            g.drawString("AIOFighter by Kenneh", 7, y);
            y = y + 15;
            g.drawString(runtime(startTime), 7, y);
            y = y + 15;
            g.drawString("State: " + getStage().toString(), 7, y);
            y = y + 15;
            g.drawString(getSkillName() + skills.getLevel(getSkill())+"(+"+(skills.getLevel(getSkill()) - startLvl) + ")",7 , y);
            y = y + 15;
            g.drawString("Exp /hr: " + perHour(xpGained) + "(+" + formatNumber(xpGained) + ")", 7, y);
            y = y + 15;
            y = y + 15;
            for(String i : mobNames()) {
                g.drawString("Fighting: " + i, 7, y);
                y = y + 15;
            }
            y = y + 15;
            for(String i : lootItems()) {
                g.drawString("Looting: " + i, 7, y);
                y = y + 15;
            }
            g.setColor(color3);
            g.fillRect(3, 35, 149, y - 30);
            g.setColor(color2);
            g1.setStroke(stroke1);
            g.drawRect(3, 35, 149, y - 30);
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g1 = (Graphics2D)g;
        g1.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g1.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        xpGained = skills.getExperience(getSkill()) - startXp;
        drawCharInfo(g, 65, g1);
        g.setColor(color1);
        g.fillRect(456, 312, 59, 25);
        g.setColor(color2);
        g1.setStroke(stroke1);
        g.drawRect(456, 312, 59, 25);
        g.setFont(font1);
        g.drawString("Add loot", 468, 330);
        mt.add(mouse.getLocation());
        mt.drawCursor(g);
        mt.drawTrail(g);
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
        Point p = arg0.getPoint();
        if(rect.contains(p)) {
            String loot1 = JOptionPane.showInputDialog(null, "Enter the items you want to loot seperated by a comma");
            String[] loot2 = loot1.split(",");
            for(String loot3:loot2) {
                lootList.add(loot3.trim());
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {

    }

    @Override
    public void mouseExited(MouseEvent arg0) {

    }

    @Override
    public void mousePressed(MouseEvent arg0) {

    }

    @Override
    public void mouseReleased(MouseEvent arg0) {

    }

    public class AutoFighterGUI extends JPanel {
        /**
         *
         */
        private static final long serialVersionUID = 3849200350948539597L;

        public AutoFighterGUI() {
            initComponents();
        }

        @SuppressWarnings("deprecation")
        private void button1ActionPerformed(ActionEvent e) {
            foodName = comboBox1.getSelectedItem().toString();

            String loot1 = textArea2.getText();
            String[] loot2 = loot1.split(",");
            for(String loot3 : loot2) {
                lootList.add(loot3.trim());
            }

            String npc1 = textArea1.getText();
            String[] npc2 = npc1.split(",");
            for(String npc3 : npc2) {
                npcList.add(npc3.trim());
            }

            USER = textField2.getText();
            PASS = passwordField1.getText();

            frame1.dispose();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private void initComponents() {
            label1 = new JLabel();
            label3 = new JLabel();
            textField2 = new JTextField();
            label4 = new JLabel();
            passwordField1 = new JPasswordField();
            button1 = new JButton();
            label5 = new JLabel();
            scrollPane1 = new JScrollPane();
            textArea1 = new JEditorPane();
            label6 = new JLabel();
            scrollPane2 = new JScrollPane();
            textArea2 = new JEditorPane();
            label7 = new JLabel();
            comboBox1 = new JComboBox();
            frame1 = new JFrame();

            //======== this ========

            // JFormDesigner evaluation mark
            setBorder(new javax.swing.border.CompoundBorder(
                    new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
                            "", javax.swing.border.TitledBorder.CENTER,
                            javax.swing.border.TitledBorder.BOTTOM, new Font("Dialog", Font.BOLD, 12),
                            Color.red), getBorder())); addPropertyChangeListener(new java.beans.PropertyChangeListener(){public void propertyChange(java.beans.PropertyChangeEvent e){if("border".equals(e.getPropertyName()))throw new RuntimeException();}});

            setLayout(null);

            //---- label1 ----
            label1.setText("Kennehs Autofighter");
            label1.setVerticalAlignment(SwingConstants.TOP);
            label1.setFont(new Font("Calibri", Font.PLAIN, 22));
            label1.setHorizontalAlignment(SwingConstants.CENTER);
            add(label1);
            label1.setBounds(45, 5, 310, 40);

            //---- label3 ----
            label3.setText("Username:");
            add(label3);
            label3.setBounds(new Rectangle(new Point(10, 50), label3.getPreferredSize()));
            add(textField2);
            textField2.setBounds(75, 45, 125, textField2.getPreferredSize().height);

            //---- label4 ----
            label4.setText("Password:");
            add(label4);
            label4.setBounds(new Rectangle(new Point(205, 50), label4.getPreferredSize()));
            add(passwordField1);
            passwordField1.setBounds(265, 45, 130, passwordField1.getPreferredSize().height);

            //---- button1 ----
            button1.setText("Start Script");
            button1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    button1ActionPerformed(e);
                }
            });
            add(button1);
            button1.setBounds(65, 265, 275, button1.getPreferredSize().height);

            //---- label5 ----
            label5.setText("Enter the monster names:");
            add(label5);
            label5.setBounds(new Rectangle(new Point(10, 85), label5.getPreferredSize()));

            //======== scrollPane1 ========
            {
                scrollPane1.setViewportView(textArea1);
            }
            add(scrollPane1);
            scrollPane1.setBounds(10, 105, 145, 90);

            //---- label6 ----
            label6.setText("Enter the item names:");
            add(label6);
            label6.setBounds(new Rectangle(new Point(240, 85), label6.getPreferredSize()));

            //======== scrollPane2 ========
            {
                scrollPane2.setViewportView(textArea2);
            }
            add(scrollPane2);
            scrollPane2.setBounds(240, 105, 145, 90);

            //---- label7 ----
            label7.setText("Food name:");
            add(label7);
            label7.setBounds(new Rectangle(new Point(165, 200), label7.getPreferredSize()));
            add(comboBox1);
            comboBox1.setBounds(135, 220, 130, comboBox1.getPreferredSize().height);
            comboBox1.setModel(new DefaultComboBoxModel(new String[] {
                    "Swordfish", "Lobster", "Manta", "Shark", "Monkfish"
            }));

            { // compute preferred size
                Dimension preferredSize = new Dimension();
                for(int i = 0; i < getComponentCount(); i++) {
                    Rectangle bounds = getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                setMinimumSize(preferredSize);
                setPreferredSize(preferredSize);
            }
            // JFormDesigner - End of component initialization  //GEN-END:initComponents

            frame1.add(this);
            frame1.setResizable(false);
            frame1.setSize(410, 325);
            frame1.setVisible(true);
        }

        // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
        // Generated using JFormDesigner Evaluation license - kenneth lacombe
        private JLabel label1;
        private JLabel label3;
        private JTextField textField2;
        private JLabel label4;
        private JPasswordField passwordField1;
        private JButton button1;
        private JLabel label5;
        private JScrollPane scrollPane1;
        private JEditorPane textArea1;
        private JLabel label6;
        private JScrollPane scrollPane2;
        private JEditorPane textArea2;
        private JLabel label7;
        private JComboBox comboBox1;
        private JFrame frame1;
        // JFormDesigner - End of variables declaration  //GEN-END:variables
    }

    private class MousePaint {
        /**
         *  Length of the trail
         */
        private final int SIZE = 50;
        /**
         * Gets the color of the trail depending on the SIZE
         */
        private final float rainbowStep = (float) (1.0/SIZE);
        /**
         * Gets the alpha of the trail depending on the SIZE
         */
        private final double alphaStep = (255.0/SIZE);

        /**
         * Declares the mouse points
         */
        private Point[] points;
        /**
         * Counts up the points
         */
        private int index;
        /**
         * Trail offset
         */
        private float offSet = 0.05f;
        /**
         * Trail start
         */
        private float start = 0;

        /**
         * Construter for MousePaint()
         */
        public MousePaint() {
            points = new Point[SIZE];
            index = 0;
        }

        /**
         * Adds the current mouse location as a point to draw the trail
         * @param p MouseLocation()
         */
        public void add(Point p) {
            points[index++] = p;
            index %= SIZE;
        }

        /**
         * Draws the cursor on the screen
         * @param graphics
         */
        public void drawCursor(Graphics graphics) {
            int x = (int)mouse.getLocation().getX();
            int y = (int)mouse.getLocation().getY();
            Graphics2D g2D = (Graphics2D) graphics;
            graphics.setColor(new Color((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256)));
            Graphics2D spinner = (Graphics2D) g2D.create();
            spinner.rotate(System.currentTimeMillis() % 2000d / 2000d * (360d) * 2 * Math.PI / 180.0, x, y);
            spinner.drawLine(x - 6, y, x + 6, y);
            spinner.drawLine(x, y - 6, x, y +6);
        }

        /**
         * Draws the trail on screen
         * @param graphics
         */
        public void drawTrail(Graphics graphics) {
            Graphics2D g2D = (Graphics2D) graphics;
            g2D.setStroke(new BasicStroke(1F));
            double alpha = 0;
            float rainbow = start;

            start += offSet;
            if (start > 1) {
                start -= 1;
            }

            for (int i = index; i != (index == 0 ? SIZE-1 : index-1); i = (i+1)%SIZE) {
                if (points[i] != null && points[(i+1)%SIZE] != null) {
                    int rgb = Color.HSBtoRGB(rainbow, 0.9f, 0.9f);
                    rainbow += rainbowStep;

                    if (rainbow > 1) {
                        rainbow -= 1;
                    }
                    g2D.setColor(new Color((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff, (int)alpha));
                    g2D.drawLine(points[i].x, points[i].y, points[(i+1)%SIZE].x, points[(i+1)%SIZE].y);


                    alpha += alphaStep;
                }
            }
        }
    }

}