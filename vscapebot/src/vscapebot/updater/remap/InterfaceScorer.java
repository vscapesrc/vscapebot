package vscapebot.updater.remap;

import org.objectweb.asm.tree.ClassNode;

class InterfaceScorer extends ClassScorer {

	@Override
	void evaluateScore(ClassNode node1, ClassNode node2) {
		score = ComparisonRemapper.SCORE_MIN;
		
		ClassLoader classLoader = this.getClass().getClassLoader();
		
		if(node1.interfaces.size() == 0 && node2.interfaces.size() == 0) {
			/* No interfaces for either class*/
			score = ComparisonRemapper.SCORE_UNAVAILABLE;
		}
		else if((node1.interfaces.size() == 0 && node2.interfaces.size() > 0) ||
				(node1.interfaces.size() > 0 && node2.interfaces.size()== 0)) {
			/* one class has at least one interface and the other has none */
			score = ComparisonRemapper.SCORE_MIN;
		}
		else if(node1.interfaces.size() == node2.interfaces.size()) {
			score = ComparisonRemapper.SCORE_MIN;
			int matches = 0;
			
			for(Object iface1: node1.interfaces) {
				for(Object iface2: node2.interfaces) {
					if(((String)iface1).equals((String)iface2)) {
						matches++;
					}
				}
			}
			
			
			
			if(matches > 0) {
				score = (ComparisonRemapper.SCORE_MAX * matches) / node1.interfaces.size();
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
					score = (numer * ComparisonRemapper.SCORE_MAX) / denom;
				}
				else {
					score = ComparisonRemapper.SCORE_UNAVAILABLE;
				}
			}
		}
		else if(node1.interfaces.size() > node2.interfaces.size() || node1.interfaces.size() < node2.interfaces.size()) {
			score = ComparisonRemapper.SCORE_MIN;
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
				score = (ComparisonRemapper.SCORE_MAX * matches) / more.interfaces.size();
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
					score = (numer * ComparisonRemapper.SCORE_MAX) / denom;
				}
				else {
					score = ComparisonRemapper.SCORE_UNAVAILABLE;
				}
			}
		}
	}
}