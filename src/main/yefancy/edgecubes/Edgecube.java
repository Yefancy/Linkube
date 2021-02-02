package yefancy.edgecubes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import yefancy.cube.interfaces.IAction;
import yefancy.cube.interfaces.ICube;
import yefancy.cube.interfaces.IDataPoint;

public class Edgecube implements ICube<List<ACuboid>> {
	private final RootNode root;
	private final Class<ACuboid> cuboidClass;
	private int[] aggSplit;
//	boolean flag1 = false;
//	boolean flag2 = false;

	public <C extends ACuboid> Edgecube(Class<C> clazz, int[] aggSplit) {
		root = new RootNode(0);
		this.cuboidClass = (Class<ACuboid>) clazz;
		this.aggSplit = aggSplit;
	}

	public void insert(IDataPoint dataPoint) {
		Set<IContent> updatedNodes = new HashSet<>();
		add(root, dataPoint, 0, updatedNodes);
	}

	public RootNode getRoot() {
		return root;
	}

	private boolean checkChain(List<Long> chain) {
		long[] ary = { 0, 3, 0, 0, 2, 3, 2, 0, 2, 3, 2, 1 };
		for (int i = 0; i < ary.length; i++)
			if (ary[i] != chain.get(i))
				return false;
		return true;
	}

	private void printChain(List<Long> chain) {
		for (int i = 0; i < chain.size() - 2; i++)
			System.out.print(chain.get(i) + "->");
		System.out.println(chain.get(chain.size() - 1));
	}

	private void add(Node root, IDataPoint dataPoint, int dimension, Set<IContent> updatedNodes) {
		List<Long> chain = dataPoint.getLabels().get(dimension);
		List<Node> nodePathStack = trailProperPath(root, chain);
//		if (dimension == 0)
//			if (checkChain(chain)) {
//				printChain(chain);
//				flag1 = true;
//			}
		boolean isCuboid = dimension == dataPoint.getDimension() - 1;
		for (int i = nodePathStack.size() - 1; i >= 0; i--) {
			Node pathNode = nodePathStack.get(i);
			////////////////////////////////////////////////
			boolean update = false;
			int offset = (nodePathStack.size() - 1 - i) % aggSplit[dimension];
			if (offset == 0) {
				if (pathNode.getChildrenSize() == 1 && pathNode.getChildrenCount() == 1
						//&& pathNode.getContent() == null) {
						) {
					pathNode.setSharedContentWithNode(pathNode.getChild(0));
					remakeChain(nodePathStack, i, pathNode.getContent(), dimension);
				} else if (pathNode.getContent() == null) {
					IContent tmpRN = null;
					if (isCuboid)
						try {
							tmpRN = cuboidClass.newInstance();
//							if(flag1 && !flag2) {
//								flag2 = true;
//								((Cuboid)tmpRN).debug = true;
//							}
							pathNode.setContent(false, tmpRN);
						} catch (InstantiationException | IllegalAccessException e) {
							e.printStackTrace();
						}
					else {
						tmpRN = new RootNode(dimension + 1);
						pathNode.setContent(false, tmpRN);
					}
					remakeChain(nodePathStack, i, tmpRN, dimension);
					update = true;
				} else if (pathNode.isContentShared() && !updatedNodes.contains(pathNode.getContent())) {
					IContent origin = pathNode.getContent();
					if(pathNode.getChildrenSize() > 0)
						origin = pathNode.getChild(0).getContent();
					IContent copy = origin.shallowCopy();
					if (pathNode.getChildrenSize() > 0) {
						for (int j = pathNode.getChild(0).getCount(); j > 1; j--)
							origin = origin.getNextNode();
						copy.setNextNode(origin.getNextNode());
						origin.setNextNode(null);
					}
					pathNode.setContent(false, copy);
					remakeChain(nodePathStack, i, copy, dimension);
					update = true;
				} else if (!pathNode.isContentShared())
					update = true;
			} else if (pathNode.getChildrenSize() >= 1) {
				pathNode.updateCount();
				pathNode.setSharedContentWithNode(pathNode.getChild(0));
			}
			//////////////////////////////////////////////////
			if (update) {
				if (isCuboid) {
					ACuboid cuboid = pathNode.getContent(cuboidClass);
					cuboid.insert(dataPoint);
				} else {
					add(pathNode.getContent(Node.class), dataPoint, dimension + 1, updatedNodes);
				}
				updatedNodes.add(pathNode.getContent());
			}
		}
	}

