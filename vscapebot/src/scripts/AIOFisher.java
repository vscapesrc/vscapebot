package scripts;

import javax.swing.*;

import com.vsbot.api.TilePath;
import com.vsbot.wrappers.RSNPC;
import com.vsbot.wrappers.RSObject;
import com.vsbot.wrappers.RSTile;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;


public class AIOFisher extends Script {

    public String state;

    public Image x;


    /*
      * static TilePath toLobsters = new TilePath(new RSTile(2588, 3420), new
      * RSTile(2594, 3415), new RSTile(2598, 3409), new RSTile(2611, 3411));
      */
    TilePath toLobsters = createTilePath(new RSTile(2587, 3419),
            new RSTile(2595, 3414), new RSTile(2601, 3405), new RSTile(2611,
            3411));

    TilePath lobstersToBank = toLobsters.reverse();

    TilePath toManta = createTilePath(new RSTile(2587, 3419), new RSTile(
            2595, 3420), new RSTile(2603, 3425));

    TilePath mantaToBank = toManta.reverse();

    TilePath shrimpToBank = createTilePath(new RSTile(2603, 3411),
            new RSTile(2595, 3412), new RSTile(2587, 3419));

    TilePath toShrimp = shrimpToBank.reverse();

    int startLevel, startExp;

    private long startTime;

    String fishOption;

    Fish weFish;

    String fish;

    private static final Font font = new Font("Arial", 0, 12);

    boolean guiOn = true;

    boolean canPaint;

    GUI g;

    boolean isHidden = false;


    @Override
    public int loop() {
        RSNPC fish = getNearestNpc(weFish.getId());
        if (!inventory.isFull()) {
            if (fish != null && fish.isOnScreen()) {
                state = "Fishing";
                if (getMyPlayer().getInteracting() == null
                        || getMyPlayer().getAnimation() == -1) {
                    fish.interact(weFish.getAction());
                    return 1000;

                }
            } else {
                state = "Walking to fish";
                weFish.getToFish().traverse();
                return 300;
            }
        } else {
            if (banking.isOpen() && inventory.getCount() > 1) {
                state = "Banking";
                banking.depositAllExcept(weFish.getItemName());
                return 600;
            }
            RSObject bank = objects.getNearest(2213);
            if (bank != null && !banking.isOpen()
                    && calc.distanceTo(bank.getLocation()) < 10) {
                if (bank.isOnScreen()) {
                    if (!banking.isOpen()) {
                        state = "Open bank";
                        bank.interact("Quick");
                    }
                    return 600;
                } else {
                    state = "Turning camera to bank";
                    camera.turnTo(bank);
                    return 600;
                }
            } else {
                state = "Walking to bank";
                weFish.getToBank().traverse();
                return 1000;
            }
        }
        return 10;
    }


    public void onBegin() {
        ArrayList<Fish> fishes = new ArrayList<Fish>();
        fishes.add(new Fish(196, "Harpoon", "Harpoon", 2800, mantaToBank, toManta)); //manta
        fishes.add(new Fish(191, "Lobster pot", "Cage", 1800, lobstersToBank, toLobsters));
        fishes.add(new Fish(186, "Small fishing net", "Net", 200, shrimpToBank, toShrimp));
        guiOn = true;
        canPaint = false;
        g = new GUI();
        g.setVisible(true);
        while (guiOn) {
            sleep(500);
        }

        if (fishOption != null) {
            if (fishOption.equals("Shrimp")) {
                weFish = fishes.get(2);
            } else if (fishOption.equals("Lobster")) {
                weFish = fishes.get(1);
            } else if (fishOption.equals("Manta ray")) {
                weFish = fishes.get(0);
            }
            fish = weFish.toString();
            System.out.println("Starting up");
            if (!inventory.containsItem(weFish.getItemName())) {
                System.out.println("no fishing item " + weFish.getItemName()
                        + " found in inv, please get one before starting");
                stop();
            }
        }
        startLevel = skills.getLevel(skills.FISHING);
        startTime = System.currentTimeMillis();
        startExp = skills.getExperience(skills.FISHING);
        guiOn = false;
        System.out.println(weFish.getExpCount());
        canPaint = true;
    }

    @Override
    public void paint(Graphics g) {
        if (canPaint && !isHidden) {
            long millis = System.currentTimeMillis() - startTime;
            long hours = millis / (1000 * 60 * 60);
            millis -= hours * 1000 * 60 * 60;
            long minutes = millis / (1000 * 60);
            millis -= minutes * 1000 * 60;
            long seconds = millis / 1000;
            int curLevel = skills.getLevel(skills.FISHING);
            g.setFont(font);
            g.setColor(Color.BLACK);
            g.fillRect(3, 344, 503, 112);
            g.setColor(Color.GREEN);
            g.drawString("Time run: " + hours + " : " + minutes + " : "
                    + seconds, 8, 370);
            g.drawString("our level in fishing: " + curLevel, 8, 400);
            g.drawString("levels gained: " + (curLevel - startLevel), 8, 420);
            g.drawString("We are fishing: " + fish, 8, 440);
            int expGained = skills.getExperience(skills.FISHING) - startExp;
            g.drawString("Fishing experience gained: " + expGained, 150, 370);
            int fished = expGained / weFish.getExpCount();
            g.drawString(weFish.toString() + " fished: " + fished, 150, 400);
            g.drawString("State: " + state + " made by kubko", 150, 440);
        }

    }

