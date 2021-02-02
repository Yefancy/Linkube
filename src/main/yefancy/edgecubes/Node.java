package yefancy.edgecubes;

import java.util.ArrayList;
import java.util.List;

public class Node implements IContent {
	private static final int CONTENT_SHARED_BIT_INDEX = 0;
	public static long nodeCount = 0;
	public final long label;
	protected List<Node> children = new ArrayList<Node>();
	private IContent content;
	private int count = 1;
	private long sharedLinkBitSet = 0L;

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		nodeCount--;
		super.finalize();
	}

	public Node(long user_label, int dim, int level) {
		this.label = encodeLabel(user_label, dim, level);
		nodeCount++;
	}

	protected Node(long label) {
		this.label = label;
	}

	public static long encodeLabel(long user_label, int dim, int level) {
		if (dim > 99 || level > 99)
			throw new IllegalArgumentException("dim and level should smaller than 99!");
		return user_label * 10000 + dim * 100 + level;
	}

	public long decodeLabel() {
		return this.label / 10000;
	}

	public int decodeDim() {
		return (int) (this.label % 10000 / 100);
	}

	public int decodeLevel() {
		return (int) (this.label % 100);
	}

	@Override
	public String toString() {
		return String.format("Label=%d, Dim=%d, Level=%d", decodeLabel(), decodeDim(), decodeLevel());
	}

	/**
	 * Creates a new child link to node keyed on label.
	 */
	public Node newProperChild(long user_label) {
		Node childNode = new Node(user_label, this.decodeDim(), this.decodeLevel() + 1);
		addChildNode(childNode, false);
		return childNode;
	}

	protected void addChildNode(Node childNode, boolean shared) {
		children.add(childNode);
		setLinkShared(children.size(), shared);
	}

	public int getChildrenSize() {
		return children.size();
	}

	public boolean isChildShared(long user_label) {
		int childIndex = getChildIndex(user_label);
		return isLinkShared(childIndex + 1);
	}

	public boolean isChildIndexShared(int childIndex) {
		return isLinkShared(childIndex + 1);
	}

	public int getChildIndex(Node node) {
		for (int i = 0; i < children.size(); i++)
			if (node == children.get(i))
				return i;
		return -1;
	}

	private int getChildIndex(long user_label) {
		for (int i = 0; i < children.size(); i++) {
			Node node = children.get(i);
			if (node.decodeLabel() == user_label) {
				return i;
			}
		}
		throw new IllegalStateException("Cannot find child not with label: " + user_label);
	}

	public Node getChild(long user_label) {
		for (Node child : children) {
			if (child.decodeLabel() == user_label) {
				return child;
			}
		}
		return null;
	}

	public Node getChild(int index) {
		if (index >= children.size())
			return null;
		return children.get(index);
	}

	public void replaceChild(Node newChild) {
		int childIndex = getChildIndex(newChild.decodeLabel());
		children.set(childIndex, newChild);
		setLinkShared(childIndex + 1, false);
	}

	/**
	 * Convenience method to create a shared content link to the content in given
	 * node.
	 */
	public void setSharedContentWithNode(Node node) {
		IContent nodeContent = node.getContent();
		setContent(true, nodeContent);
	}

	public void setContent(boolean shared, IContent content) {
		this.content = content;
		setLinkShared(CONTENT_SHARED_BIT_INDEX, shared);
	}

	public boolean isContentShared() {
		if (content == null) {
			throw new IllegalStateException("There is no content");
		}
		return isLinkShared(CONTENT_SHARED_BIT_INDEX);
	}

	public IContent getContent() {
		return content;
	}

	public <C extends IContent> C getContent(Class<C> clazz) {
		return (C) getContent();
	}

	@Override
	public IContent shallowCopy() {
		// TODO fix
		Node copy = new Node(label);
		copy.setSharedContentWithNode(this);
		for (Node child : children) {
			copy.addChildNode(child, true);
		}
		return copy;
	}

	private boolean isLinkShared(int linkIndex) {
		validateLinkIndex(linkIndex);
		return (sharedLinkBitSet & (1L << linkIndex)) != 0;
	}

	private void setLinkShared(int linkIndex, boolean shared) {
		validateLinkIndex(linkIndex);
		if (shared) {
			sharedLinkBitSet |= (1L << linkIndex);
		} else {
			sharedLinkBitSet &= ~(1L << linkIndex);
		}
	}

	private void validateLinkIndex(int linkIndex) {
		if (linkIndex > 32) {
			throw new IllegalArgumentException("Link index cannot be larger than 32");
		}
	}

	public int getCount() {
		return count;
	}

	public void updateCount() {
		count = getChildrenCount();
		count = count > 1 ? count : 1;
	}

	public void updateCount(int count) {
		this.count = count;
	}

	public int getChildrenCount() {
		int total = 0;
		for (Node child : children)
			total += child.getCount();
		return total;
	}
	
	public boolean isChildrenAggregates() {
		if(!children.isEmpty())
			if(children.get(0).getContent().getNextNode() == null)
				return true;
		return false;
	}

	@Override
	public IContent getNextNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNextNode(IContent nextContent) {
		// TODO Auto-generated method stub
	}

	@Override
	public void insertNextNode(IContent nextContent) {
		// TODO Auto-generated method stub
	}
}
