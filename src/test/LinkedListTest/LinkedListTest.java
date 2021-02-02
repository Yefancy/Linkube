package LinkedListTest;

import yefancy.cube.interfaces.IDataPoint;

public class LinkedListTest {
	public static void main(String[] args) throws Exception {
		for(int i = 5; i < 25; i++) {
			Tree tree = new Tree();
			for(int j = 0; j <= 10000; j++) {
				IDataPoint dp = new GaussianDataPoint(i);
				tree.insert(dp);
			}
			System.out.println(String.format("k=%d\t%d\t%d", i, tree.testTreeTime(), tree.testLinkTime()));
		}
	}
	
}
