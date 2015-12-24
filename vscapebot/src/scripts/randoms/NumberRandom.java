package scripts.randoms;

import com.vsbot.hooks.GameInterface;
import com.vsbot.wrappers.RSInterface;

public class NumberRandom extends Random {

	@Override
	public int loop() {
		RSInterface[] parents = script.interfaces.getAllParents();
		for (RSInterface parent : parents) {
			if (parent != null) {
				for (GameInterface child : parent.getParentInterface()) {
					if (child != null) {
						if (child.getText() != null
								&& child.getText().contains("word is")
								&& child.getId() != 2162882
								&& !child
										.getText()
										.equals("Your password is only as safe as your computer.")
								|| child.getText() != null
								&& child.getText().contains("the word")) {
							// type the random text
							String text = child.getText();
							String[] subString = null;
							if (text.contains("word is")) {
								subString = child.getText().split("word is ");
							} else if (text.contains("the word")) {
								subString = child.getText().split("the word ");
							}
							String text2 = subString[1];
							// send it
							script.keyboard.sendKeys(text2);
						}
					}
				}
			}
		}
		return 500;
	}

	@Override
	public boolean startUp() {
        System.out.println("random startup");
		RSInterface[] parents = script.interfaces.getAllParents();
		for (RSInterface parent : parents) {
			if (parent != null) {
				for (GameInterface child : parent.getParentInterface()) {
					if (child != null) {
						if (child.getText() != null
								&& child.getText().contains("word is")
								&& child.getId() != 2162882
								&& !child
										.getText()
										.equals("Your password is only as safe as your computer.")
								|| child.getText() != null
								&& child.getText().contains("the word")) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

}