	private void remakeChain(List<Node> nodePathStack, int i, IContent content, int dimension) {
		for (int j = i - 1; j >= 0 && j > i - aggSplit[dimension]; j--) {
			Node tmpNode = nodePathStack.get(j);
			Node child = nodePathStack.get(j + 1);
			int index = tmpNode.getChildIndex(child);
			if (index < 1)
				continue;
			if (tmpNode.isChildIndexShared(index - 1)) {
				List<Node> stack = new ArrayList<Node>();
				for (int k = 0; k <= j; k++)
					stack.add(nodePathStack.get(k));
				Node copy = (Node) tmpNode.getChild(index - 1).shallowCopy();
				tmpNode.replaceChild(copy);
				stack.add(copy);
				tmpNode = copy;
				for (int k = j + 1; k < i; k++) {
					copy = (Node) tmpNode.getChild(tmpNode.getChildrenSize() - 1).shallowCopy();
					tmpNode.replaceChild(copy);
					stack.add(copy);
					tmpNode = copy;
				}
				IContent copyContent = tmpNode.getContent().shallowCopy();
				copyContent.insertNextNode(content);
				tmpNode.setContent(false, copyContent);
				remakeChain(stack, i, copyContent, dimension);
				break;
			}
			tmpNode = tmpNode.getChild(index - 1);
			for (int k = j + 1; k < i; k++)
				tmpNode = tmpNode.getChild(tmpNode.getChildrenSize() - 1);
			tmpNode.getContent().insertNextNode(content);
			break;
		}
	}

	private List<Node> trailProperPath(Node root, List<Long> chain) {
		List<Node> stack = new ArrayList<>();
		stack.add(root);
		Node node = root;
		for (Long label : chain) {
			Node child = node.getChild(label);
			if (child == null) {
				child = node.newProperChild(label);
			} else if (node.isChildShared(label)) {
				Node copy = (Node) child.shallowCopy();
				node.replaceChild(copy);
				child = copy;
			}
			stack.add(child);
			node = child;
		}
		return stack;
	}

	@Override
	public void queryHandler(List<List<Long>> query, IAction<List<ACuboid>> action) {
		action.invoke(query(query));
	}

	@Override
	public List<ACuboid> query(List<List<Long>> query) {
		List<ACuboid> list = new ArrayList<ACuboid>();
		return queryDFS(root, list, query, 0);
	}

	private List<ACuboid> queryDFS(RootNode rootNode, List<ACuboid> list, List<List<Long>> query, int dimension) {
		if (dimension >= query.size())
			return list;
		List<Long> chain = query.get(dimension);
		Node node = rootNode;
		for (int i = 0; i < chain.size(); i++) {
			node = node.getChild(chain.get(i)); //== null?node.getChild(0):node.getChild(chain.get(i));
			if (node == null)
				return list;
		}
		if (node == null) {
			for (Long deo : query.get(0))
				System.out.print(deo + "->");
		}
		IContent content = node.getContent();
		if (dimension == query.size() - 1)
			for (int count = 0; count < node.getCount(); count++) {
				list.add((ACuboid) content);
				content = content.getNextNode();
			}
		else
			for (int count = 0; count < node.getCount(); count++) {
				queryDFS((RootNode) content, list, query, dimension + 1);
				content = content.getNextNode();
			}
		return list;
	}
}
