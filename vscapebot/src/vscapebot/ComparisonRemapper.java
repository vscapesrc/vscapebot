package vscapebot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarInputStream;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class ComparisonRemapper extends ClassRemapper {
	
	private ClientClass[] refactoredClasses;
	
	ComparisonRemapper(JarInputStream refactorJis) {
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
	void reset() {
		examined.clear();
	
		for(List<Score> ls: scores.values()) {
			ls.clear();
		}
		scores.clear();
	}
	
	List<ClientClass> examined;
	Map<ClientClass,List<Score>> scores;

	@Override
	void examine(ClientClass cc) {
		ClassNode node, refNode;
		
		node = cc.getNode();
		
		examined.add(cc);
		List<Score> scoreList = new ArrayList<Score>();
		
		LinkedList<Integer> scores = new LinkedList<Integer>();
		
		for(ClientClass refCC: refactoredClasses) {
			refNode = refCC.getNode();
			scores.clear();
			
			
			scores.add(scoreInterfaces(node, refNode));
			scores.add(scoreFields(node, refNode));
			
			int score = 100;
			for(Integer s: scores) {
				score = (score * s) / 100;
			}
			//score /= scores.size();
			
			if(score > 0) {
			//	System.out.println(cc + " <=> " + refCC + " = score of " + score);
			}

			scoreList.add(new Score(refCC,score));
		}
		
		this.scores.put(cc,scoreList);
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
	
	private static int numTypeFields(ClassNode node, Type type) {
		int num = 0;
		
		for(Object fn: node.fields) {
			if(Type.getType(((FieldNode)fn).desc).equals(type)) {
				num++;
			}
		}
		
		return num;
	}
	
	private static int getFieldsScore(ClassNode less, ClassNode more, Type type) {
		int numer, denom;
		int score;
		numer = numTypeFields(less,type) * 100;
		denom = numTypeFields(more,type);
		if(denom == 0) {
			if(numer == 0) {
				score = SCORE_MAX;
			}
			else {
				score = SCORE_MIN;
			}
		}
		else {
			score =  numer / denom ;
		}
		
		return score;
	}
	
	private static int getObjectFieldsScore(ClassNode less, ClassNode more) {
		HashMap<Type,Integer> lessFieldsCount = new HashMap<Type,Integer>();
		HashMap<Type,Integer> moreFieldsCount = new HashMap<Type,Integer>();	
		
		FieldNode fieldNode;
		Type type;
		for(Object fn: less.fields) {
			fieldNode = (FieldNode)fn;
			if((type = Type.getType(fieldNode.desc)).equals(Type.OBJECT)) {
				int count = 1;
				if(lessFieldsCount.containsKey(type)) {
					count += lessFieldsCount.get(type);
				}
				
				lessFieldsCount.put(type, count);
			}
		}
		
		for(Object fn: more.fields) {
			fieldNode = (FieldNode)fn;
			if((type = Type.getType(fieldNode.desc)).equals(Type.OBJECT)) {
				int count = 1;
				if(moreFieldsCount.containsKey(type)) {
					count += moreFieldsCount.get(type);
				}
				
				moreFieldsCount.put(type, count);
			}
		}
		
		int score = 0;
		LinkedList<Integer> scores = new LinkedList<Integer>();
		for(Type moreType: moreFieldsCount.keySet()) {
			int moreCount = moreFieldsCount.get(moreType);
			int lessCount = 0;
			
			if(lessFieldsCount.containsKey(moreType)) {
				try {
					less.getClass().getClassLoader().loadClass(moreType.getInternalName());
				}
				catch(ClassNotFoundException e) {
					continue;
				}
				
				lessCount = 0;
			}
			else {
				lessCount = lessFieldsCount.get(moreType);
			}
			
			scores.add(lessCount * 100 / moreCount);
		}
		
		for(Integer s: scores) {
			score += s;
		}
		
		if(scores.size() == 0) {
			score = 0;
		}
		else {
			score /= scores.size();
		}
		
		return score;
	}
	
	int scoreFields(ClassNode node1, ClassNode node2) {
		int score = SCORE_MIN;
		LinkedList<Integer> scores = new LinkedList<Integer>();
		
		int numFieldsScore;
		ClassNode less,more;
		if((node1.fields.size() == node2.fields.size()) || (node1.fields.size() < node2.fields.size())) {
			less = node1;
			more = node2;
		}
		else {
			less = node2;
			more = node1;
		}
		
		int byteFieldsScore = SCORE_MAX;
		int charFieldsScore = SCORE_MAX;
		int shortFieldsScore = SCORE_MAX;
		int intFieldsScore = SCORE_MAX;
		int longFieldsScore = SCORE_MAX;
		int floatFieldsScore = SCORE_MAX;
		int doubleFieldsScore = SCORE_MAX;
		int objectFieldsScore = SCORE_MAX;
		if(more.fields.size() == 0) {
			if(less.fields.size() == 0) {
				numFieldsScore = 100;
			}
			else {
				numFieldsScore = SCORE_MIN;
				byteFieldsScore = SCORE_MIN;
				charFieldsScore = SCORE_MIN;
				shortFieldsScore = SCORE_MIN;
				intFieldsScore = SCORE_MIN;
				longFieldsScore = SCORE_MIN;
				floatFieldsScore = SCORE_MIN;
				doubleFieldsScore = SCORE_MIN;
				objectFieldsScore = SCORE_MIN;
			}
		}
		else {
			numFieldsScore = less.fields.size() * 100 / more.fields.size();
			byteFieldsScore = getFieldsScore(less,more,Type.BYTE_TYPE);
			charFieldsScore = getFieldsScore(less,more,Type.CHAR_TYPE);
			shortFieldsScore = getFieldsScore(less,more,Type.SHORT_TYPE);
			intFieldsScore = getFieldsScore(less,more,Type.INT_TYPE);
			longFieldsScore = getFieldsScore(less,more,Type.LONG_TYPE);
			floatFieldsScore = getFieldsScore(less,more,Type.FLOAT_TYPE);
			doubleFieldsScore = getFieldsScore(less,more,Type.DOUBLE_TYPE);
			objectFieldsScore = getObjectFieldsScore(less,more);
			
		}
		
		scores.add(numFieldsScore);
		scores.add(byteFieldsScore);
		scores.add(charFieldsScore);
		scores.add(shortFieldsScore);
		scores.add(intFieldsScore);
		scores.add(longFieldsScore);
		scores.add(floatFieldsScore);
		scores.add(doubleFieldsScore);
		scores.add(objectFieldsScore);
		
		for(Integer s: scores) {
			score += s;
		}
	
		score /= scores.size();
		
		return score;
	}

	@Override
	void remap() {
		for(List<Score> scoreList: scores.values()) {
			Collections.sort(scoreList);
		}
		
		for(ClientClass cc: scores.keySet()) {
			cc.assignedName = scores.get(cc).get(0).cc.getName();
			System.out.println(cc);
		}
		
		for(ClientClass cc: refactoredClasses) {
			cc.finishEditing();
		}
	}

}
