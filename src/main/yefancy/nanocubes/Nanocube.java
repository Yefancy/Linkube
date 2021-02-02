package yefancy.nanocubes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import yefancy.cube.interfaces.*;

public class Nanocube implements ICube<SummedTimeCountsTable> {
	private final Node root;

    public Nanocube() {
        // root node doesn't have a value
        this.root = new Node();
    }

    public Node getRoot() {
        return root;
    }

    public void insert(IDataPoint dataPoint) {
        Set<Content> updatedNodes = new HashSet<>();
        add(root, dataPoint, 0, updatedNodes);
    }

    /**
     * add nodes depth-first to a given label.  As you recurse to lower levels, construct shared links or split off to
     */
    private void add(Node root, IDataPoint dataPoint, int dimension, Set<Content> updatedNodes) {
    	List<Long> chain = dataPoint.getLabels().get(dimension);
        List<Node> dimensionPathNodes = trailProperDimensionPath(root, chain);

        Node child = null;
        // start with finest level ...
        for (int i = dimensionPathNodes.size() - 1; i >= 0; i--) {
            Node pathNode = dimensionPathNodes.get(i);
            boolean update = processDimensionPathNode(pathNode, child, dimension, updatedNodes, (dimension == dataPoint.getDimension() - 1));

            if (update) {
                if (dimension == dataPoint.getDimension() - 1) {
                    SummedTimeCountsTable summedTimeCountsTable = pathNode.getContent(SummedTimeCountsTable.class);
                    summedTimeCountsTable.insert(dataPoint.getTime());

                } else {
                    add(pathNode.getContent(Node.class), dataPoint, dimension + 1, updatedNodes);
                }
                updatedNodes.add(pathNode.getContent());
            }

            child = pathNode;
        }
    }

    private boolean processDimensionPathNode(Node node, Node child, int dimension, Set<Content> updatedNodes, boolean isSum) {
        // We have a single child node.
        if (node.getChildrenSize() == 1) {
            node.setSharedContentWithNode(child);

        } else if (node.getContent() == null) {
            // If we have no content (this will only happen if we have no children) then we need to link to the next dimension.
            node.setContent(false, createNewContent(dimension, isSum));
            return true;

        } else if (node.isContentShared() && !updatedNodes.contains(node.getContent())) {
            // This part is tough to understand: if our content link is shared and the content node we link to is not 'updated'.
            // This happens when there has been an update downstream from our current node, but its content node, doesn't match
            // the node that's been updated.  In such a case, we need to split off and make a new content node.
            Content shallowCopy = node.getContent().shallowCopy();
            node.setContent(false, shallowCopy);

            return true;

        } else if (!node.isContentShared()) {
            // If we have a direct link to a content node, mark it as updated
            return true;
        }

        return false;
    }

    private Content createNewContent(int dimension, boolean isSum) {
        if (isSum) {
            // if the next dimension is time, then we need to make a new time series table
            return new SummedTimeCountsTable();
        } else {
            // Otherwise we need to make a new node in the next dimension (dimension root)
            return new Node();
        }
    }

    private List<Node> trailProperDimensionPath(Node root, List<Long> labels) {
        List<Node> stack = new ArrayList<>();
        stack.add(root);

        Node node = root;
        for (Long label : labels) {
            Node child = getOrCreateProperChildNode(node, label);
            stack.add(child);
            node = child;
        }

        return stack;
    }

    /**
     * Builds a path of nodes to the finest level in a chain creating new nodes with shared links when necessary
     */
    private Node getOrCreateProperChildNode(Node node, Long label) {
        Node child = node.getChild(label);
        if (child == null) {
            return node.newProperChild(label);

        } else if (node.isChildShared(label)) {
            Node copy = (Node) child.shallowCopy();
            node.replaceChild(copy);
            return copy;

        } else {
            return child;
        }
    }

    public String toPrettyString() {
        StringBuilder sb = new StringBuilder();
        root.appendPrettyPrint(sb, 0);
        return sb.toString();
    }
    
    @Override
    public void queryHandler(List<List<Long>> query, IAction<SummedTimeCountsTable> action) {
    	action.invoke(query(query));
    }
    
    @Override
    public SummedTimeCountsTable query(List<List<Long>> query) {
    	return queryDFS(root, query, 0);
    }
    
    private SummedTimeCountsTable queryDFS(Node rootNode, List<List<Long>> query, int dimension) {
		if (dimension >= query.size())
			return null;
		List<Long> chain = query.get(dimension);
		Node node = rootNode;
		for (int i = 0; i < chain.size(); i++) {
			node = node.getChild(chain.get(i));
			if (node == null)
				return null;
		}
		Content content = node.getContent();
		if (dimension == query.size() - 1)
			return (SummedTimeCountsTable)content;
		else
			return queryDFS((Node)content, query, dimension + 1);	
	}
}
