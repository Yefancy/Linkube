package yefancy.edgecubes;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import yefancy.cube.interfaces.IQueryEngine;
import yefancy.edgecubes.utils.Point2D;
import yefancy.edgecubes.utils.Point3D;

public class QueryEngine<T extends ACuboid> implements IQueryEngine {
	private final RootNode root;
	private long queryCount = 0;
	private final Class<ACuboid> cuboidClass;
	public long[] queryTime = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	private List<Integer> counts = new ArrayList<Integer>();
	private HashMap<Long, List<Point3D>> lastMap = new HashMap<Long, List<Point3D>>();
	private HashMap<Long, List<Point3D>> thisMap = new HashMap<Long, List<Point3D>>();
	private long spatial;
	private long temporal;
	private long categorical;
	private int lastZoom = 0;
	public int hit;
	public int cacheSize;
	
	
	public QueryEngine(RootNode root, Class<T> clazz){
		this.root = root;
		this.cuboidClass = (Class<ACuboid>) clazz;
	}
	
	public long UpdateCount() {
		long last = queryCount;
		queryCount = 0;
		return last;
	}
	
	public long getLastSpatialTime() {
		if (categorical == 0)
			return spatial - temporal;
		return spatial - categorical;
	}
	
	public long getLastCategoricalTime() {
		if (categorical == 0)
			return 0;
		return categorical - temporal;
	}
	
	public long getLastTemporalTime() {
		return temporal;
	}

	@Override
	public List<Point3D> queryMap(double ltLat, double ltLon, double rbLat, double rbLon, int zoom, List<List<Long>> cate,
			Date from, Date to) {
		hit = 0;
		cacheSize = lastMap.size();
		cacheSize = cacheSize!=0?cacheSize:1;
		List<Point3D> nodes = new ArrayList<Point3D>();
		Stack<Node> geo = new Stack<Node>();
		geo.push(root);
		spatial = 0;
		temporal = 0;
		categorical = 0;
		long st = System.nanoTime();
		queryGeoDFS(nodes, geo,(long) ((ltLat + 89) * 10000000), (long) ((ltLon + 179) * 10000000),
				(long) ((rbLat + 89) * 10000000), (long) ((rbLon + 179) * 10000000), zoom, cate, from, to);
		spatial += System.nanoTime() - st;
		long c = UpdateCount() * zoom;
		queryTime[zoom] += c;
		counts.add((int) c);
		lastMap = thisMap;
		thisMap = new HashMap<Long, List<Point3D>>();
		lastZoom = zoom;
		return nodes;
	}
	
