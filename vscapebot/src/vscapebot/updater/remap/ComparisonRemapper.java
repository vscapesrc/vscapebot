package vscapebot.updater.remap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarInputStream;

import org.objectweb.asm.tree.ClassNode;

import vscapebot.ClientClass;
import vscapebot.ClientLoader;

public class ComparisonRemapper extends ClassRemapper {
	
	private ClientClass[] clientClasses;
	private ClientClass[] refactoredClasses;
	private ClassScorer[] classScorers;
	
	public String getSuperClassName(String internalName) {
		ClassLoader classLoader = this.getClass().getClassLoader();
		
		try {
			Class<?> clazz = classLoader.loadClass(internalName.replace('/', '.'));
			return clazz.getSuperclass().getName().replace('.', '/');
		} catch (ClassNotFoundException e) {
			for(ClientClass cc: clientClasses) {
				if(cc.getName().equals(internalName)) {
					return cc.getSuperName();
				}
			}
			
			for(ClientClass cc: refactoredClasses) {
				if(cc.getName().equals(internalName)) {
					return cc.getSuperName();
				}
			}
			
			return null;
		}
	}
	
	public ComparisonRemapper(JarInputStream refactorJis, ClientClass[] classes) {
		clientClasses = classes;
		refactoredClasses = ClientClass.getJarClasses(refactorJis);
		for(ClientClass cc: refactoredClasses) {
			cc.editClass();
		}
		
		examined = new LinkedList<ClientClass>();
		scores = new HashMap<ClientClass,List<Score>>();
		
		classScorers = new ClassScorer[3];
		classScorers[0] = new InterfaceScorer();
		classScorers[1] = new FieldScorer();
		classScorers[2] = new InheritanceHierarchyScorer(this);
	}
	
	private class Score implements Comparable<Score> {
		ClientClass cc;
		int matchingScore;
		
		Score(ClientClass cc, int matchingScore) {
			this.cc = cc;
			this.matchingScore = matchingScore;
		}

		@Override
		public int compareTo(Score o) {
			return -(matchingScore-o.matchingScore);
		}
		
		
	}
	
	@Override
	public void reset() {
		examined.clear();
	
		for(List<Score> ls: scores.values()) {
			ls.clear();
		}
		scores.clear();
	}
	
	List<ClientClass> examined;
	Map<ClientClass,List<Score>> scores;
	
	@Override
	public void examine(ClientClass cc) {
		ClassNode node, refNode;
		
		node = cc.getNode();
		
		examined.add(cc);
		List<Score> scoreList = new ArrayList<Score>();
		
		LinkedList<Integer> scores = new LinkedList<Integer>();
		
		for(ClientClass refCC: refactoredClasses) {
			refNode = refCC.getNode();
			scores.clear();
			
			for(ClassScorer scorer: classScorers) {
				scorer.evaluateScore(node, refNode);
				scorer.addScore(scores);
			}
			
			int score = SCORE_MIN;
			for(Integer s: scores) {
				score += s;
			}
			
			if(score > 0) {
				score /= scores.size();
				if(score > 60) System.out.println(cc + " <=> " + refCC + " = score of " + score);
			}

			scoreList.add(new Score(refCC,score));
		}
		
		this.scores.put(cc,scoreList);
	}
	
	static boolean isStandardLibraryClass(ClassLoader classLoader, String internalName) {
		boolean isStandard = false;
	
		if(internalName != null) {
			try {
				classLoader.loadClass(internalName);
			} catch (ClassNotFoundException e) {
				isStandard = false;
			}
		}
		
		return isStandard;
	}
	
	static int SCORE_MAX = 100;
	static int SCORE_MIN = 0;
	static int SCORE_UNAVAILABLE = -1;
	
	class InterfaceScorer extends ClassScorer {

		@Override
		void evaluateScore(ClassNode node1, ClassNode node2) {
			score = SCORE_MIN;
			
			ClassLoader classLoader = this.getClass().getClassLoader();
			
			if(node1.interfaces.size() == 0 && node2.interfaces.size() == 0) {
				/* No interfaces for either class*/
				score = SCORE_UNAVAILABLE;
			}
			else if((node1.interfaces.size() == 0 && node2.interfaces.size() > 0) ||
					(node1.interfaces.size() > 0 && node2.interfaces.size()== 0)) {
				/* one class has at least one interface and the other has none */
				score = SCORE_MIN;
			}
			else if(node1.interfaces.size() == node2.interfaces.size()) {
				score = SCORE_MIN;
				int matches = 0;
				
				for(Object iface1: node1.interfaces) {
					for(Object iface2: node2.interfaces) {
						if(((String)iface1).equals((String)iface2)) {
							matches++;
						}
					}
				}
				
				
				
				if(matches > 0) {
					score = (SCORE_MAX * matches) / node1.interfaces.size();
				}
				else {
					int gameifaces1 = 0, gameifaces2 = 0;
					for(Object iface1: node1.interfaces) {
						if(!ComparisonRemapper.isStandardLibraryClass(classLoader,(String)iface1)) gameifaces1++;
					}
					for(Object iface2: node2.interfaces) {
						if(!ComparisonRemapper.isStandardLibraryClass(classLoader,(String)iface2)) gameifaces2++;
					}
					
					int numer = 0, denom = 0;
					
					if(gameifaces1 > gameifaces2) {
						numer = gameifaces2;
						denom = gameifaces1;
					}
					else if(gameifaces1 < gameifaces2) {
						numer = gameifaces1;
						denom = gameifaces2;
					}
	
					if(denom != 0) {
						score = (numer * SCORE_MAX) / denom;
					}
				}
			}
			else if(node1.interfaces.size() > node2.interfaces.size() || node1.interfaces.size() < node2.interfaces.size()) {
				score = SCORE_MIN;
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
					score = (SCORE_MAX * matches) / more.interfaces.size();
				}
				else {
					int gameifaces1 = 0, gameifaces2 = 0;
					for(Object iface1: node1.interfaces) {
						if(!ComparisonRemapper.isStandardLibraryClass(classLoader,(String)iface1)) gameifaces1++;
					}
					for(Object iface2: node2.interfaces) {
						if(!ComparisonRemapper.isStandardLibraryClass(classLoader,(String)iface2)) gameifaces2++;
					}
					
					int numer = 0, denom = 0;
					
					if(gameifaces1 > gameifaces2) {
						numer = gameifaces2;
						denom = gameifaces1;
					}
					else if(gameifaces1 < gameifaces2) {
						numer = gameifaces1;
						denom = gameifaces2;
					}
	
					if(denom != 0) {
						score = (numer * SCORE_MAX) / denom;
					}
				}
			}
		}
	}
	
	
	
	@Override
	public void remap() {
		for(List<Score> scoreList: scores.values()) {
			Collections.sort(scoreList);
		}
		
		for(ClientClass cc: scores.keySet()) {
			cc.setAssignedName(scores.get(cc).get(0).cc.getName());
			System.out.println(cc);
		}
		
		for(ClientClass cc: refactoredClasses) {
			cc.finishEditing();
		}
	}

}
