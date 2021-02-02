package yefancy.linkube;

import net.yadan.banana.map.HashMap;
import net.yadan.banana.map.IHashMap;

public class HashTree {
	IHashMap map;
	
	public HashTree() {
		map = new HashMap(100, 5, 0.75, 2.0);
	}
	
	public HashTree(int maxBlocks, int blockSize, double loadFactor, double growthFactor) {
		map = new HashMap(maxBlocks, blockSize, loadFactor, growthFactor);
	}
	
	
}
