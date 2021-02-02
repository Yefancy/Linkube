package LinkedListTest;

import java.util.ArrayList;

import yefancy.cube.interfaces.IDataPoint;

public class LinkedList {
	listNode root;
	listNode last;
	public LinkedList() {
		root = new listNode(null);
		last = root;
	}
	
	public void insert(IDataPoint dp) {
		last = last.setNextNode(new listNode(dp));
	}
	
	public long testTime() {
		long st = System.currentTimeMillis();
		listNode node = root;
		while(node.nextNode != null)
			node = node.nextNode;
		return(System.currentTimeMillis() - st);
	}
}

class listNode{
	listNode nextNode;
	IDataPoint dp;
	public listNode(IDataPoint dp) {
		this.dp = dp;
		nextNode = null;
	}
	
	public listNode setNextNode(listNode node) {
		nextNode = node;
		return node;
	}
}
