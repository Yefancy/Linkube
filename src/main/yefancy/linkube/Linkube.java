//package yefancy.linkube;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
//import java.util.Stack;
//
//import yefancy.cube.interfaces.IAction;
//import yefancy.cube.interfaces.ICube;
//import yefancy.cube.interfaces.IDataPoint;
//
//public class Linkube<T> implements ICube<T>{
//	
//	private final RootNode root;
//	private final Class<ACuboid> cuboidClass;
//	private List<Set<Integer>> aggSplit;
//	private long queryCount = 0;
//
//	public List<Set<Integer>> getAggSplit() {
//		return aggSplit;
//	}
//
//	public Linkube(Class<T> clazz, List<Set<Integer>> aggSplit) {
//		root = new RootNode(0);
//		this.cuboidClass = (Class<ACuboid>) clazz;
//		this.aggSplit = aggSplit;
//	}
//	
//	public EdgecubeV2(Class<T> clazz, int[][] aggSplit) {
//		root = new RootNode(0);
//		this.cuboidClass = (Class<ACuboid>) clazz;
//		this.aggSplit = new ArrayList<Set<Integer>>();
//		for(int i = 0; i < aggSplit.length; i++) {
//			Set<Integer> set = new HashSet<Integer>();
//			for (int j = 0; j < aggSplit[i].length; j++)
//				set.add(aggSplit[i][j]);
//			this.aggSplit.add(set);
//		}
//	}
//	
//	public long UpdateCount() {
//		long last = queryCount;
//		queryCount = 0;
//		return last;
//	}
//
//	public void insert(IDataPoint dataPoint) {
//		Set<IContent> updatedNodes = new HashSet<>();
//		add(root, dataPoint, 0, updatedNodes);
//	}
//
//	public RootNode getRoot() {
//		return root;
//	}
//	
//	public void updateAggSplit(List<Set<Integer>> updateSplit) {
//		for (int d = 0; d < aggSplit.size(); d++) {
//			Set<Integer> origin = aggSplit.get(d);
//			Set<Integer> update = updateSplit.get(d);
//			List<Integer> toAdd = new ArrayList<Integer>();
//			List<Integer> toRemove = new ArrayList<Integer>();
//			Iterator<Integer> iter = update.iterator();
//			while (iter.hasNext()) {
//				int level = iter.next();
//				if(!origin.contains(level))
//					toAdd.add(level);			
//			}
//			
//			iter = origin.iterator();
//			while (iter.hasNext()) {
//				int level = iter.next();
//				if(!update.contains(level))
//					toRemove.add(level);
//			}
//			
//			for(int level : toAdd) {
//				origin.add(level);
//				updateLevel(d, level);
//			}
//			
//			for(int level : toRemove) {
//				origin.remove(level);
//				updateLevel(d, level);
//			}
//		}
//	}
//	
//	public void updateLevel(int dim, int level) {
//		Stack<Node> updatePath = new Stack<Node>();
//		dfsUpdate(updatePath, root, dim, level, 0);
//	}
//	
//	private void dfsUpdate(Stack<Node> updatePath, Node root, int dim, int level, int dimension) {
//		if(dimension != dim) {
//			//FIXME: need to finish dfs of aggregation.
//			if(root.getChildrenSize() == 0)
//				dfsUpdate(updatePath, (Node) root.getContent(), dim, level, dimension + 1);
//			for(Node child : root.children)
//				dfsUpdate(updatePath, child, dim, level, dimension);
//		} else {
//			if(root.decodeLevel() == level) {
//				updatePath.push(root);
//				updateAggregateState((Stack<Node>) updatePath.clone());
//				updatePath.pop();
//			}else {
//				updatePath.push(root);
//				for(Node child : root.children)
//					dfsUpdate(updatePath, child, dim, level, dimension);
//				updatePath.pop();
//			}
//		}
//	}
//	
//	private void updateAggregateState(Stack<Node> updatePath) {
//		Node node = updatePath.get(updatePath.size() - 1);
//		if (!needToAggregate(node)) {
//			if (!node.isContentShared()) {
//				IContent lastContent = null;
//				for(Node child : node.children){
//					IContent firstContent = child.getContent();
//					if (lastContent != null)
//						lastContent.setNextNode(firstContent);
//					for(int i = 1;i<child.getCount();i++)
//						firstContent = firstContent.getNextNode();
//					lastContent = firstContent;
//				}
//			}			
//			node.updateCount();
//			node.setSharedContentWithNode(node.getChild(0));
//			remakeChain((Stack<Node>) updatePath.clone(), node.getContent());
//		}
//		else if(needToAggregate(node)){
//			IContent content = node.getContent();
//			if(content instanceof ACuboid) {				
//				try {
//					if(node.getCount() <= 1) {
//						node.updateCount(1);
//						node.setContent(true, content);
//						remakeChain((Stack<Node>) updatePath.clone(), content);
//					}else {
//						content = cuboidClass.newInstance();
//						IContent firstContent = node.getContent();
//						((ACuboid) content).aggregateAdd((ACuboid) firstContent);
//						for(int i = 1;i<node.getCount();i++) {
//							firstContent = firstContent.getNextNode();
//							((ACuboid) content).aggregateAdd((ACuboid) firstContent);
//						}
//						node.updateCount(1);
//						node.setContent(false, content);
//						remakeChain((Stack<Node>) updatePath.clone(), content);
//					}					
//				} catch (InstantiationException | IllegalAccessException e) {
//					e.printStackTrace();
//				}
//			}
//			else if (node.getCount() > 1){
//				content = new RootNode(node.decodeDim() + 1);
//				IContent firstContent = node.getContent();
//				UDP udp = new UDP();
//				dfsCombine((RootNode) content, firstContent, udp);
//				for(int i = 1;i<node.getCount();i++) {
//					firstContent = firstContent.getNextNode();
//					udp = new UDP();
//					dfsCombine((RootNode) content, firstContent, udp);
//				}
//				node.updateCount(1);
//				node.setContent(false, content);
//				remakeChain((Stack<Node>) updatePath.clone(), content);
//			} else {
//				node.updateCount(1);
//				node.setContent(true, content);
//				remakeChain((Stack<Node>) updatePath.clone(), content);
//			}
//			
//		}
//		node = updatePath.pop();
//		while(!updatePath.isEmpty()) {
//			node = updatePath.pop();
//			if(needToAggregate(node))
//				break;
//			node.updateCount();
//			node.setSharedContentWithNode(node.getChild(0));
//		}
//	}
//	
//	private class UDP implements IDataPoint, IDPConvert{
//		List<List<Long>> labels = new ArrayList<List<Long>>();
//		public ACuboid cuboid = null;
//		@Override
//		public List<List<Long>> getLabels() {
//			return labels;
//		}
//
//		@Override
//		public long getLabel(int dimension, int chainDim) {
//			return 0;
//		}
//
//		@Override
//		public int getDimension() {
//			return labels.size();
//		}
//
//		@Override
//		public long getTime() {
//			return 0;
//		}
//
//		@Override
//		public ACuboid convertToACuboid() {
//			return cuboid;
//		}
//		
//	}
//	
//	private boolean needToAggregate(Node node) {
//		return aggSplit.get(node.decodeDim()).contains(node.decodeLevel());
//	}
//	
//	private void dfsCombine(RootNode root, IContent now,UDP udp) {
//		if(now instanceof ACuboid) {
//			udp.cuboid = (ACuboid) now;
//			add(root, udp, 0, new HashSet<>());
//			udp.getLabels().add(new ArrayList<Long>());
//		}			
//		else {
//			if(((Node)now).decodeLevel() == 0)
//				udp.getLabels().add(new ArrayList<Long>());
//			else
//				udp.getLabels().get(udp.getLabels().size() - 1).add(((Node)now).decodeLabel());
//			if (((Node)now).getChildrenSize() == 0) {
//				dfsCombine(root, ((Node)now).getContent(), udp);
//				udp.getLabels().remove(udp.getLabels().size() - 1);
//			}				
//			else
//				for(Node child : ((Node)now).children) {
//					dfsCombine(root, child, udp);
//					udp.getLabels().get(udp.getLabels().size() - 1).remove(udp.getLabels().get(udp.getLabels().size() - 1).size() - 1);
//				}
//		}
//	}
//
//	private void add(Node root, IDataPoint dataPoint, int dimension, Set<IContent> updatedNodes) {
//		List<Long> chain = dataPoint.getLabels().get(dimension);
//		List<Node> nodePathStack = trailProperPath(root, chain);
// 
//		boolean isCuboid = dimension == dataPoint.getDimension() - 1;
//		for (int i = nodePathStack.size() - 1; i >= 0; i--) {
//			Node pathNode = nodePathStack.get(i);
//			////////////////////////////////////////////////
//			boolean update = false;
//			if (needToAggregate(pathNode)) {
//				if (pathNode.getChildrenSize() == 1 && pathNode.getChildrenCount() == 1) {
////				if (pathNode.getChildrenSize() == 1 && pathNode.isChildrenAggregates()) {
//					pathNode.setSharedContentWithNode(pathNode.getChild(0));
//					remakeChain(buildStack(nodePathStack, i), pathNode.getContent());
//				} else if (pathNode.getContent() == null) {
//					IContent tmpRN = null;
//					if (isCuboid)
//						try {
//							tmpRN = cuboidClass.newInstance();
//							pathNode.setContent(false, tmpRN);
//						} catch (InstantiationException | IllegalAccessException e) {
//							e.printStackTrace();
//						}
//					else {
//						tmpRN = new RootNode(dimension + 1);
//						pathNode.setContent(false, tmpRN);
//					}
//					remakeChain(buildStack(nodePathStack, i), tmpRN);
//					update = true;
//				} else if (pathNode.isContentShared() && !updatedNodes.contains(pathNode.getContent())) {
//					IContent origin = pathNode.getContent();
//					if(pathNode.getChildrenSize() > 0 && !needToAggregate(pathNode))
//						origin = pathNode.getChild(0).getContent();
//					IContent copy = origin.shallowCopy();
//					if (pathNode.getChildrenSize() > 0) {
//						for (int j = pathNode.getChild(0).getCount(); j > 1; j--)
//							origin = origin.getNextNode();
//						copy.setNextNode(origin.getNextNode());
//						origin.setNextNode(null);
//					}
//					pathNode.setContent(false, copy);
//					remakeChain(buildStack(nodePathStack, i), copy);
//					update = true;
//				} else if (!pathNode.isContentShared())
//					update = true;
//			} else if (pathNode.getChildrenSize() >= 1) {
//				pathNode.updateCount();
//				pathNode.setSharedContentWithNode(pathNode.getChild(0));
//			}
//			//////////////////////////////////////////////////
//			if (update) {
//				if (isCuboid) {
//					ACuboid cuboid = pathNode.getContent(cuboidClass);
//					cuboid.insert(dataPoint);
//				} else {
//					add(pathNode.getContent(Node.class), dataPoint, dimension + 1, updatedNodes);
//				}
//				updatedNodes.add(pathNode.getContent());
//			}
//		}
//	}
//	
//	private Stack<Node> buildStack(List<Node> nodePathStack, int i){
//		Stack<Node> stack = new Stack<Node>();
//		for (int j =0;j<=i;j++) {
//			stack.push(nodePathStack.get(j));
//		}
//		return stack;
//	}
//
//	private void remakeChain(Stack<Node> nodePathStack, IContent content) {
//		if (nodePathStack.size() < 2)
//			return;
//		Node child = nodePathStack.pop();
//		Node node = nodePathStack.pop();
//		int pop_size = 1;
//		while (!needToAggregate(node)) {
//			int index = node.getChildIndex(child);
//			if(index < 1) {
//				if(nodePathStack.isEmpty())
//					return;
//				child = node;
//				node = nodePathStack.pop();
//				pop_size++;
//			}else {
//				if(node.isChildIndexShared(index - 1)) {
//					Node origin_node = node;
//					Node copy = (Node) node.getChild(index - 1).shallowCopy();
//					node.replaceChild(copy);
//					nodePathStack.push(node);
//					nodePathStack.push(copy);
//					node = copy;
//					for (int k = 1; k < pop_size; k++) {
//						copy = (Node) node.getChild(node.getChildrenSize() - 1).shallowCopy(); 
//						node.replaceChild(copy);
//						nodePathStack.push(copy);
//						node = copy;
//					}
//					IContent copyContent = node.getContent().shallowCopy();
//					copyContent.insertNextNode(content);
//					node.setContent(false, copyContent);
//					remakeChain((Stack<Node>)nodePathStack.clone(), copyContent);
//					nodePathStack.pop();
//					while(true) {
//						Node lastNode = nodePathStack.pop();
//						lastNode.setSharedContentWithNode(lastNode.getChild(0));
//						if (lastNode == origin_node)
//							break;
//					}
//				}
//				else {
//					node = node.getChild(index - 1);
////					for (int k = 1; k < pop_size; k++)
////						node = node.getChild(node.getChildrenSize() - 1);
//					IContent lastContent = node.getContent();
//					for (int k = 1; k < node.getCount(); k++)
//						lastContent = lastContent.getNextNode();
//					lastContent.insertNextNode(content);
//				}
//				break;
//			}
//		}
//	}
//
//	private List<Node> trailProperPath(Node root, List<Long> chain) {
//		List<Node> stack = new ArrayList<>();
//		stack.add(root);
//		Node node = root;
//		for (Long label : chain) {
//			Node child = node.getChild(label);
//			if (child == null) {
//				child = node.newProperChild(label);
//			} else if (node.isChildShared(label)) {
//				Node copy = (Node) child.shallowCopy();
//				node.replaceChild(copy);
//				child = copy;
//			}
//			stack.add(child);
//			node = child;
//		}
//		return stack;
//	}
//
//	@Override
//	public void queryHandler(List<List<Long>> query, IAction<T> action) {
//		action.invoke(query(query));
//	}
//
//	@Override
//	public T query(List<List<Long>> query) {
//		T result;
//		try {
//			result = (T) cuboidClass.newInstance();
//			return queryDFS(root, result, query, 0);
//		} catch (InstantiationException | IllegalAccessException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	private T queryDFS(RootNode rootNode, T result, List<List<Long>> query, int dimension) {
//		if (dimension >= query.size())
//			return result;
//		List<Long> chain = query.get(dimension);
//		Node node = rootNode;
//		for (int i = 0; i < chain.size(); i++) {
//			node = node.getChild(chain.get(i)); //== null?node.getChild(0):node.getChild(chain.get(i));
//			if (node == null)
//				return result;
//		}
//		if (node == null) {
//			for (Long deo : query.get(0))
//				System.out.print(deo + "->");
//		}
//		IContent content = node.getContent();
//		if (dimension == query.size() - 1) {
//			queryCount += node.getCount();
//			for (int count = 0; count < node.getCount(); count++) {
//				result.aggregateAdd((ACuboid) content);
//				content = content.getNextNode();
//			}
//		}			
//		else
//			for (int count = 0; count < node.getCount(); count++) {
//				queryDFS((RootNode) content, result, query, dimension + 1);
//				content = content.getNextNode();
//			}
//		return result;
//	}
//
//}
