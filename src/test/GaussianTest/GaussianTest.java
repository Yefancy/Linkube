package GaussianTest;

import java.util.Random;

import testUtils.TestHelper;
import yefancy.cube.interfaces.IDataPoint;
import yefancy.edgecubes.Cuboid;
import yefancy.edgecubes.Edgecube;
import yefancy.edgecubes.Node;

public class GaussianTest {
	public static long startTime;
	public static void main(String[] args) throws Exception {
		System.out.println("start: " + TestHelper.usedMemory() / 1024 / 1024);
		startTime = System.currentTimeMillis();

		for(int d = 1; d < 6;d++) {
			System.out.println("split:" + d);
			int[] split = {d, d, d, d, d};
			Edgecube cube = new Edgecube(Cuboid.class, split);
			TestHelper th = new TestHelper(cube, 100000);
			th.onWCountInsert = _th->{
//				System.out.print("query test:");
//				for(int i = 0;i < 20;i++){
//					System.out.print("\t" + _th.queryTest(4, 100000));
//				}	
//				System.out.println();
			};
			test(th);
			System.out.println("finish: " + TestHelper.usedMemory() / 1024 / 1024);
			System.out.println("Nodes C ount: " + Node.nodeCount);
			System.out.println("Cuboid Count: " + Cuboid.cuboidCount);
			cube = null;
			th = null;
			while (TestHelper.usedMemory() / 1024 / 1024 > 50) {
				System.out.println("wait 4 gc: " + TestHelper.usedMemory() / 1024 / 1024);
			}
		}
	}
	
	public static void test(TestHelper testHelper) {
		int[] config = {4, 4, 4, 4, 4};
		for (int i = 0; i <= 10000000; i++)
			testHelper.buildMemTest(new GaussianDataPoint(config));		
	}
}
