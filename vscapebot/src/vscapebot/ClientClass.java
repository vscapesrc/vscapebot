package vscapebot;

import java.util.HashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

class ClientClass {
	private String name;
	private byte[] bytes;
	private int size;
	
	ClientClass(String name, byte[] bytes, int size) {
		this.assignedName = this.name = name;
		this.bytes = bytes;
		this.size = size;
		fieldMap = new HashMap<String,String>();
		methodMap = new HashMap<String,String>();
		
		ClassReader classReader = new ClassReader(this.bytes);
		node = new ClassNode(Opcodes.ASM5);
		editing = false;
		dirty = false;
		classReader.accept(node, ClassReader.EXPAND_FRAMES);
	}
	
	private boolean editing;
	private boolean dirty;
	
	ClassNode editClass() {
		if(editing == true) {
			System.out.println("ClientClass Warning: editClass called for class " + this + " while already editing.");
		}
		else {
			editing = true;
			dirty = true;
		}
		
		return node;
	}
	
	String getName() {
		return name;
	}
	
	/**
	 * Updates byte array based on the current state of the ClassNode.
	 * Call after making any modifications to the node.
	 */
	private void updateBytes() {
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		node.accept(classWriter);
		
		bytes = classWriter.toByteArray();
		size = bytes.length;
		
		dirty = false;
	}
	
	int getSize() {
		return size;
	}
	
	void finishEditing() {
		if(editing == false) {
			System.out.println("ClientClass Warning: finishEditing called for class " + this + " while not editing.");
		}
		else {
			if(dirty == true) {
				updateBytes();
			}
			editing = false;
		}
	}
	
	byte[] getBytes() throws IllegalStateException {
		if(dirty == true) {
			throw new IllegalStateException("Attempted to get bytes for " + this + " while dirty");
		}
		else if(editing == true) {
			throw new IllegalStateException("Attempted to get bytes for class " + this + " while still editing");
		}
		
		return bytes;
	}
	
	@Override
	public
	String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("class ");
		sb.append(name);
		sb.append(" (assignedName=");
		sb.append(assignedName);
		sb.append(")");
		
		return sb.toString();
	}
	
	ClassNode node;
	String assignedName;
	HashMap<String, String> fieldMap;
	HashMap<String, String> methodMap;
}