	private void queryGeoDFS2(List<Point3D> list, Stack<Node> geo,long ltLat, long ltLon, long rbLat, long rbLon, int zoom, List<List<Long>> cate,
			Date from, Date to) {
		int size = geo.size();
		if (size - 1 == zoom && size > 5) {
			ACuboid cuboid = null;
			try {
				cuboid = cuboidClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
			IContent content = geo.peek().getContent();
			for (int count = 0; count < geo.peek().getCount(); count++) {
				if(cate.size() == 0) {
					long st = System.nanoTime();
					cuboid = queryMinTimeDFS((Node) content, cuboid, from, to);
					temporal += System.nanoTime() - st;
				}
				else {
					long st = System.nanoTime();
					cuboid = queryCateDFS((RootNode) content, cuboid, cate, 0, from, to);
					categorical += System.nanoTime() - st;
				}
				content = content.getNextNode();
			}				
			int count = (int) ((CuboidV2) cuboid).count;
			if (count == 0)
				return;
			list.add(getGeo(geo, count));
		}else if(size == 5) {
			for (Node node : geo.peek().children) {
				geo.push(node);
				long key = toKey(geo);
				switch (checkGeoAvaliabe(geo, ltLat, ltLon, rbLat, rbLon)) {
				case 0:
					if (lastZoom == zoom)
						if (lastMap.containsKey(key)) {
							hit++;
							List<Point3D> ps = lastMap.get(key);
							list.addAll(ps);
							thisMap.put(key, ps);
							break;
						}
				case 2:
					List<Point3D> _list = new ArrayList<Point3D>();
					queryGeoDFS2(_list, geo, ltLat, ltLon, rbLat, rbLon, zoom, cate, from, to);
					list.addAll(_list);
					thisMap.put(key, _list);
				}
				geo.pop();
			}
		}
		else {
			for (Node node : geo.peek().children) {
				geo.push(node);
				if (checkGeoAvaliabe(geo, ltLat, ltLon, rbLat, rbLon) != 1) {
					queryGeoDFS2(list, geo, ltLat, ltLon, rbLat, rbLon, zoom, cate, from, to);
				}
				geo.pop();
			}
		}
	}
	
	private long toKey(Stack<Node> geo) {
		long back = 0;
		for(int i=0;i<geo.size();i++) {
			back = back*10 + geo.get(i).label;
		}
		return back;
	}
	
	private void queryGeoDFS(List<Point3D> list, Stack<Node> geo,long ltLat, long ltLon, long rbLat, long rbLon, int zoom, List<List<Long>> cate,
			Date from, Date to) {
		if (geo.size() - 1 == zoom) {
			ACuboid cuboid = null;
			try {
				cuboid = cuboidClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
			IContent content = geo.peek().getContent();
			for (int count = 0; count < geo.peek().getCount(); count++) {
				if(cate.size() == 0) {
					long st = System.nanoTime();
					cuboid = queryMinTimeDFS((Node) content, cuboid, from, to);
					temporal += System.nanoTime() - st;
				}
				else {
					long st = System.nanoTime();
					cuboid = queryCateDFS((RootNode) content, cuboid, cate, 0, from, to);
					categorical += System.nanoTime() - st;
				}
				content = content.getNextNode();
			}				
			int count = (int) ((CuboidV2) cuboid).count;
			if (count == 0)
				return;
			list.add(getGeo(geo, count));
		} else {
			for (Node node : geo.peek().children) {
				geo.push(node);
				if (checkGeoAvaliabe(geo, ltLat, ltLon, rbLat, rbLon) != 1) {
					queryGeoDFS(list, geo, ltLat, ltLon, rbLat, rbLon, zoom, cate, from, to);
				}
				geo.pop();
			}
		}
	}
	
	private ACuboid queryCateDFS(RootNode rootNode, ACuboid result, List<List<Long>> query, int dimension, Date from, Date to) {
		if (dimension >= query.size()) {
			return result;
		}
		List<Long> chain = query.get(dimension);
		Node node = rootNode;
		for (int i = 0; i < chain.size(); i++) {
			node = node.getChild(chain.get(i)); //== null?node.getChild(0):node.getChild(chain.get(i));
			if (node == null) {
				return result;
			}
		}
		IContent content = node.getContent();
		if (dimension == query.size() - 1) {
			for (int count = 0; count < node.getCount(); count++) {
				long st = System.nanoTime();
				queryMinTimeDFS((Node) content, result, from, to);
				temporal += System.nanoTime() - st;
				content = content.getNextNode();
			}
		}			
		else
			for (int count = 0; count < node.getCount(); count++) {
				queryCateDFS((RootNode) content, result, query, dimension + 1, from, to);
				content = content.getNextNode();
			}
		return result;
	}
	
	private ACuboid queryMinTimeDFS(Node rootNode, ACuboid result, Date from, Date to) {
		for (Node node : rootNode.children) {
			if( node.decodeLevel() == 1) {
				int year = (int) node.decodeLabel();
				if (year > from.getYear() && year < to.getYear()) {
					IContent content = node.getContent();
					queryCount += node.getCount();
					for (int count = 0; count < node.getCount(); count++) {
						result.aggregateAdd((ACuboid) content);
						content = content.getNextNode();
					}
				}					
				else if (year == from.getYear() || year == to.getYear())
					queryMinTimeDFS(node, result, from, to);
			}
			else if( node.decodeLevel() == 2) {
				int month = (int) node.decodeLabel();
				if (month > from.getMonth() && month < to.getMonth()){
					IContent content = node.getContent();
					queryCount += node.getCount();
					for (int count = 0; count < node.getCount(); count++) {
						result.aggregateAdd((ACuboid) content);
						content = content.getNextNode();
					}
				}	
				else if (month == from.getMonth() || month == to.getMonth())
					queryMinTimeDFS(node, result, from, to);
			}
			else if( node.decodeLevel() == 3) {
				int date = (int) node.decodeLabel();
				if (date >= from.getDate() && date <= to.getDate()) {
					queryCount += node.getCount();
					result.aggregateAdd((ACuboid) node.getContent());
				}
					
			}
		}
		
		return result;
	}
	
	private int checkGeoAvaliabe(Stack<Node> path, long ltLat, long ltLon, long rbLat, long rbLon) {
		long maxLat = 1800000000L;
		long minLat = 0;
		long maxLon = 3600000000L;
		long minLon = 0;
		for (int i = 1; i < path.size(); i++) {
			switch ((int)(path.get(i).decodeLabel())) {
			case 0:
				minLat = minLat + (maxLat + 1 - minLat) / 2;
				maxLon = minLon + (maxLon + 1 - minLon) / 2;
				break;
			case 1:
				minLat = minLat + (maxLat + 1 - minLat) / 2;
				minLon = minLon + (maxLon + 1 - minLon) / 2;
				break;
			case 2:
				maxLat = minLat + (maxLat + 1 - minLat) / 2;
				maxLon = minLon + (maxLon + 1 - minLon) / 2;
				break;
			case 3:
				maxLat = minLat + (maxLat + 1 - minLat) / 2;
				minLon = minLon + (maxLon + 1 - minLon) / 2;
				break;
			}
		}
		// 0-完全范围内 1-范围外 2-涉及范围内
		if (minLon > rbLon || maxLat < rbLat)
			return 1;
		else if (maxLon < ltLon || minLat > ltLat)
			return 1;
		else if (maxLon <= rbLon && maxLat <= ltLat && minLon >= ltLon && minLat >= rbLat)
			return 0;
		return 2;
	}

	public static Point3D getGeo(Stack<Node> path, int count) {
		long maxLat = 1800000000L;
		long minLat = 0;
		long maxLon = 3600000000L;
		long minLon = 0;
		for (int i = 1; i < path.size(); i++) {
			switch ((int)(path.get(i).decodeLabel())) {
			case 0:
				minLat = minLat + (maxLat + 1 - minLat) / 2;
				maxLon = minLon + (maxLon + 1 - minLon) / 2;
				break;
			case 1:
				minLat = minLat + (maxLat + 1 - minLat) / 2;
				minLon = minLon + (maxLon + 1 - minLon) / 2;
				break;
			case 2:
				maxLat = minLat + (maxLat + 1 - minLat) / 2;
				maxLon = minLon + (maxLon + 1 - minLon) / 2;
				break;
			case 3:
				maxLat = minLat + (maxLat + 1 - minLat) / 2;
				minLon = minLon + (maxLon + 1 - minLon) / 2;
				break;
			}
		}
		return new Point3D((maxLon + minLon) * 1.0d / 20000000 - 179, (maxLat + minLat) * 1.0d / 20000000 - 89, count);
	}

	@Override
	public List<Point2D> queryTime(double ltLat, double ltLon, double rbLat, double rbLon, int zoom, List<List<Long>> cate,
			int timeLevel) {
		List<Point2D> nodes = new ArrayList<Point2D>();
		Stack<Node> geo = new Stack<Node>();
		geo.push(root);
		spatial = 0;
		temporal = 0;
		categorical = 0;
		long st = System.nanoTime();
		queryMinGeoDFS(nodes, geo,(long) ((ltLat + 89) * 10000000), (long) ((ltLon + 179) * 10000000),
				(long) ((rbLat + 89) * 10000000), (long) ((rbLon + 179) * 10000000), zoom, cate, timeLevel);
		spatial += System.nanoTime() - st;
		return nodes;
	}
	
	public void queryMinGeoDFS(List<Point2D> list, Stack<Node> geo,long ltLat, long ltLon, long rbLat, long rbLon, int zoom, List<List<Long>> cate, int timeLevel) {	
		if (geo.size() - 1 == zoom) {
			IContent content = geo.peek().getContent();
			for (int count = 0; count < geo.peek().getCount(); count++) {
				if(cate.size() == 0) {
					Stack<Node> path =new Stack<Node>();
					path.push((Node) geo.peek().getContent());
					queryTimeDFS(path, list, timeLevel);
				}				
				else {
					long st = System.nanoTime();
					queryCateDFS((RootNode) geo.peek().getContent(), list, cate, 0, timeLevel);
					categorical += System.nanoTime() - st;
				}
				content = content.getNextNode();
			}
		} else {
			for (Node node : geo.peek().children) {
				geo.push(node);
				if (checkGeoAvaliabe(geo, ltLat, ltLon, rbLat, rbLon) == 2)
					queryMinGeoDFS(list, geo, ltLat, ltLon, rbLat, rbLon, zoom, cate, timeLevel);
				else if (checkGeoAvaliabe(geo, ltLat, ltLon, rbLat, rbLon) == 0) {
					IContent content = geo.peek().getContent();
					for (int count = 0; count < geo.peek().getCount(); count++) {
						if(cate.size() == 0) {
							Stack<Node> path =new Stack<Node>();
							path.push((Node) geo.peek().getContent());
							long st = System.nanoTime();
							queryTimeDFS(path, list, timeLevel);
							temporal += System.nanoTime() - st;
						}				
						else {
							long st = System.nanoTime();
							queryCateDFS((RootNode) geo.peek().getContent(), list, cate, 0, timeLevel);
							categorical += System.nanoTime() - st;
						}
						content = content.getNextNode();
					}	
					
				}
				geo.pop();
			}
		}
	}
	
	private List<Point2D> queryCateDFS(RootNode rootNode, List<Point2D> result, List<List<Long>> query, int dimension, int level) {
		if (dimension >= query.size())
			return result;
		List<Long> chain = query.get(dimension);
		Node node = rootNode;
		for (int i = 0; i < chain.size(); i++) {
			node = node.getChild(chain.get(i)); //== null?node.getChild(0):node.getChild(chain.get(i));
			if (node == null)
				return result;
		}
		IContent content = node.getContent();
		if (dimension == query.size() - 1) {
			for (int count = 0; count < node.getCount(); count++) {
				Stack<Node> path = new Stack<Node>();
				path.add((Node) content);
				long st = System.nanoTime();
				queryTimeDFS(path, result, level);
				temporal += System.nanoTime() - st;
				content = content.getNextNode();
			}
		}			
		else
			for (int count = 0; count < node.getCount(); count++) {
				queryCateDFS((RootNode) content, result, query, dimension + 1, level);
				content = content.getNextNode();
			}
		return result;
	}
	
	private List<Point2D> queryTimeDFS(Stack<Node> path, List<Point2D> result, int level) {
		for (Node node : path.peek().children) {
			path.push(node);
			if (path.size() - 1 == level) {
				IContent content = node.getContent();
				ACuboid cuboid = null;
				try {
					cuboid = cuboidClass.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
				for (int count = 0; count < node.getCount(); count++) {
					cuboid.aggregateAdd((ACuboid) content);
					content = content.getNextNode();
				}
				Point2D point = getTime(path, ((CuboidV2) cuboid).count);
				for (int i = result.size() - 1; i >= 0; i--) {
					Point2D tmp = result.get(i);
					if (tmp.x == point.x) {
						tmp.y += point.y;
						break;
					}
					if (tmp.x < point.x) {
						result.add(i + 1, point);
						break;
					}						
				}
				if (result.size() == 0)
					result.add(point);
				else if (result.get(0).x > point.x)
					result.add(0, point);
			}
			else 
				queryTimeDFS(path, result, level);
			path.pop();
		}
		return result;
	}
	
	private Point2D getTime(Stack<Node> path, int count) {
		int off = 10000;
		int time = 0;
		for (int i = 1; i < path.size(); i++) {
			time += path.get(i).decodeLabel() * off;
			off /= 100;
		}
		return new Point2D(time, count);
	}
}
