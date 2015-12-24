package com.vsbot.launcher.updater.remap;

import java.util.List;

import org.objectweb.asm.tree.ClassNode;

abstract class ClassScorer {
	int score;
	ClassScorer() {
		score = ComparisonClassRemapper.SCORE_UNAVAILABLE;
	}
	
	abstract void evaluateScore(ClassNode node1, ClassNode node2);
	final void addScore(List<Integer> scores) {
		if(scores != null) {
			if(score != ComparisonClassRemapper.SCORE_UNAVAILABLE) {
				scores.add(score);
			}
		}
	}
}