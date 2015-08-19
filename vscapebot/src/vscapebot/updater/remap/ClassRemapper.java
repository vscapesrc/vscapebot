package vscapebot.updater.remap;

import vscapebot.ClientClass;

public abstract class ClassRemapper {
	public abstract void examine(ClientClass cc);
	public abstract void remap();
	public abstract void reset();
}