    @Override
    public void onFinish() {
    }


    public class GUI extends JFrame {
        public GUI() {
            initComponents();
        }

        private void initComponents() {
            // JFormDesigner - Component initialization - DO NOT MODIFY
            // //GEN-BEGIN:initComponents
            // Generated using JFormDesigner Evaluation license - Kubko Coder
            label1 = new JLabel();
            comboBox1 = new JComboBox();
            label2 = new JLabel();
            comboBox2 = new JComboBox();
            label3 = new JLabel();
            button1 = new JButton();
            label4 = new JLabel();
            label5 = new JLabel();

            // ======== this ========
            setTitle("AIO Fisher GUI");
            Container contentPane = getContentPane();
            contentPane.setLayout(null);

            // ---- label1 ----
            label1.setText("What to fish");
            contentPane.add(label1);
            label1.setBounds(5, 50, 135, 20);

            // ---- comboBox1 ----
            comboBox1.setModel(new DefaultComboBoxModel(new String[]{
                    "Shrimp", "Lobster", "Manta ray"}));
            contentPane.add(comboBox1);
            comboBox1.setBounds(5, 75, 205, 25);

            // ---- label2 ----
            label2.setText("What 2 do with fish");
            contentPane.add(label2);
            label2.setBounds(260, 45, 105, 20);

            // ---- comboBox2 ----
            comboBox2.setModel(new DefaultComboBoxModel(new String[]{"Drop",
                    "Bank"}));
            contentPane.add(comboBox2);
            comboBox2.setBounds(new Rectangle(new Point(260, 70), comboBox2
                    .getPreferredSize()));

            // ---- label3 ----
            label3.setText("AIO Fisher v");
            label3.setFont(label3.getFont().deriveFont(
                    label3.getFont().getSize() + 15f));
            contentPane.add(label3);
            label3.setBounds(15, 0, 270, 35);

            // ---- button1 ----
            button1.setText("Start!");
            button1.setFont(button1.getFont().deriveFont(
                    button1.getFont().getSize() + 6f));
            contentPane.add(button1);
            button1.setBounds(30, 185, 290, 55);

            // ---- label4 ----
            label4.setText("Requirements:");
            contentPane.add(label4);
            label4.setBounds(110, 100, 150, 25);

            // ---- label5 ----
            label5.setText("1 fishing, small fishing net");
            contentPane.add(label5);
            label5.setBounds(110, 120, 200, 25);

            { // compute preferred size
                Dimension preferredSize = new Dimension();
                for (int i = 0; i < contentPane.getComponentCount(); i++) {
                    Rectangle bounds = contentPane.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width,
                            preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height,
                            preferredSize.height);
                }
                Insets insets = contentPane.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                contentPane.setMinimumSize(preferredSize);
                contentPane.setPreferredSize(preferredSize);
            }
            button1.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String action = e.getActionCommand();
                    Object toFish = comboBox1.getSelectedItem();
                    fishOption = (String) toFish;
                    guiOn = false;
                    doDispose();

                }

            });
            comboBox1.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getItem().toString().equals("Shrimp")) {
                        System.out.println("shrimp");
                        label5.setText("1 fishing, small fishing net");
                    } else if (e.getItem().toString().equals("Lobster")) {
                        label5.setText("40 fishing, lobster pot");
                    } else if (e.getItem().toString().equals("Manta ray")) {
                        label5.setText("81 fishing, harpoon");
                    }
                }


            });
            pack();
            setLocationRelativeTo(getOwner());
            // JFormDesigner - End of component initialization
            // //GEN-END:initComponents
        }

        public void doDispose() {
            setVisible(false);
            super.dispose();
            guiOn = false;
        }

        // JFormDesigner - Variables declaration - DO NOT MODIFY
        // //GEN-BEGIN:variables
        // Generated using JFormDesigner Evaluation license - Wild Kubko
        private JLabel label1;
        private JComboBox comboBox1;
        private JLabel label2;
        private JComboBox comboBox2;
        private JLabel label3;
        private JButton button1;
        private JLabel label4;
        private JLabel label5;
        // JFormDesigner - End of variables declaration //GEN-END:variables
    }

    private class Fish {


        int id;
        String action;

        String fishItemName;

        TilePath toBank;

        TilePath toFish;

        int exp;

        public Fish(int npcId, String itemName, String fishAction,
                    int expCount, TilePath toBank, TilePath toFish) {
            this.exp = expCount;
            this.id = npcId;
            this.action = fishAction;
            this.fishItemName = itemName;
            this.toBank = toBank;
            this.toFish = toFish;
        }

        public String getItemName() {
            return fishItemName;
        }

        public int getExpCount() {
            return exp;
        }

        public int getId() {
            return id;
        }

        public String getAction() {
            return action;
        }

        public TilePath getToBank() {
            return toBank;
        }

        public TilePath getToFish() {
            return toFish;
        }
    }

}
