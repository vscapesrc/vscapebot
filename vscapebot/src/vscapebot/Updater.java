package vscapebot;

import java.util.LinkedList;

public class Updater {
	
	private LinkedList<ClassRemapper> remapperList;
			
	Updater() {
		remapperList = new LinkedList<ClassRemapper>();
	}
	
	void addRemapper(ClassRemapper id) {
		if(id != null) {
			remapperList.add(id);
		}
	}
	
	void run(ClientClass[] classes) throws IllegalStateException {
		ClassRemapper[] rems = remapperList.toArray(new ClassRemapper[0]);
		
		if(classes.length > 0 && rems.length > 0) {
			
			for(ClientClass cc: classes) {
				cc.editClass();
			}
			
			for(ClassRemapper r: rems) {
				r.reset();
				
				for(ClientClass cc: classes) {
					r.examine(cc);
				}
				r.remap();	
			}
			
			for(ClientClass cc: classes) {
				cc.finishEditing();
			}
			
			for(ClientClass cc1: classes) {
				for(ClientClass cc2: classes) {
					if(cc1 == cc2) continue;
					if(cc1 != null && cc2 != null && cc1.assignedName.equals(cc2.assignedName)) {
						throw new IllegalStateException("Conflicting remapped classes: " + cc1 + " and " + cc2);
					}
				}
			}
			

		}
	}
}
