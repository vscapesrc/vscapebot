package vscapebot.updater.remap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import vscapebot.ClientClass;

public class CodeReferenceScorer extends ClassScorer {
	
	ClientClass[] clientClasses, refactoredClasses;
	Map<String,ClassNode> clientMap;
	Map<String,ClassNode> refactorMap;
	
	
	Map<String,Integer> clientFieldRefs;
	Map<String,Integer> clientMethodRefs;
	Map<String,Integer> refactorFieldRefs;
	Map<String,Integer> refactorMethodRefs;
	
	CodeReferenceScorer(ClientClass[] clientClasses, ClientClass[] refactoredClasses) {
		this.clientClasses = clientClasses;
		this.refactoredClasses = refactoredClasses;
		
		
		Map<String,Map<String,Integer>> clientFieldRefMap;
		Map<String,Map<String,Integer>> clientMethodRefMap;
		Map<String,Map<String,Integer>> refactorFieldRefMap;
		Map<String,Map<String,Integer>> refactorMethodRefMap;
		
		clientFieldRefMap = new HashMap<String,Map<String,Integer>>();
		computeFieldReferenceCounts(clientClasses, clientFieldRefMap);
		clientFieldRefs = new HashMap<String,Integer>();
		clientMethodRefMap = new HashMap<String,Map<String,Integer>>();
		computeMethodReferenceCounts(clientClasses, clientMethodRefMap);
		clientMethodRefs = new HashMap<String,Integer>();
		
		clientMap = new HashMap<String,ClassNode>();
		for(ClientClass cc: clientClasses) {
			clientMap.put(cc.getName(), cc.getNode());
			clientFieldRefs.put(cc.getName(), fieldReferenceCount(cc.getNode(),clientFieldRefMap));
			clientMethodRefs.put(cc.getName(), methodReferenceCount(cc.getNode(),clientMethodRefMap));
		}
		
		refactorFieldRefMap = new HashMap<String,Map<String,Integer>>();
		computeFieldReferenceCounts(refactoredClasses, refactorFieldRefMap);
		refactorFieldRefs = new HashMap<String,Integer>();
		refactorMethodRefMap = new HashMap<String,Map<String,Integer>>();
		computeMethodReferenceCounts(refactoredClasses, refactorMethodRefMap);
		refactorMethodRefs = new HashMap<String,Integer>();
		
		refactorMap = new HashMap<String,ClassNode>();
		for(ClientClass cc: refactoredClasses) {
			refactorMap.put(cc.getName(), cc.getNode());
			refactorFieldRefs.put(cc.getName(), fieldReferenceCount(cc.getNode(),refactorFieldRefMap));
			refactorMethodRefs.put(cc.getName(), methodReferenceCount(cc.getNode(),refactorMethodRefMap));
		}
	}
	
	static void computeFieldReferenceCounts(ClientClass[] classes, Map<String,Map<String,Integer>> map) {
		for(ClientClass cc: classes) {
			for(MethodNode m: (List<MethodNode>)cc.getNode().methods) {
				for(AbstractInsnNode insn: m.instructions.toArray()) {
					int type = insn.getType();
					if(type == AbstractInsnNode.FIELD_INSN) {
						FieldInsnNode fin = (FieldInsnNode)insn;
						Map<String,Integer> fieldMap;
						if(map.containsKey(fin.owner) == false) {
							fieldMap = new HashMap<String,Integer>();
							map.put(fin.owner, fieldMap);
						}
						else {
							fieldMap = map.get(fin.owner);
						}
						
						int refs = 1;
						if(fieldMap.containsKey(fin.name)) {
							refs = fieldMap.get(fin.name);
							refs++;
						}
						
						fieldMap.put(fin.name, refs);
					}
				}
			}
		}
	}
	
