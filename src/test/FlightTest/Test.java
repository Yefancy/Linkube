package FlightTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import testUtils.TestHelper;
import yefancy.cube.interfaces.IDataPoint;
import yefancy.edgecubes.Cuboid;
import yefancy.edgecubes.Edgecube;
import yefancy.edgecubes.Node;
import yefancy.edgecubes.RootNode;
import yefancy.nanocubes.Nanocube;

public class Test {
	public static long startTime;
	private static Map<String, Double> lat = new HashMap<String, Double>();
	private static Map<String, Double> lon = new HashMap<String, Double>();

	public static void main(String[] args) throws Exception {
		preAirport();
		System.out.println("start: " + TestHelper.usedMemory() / 1024 / 1024);
		startTime = System.currentTimeMillis();

		for(int d = 5; d < 7; d+=2) {
			System.out.println("split:" + d);
			int[] split = {d, 1, 1 };
			Edgecube cube = new Edgecube(Cuboid.class, split);
//			Nanocube cube = new Nanocube();
			TestHelper th = new TestHelper(cube, 10000000);
//			th.onWCountInsert = _th->{
//				System.out.print("query test:");
//				for(int i = 0;i < 20;i++){
//					System.out.print("\t" + _th.queryTest(3, 100000));
//				}	
//				System.out.println();
//			};
			test(th);
//			test2();
//			System.out.println("t1 100000: " + th.queryTest(3, 100000));
			System.out.println("finish: " + TestHelper.usedMemory() / 1024 / 1024);
			System.out.println("Nodes Count: " + Node.nodeCount);
			System.out.println("Cuboid Count: " + Cuboid.cuboidCount);
			System.out.println("Nodes LL ount: " + RootNode.linkedListCount);
			System.out.println("Cuboid LL Count: " + Cuboid.linkedListCount);
			cube = null;
			th = null;
			while (TestHelper.usedMemory() / 1024 / 1024 > 50) {
				System.out.println("wait 4 gc: " + TestHelper.usedMemory() / 1024 / 1024);
			}
		}
	}

