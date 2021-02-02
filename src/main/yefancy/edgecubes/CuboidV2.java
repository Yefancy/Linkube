package yefancy.edgecubes;

import yefancy.cube.interfaces.IDataPoint;

public class CuboidV2 extends ACuboid{
	public int count = 0;
	
	public CuboidV2() {
		super();
    }
	
	public CuboidV2(CuboidV2 original) {
		super();
		this.count += original.count;
        this.setNextNode(original.getNextNode());
    }
	
	@Override
	public IContent shallowCopy() {
		return new CuboidV2(this);
	}

	@Override
	public void insert(IDataPoint dataPoint) {
		if(dataPoint instanceof  IDPConvert)
			this.aggregateAdd(((IDPConvert) dataPoint).convertToACuboid());
		else 
			count++;
	}

	@Override
	public ACuboid aggregateAdd(ACuboid cuboid) {
		this.count += ((CuboidV2)cuboid).count;
		return this;
	}
	
	@Override
    public String toString() {
        return "Count:" + count;
    }

}
