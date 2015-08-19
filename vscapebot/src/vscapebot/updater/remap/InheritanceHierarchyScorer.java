package vscapebot.updater.remap;

import java.util.LinkedList;

import org.objectweb.asm.tree.ClassNode;

public class InheritanceHierarchyScorer extends ClassScorer {

	ComparisonRemapper remapper;
	InheritanceHierarchyScorer(ComparisonRemapper remapper) {
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
			score = ComparisonRemapper.SCORE_MIN;
		}
		else {
			if(preds1.length == 0) {
				score = ComparisonRemapper.SCORE_UNAVAILABLE;
			}
			else {
			int length = preds1.length;
			int matches = 0;
			
			for(int i = 0; i < length; i++) {
				if(preds1[i].equals(preds2[i])) {
					matches++;
				}
			}
		
			score = (ComparisonRemapper.SCORE_MAX/2) + (matches * (ComparisonRemapper.SCORE_MAX/2)) / length;
			}
		}
	}

}
