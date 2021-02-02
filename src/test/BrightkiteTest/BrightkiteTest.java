package BrightkiteTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import GaussianTest.GaussianDataPoint;
import testUtils.TestHelper;
import yefancy.cube.interfaces.IDataPoint;
import yefancy.edgecubes.Cuboid;
import yefancy.edgecubes.Edgecube;
import yefancy.edgecubes.Node;
import yefancy.edgecubes.RootNode;

public class BrightkiteTest {
	public static long startTime;
	public static void main(String[] args) throws Exception {
		System.out.println("start: " + TestHelper.usedMemory() / 1024 / 1024);
		startTime = System.currentTimeMillis();
		for (int d = 5; d < 7; d+=2) {
			int[] split = {d, 1};
			Edgecube cube = new Edgecube(Cuboid.class, split);
			TestHelper th = new TestHelper(cube, 100000);
			th.onWCountInsert = _th->{
//					System.out.print("query test:");
//					for(int i = 0;i < 20;i++){
//						System.out.print("\t" + _th.queryTest(4, 100000));
//					}	
//					System.out.println();
			};
			test(th);
			System.out.println("finish: " + TestHelper.usedMemory() / 1024 / 1024);
			System.out.println("Nodes C ount: " + Node.nodeCount);
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
	
	public static void test(TestHelper testHelper) {
		try {
			FileReader fr = new FileReader("C:\\Users\\lfz47\\Desktop\\tweet-geolocation-5m\\Brightkite_totalCheckins.txt");
			BufferedReader bf = new BufferedReader(fr);
			String str;
			while ((str = bf.readLine()) != null) {
				IDataPoint dp = parseJson(str);
				if(dp != null)
					testHelper.buildTimeTest(dp);
			}
			bf.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public static BrightkiteDataPoint parseJson(String str) {
		String[] splits =  str.split("\t");
		try {
			return new BrightkiteDataPoint(Double.parseDouble(splits[2]), Double.parseDouble(splits[3]), Long.parseLong(splits[1].substring(0, 4)), Long.parseLong(splits[1].substring(5, 7)), Long.parseLong(splits[1].substring(8, 10)), Long.parseLong(splits[1].substring(11, 13)));
		}
		catch (Exception e) {
			return null;
		}		
	}
}
