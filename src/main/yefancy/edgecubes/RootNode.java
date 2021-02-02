package yefancy.edgecubes;

public class RootNode extends Node{
	private RootNode nextNode = null;
	public static int linkedListCount = 0;
	public RootNode(int dim) {
		super(0L, dim, 0);
	}
	
	private RootNode(long label) {
		super(label);
	}
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		if(this.nextNode != null)
    		linkedListCount--;
		super.finalize();
	}
	
	@Override
	public IContent getNextNode() {
		return nextNode;
	}
	
	@Override
	public void setNextNode(IContent nextNode) {
		if(this.nextNode == null && nextNode !=null)
			linkedListCount++;
		if(this.nextNode != null && nextNode ==null)
			linkedListCount--;
		this.nextNode = (RootNode)nextNode;
	}
	
	@Override
	public void insertNextNode(IContent nextNode) {
		if (nextNode.getNextNode() == null)
			nextNode.setNextNode(this.nextNode);
		setNextNode(nextNode);
	}
	
	@Override
	public IContent shallowCopy() {
		// TODO fix
		RootNode copy = new RootNode(this.label);
		copy.setSharedContentWithNode(this);
		for (Node child : children) {
			copy.addChildNode(child, true);
		}
		copy.setNextNode(nextNode);
		return copy;
	}
}
