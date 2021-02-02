package yefancy.edgecubes;

import yefancy.cube.interfaces.IDataPoint;

public abstract class ACuboid implements IContent{
	private ACuboid nextACuboid;
	public static int linkedListCount = 0;
	public static long cuboidCount = 0;
	public abstract void insert(IDataPoint dataPoint);
	
	public ACuboid() {
		cuboidCount++;
	}
	
	public IContent getNextNode() {
		return nextACuboid;
	}
	
	public void setNextNode(IContent nextNode) {
		if(nextACuboid == null && nextNode !=null)
			linkedListCount++;
		if(nextACuboid != null && nextNode ==null)
			linkedListCount--;
		this.nextACuboid = (ACuboid)nextNode;
	}
	
	public void insertNextNode(IContent nextNode) {
		if (nextNode == null)
			return;
		if (nextNode.getNextNode() == null)
			nextNode.setNextNode(this.nextACuboid);
		setNextNode(nextNode);
	}
	
	public abstract ACuboid aggregateAdd(ACuboid cuboid);
	
    @Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
    	cuboidCount--;
    	if(nextACuboid != null)
    		linkedListCount--;
		super.finalize();
	}
}
