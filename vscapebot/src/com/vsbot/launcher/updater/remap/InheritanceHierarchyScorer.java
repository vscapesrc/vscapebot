package com.vsbot.launcher.updater.remap;

import java.util.LinkedList;

import org.objectweb.asm.tree.ClassNode;

public class InheritanceHierarchyScorer extends ClassScorer {

	ComparisonClassRemapper remapper;
	InheritanceHierarchyScorer(ComparisonClassRemapper remapper) {
		this.remapper = remapper;
	}

	private String[] inheritancePredecessors(ClassNode node) {
		LinkedList<String> classes = new LinkedList<String>();
		
		String superClass = node.name;
		while((superClass = remapper.getSuperClassName(superClass)).equals("java/lang/Object") != true) {
			classes.add(superClass);
		}
		
		return classes.toArray(new String[0]);
	}

	@Override
	void evaluateScore(ClassNode node1, ClassNode node2) {
		String[] preds1 = inheritancePredecessors(node1);
		String[] preds2 = inheritancePredecessors(node2);
		
		if(preds1.length != preds2.length) {
			score = ComparisonClassRemapper.SCORE_MIN;
		}
		else {
			if(preds1.length == 0) {
				score = ComparisonClassRemapper.SCORE_UNAVAILABLE;
			}
			else {
			int length = preds1.length;
			int matches = 0;
			
			for(int i = 0; i < length; i++) {
				if(preds1[i].equals(preds2[i]) || ((ComparisonClassRemapper.isStandardLibraryClass(this.getClass().getClassLoader(), preds1[i]) == false) && (ComparisonClassRemapper.isStandardLibraryClass(this.getClass().getClassLoader(), preds2[i]) == false))) {
					matches++;
				}
			}
		
			score = (matches * (ComparisonClassRemapper.SCORE_MAX)) / length;
			}
		}
	}

}
