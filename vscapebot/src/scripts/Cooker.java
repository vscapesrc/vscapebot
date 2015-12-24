package scripts;

import scripts.Script;

import javax.swing.*;

import com.vsbot.api.Calculations;
import com.vsbot.api.Objects;
import com.vsbot.api.TilePath;
import com.vsbot.wrappers.RSBankItem;
import com.vsbot.wrappers.RSItem;
import com.vsbot.wrappers.RSObject;
import com.vsbot.wrappers.RSTile;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Cooker extends Script {

	TilePath toCook = createTilePath(new RSTile(2586, 3419),new RSTile(2595, 3413), new RSTile(2599, 3406), new RSTile(2607,
			3401), new RSTile(2615, 3397));

	boolean guiOn;

	String fishOption;
	
	int startLevel, startExp;

	private long startTime;
	
	int foodCheck;
	
	private static final Font font = new Font("Arial", 0, 12);

	GUI g;
	

	enum Food {

		SHRIMP("Raw shrimps", 600, "Shrimps"), LOBSTER("Raw lobster", 2000, "Lobster"), MANTA("Raw manta ray", 5000, "Manta ray");

		private String name;
		
		private int expCount;
		
		private String cookedName;

		Food(String itemName, int exp, String cooked) {
			this.expCount = exp;
			this.name = itemName;
			this.cookedName = cooked;
		}
		
		public String getCookedName(){
			return cookedName;
		}
		
		public int getExpCount(){
			return expCount;
		}

		public String getName() {
			return name;
		}
	};
	
	boolean canPaint;


	Food ourFood;
	
	String state;

	enum States {
		STATE_STOP, STATE_BANK, STATE_WALK, STATE_SLEEP, STATE_WALK_TO_BANK, STATE_COOK, STATE_TURN_CAMERA_TO_SHOVE, STATE_OPEN_BANK, STATE_TURN_CAMERA_TO_BANK, STATE_UNKNOWN
	};

	public States getState() {
		if(foodCheck >= 100){
			return States.STATE_STOP;
		}
		if (!inventory.containsItem(ourFood.getName())) {
			RSObject bank = objects.getNearest(2213);
			if (bank != null
					&& calc.distanceTo(bank.getLocation()) < 10) {
				if (bank.isOnScreen()) {
					if (!banking.isOpen()) {
						return States.STATE_OPEN_BANK;
					}else{
						return States.STATE_BANK;
					}
				} else {
					return States.STATE_TURN_CAMERA_TO_BANK;
				}
			} else {
				return States.STATE_WALK_TO_BANK;
			}
		} else {
			RSObject shove = objects.getNearestByName("Range");
			if (shove != null
					&& calc.distanceTo(shove.getLocation()) < 10) {
				if (shove.isOnScreen()) {
					if (getMyPlayer().getAnimation() == -1) {
						return States.STATE_COOK;
					} else {
						return States.STATE_SLEEP;
					}
				} else {
					return States.STATE_TURN_CAMERA_TO_SHOVE;
				}
			}else{
				return States.STATE_WALK;
		}
		}

	}

	@Override
	public void paint(Graphics g) {
		if(canPaint){
			String fish = ourFood.getName();
			long millis = System.currentTimeMillis() - startTime;
			long totalseconds = millis / 1000;
			long hours = millis / (1000 * 60 * 60);
			millis -= hours * 1000 * 60 * 60;
			long minutes = millis / (1000 * 60);
			millis -= minutes * 1000 * 60;
			long seconds = millis / 1000;
			int curLevel = skills.getLevel(skills.COOKING);
			g.setFont(font);
			g.setColor(Color.BLACK);
			g.fillRect(3, 344, 503, 112);
			g.setColor(Color.GREEN);
			g.drawString("Time run: " + hours + " : " + minutes + " : "
					+ seconds, 8, 370);
			g.drawString("our level in cooking: " + curLevel
					, 8, 400);
			g.drawString("levels gained: " + (curLevel - startLevel), 8, 420);
			g.drawString("We are cooking: " + fish, 8, 440);
			int expGained = skills.getExperience(skills.COOKING) - startExp;
			g.drawString("Cooking experience gained: " + expGained, 250, 370);
			int fished = expGained / ourFood.getExpCount();
			g.drawString(ourFood.toString() + " cooked: " + fished, 250,
					400);
			g.drawString("State: " + state + " made by kubko", 250, 440);
		}
	}



	@Override
	public int loop() {
		RSObject bank = objects.getNearest(2213);
		RSObject shove = objects.getNearestByName("Range");
		switch (getState()) {
		case STATE_STOP:
			stop();
			return 300;
		case STATE_BANK:
			for(RSItem i : inventory.getItems()){
			if(!i.getName().equals(ourFood.getName()) && i.getId() >= 1){
				state = "Depositing cooked food";
				banking.depositAllExcept(ourFood.getName());
				return 300;
			}
			}
			state = "Withdrawing uncooked food";
			System.out.println(ourFood.getName());
			RSBankItem item = banking.getItem(ourFood.getName());
			if(item != null){
			foodCheck = 0;
			item.interact("Withdraw all");
			System.out.println("withdraw all");
			}else{
				foodCheck++;
				System.out.println("out of food, stopping");
				return 500;
			}
			return 500;
		case STATE_OPEN_BANK:
			state = "Opening bank";
			bank.interact("Use-quickly");
			return 600;
		case STATE_TURN_CAMERA_TO_BANK:
			state = "Turning camera to bank";
			camera.turnTo(bank);
			return 300;
		case STATE_TURN_CAMERA_TO_SHOVE:
			state = "Turning camera to shove";
			camera.turnTo(shove);
			return 300;
		case STATE_WALK_TO_BANK:
			state = "Walkign to bank";
			toCook.reverse().traverse();
			return 500;
		case STATE_WALK:
			state = "Walking to shove";
			toCook.traverse();
			return 500;
		case STATE_SLEEP:
			state = "Waiting";
			return 1500;
		case STATE_COOK:
			state = "Cooking";
			RSItem cookItem = inventory.getItem(ourFood.getName());
			cookItem.interact("Use");
			shove.interact("Use " + ourFood.getName() + " with Range");
			return 600;
		case STATE_UNKNOWN:
			return 500;

		}
		return 300;
		// TODO Auto-generated method stub
	}

	@Override
	public void onBegin() {
		guiOn = true;
		g = new GUI();
		while (guiOn) {
			sleep(500);
		}

		if (fishOption.equals("Shrimp")) {
			ourFood = Food.SHRIMP;
		}else if(fishOption.equals("Manta")){
			ourFood = Food.MANTA;
		}else if(fishOption.equals("Lobster")){
			ourFood = Food.LOBSTER;
		}
		startLevel = skills.getLevel(skills.COOKING);
		startTime = System.currentTimeMillis();
		startExp = skills.getExperience(skills.COOKING);
		canPaint = true;
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub

	}

	public class GUI extends JFrame {
		public GUI() {
			initComponents();
		}

		private void initComponents() {
			super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			// JFormDesigner - Component initialization - DO NOT MODIFY
			// //GEN-BEGIN:initComponents
			// Generated using JFormDesigner Evaluation license - Wild Kubko
			label1 = new JLabel();
			comboBox1 = new JComboBox();
			label2 = new JLabel();
			comboBox2 = new JComboBox();
			label3 = new JLabel();
			button1 = new JButton();

			// ======== this ========
			setTitle("AIO Cooker GUI");
			Container contentPane = getContentPane();
			contentPane.setLayout(null);

			// ---- label1 ----
			label1.setText("What to Cook");
			contentPane.add(label1);
			label1.setBounds(5, 50, 135, 20);

			// ---- comboBox1 ----
			comboBox1.setModel(new DefaultComboBoxModel(new String[] {
					"Shrimp", "Lobster", "Manta ray" }));
			contentPane.add(comboBox1);
			comboBox1.setBounds(5, 75, 100, 25);

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
			pack();
			setLocationRelativeTo(getOwner());
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
			setVisible(true);
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
		// JFormDesigner - End of variables declaration //GEN-END:variables
	}

}
