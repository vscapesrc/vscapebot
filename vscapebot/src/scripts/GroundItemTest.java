package scripts;

import java.awt.*;

public class GroundItemTest extends Script {

	@Override
	public void paint(Graphics g) {
		// TODO Auto-generated method stub

	}

	@Override
	public int loop() {
	/*	for (RSGroundItem a : grounditems.getAll()) {
		if (a != null){
			System.out.println(a.getName());
		}*/
		System.out.println(getMyPlayer().getName() + " anim " + getMyPlayer().getAnimation());
		/*for(RSPlayer player : getAllPlayers()){ 
			if(player != null && player.getName() != null && player.getName() != ""){
				System.out.println(player.getName() + " " + player.getLocation());
			}
	}*/
		return 3000;
	}

	@Override
	public void onBegin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub

	}

}
