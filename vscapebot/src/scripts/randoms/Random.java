package scripts.randoms;

import scripts.Script;
public abstract class Random  implements Runnable {
	
	private boolean running = false;
	
	private boolean asd;
	
	public Script script;
	
	public void setRunning(boolean what){
		running = what;
	}
	
	public void setScript(Script s){
		this.script=s;
	}
	
	
	public abstract boolean startUp();
	
	public abstract int loop();
	

	@Override
	public void run() {
	int sleep;
	running = true;
	try{
		while(running){
			try{
			asd = startUp();
			if(asd){
				script.setPaused(true);
				sleep = loop();
				script.setPaused(false);
			if(sleep < 0){
				running = false;
			}else{
				Thread.sleep(sleep);
				Thread.sleep(2000);
			}
			}else{
				Thread.sleep(2000);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
		
	}catch(Exception e){
	}
	}

}
