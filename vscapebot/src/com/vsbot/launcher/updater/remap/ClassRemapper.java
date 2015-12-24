package com.vsbot.launcher.updater.remap;

import com.vsbot.launcher.ClientClass;

public abstract class ClassRemapper {
	public abstract void examine(ClientClass cc);
	
	public abstract void remap();
	public abstract void reset();
}