	static void computeMethodReferenceCounts(ClientClass[] classes, Map<String,Map<String,Integer>> map) {
		for(ClientClass cc: classes) {
			for(MethodNode m: (List<MethodNode>)cc.getNode().methods) {
				for(AbstractInsnNode insn: m.instructions.toArray()) {
					
					int type = insn.getType();
					if(type == AbstractInsnNode.METHOD_INSN) {
						MethodInsnNode min = (MethodInsnNode)insn;
						Map<String,Integer> methodMap;
						if(map.containsKey(min.owner) == false) {
							methodMap = new HashMap<String,Integer>();
							map.put(min.owner, methodMap);
						}
						else {
							methodMap = map.get(min.owner);
						}
						
						int refs = 1;
						if(methodMap.containsKey(min.name)) {
							refs = methodMap.get(min.name);
							refs++;
						}
						
						methodMap.put(min.name, refs);
					}
				}
			}
		}
	}
	
	
	static int fieldReferenceCount(ClassNode node, Map<String,Map<String,Integer>> map) {
		int refs = 0;
		Map<String,Integer> fieldMap;
		if(map.containsKey(node.name)) {
			fieldMap = map.get(node.name);
			for(FieldNode field: (List<FieldNode>)node.fields) {
				if(fieldMap.containsKey(field.name)) {
					refs += fieldMap.get(field.name);
				}
			}
		}
		
		return refs;
	}
	
	static int methodReferenceCount(ClassNode node, Map<String,Map<String,Integer>> map) {
		int refs = 0;
		
		Map<String,Integer> methodMap;
		if(map.containsKey(node.name)) {
			methodMap = map.get(node.name);
			for(MethodNode method: (List<MethodNode>)node.methods) {
				if(methodMap.containsKey(method.name)) {
					refs += methodMap.get(method.name);
				}
			}
		}
		
		return refs;
	}

	@Override
	void evaluateScore(ClassNode node1, ClassNode node2) {
		int fieldRefScore, methodRefScore = ComparisonRemapper.SCORE_UNAVAILABLE;
		
		ClassNode clientNode, refactorNode;
		
		if(clientMap.containsKey(node1.name) && refactorMap.containsKey(node2.name)) {
			clientNode = node1;
			refactorNode = node2;
		}
		else if(clientMap.containsKey(node2.name) && refactorMap.containsKey(node1.name)) {
			clientNode = node2;
			refactorNode = node1;
		}
		else {
			score = ComparisonRemapper.SCORE_UNAVAILABLE;
			return;
		}
		
		int less, more;
		
		int clientFieldRefs, refactorFieldRefs;
		clientFieldRefs = this.clientFieldRefs.get(clientNode.name);
		refactorFieldRefs = this.refactorFieldRefs.get(refactorNode.name);
		
		if(clientFieldRefs <= refactorFieldRefs) {
			less = clientFieldRefs;
			more = refactorFieldRefs;
		}
		else {
			more = clientFieldRefs;
			less = refactorFieldRefs;
		}
		
		if(more > 0) {
			fieldRefScore = (ComparisonRemapper.SCORE_MAX * less) / more;
		}
		else {
			fieldRefScore = ComparisonRemapper.SCORE_UNAVAILABLE;
		}
		
		int clientMethodRefs, refactorMethodRefs;
		clientMethodRefs = this.clientMethodRefs.get(clientNode.name);
		refactorMethodRefs = this.refactorMethodRefs.get(refactorNode.name);
		
		if(clientMethodRefs <= refactorMethodRefs) {
			less = clientMethodRefs;
			more = refactorMethodRefs;
		}
		else {
			more = clientMethodRefs;
			less = refactorMethodRefs;
		}
		
		if(more > 0) {
			methodRefScore = (ComparisonRemapper.SCORE_MAX * less) / more;
		}
		else {
			methodRefScore = ComparisonRemapper.SCORE_UNAVAILABLE;
		}
		
		if(fieldRefScore == ComparisonRemapper.SCORE_UNAVAILABLE) {
			score = methodRefScore;
		}
		else if(methodRefScore == ComparisonRemapper.SCORE_UNAVAILABLE) {
			score = fieldRefScore;
		}
		else {
			score = (methodRefScore + fieldRefScore) / 2;
		}

	}

}
