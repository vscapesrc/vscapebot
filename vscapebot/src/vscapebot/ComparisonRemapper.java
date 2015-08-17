package vscapebot;

import java.util.LinkedList;
import java.util.jar.JarInputStream;

import org.objectweb.asm.tree.ClassNode;

public class ComparisonRemapper extends ClassRemapper {
	
	private ClientClass[] refactoredClasses;
	
	ComparisonRemapper(JarInputStream refactorJis) {
		refactoredClasses = ClientClass.getJarClasses(refactorJis);
		for(ClientClass cc: refactoredClasses) {
			cc.editClass();
		}
		
		examined = new LinkedList<ClientClass>();
	}
	
	LinkedList<ClientClass> examined;
	LinkedList<ClientClass> matched;

	@Override
	void examine(ClientClass cc) {
		ClassNode node, refNode;
		
		node = cc.getNode();
		
		LinkedList<Integer> scores = new LinkedList<Integer>();
		
		for(ClientClass refCC: refactoredClasses) {
			refNode = refCC.getNode();
			scores.clear();
			
			
			scores.add(scoreInterfaces(node, refNode));
			
			int score = 0;
			for(Integer s: scores) {
				score += s;
			}
			score /= scores.size();
			if(node.interfaces.size() > 0)
				System.out.println(cc + " + " + refCC + " = score " + score);
		}
	}
	
	private static int SCORE_MAX = 100;
	private static int SCORE_MIN = 0;
	
	int scoreInterfaces(ClassNode node1, ClassNode node2) {
		int score = SCORE_MIN;
		
		if(node1.interfaces.size() == 0 && node2.interfaces.size() == 0) {
			/* No interfaces for either class*/
			score = SCORE_MAX/3;
		}
		else if((node1.interfaces.size() == 0 && node2.interfaces.size() > 0) ||
				(node1.interfaces.size() > 0 && node2.interfaces.size()== 0)) {
			/* one class has at least one interface and the other has none */
			score = SCORE_MIN;
		}
		else if(node1.interfaces.size() == node2.interfaces.size()) {
			score = SCORE_MAX/2;
			int matches = 0;
			
			for(Object iface1: node1.interfaces) {
				for(Object iface2: node2.interfaces) {
					if(((String)iface1).equals((String)iface2)) {
						matches++;
					}
				}
			}
			
			if(matches > 0) {
				score = (100 * matches) / node1.interfaces.size();
			}
		}
		else if(node1.interfaces.size() > node2.interfaces.size() || node1.interfaces.size() < node2.interfaces.size()) {
			score = SCORE_MAX/3;
			int matches = 0;
			
			ClassNode less, more;
			if(node1.interfaces.size() < node2.interfaces.size()) {
				less = node1;
				more = node2;
			}
			else {
				less = node2;
				more = node1;
			}
			
			for(Object iface1: less.interfaces) {
				for(Object iface2: more.interfaces) {
					if(((String)iface1).equals((String)iface2)) {
						matches++;
					}
				}
			}
			
			if(matches > 0) {
				score = (100 * matches) / more.interfaces.size();
			}
		}

		return score;
	}
	

	@Override
	void remap() {
		for(ClientClass cc: refactoredClasses) {
			cc.finishEditing();
		}
	}

}
