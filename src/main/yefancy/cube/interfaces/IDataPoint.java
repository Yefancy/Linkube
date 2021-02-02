package yefancy.cube.interfaces;

import java.util.List;

public interface IDataPoint {
	public List<List<Long>> getLabels();
	public long getLabel(int dimension, int chainDim);
	public int getDimension();
	public long getTime();
}
