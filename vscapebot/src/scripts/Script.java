package scripts;

import scripts.randoms.AntiMod;
import scripts.randoms.LevelUp;
import scripts.randoms.NumberRandom;
import scripts.randoms.Random;

import java.awt.*;

import com.vsbot.api.Methods;
import com.vsbot.launcher.Loader;

public abstract class Script extends Methods implements Runnable {


	
	
	public void setLoader(Loader l){
		super.setLoader(l);
	}

    public boolean isRunning(){
        return running;
    }


	public abstract void paint(Graphics g);


	public abstract int loop();

	public abstract void onBegin();

	public abstract void onFinish();

	private boolean running;

	private boolean paused;

	public void setPaused(boolean a) {
		paused = a;
	}

	public void stop() {
		running = false;
	}

	Random[] randoms = new Random[3];// = new NumberRandom();

	public void setRunning() {
		running = true ? running == false : running == true;
		System.out.println(running);
	}
	
	public void setRunning(boolean a) {
		running = a;
	}

	@Override
	public void run() {
			running = true;
			int sleep;
			try {
				onBegin();
				randoms[0] = new NumberRandom();
				///Thread s = new Thread(randoms[0], "NumberRandom");
				randoms[0].setScript(this);
                //randoms[1] = new LevelUp();
               // randoms[1].setScript(this);
				//randoms[1] = new LevelUp();
			//	randoms[1].setScript(this);
				//randoms[2] = new AntiMod();
				//randoms[2] = new AntiMod();
				//Thread l = new Thread(randoms[1], "LevelUp");
			//	l.start();

				try {
					while (running) {
						try {
							if (!paused) {
								for(Random r : randoms){
									if(r != null && r.startUp()){
										sleep = r.loop();
										Thread.sleep(sleep);
									}
								}
								sleep = loop();
								if (sleep < 0) {
									running = false;
								} else {
									Thread.sleep(sleep);
									Thread.sleep(300);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}

				} catch (Exception e) {
					for (Random rand : randoms) {
						rand.setRunning(false);
						rand = null;
					}// / stop random event
					randoms = null;
					onFinish();

				}
			} catch (Exception e) {
			}
		}
	}