	public static void CubeTest(TestHelper testHelper, String filePath) {
		try {
			FileReader fr = new FileReader(filePath);
			BufferedReader bf = new BufferedReader(fr);
			CSVReader csvReader = new CSVReaderBuilder(bf).build();
			Iterator<String[]> iterator = csvReader.iterator();
			iterator.next();
			while (iterator.hasNext()) {
				String[] as = iterator.next();
				IDataPoint dp = parseData(as);
				if (dp != null) {
//					testHelper.buildMemTest(dp);
					testHelper.buildTimeTest(dp);
				}
			}
			bf.close();
			fr.close();
			csvReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void CubeTest(List<IDataPoint> dps, String filePath) {
		try {
			FileReader fr = new FileReader(filePath);
			BufferedReader bf = new BufferedReader(fr);
			CSVReader csvReader = new CSVReaderBuilder(bf).build();
			Iterator<String[]> iterator = csvReader.iterator();
			iterator.next();
			while (iterator.hasNext()) {
				String[] as = iterator.next();
				IDataPoint dp = parseData(as);
				if (dp != null) {
					dps.add(dp);
				}
				if((dps.size() + 1) % 100000 == 0)
					System.out.println(dps.size()/10000);
			}
			bf.close();
			fr.close();
			csvReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void test(TestHelper testHelper) {
		for (int i = 1987; i <= 2008; i++)
			CubeTest(testHelper, "F:\\Datasets\\Flight\\" + i + ".csv");
	}
	
	public static void test2() {
		Thread thread1 = new Thread() {
			public void run() {			
				long st = System.currentTimeMillis();
				int[] split = { 3, 1, 1 };
				Edgecube cube = new Edgecube(Cuboid.class, split);
				TestHelper th = new TestHelper(cube);
				List<IDataPoint> dps = new ArrayList<IDataPoint>();
				for (int i = 1994; i <= 1998; i++) {
					CubeTest(th, "C:\\Users\\lfz47\\Desktop\\flight\\" + i + ".csv");
				}
				System.out.println("T1 finish" + (System.currentTimeMillis() - st) / 1000);
				System.out.println("t1 1000: " + th.queryTest(3, 1000));
				System.out.println("t1 10000: " + th.queryTest(3, 10000));
				System.out.println("t1 100000: " + th.queryTest(3, 100000));
			}
		};
		
		Thread thread2 = new Thread() {
			public void run() {
				long st = System.currentTimeMillis();
				int[] split = { 3, 1, 1 };
				Edgecube cube = new Edgecube(Cuboid.class, split);
				TestHelper th = new TestHelper(cube);
				List<IDataPoint> dps = new ArrayList<IDataPoint>();
				for (int i = 1999; i <= 2003; i++) {
					CubeTest(th, "C:\\Users\\lfz47\\Desktop\\flight\\" + i + ".csv");
				}				
				System.out.println("T2 finish" + (System.currentTimeMillis() - st) / 1000);
				System.out.println("t2 1000: " + th.queryTest(3, 1000));
				System.out.println("t2 10000: " + th.queryTest(3, 10000));
				System.out.println("t2 100000: " + th.queryTest(3, 100000));
			}
		};
		Thread thread3 = new Thread() {
			public void run() {
				long st = System.currentTimeMillis();
				int[] split = { 3, 1, 1 };
				Edgecube cube = new Edgecube(Cuboid.class, split);
				TestHelper th = new TestHelper(cube);
				List<IDataPoint> dps = new ArrayList<IDataPoint>();
				for (int i = 2004; i <= 2008; i++) {
					CubeTest(th, "C:\\Users\\lfz47\\Desktop\\flight\\" + i + ".csv");
				}
				System.out.println("T3 finish" + (System.currentTimeMillis() - st) / 1000);
				System.out.println("t3 1000: " + th.queryTest(3, 1000));
				System.out.println("t3 10000: " + th.queryTest(3, 10000));
				System.out.println("t3 100000: " + th.queryTest(3, 100000));
			}
		};
		Thread thread4 = new Thread() {
			public void run() {				
				long st = System.currentTimeMillis();
				int[] split = { 3, 1, 1 };
				Edgecube cube = new Edgecube(Cuboid.class, split);
				TestHelper th = new TestHelper(cube);
				List<IDataPoint> dps = new ArrayList<IDataPoint>();
				for (int i = 1987; i <= 1993; i++) {
					CubeTest(th, "F:\\Datasets\\Flight\\" + i + ".csv");
				}
				System.out.println("T4 finish" + (System.currentTimeMillis() - st) / 1000);
				System.out.println("t4 1000: " + th.queryTest(3, 1000));
				System.out.println("t4 10000: " + th.queryTest(3, 10000));
				System.out.println("t4 100000: " + th.queryTest(3, 100000));
			}
		};
		thread1.start();
		thread2.start();
		thread3.start();
		thread4.start();
	}

	public static FlightDataPoint parseData(String[] as) {
		FlightDataPoint dp = null;
		if (lat.containsKey(as[16]))
			dp = new FlightDataPoint(lat.get(as[16]), lon.get(as[16]), as[8].hashCode(), Long.parseLong(as[0]),
					Long.parseLong(as[1]), Long.parseLong(as[2]));
		return dp;
	}

	public static void preAirport() {
		try {
			FileReader fr = new FileReader("F:\\Datasets\\Flight\\airports.csv");
			BufferedReader bf = new BufferedReader(fr);
			CSVReader csvReader = new CSVReaderBuilder(bf).build();
			Iterator<String[]> iterator = csvReader.iterator();
			iterator.next();
			while (iterator.hasNext()) {
				String[] as = iterator.next();
				lat.put(as[0], Double.parseDouble(as[5]));
				lon.put(as[0], Double.parseDouble(as[6]));
			}
			bf.close();
			fr.close();
			csvReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
