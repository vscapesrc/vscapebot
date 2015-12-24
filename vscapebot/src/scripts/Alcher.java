package scripts;

import javax.swing.*;
import java.awt.*;

public class Alcher extends Script{
	
	String alchMode;
	
	Point lowAlch = new Point(706, 258);
	Point highAlch = new Point(705, 332);

	@Override
	public void paint(Graphics g) {
		
		
	}

	@Override
	public int loop() {
		if(alchMode.equals("low")){
			if(tabs.getTab() != tabs.MAGE){
				tabs.switchTab(tabs.MAGE);
				return 500;
			}
				mouse.clickMouse(lowAlch, true);
				sleep(1000);
				mouse.clickMouse(lowAlch, true);
				return 1000;
		}else{
			if(tabs.getTab() != tabs.MAGE){
				tabs.switchTab(tabs.MAGE);
				return 500;
			}
				mouse.clickMouse(highAlch, true);
				sleep(1000);
				mouse.clickMouse(highAlch, true);
				return 1000;
			}
		}

	@Override
	public void onBegin() {
		alchMode = JOptionPane
				.showInputDialog(null,
						"high or low alch?(high low)");
		
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}
	
	

}
