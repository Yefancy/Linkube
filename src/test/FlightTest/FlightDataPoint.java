package FlightTest;

import java.util.ArrayList;
import java.util.List;

import yefancy.cube.interfaces.IDataPoint;

public class FlightDataPoint implements IDataPoint {
	public final long lat;
	public final long lon;
	public final long carrier;
	public final long year;
	public final long month;
	public final long day;
	private final List<List<Long>> labels = new ArrayList<List<Long>>();
	

	public FlightDataPoint(long lat, long lon, long carrier, long year, long month, long day) {
		// TODO Auto-generated constructor stub
		this.lat = lat;
		this.lon = lon;
		this.carrier = carrier<0? -carrier:carrier;
		this.year = year;
		this.month = month;
		this.day = day;

		List<Long> carrierList = new ArrayList<Long>();
		List<Long> timeList = new ArrayList<Long>();
		//delayList.add(delay);
		carrierList.add(this.carrier);
		timeList.add(year);
		timeList.add(month);
		timeList.add(day);
		labels.add(getGeo(24));
		//labels.add(delayList);
		labels.add(carrierList);
		labels.add(timeList);
	}
	
	public FlightDataPoint(double lat, double lon, long carrier, long year, long month, long day) {
		// TODO Auto-generated constructor stub
		this.lat = (long) ((lat + 89) * 10000000);
		this.lon = (long) ((lon + 179) * 10000000);
		this.carrier = carrier<0? -carrier:carrier;
		this.year = year;
		this.month = month;
		this.day = day;
		
		List<Long> carrierList = new ArrayList<Long>();
		List<Long> timeList = new ArrayList<Long>();
		//delayList.add(delay);
		carrierList.add(this.carrier);
		timeList.add(year);
		timeList.add(month);
		timeList.add(day);
		labels.add(getGeo(24));
		//labels.add(delayList);
		labels.add(carrierList);
		labels.add(timeList);
	}
	
	public long getX() {return lat;}
	public long getY() {return lon;}
	//public long getDelay() {return delay;}
	public long getCarrier() {return carrier;}
	public long getYear() {return year;}
	public long getMonth() {return month;}
	
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
		return 3;
	}

	@Override
	public long getTime() {
		// TODO Auto-generated method stub
		return 0;
	}
	

}
