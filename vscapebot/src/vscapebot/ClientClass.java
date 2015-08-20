package vscapebot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public class ClientClass {
	
	ClientClass(String name, byte[] bytes, int size) {
		this.setAssignedName(this.name = name);
		this.bytes = bytes;
		this.size = size;
		fieldMap = new HashMap<String,String>();
		methodMap = new HashMap<String,String>();
		
		ClassReader classReader = new ClassReader(this.bytes);
		node = new ClassNode(Opcodes.ASM5);
		editing = false;
		dirty = false;
		classReader.accept(node, ClassReader.EXPAND_FRAMES);
		
		this.superName = node.superName;
	}
	
	
	ClassNode node;
	private boolean editing;
	private boolean dirty;
	public ClassNode editClass() {
		if(editing == true) {
			System.out.println("ClientClass Warning: editClass called for class " + this + " while already editing.");
		}
		else {
			editing = true;
			dirty = true;
		}
		
		return node;
	}
	
	public ClassNode getNode() throws IllegalStateException {
		if(editing == false) {
			throw new IllegalStateException("Can't get ClassNode for " + this + " while not editing");
		}
		
		return node;
	}
	
	public void setNode(ClassNode node) throws IllegalStateException {
		if(editing == false) {
			throw new IllegalStateException("Can't set ClassNode for " + this + " while not editing");
		}
		
		this.node = node;
	}
	
	private String name;
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		if(editing) {
			this.name = name;
		}
	}
	
	private String superName;
	public String getSuperName() {
		return superName;
	}
	
	private int size;
	int getSize() {
		return size;
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
	
	public void finishEditing() {
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
	
	private byte[] bytes;
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
		sb.append(getAssignedName());
		sb.append(")");
		
		return sb.toString();
	}
	
	
	private String assignedName;
	HashMap<String, String> fieldMap;
	HashMap<String, String> methodMap;
	
	public static ClientClass[] getJarClasses(JarInputStream jis) {
		LinkedList<ClientClass> classes = new LinkedList<ClientClass>();
		
		JarEntry je;
		byte[] block = new byte[4096];
		ByteArrayOutputStream baos;
		int read;
		int size;
		try {
			while((je = jis.getNextJarEntry()) != null) {
				if(je.getName().toLowerCase().endsWith(".class") == false)
					continue;
				
				baos = new ByteArrayOutputStream();
				size = 0;
				
				while((read = jis.read(block)) != -1) {
					size += read;
					baos.write(block, 0, read);
				}
				
				ClientClass cc = new ClientClass(je.getName().replace(".class",""),baos.toByteArray(),size);
				classes.add(cc);
				
				baos.close();
			}
		} catch (IOException e) {
			return null;
		}
		
		ClientClass[] clazzes = classes.toArray(new ClientClass[0]);
		classes.clear();
		
		return clazzes;
	}
	
	public static void saveJar(ClientClass[] classes, File output) throws IOException {
		if(output.exists()) {
			if(!output.delete()) {
				throw new IOException("Could not re-create file: " + output.toString());
			}
		}
		if(!output.createNewFile()) {
			throw new IOException("Could not create file: " + output.toString());
		}
		
		JarOutputStream jos = new JarOutputStream(new FileOutputStream(output));
		
		ZipEntry entry;
		for(ClientClass cc: classes) {
			entry = new ZipEntry(cc.getName() + ".class");
			jos.putNextEntry(entry);
			jos.write(cc.getBytes(), 0, cc.getSize());
		}
		jos.closeEntry();
		jos.finish();
		jos.flush();
		jos.close();
	}

	public String getAssignedName() {
		return assignedName;
	}

	public void setAssignedName(String assignedName) {
		this.assignedName = assignedName;
	}
}