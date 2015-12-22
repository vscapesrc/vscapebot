package vscapebot.updater.remap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

class FieldScorer extends ClassScorer {
	
	private int numTypeFields(ClassNode node, Type type) {
		int num = 0;
		
		for(Object fn: node.fields) {
			if(Type.getType(((FieldNode)fn).desc).equals(type)) {
				num++;
			}
		}
		
		return num;
	}
	
	private int getFieldsScore(ClassNode less, ClassNode more, Type type) {
		int numer, denom;
		int score;
		
		int one, two;
		
		one = numTypeFields(less,type);
		two = numTypeFields(more,type);
		
		if((one < two) || (one == two)) {
			numer = one;
			denom = two;
		}
		else {
			numer = two;
			denom = one;
		}
		
		if(denom == 0) {
			if(numer == 0) {
				score = ComparisonClassRemapper.SCORE_UNAVAILABLE;
			}
			else {
				score = ComparisonClassRemapper.SCORE_MIN;
			}
		}
		else {
			score =  numer / denom ;
		}
		
		return score;
	}

	
	private int getObjectFieldsScore(ClassNode less, ClassNode more) {
		HashMap<Type,Integer> lessFieldsCount = new HashMap<Type,Integer>();
		HashMap<Type,Integer> moreFieldsCount = new HashMap<Type,Integer>();
		
		boolean lessSelfReferences = false;
		boolean moreSelfReferences = false;
		
		ClassLoader classLoader = this.getClass().getClassLoader();
		
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
				if(type.getClassName().equals(less.name)) {
					lessSelfReferences = true;
				}
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
				if(type.getClassName().equals(less.name)) {
					moreSelfReferences = true;
				}
			}
		}
		
		int score = ComparisonClassRemapper.SCORE_MIN;
		LinkedList<Integer> scores = new LinkedList<Integer>();
		for(Type moreType: moreFieldsCount.keySet()) {
			int moreCount = moreFieldsCount.get(moreType);
			int lessCount = 0;
			
			if(lessFieldsCount.containsKey(moreType)) {
				if(!ComparisonClassRemapper.isStandardLibraryClass(classLoader,moreType.getInternalName())) {
					continue;
				}
				else {
					lessCount = 0;
				}
			}
			else {
				lessCount = lessFieldsCount.get(moreType);
			}
			
			if(lessCount <= moreCount) {
				scores.add(lessCount * ComparisonClassRemapper.SCORE_MAX / moreCount);
			}
			else {
				scores.add(moreCount * ComparisonClassRemapper.SCORE_MAX / lessCount);
			}
		}
		
		for(Integer s: scores) {
			score += s;
		}
		
		if(scores.size() == 0) {
			score = ComparisonClassRemapper.SCORE_UNAVAILABLE;
		}
		else {
			score /= scores.size();
		}
		
		if(lessSelfReferences == moreSelfReferences)
			return score;
		else {
			return ComparisonClassRemapper.SCORE_MIN;
		}
	}
	
	void addScore(List<Integer> scores, int score) {
		if(score != ComparisonClassRemapper.SCORE_UNAVAILABLE) {
			scores.add(score);
		}
	}
	
	void evaluateScore(ClassNode node1, ClassNode node2) {
		score = ComparisonClassRemapper.SCORE_MIN;
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
		
		int byteFieldsScore = ComparisonClassRemapper.SCORE_UNAVAILABLE;
		int charFieldsScore = ComparisonClassRemapper.SCORE_UNAVAILABLE;
		int shortFieldsScore = ComparisonClassRemapper.SCORE_UNAVAILABLE;
		int intFieldsScore = ComparisonClassRemapper.SCORE_UNAVAILABLE;
		int longFieldsScore = ComparisonClassRemapper.SCORE_UNAVAILABLE;
		int floatFieldsScore = ComparisonClassRemapper.SCORE_UNAVAILABLE;
		int doubleFieldsScore = ComparisonClassRemapper.SCORE_UNAVAILABLE;
		int objectFieldsScore = ComparisonClassRemapper.SCORE_UNAVAILABLE;
		if(more.fields.size() == 0) {
			if(less.fields.size() == 0) {
				numFieldsScore = ComparisonClassRemapper.SCORE_UNAVAILABLE;
			}
			else {
				numFieldsScore = ComparisonClassRemapper.SCORE_MIN;
				byteFieldsScore = ComparisonClassRemapper.SCORE_MIN;
				charFieldsScore = ComparisonClassRemapper.SCORE_MIN;
				shortFieldsScore = ComparisonClassRemapper.SCORE_MIN;
				intFieldsScore = ComparisonClassRemapper.SCORE_MIN;
				longFieldsScore = ComparisonClassRemapper.SCORE_MIN;
				floatFieldsScore = ComparisonClassRemapper.SCORE_MIN;
				doubleFieldsScore = ComparisonClassRemapper.SCORE_MIN;
				objectFieldsScore = ComparisonClassRemapper.SCORE_MIN;
			}
		}
		else {
			numFieldsScore = (less.fields.size() * ComparisonClassRemapper.SCORE_MAX) / more.fields.size();
			byteFieldsScore = getFieldsScore(less,more,Type.BYTE_TYPE);
			charFieldsScore = getFieldsScore(less,more,Type.CHAR_TYPE);
			shortFieldsScore = getFieldsScore(less,more,Type.SHORT_TYPE);
			intFieldsScore = getFieldsScore(less,more,Type.INT_TYPE);
			longFieldsScore = getFieldsScore(less,more,Type.LONG_TYPE);
			floatFieldsScore = getFieldsScore(less,more,Type.FLOAT_TYPE);
			doubleFieldsScore = getFieldsScore(less,more,Type.DOUBLE_TYPE);
			objectFieldsScore = getObjectFieldsScore(less,more);
			
		}
		
		addScore(scores,numFieldsScore);
		addScore(scores,byteFieldsScore);
		addScore(scores,charFieldsScore);
		addScore(scores,shortFieldsScore);
		addScore(scores,intFieldsScore);
		addScore(scores,longFieldsScore);
		addScore(scores,floatFieldsScore);
		addScore(scores,doubleFieldsScore);
		addScore(scores,objectFieldsScore);
		
		for(Integer s: scores) {
			score += s;
		}
	
		if(scores.size() > 0) {
			score /= scores.size();
		}
		else {
			score = ComparisonClassRemapper.SCORE_MIN;
		}
		
	}
}