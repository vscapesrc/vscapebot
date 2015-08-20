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
	
	static int SCORE_MAX = 1000;
	static int SCORE_MIN = 0;
	static int SCORE_UNAVAILABLE = -1;
	
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
		
		classScorers = new ClassScorer[5];
		classScorers[0] = new InterfaceScorer();
		classScorers[1] = new FieldScorer();
		classScorers[2] = new InheritanceHierarchyScorer(this);
		classScorers[3] = new MethodScorer();
		classScorers[4] = new CodeReferenceScorer(clientClasses, refactoredClasses);
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
			
			if(scores.size() > 0) {
				score /= scores.size();
				//if(score > ComparisonRemapper.SCORE_MAX * 0.6) System.out.println(cc + " <=> " + refCC + " = score of " + score);
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
	
	@Override
	public void remap() {
		for(List<Score> scoreList: scores.values()) {
			Collections.sort(scoreList);
		}
		
		Map<String, ClientClass> mappings = new HashMap<String, ClientClass>();
		
		String name;
		int iterationsRequired = 1;
		while(iterationsRequired-- > 0) {
			for(ClientClass cc: scores.keySet()) {
				
				List<Score> scores = this.scores.get(cc);
				name = scores.get(0).cc.getName();
				
				if(mappings.containsKey(name) && mappings.get(name).getName().equals(cc.getName()) == false) {
					// we have a mapping ambiguity so try to resolve it 
					int otherIndex = 0;
					List<Score> otherScores = this.scores.get(mappings.get(name));
					for(Score s: otherScores) {
						if(s.cc.getName().equals(name)) {
							break;
						}
						otherIndex++;
					}
					
					int index = 0;
					Score scoree = null, otherScore = null;
					while(index < scores.size() && otherIndex < otherScores.size()) {
						scoree = scores.get(index);
						otherScore = otherScores.get(otherIndex);
						if(scoree.matchingScore == otherScore.matchingScore) {
							index++;
							otherIndex++;
						}
						else if(scoree.matchingScore < otherScore.matchingScore) {
							index++;
							break;
						}
						else {
							otherIndex++;
							break;
						}
					}
					
					scoree = scores.get(index);
					this.scores.put(cc,scores.subList(index, scores.size()-1));
					
					otherScore = otherScores.get(otherIndex);
					ClientClass otherCC = mappings.get(name);
					this.scores.put(otherCC,otherScores.subList(otherIndex, otherScores.size()-1));
					mappings.remove(name);
					mappings.put(otherScore.cc.getName(), otherCC);
					otherCC.setAssignedName(otherScore.cc.getName());
					
					mappings.put(scoree.cc.getName(),cc);
					cc.setAssignedName(scoree.cc.getName());
					
					iterationsRequired++;
					
				}
				else {
					mappings.put(name, cc);
					cc.setAssignedName(name);
				}
				
			}
		}
		
		/* Finish editing classes for the refactor which was loaded in this class */
		for(ClientClass cc: refactoredClasses) {
			cc.finishEditing();
		}
	}

}
