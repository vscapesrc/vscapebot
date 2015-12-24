package scripts.randoms;

import javax.swing.*;

import com.vsbot.wrappers.RSPlayer;

import java.awt.*;

public class AntiMod extends Random {
	Point loginPoint1 = new Point(633, 485);
	Point loginPoint2 = new Point(623, 372);

	@Override
	public boolean startUp() {
		if (script.isLoggedIn()) {
			try {
				for (RSPlayer p : script.getAllPlayers()) {
					if (p != null && p.getName() != null) {
						if (p.getName().equals("Go Hard")
								|| p.getName().equals("U Got 0wned") || p.getName().contains("D34d Pk3r") || p.getName().equalsIgnoreCase("Pumbata")) {
							return true;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {

		JOptionPane.showMessageDialog(null, "We have logged out because of a mod being in fishing guild. This is to prevent ban.");
			script.stop();
			script.setRunning(false);
		}
		return false;

	}

	@Override
	public int loop() {
		System.out.println("loop");
		if (script.tabs.getTab() != 10) {
			script.mouse.clickMouse(loginPoint1, true);
			script.sleep(300);
		}
		if (script.tabs.getTab() == 10) {
			script.mouse.clickMouse(loginPoint2, true);
			script.sleep(300);
		}
		return 0;
	}

}
