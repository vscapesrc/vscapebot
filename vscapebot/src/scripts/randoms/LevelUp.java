package scripts.randoms;

import java.awt.*;

import com.vsbot.hooks.GameInterface;
import com.vsbot.input.Mouse;
import com.vsbot.wrappers.RSInterface;

public class LevelUp extends Random {

	Mouse n = script.mouse;

	@Override
	public
	int loop() {
							n.moveMouse(new Point(273, 426));
							script.sleep(500);
							n.clickMouse(new Point(273, 426), true);
		return 0;
	}

	@Override
	public
	boolean startUp() {
		RSInterface[] parents = script.interfaces.getAllParents();
		for (RSInterface single : parents) {
			if (single != null) {
				for (GameInterface child : single.getParentInterface()) {
					if (child != null) {
						if (child.getText() != null
								&& child.getText().contains(
										"Click here to continue")) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

}
