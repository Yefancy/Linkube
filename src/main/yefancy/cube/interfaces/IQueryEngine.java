package yefancy.cube.interfaces;

import java.util.Date;
import java.util.List;

import yefancy.edgecubes.utils.Point2D;
import yefancy.edgecubes.utils.Point3D;


public interface IQueryEngine {
	//T queryMerge(double ltLat, double ltLon, double rbLat, double rbLon, List<List<Long>> cate, long from, long to);
	List<Point3D> queryMap(double ltLat, double ltLon, double rbLat, double rbLon, int zoom, List<List<Long>> cate, Date from, Date to);
	List<Point2D> queryTime(double ltLat, double ltLon, double rbLat, double rbLon, int zoom, List<List<Long>> cate, int timeLevel);
}
