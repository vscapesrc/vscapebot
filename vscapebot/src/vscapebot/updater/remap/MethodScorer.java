package vscapebot.updater.remap;

import org.objectweb.asm.tree.ClassNode;

import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;

import org.objectweb.asm.Type;

public class MethodScorer extends ClassScorer {
	
	boolean methodDescriptorsAreSimilar(MethodNode method1, MethodNode method2) {
		Type type1 = Type.getType(method1.desc);
		Type type2 = Type.getType(method2.desc);
		
		HashMap<Type,Integer> types1;
		HashMap<Type,Integer> types2;
		
		int count;
		
		if(method1.access != method2.access) {
			return false;
		}
		
		if(type1.getArgumentTypes().length == type2.getArgumentTypes().length) {
			if(type1.getReturnType() == type2.getReturnType()) {
				types1 = new HashMap<Type,Integer>();
				types2 = new HashMap<Type,Integer>();
				
				for(Type type: type1.getArgumentTypes()) {
					count = 1;
					if(types1.containsKey(type)) {
						count = types1.get(type);
					}
					types1.put(type,count);
				}
				
				for(Type type: type2.getArgumentTypes()) {
					count = 1;
					if(types2.containsKey(type)) {
						count = types2.get(type);
					}
					types2.put(type,count);
				}
				
				for(Type type: types1.keySet()) {
					if(!types2.containsKey(type) || (types1.get(type) != types2.get(type))) {
						return false;
					}
				}
				
				return true;
			}
		}

		return false;
	}

	@Override
	void evaluateScore(ClassNode node1, ClassNode node2) {
		int numMethodsScore;
		
		ClassNode less, more;
		
		if(node1.methods.size() <= node2.methods.size()) {
			less = node1;
			more = node2;
		}
		else {
			less = node2;
			more = node1;
		}
		
		if(less.methods.size() == more.methods.size()) {
			numMethodsScore = ComparisonRemapper.SCORE_MAX;
		}
		else {
			numMethodsScore = (ComparisonRemapper.SCORE_MAX * less.methods.size()) / more.methods.size();
		}
		
		int matches = 0;
		
		for(Object method1: less.methods) {
			for(Object method2: more.methods) {
				if(methodDescriptorsAreSimilar((MethodNode)method1,(MethodNode)method2)) {
					matches++;
					break;
				}
			}
		}
		
		if(less.methods.size() > 0) {
			score = (matches * ComparisonRemapper.SCORE_MAX) / more.methods.size();
		}
		else {
			score = ComparisonRemapper.SCORE_UNAVAILABLE;
		}
	}
}
