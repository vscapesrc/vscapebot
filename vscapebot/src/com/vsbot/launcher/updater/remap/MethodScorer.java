package com.vsbot.launcher.updater.remap;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;

import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class MethodScorer extends ClassScorer {
	
	boolean methodDescriptorsAreSimilar(MethodNode method1, MethodNode method2) {
		Type type1 = Type.getType(method1.desc);
		Type type2 = Type.getType(method2.desc);
		
		HashMap<Type,Integer> types1;
		HashMap<Type,Integer> types2;
		
		int count;
		
		int flags = Opcodes.ACC_FINAL | Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE;
		if((method1.access&flags) != (method2.access&flags)) {
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
	
	Map<Integer,Integer> getMethodInsnCounts(MethodNode method) {
		int type;
		ListIterator<AbstractInsnNode> li = (ListIterator<AbstractInsnNode>)method.instructions.iterator();
		AbstractInsnNode insn;
		Map<Integer,Integer> counts = new HashMap<Integer,Integer>();
		
		while(li.hasNext()) {
			insn = li.next();
			type = insn.getType();
			
			if(type != AbstractInsnNode.FRAME && type != AbstractInsnNode.LABEL && type != AbstractInsnNode.LINE) {
				if(counts.containsKey(type)) {
					counts.put(type, counts.get(type)+1);
				}
				else {
					counts.put(type, 1);
				}
			}
		}
		
		return counts;
	}
	
	@SuppressWarnings("unchecked")
	boolean methodCodeIsSimilar(MethodNode method1, MethodNode method2) {
		int sizeScore;
		
		int size1 = method1.instructions.size();
		int size2 = method2.instructions.size();
		
		if(size1 == size2) {
			if(size1 == 0) {
				return true;
			}
			else {
				sizeScore = ComparisonClassRemapper.SCORE_MAX;
			}
		}
		else if(size1 < size2) {
			sizeScore = (size1 * ComparisonClassRemapper.SCORE_MAX) / size2;
		}
		else {
			sizeScore = (size2 * ComparisonClassRemapper.SCORE_MAX) / size1;
		}
		
		Map<Integer, Integer> counts1, counts2;
		
		counts1 = getMethodInsnCounts(method1);
		counts2 = getMethodInsnCounts(method2);
		
		List<Integer> insnScores = new LinkedList<Integer>();
		
		int count1,count2;
		for(Integer type: counts1.keySet()) {
			if(counts2.containsKey(type)) {
				count1 = counts1.get(type);
				count2 = counts2.get(type);
				
				if(count1 <= count2) {
					insnScores.add((count1 * ComparisonClassRemapper.SCORE_MAX) / count2);
				}
				else {
					insnScores.add((count2 * ComparisonClassRemapper.SCORE_MAX) / count1);
				}
			}
			else {
				insnScores.add(ComparisonClassRemapper.SCORE_MIN);
			}
		}
		
		int insnScore = ComparisonClassRemapper.SCORE_MIN;
		for(Integer s: insnScores) {
			insnScore += s;
		}
		
		if(insnScores.size() > 0) {
			insnScore /= insnScores.size();
		}
		else {
			insnScore = ComparisonClassRemapper.SCORE_MIN;
		}
		
		int overallScore = (sizeScore + insnScore) / 2;
		
		return overallScore > ((ComparisonClassRemapper.SCORE_MAX * 2) / 3);
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
			numMethodsScore = ComparisonClassRemapper.SCORE_MAX;
		}
		else {
			numMethodsScore = (ComparisonClassRemapper.SCORE_MAX * less.methods.size()) / more.methods.size();
		}
		
		int matches = 0;
		
		for(Object method1: less.methods) {
			for(Object method2: more.methods) {
				if(methodDescriptorsAreSimilar((MethodNode)method1,(MethodNode)method2) && methodCodeIsSimilar((MethodNode)method1,(MethodNode)method2)) {
					matches++;
					break;
				}
			}
		}
		
		if(less.methods.size() > 0) {
			score = (((matches * ComparisonClassRemapper.SCORE_MAX) / more.methods.size()) + numMethodsScore) / 2;
		}
		else {
			score = ComparisonClassRemapper.SCORE_UNAVAILABLE;
		}
	}
}
