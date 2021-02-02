package LinkedListTest;

import java.util.ArrayList;
import java.util.List;

import yefancy.cube.interfaces.IDataPoint;

public class Tree {
	treeNode root;
	treeNode firstNode = null;
	treeNode lastNode = null;
	public Tree() {
		root = new treeNode(-1);
	}
	
	public void insert(IDataPoint dp) {
		dfsInsert(root, 0, dp);
	}
	
	public long testTreeTime() {
		long st = System.currentTimeMillis();
		for(int i =0;i<10000;i++) {
			dfsTest(root);
		}
		return(System.currentTimeMillis() - st);
	}
	
	public long testLinkTime() {
		long st = System.currentTimeMillis();
		for(int i =0;i<10000;i++) {
			treeNode node = firstNode;
			while(node.nextNode != null)
				node = node.nextNode;
		}		
		return(System.currentTimeMillis() - st);
	}
	
	private void dfsTest(treeNode root) {
		for(treeNode child : root.children)
			dfsTest(child);
	}
	
	private void dfsInsert(treeNode root, int level, IDataPoint dp) {
		if(level == dp.getLabels().get(0).size()){
			if(root.nextNode == null){
				if(lastNode == null) {
					lastNode = root;
					firstNode = root;
				}
				else if(lastNode != root){
					assert lastNode.nextNode == null;
					lastNode.nextNode = root;
					lastNode = root;
				}				
			}
			root.dp = dp;
			return;
		}
		long label = dp.getLabels().get(0).get(level);
		for(treeNode child : root.children) {
			if(child.label == label) {
				dfsInsert(child, level+1,dp);
				return;
			}			
		}
		treeNode child = new treeNode(label);
		root.children.add(child);
		dfsInsert(child, level + 1,dp);
	}
}

class treeNode{
	public List<treeNode> children;
	public IDataPoint dp;
	public long label;
	public treeNode nextNode = null;
	
	public treeNode(long label) {
		this.label = label;
		children = new ArrayList<treeNode>();
		dp = null;
	}
}
