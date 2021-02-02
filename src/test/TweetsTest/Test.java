package TweetsTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import testUtils.TestHelper;
import yefancy.cube.interfaces.IDataPoint;
import yefancy.edgecubes.Cuboid;
import yefancy.edgecubes.CuboidV2;
import yefancy.edgecubes.EdgecubeV2;
import yefancy.edgecubes.Node;
import yefancy.edgecubes.RootNode;

public class Test {
	public static long startTime;

	public static void main(String[] args) throws Exception {
		System.out.println("start: " + TestHelper.usedMemory() / 1024 / 1024);
		startTime = System.currentTimeMillis();
		for (int d = 3; d < 7; d+=2) {
			int[][] split = {{5, 15, 25}, {0, 1}};
			EdgecubeV2<CuboidV2> cube = new EdgecubeV2<CuboidV2>(CuboidV2.class, split);
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

			List<List<Long>> query = new ArrayList<List<Long>>();
			query.add(new ArrayList<Long>());
			query.add(new ArrayList<Long>());
			System.out.println("Count:" + cube.query(query).count);
			
			Set<Integer> dim0 = cube.getAggSplit().get(0);
			
			startTime = System.currentTimeMillis();
			dim0.add(1);
			cube.updateLevel(0, 1);
			System.out.println((System.currentTimeMillis() - startTime) + "ms");			
			System.out.println("Count:" + cube.query(query).count);
			System.out.println("finish: " + TestHelper.usedMemory() / 1024 / 1024);
			System.out.println("Nodes C ount: " + Node.nodeCount);
			System.out.println("Cuboid Count: " + Cuboid.cuboidCount);
			System.out.println("Nodes LL ount: " + RootNode.linkedListCount);
			System.out.println("Cuboid LL Count: " + Cuboid.linkedListCount);
			
			startTime = System.currentTimeMillis();
			dim0.add(7);
			cube.updateLevel(0, 7);
			System.out.println((System.currentTimeMillis() - startTime) + "ms");			
			System.out.println("Count:" + cube.query(query).count);
			System.out.println("finish: " + TestHelper.usedMemory() / 1024 / 1024);
			System.out.println("Nodes C ount: " + Node.nodeCount);
			System.out.println("Cuboid Count: " + Cuboid.cuboidCount);
			System.out.println("Nodes LL ount: " + RootNode.linkedListCount);
			System.out.println("Cuboid LL Count: " + Cuboid.linkedListCount);
			
			startTime = System.currentTimeMillis();
			dim0.add(12);
			cube.updateLevel(0, 12);
			System.out.println((System.currentTimeMillis() - startTime) + "ms");			
			System.out.println("Count:" + cube.query(query).count);
			System.out.println("finish: " + TestHelper.usedMemory() / 1024 / 1024);
			System.out.println("Nodes C ount: " + Node.nodeCount);
			System.out.println("Cuboid Count: " + Cuboid.cuboidCount);
			System.out.println("Nodes LL ount: " + RootNode.linkedListCount);
			System.out.println("Cuboid LL Count: " + Cuboid.linkedListCount);
			
			startTime = System.currentTimeMillis();
			dim0.add(20);
			cube.updateLevel(0, 20);
			System.out.println((System.currentTimeMillis() - startTime) + "ms");			
			System.out.println("Count:" + cube.query(query).count);
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
			FileReader fr = new FileReader("F:\\Datasets\\twitter\\tc2014.json");
			BufferedReader bf = new BufferedReader(fr);
			String str;
			while ((str = bf.readLine()) != null) {
				IDataPoint dp = parseJson(str);
				if(dp != null)
					testHelper.buildTimeTest(dp);
			}
			bf.close();
			fr.close();
			fr = new FileReader("F:\\Datasets\\twitter\\tc2015.json");
			bf = new BufferedReader(fr);
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
	
	public static TweetDataPoint parseJson(String json) {
		JsonParser parse = new JsonParser(); // 创建json解析器
		try {
			JsonObject jObj = (JsonObject) parse.parse(json); // 创建jsonObject对象
			long tweetid = jObj.get("tweetid").getAsLong() % 2;
			JsonObject location = jObj.get("location").getAsJsonObject();
			long lat = (long) ((location.get("lat").getAsDouble() + 89) * 10000000);
			long lon = (long) ((location.get("lon").getAsDouble() + 179) * 10000000);
			return new TweetDataPoint(lat, lon, tweetid);
		} catch (Exception e) {
			// TODO: handle exception

		}
		return null;
	}
}
