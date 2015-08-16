package vscapebot;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Main {
	
	public static void main(String[] args) {
		try {
			new Main().run();
		} catch (IOException e) {
			System.err.println("IO problem running bot: " + e.getMessage());
		} catch (InstantiationException e) {
			System.err.println("Could not instantiate class: " + e.getMessage());
		} catch (IllegalAccessException e) {
			System.err.println("Illegal access to class: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			System.err.println("Class not found: " + e.getMessage());
		}
	}
	
	
	ClientLoader clientLoader;
	
	Main() throws IOException {
		clientLoader = new ClientLoader(this.getClass().getClassLoader());
	}
	
	Bot bot;
	Updater updater;
	void run() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SecurityException {
		if(clientLoader.obtainClient() == false) {
			System.err.println("Unable to obtain client!");
		}
		
		int classesLoaded = clientLoader.loadClasses();
		System.out.println("Loaded " + classesLoaded + " classes.");
		
		System.out.println("Running updater...");
		updater = new Updater();
		updater.run(clientLoader.getClasses());
		
		Object client = clientLoader.loadClass("Client").newInstance();
		Class<?> clientClass = client.getClass();
		bot = new Bot(client);
		System.out.println("Loaded Client class.");
		
		Method main;
		try {
			main = clientClass.getMethod("main", String[].class);
		} catch (NoSuchMethodException e1) {
			System.err.println("Could not get Client main method: " + e1.getMessage());
			return;
		}
		
		String[] args = {"vidyascape"};
		try {
			main.invoke(client, (Object)args);
		} catch (IllegalArgumentException | InvocationTargetException e) {
			System.err.println("Could not invoke the main method: " + e.getMessage());
			return;
		}
		
		System.out.println("Client execution started.");
		bot.init();
		System.out.println("Bot initialized.");
	}
	
	
}
