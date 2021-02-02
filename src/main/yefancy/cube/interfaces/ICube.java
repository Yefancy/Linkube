package yefancy.cube.interfaces;

import java.util.List;

public interface ICube<T> {
	void insert(IDataPoint dataPoint);
	void queryHandler(List<List<Long>> query, IAction<T> action);
	T query(List<List<Long>> query);
}