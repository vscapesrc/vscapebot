package vscapebot.updater;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;

import vscapebot.ClientClass;
import vscapebot.updater.remap.ClassRemapper;

public class Updater extends Remapper {
	
	private LinkedList<ClassRemapper> remapperList;
			
	public Updater() {
		remapperList = new LinkedList<ClassRemapper>();
	}
	
	public void addClassRemapper(ClassRemapper id) {
		if(id != null) {
			remapperList.add(id);
		}
	}
	
	Map<String,String> classNameMap;
	@Override
	public
	String mapType(String typeName) {
		if(classNameMap.containsKey(typeName)) {
			return classNameMap.get(typeName);
		}
		else {
			return typeName;
		}
	}
	
	public void run(ClientClass[] classes) throws IllegalStateException {
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
			
			classNameMap = new HashMap<String,String>();
			for(ClientClass cc: classes) {
				classNameMap.put(cc.getName(), cc.getAssignedName());
				cc.setName(cc.getAssignedName());
			}
			
			for(ClientClass cc1: classes) {
				for(ClientClass cc2: classes) {
					if(cc1.getName().equals(cc2.getName())) continue;
					if(cc1 != null && cc2 != null && cc1.getAssignedName().equals(cc2.getAssignedName())) {
						System.err.println("Conflicting remapped classes: " + cc1 + " and " + cc2);
						System.err.println("Resetting mapping.");
						cc1.setAssignedName(cc1.getName());
						cc2.setAssignedName(cc2.getName());
					}
				}
			}
			
			SimpleRemapper remapper = new SimpleRemapper(classNameMap);
			RemappingClassAdapter adapter;
			for(ClientClass cc: classes) {
				ClassNode newNode = new ClassNode(Opcodes.ASM5);
				
				adapter = new RemappingClassAdapter(newNode, remapper);
				cc.getNode().accept(adapter);
				
				cc.setNode(newNode);

				cc.finishEditing();
			}

		}
	}
}
