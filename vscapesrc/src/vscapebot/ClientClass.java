package vscapebot;

import java.util.HashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

class ClientClass {
	String name;
	byte[] bytes;
	int size;
	
	ClientClass(String name, byte[] bytes, int size) {
		this.assignedName = this.name = name;
		this.bytes = bytes;
		this.size = size;
		fieldMap = new HashMap<String,String>();
		methodMap = new HashMap<String,String>();
		
		ClassReader classReader = new ClassReader(this.bytes);
		node = new ClassNode(Opcodes.ASM5);
		classReader.accept(node, ClassReader.EXPAND_FRAMES);
	}
	
	/**
	 * Updates byte array based on the current state of the ClassNode.
	 * Call after making any modifications to the node.
	 */
	void updateBytes(ClientLoader clientLoader) {
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		node.accept(classWriter);
		
		bytes = classWriter.toByteArray();
		size = bytes.length;
	}
	
	ClassNode node;
	String assignedName;
	HashMap<String, String> fieldMap;
	HashMap<String, String> methodMap;
}