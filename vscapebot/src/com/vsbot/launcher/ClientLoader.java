package com.vsbot.launcher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

public class ClientLoader extends ClassLoader {
	
	static String MAIN_CLASS_NAME = "Client";
	
	private File dataDir;

	ClientLoader(ClassLoader parent) throws IOException {
		super(parent);
		dataDir = Storage.getDataDirFile();
	}
	
	
	private File clientFile;
	
	boolean obtainClient() {
		boolean successful = false;
		boolean needToDownload;
		String remoteVersion;
		String localVersion;
		
		remoteVersion = Storage.retrieveClientVersion();
		localVersion = Storage.clientVersionExists()?Storage.localClientVersion():"None";
		
		System.out.print("Remote client version: ");
		System.out.println(remoteVersion);
		System.out.print("Local client version: ");
		System.out.println(localVersion);
		System.out.println();
		
		if(Storage.clientExists()) {
			
			if(localVersion.equals(remoteVersion)) {
				needToDownload = false;
			}
			else {
				needToDownload = true;
			}
		}
		else {
			needToDownload = true;
		}
		
		if(needToDownload) {
			successful = false;
			System.out.print("Downloading version ");
			System.out.println(remoteVersion);
			try {
				Storage.downloadClient();
			} catch (IOException e) {}
			
			
			successful = Storage.clientExists();
		}
		else {
			successful = true;
		}
		
		if(successful == true) {
			Storage.writeLocalVersion(remoteVersion);
			System.out.println("Client up to date.");
			
			clientFile = new File(Storage.clientJarPath());
		}
		
		return successful;
	}
	
	private ClientClass[] classes;
	private HashMap<String,ClientClass> classMap;
	
	int loadClasses() throws FileNotFoundException, IOException {
		JarInputStream jis = new JarInputStream(new FileInputStream(clientFile));
		

		this.classes = ClientClass.getJarClasses(jis);
		
		generateClassMap();
		
		jis.close();
		
		return this.classes.length;
	}
	
	void generateClassMap() {
		classMap = new HashMap<String,ClientClass>();
		
		for(ClientClass cc: classes) {
			classMap.put(cc.getName(), cc);
		}
	}
	
	ClientClass[] getClasses() {
		return classes;
	}
	
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		if(classMap.containsKey(name)) {
			ClientClass cc = classMap.get(name);
			return defineClass(name,cc.getBytes(), 0, cc.getSize());
		}
		else {
			return super.findClass(name);
		}
	}
}
