package yefancy.cube.interfaces;

@FunctionalInterface
public interface IAction<T> {
	public void invoke(T t);
}
