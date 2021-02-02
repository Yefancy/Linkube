package yefancy.edgecubes;

public interface IContent {
	IContent shallowCopy();
	IContent getNextNode();
	void setNextNode(IContent nextContent);	
	void insertNextNode(IContent nextContent);
}
