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

public class ComparisonClassRemapper extends ClassRemapper {
	
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
	
	public ComparisonClassRemapper(JarInputStream refactorJis, ClientClass[] classes) {
		clientClasses = classes;
		refactoredClasses = ClientClass.getJarClasses(refactorJis);
		for(ClientClass cc: refactoredClasses) {
			cc.editClass();
		}
		
		examined = new LinkedList<ClientClass>();
		classScores = new HashMap<ClientClass,List<Score>>();
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
	
		for(List<Score> ls: classScores.values()) {
			ls.clear();
		}
		classScores.clear();
		
		classScorers = new ClassScorer[5];
		classScorers[0] = new InterfaceScorer();
		classScorers[1] = new FieldScorer();
		classScorers[2] = new InheritanceHierarchyScorer(this);
		classScorers[3] = new MethodScorer();
		classScorers[4] = new CodeReferenceScorer(clientClasses, refactoredClasses);
	}
	
	List<ClientClass> examined;
	Map<ClientClass,List<Score>> classScores;
	
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
		
		this.classScores.put(cc,scoreList);
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
		for(List<Score> scoreList: classScores.values()) {
			Collections.sort(scoreList);
		}
		
		Map<String, ClientClass> previousRemappings = new HashMap<String, ClientClass>();
		
		String bestMatchClassName;
		int iterationsRequired = 1;
		while(iterationsRequired-- > 0) {
			for(ClientClass remappingClass: classScores.keySet()) {
				
				List<Score> remappingClassScores = this.classScores.get(remappingClass);
				if(remappingClassScores.size() == 0) {
					// skip a class that has no scored mappings
					continue;
				}
				
				bestMatchClassName = remappingClassScores.get(0).cc.getName();
				
				if(previousRemappings.containsKey(bestMatchClassName) && previousRemappings.get(bestMatchClassName).getName().equals(remappingClass.getName()) == false) {
					// we have a mapping ambiguity so try to resolve it 
					int otherScoreIndex = 0;
					
					List<Score> otherClassScores = this.classScores.get(previousRemappings.get(bestMatchClassName));
					for(Score score: otherClassScores) {
						if(score.cc.getName().equals(bestMatchClassName)) {
							break;
						}
						otherScoreIndex++;
					}
					
					int remappingScoreIndex = 0;
					Score remappingScore = null, otherScore = null;
					while(remappingScoreIndex < remappingClassScores.size() && otherScoreIndex < otherClassScores.size()) {
						remappingScore = remappingClassScores.get(remappingScoreIndex);
						otherScore = otherClassScores.get(otherScoreIndex);
						
						
						if(remappingScore.matchingScore == otherScore.matchingScore) {
							// both classes score equally to the mapping
							remappingScoreIndex++;
							otherScoreIndex++;
						}
						else if(remappingScore.matchingScore < otherScore.matchingScore) {
							// the class being remapped scores less than the other class
							remappingScoreIndex++;
							break;
						}
						else {
							// the class that was remapped scores less
							otherScoreIndex++;
							break;
						}
					}
					
					ClientClass prevRemappedCC = previousRemappings.get(bestMatchClassName);
					
					String prevRemappedName;
					String remappingName;
					if(remappingScoreIndex >= remappingClassScores.size()) {
						remappingName = remappingClass.getName();
					}
					else {
						remappingScore = remappingClassScores.get(remappingScoreIndex);
						remappingName = remappingScore.cc.getName();
						this.classScores.put(remappingClass,remappingClassScores.subList(remappingScoreIndex, remappingClassScores.size()-1));
					}
					
					if(otherScoreIndex >= otherClassScores.size()) {
						prevRemappedName = prevRemappedCC.getName();
					}
					else {
						otherScore = otherClassScores.get(otherScoreIndex);
						prevRemappedName = otherScore.cc.getName();
						this.classScores.put(prevRemappedCC,otherClassScores.subList(otherScoreIndex, otherClassScores.size()-1));
					}
					
					{
						//remove conflicting mapping
						previousRemappings.remove(bestMatchClassName);
						
						// create new mapping for the previously mapped class
						previousRemappings.put(prevRemappedName, prevRemappedCC);
						prevRemappedCC.setAssignedName(prevRemappedName);
						
						// create mapping for the yet unmapped class
						previousRemappings.put(remappingName,remappingClass);
						remappingClass.setAssignedName(remappingName);
					}
					
					iterationsRequired++;
				}
				else {
					previousRemappings.put(bestMatchClassName, remappingClass);
					remappingClass.setAssignedName(bestMatchClassName);
				}
				
			}
		}
		
		/* Finish editing classes for the refactor which was loaded in this class */
		for(ClientClass cc: refactoredClasses) {
			cc.finishEditing();
		}
	}

}
