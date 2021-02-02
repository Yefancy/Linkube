package NceiTest;

import java.util.ArrayList;
import java.util.List;

import yefancy.cube.interfaces.IDataPoint;

public class NceiDataPoint implements IDataPoint {
	private final long lat;
	private final long lon;
	private final long year;
	private final long month;
	private final long day;
	private final List<List<Long>> labels;

	public NceiDataPoint(long lat, long lon, long year, long month, long day) {
		this.lat = lat;
		this.lon = lon;
		this.year = year;
		this.month = month;
		this.day = day;
		labels = new ArrayList<List<Long>>();
		List<Long> timeList = new ArrayList<Long>();
		timeList.add(year);
		timeList.add(month);
		timeList.add(day);
		//timeList.add(hour);
		labels.add(getGeo(24));
		labels.add(timeList);
	}
	
	public NceiDataPoint(double lat, double lon, long year, long month, long day) {
		this.lat = (long) ((lat + 89) * 10000000);
		this.lon = (long) ((lon + 179) * 10000000);
		this.year = year;
		this.month = month;
		this.day = day;
		labels = new ArrayList<List<Long>>();
		List<Long> timeList = new ArrayList<Long>();
		timeList.add(year);
		timeList.add(month);
		timeList.add(day);
		//timeList.add(hour);
		labels.add(getGeo(24));
		labels.add(timeList);
	}
	
	public List<Long> getGeo(int depth) {
		List<Long> geo = new ArrayList<Long>();
		long maxLat = 1800000000L;
		long minLat = 0;
		long maxLon = 3600000000L;
		long minLon = 0;
		for (int i = 0; i < depth; i++) {
			long middleLat = minLat + (maxLat+1-minLat)/2;
			long latB = lat > middleLat ? 0:2;
			if(lat > middleLat)
				minLat = middleLat;
			else
				maxLat = middleLat;
			
			long middleLon = minLon + (maxLon+1-minLon)/2;
			long lonB = lon > middleLon ? 1:0;
			if(lon > middleLon)
				minLon = middleLon;
			else
				maxLon = middleLon;
			geo.add(latB+lonB);
		}
		return geo;
	}
	
	@Override
	public List<List<Long>> getLabels() {
		return labels;
	}

	@Override
	public long getLabel(int dimension, int chainDim) {
		// TODO Auto-generated method stub
		return getLabels().get(dimension).get(chainDim);
	}

	@Override
	public int getDimension() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public long getTime() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
