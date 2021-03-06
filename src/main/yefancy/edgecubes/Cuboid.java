package yefancy.edgecubes;

import java.util.ArrayList;
import java.util.List;

import yefancy.cube.interfaces.IDataPoint;
import yefancy.edgecubes.utils.Bin;


public class Cuboid extends ACuboid {
	private final List<Bin> bins = new ArrayList<>();
	public boolean debug = false;
	
	public Cuboid() {
		super();
    }

	public Cuboid(Cuboid original) {
		super();
        addAllBins(original);
        this.setNextNode(original.getNextNode());
    }
    
    public long getCount() {
    	long result = 0;
    	for (Bin bin : bins) {
    		result += bin.getCount();
    	}
    	return result;
    }

    private void addAllBins(Cuboid original) {
        for (Bin bin : original.bins) {
            bins.add(new Bin(bin));
        }
    }

    private Bin getBinAtPosition(int position) {
        return bins.get(position);
    }

    private int getBinsCount() {
        return bins.size();
    }

    private long getBinTimestamp(Bin bin) {
        return bin.getTimestamp();
    }

    private long getBinCount(Bin bin) {
        return bin.getCount();
    }

    private void incrementBinCount(Bin bin) {
        bin.incrementCount();
    }

    private void insertBin(int position, Bin newBin) {
        bins.add(position, newBin);
    }

    private Bin createBin(long timestamp, long count) {
        return new Bin(timestamp, count);
    }

    private void updateBin(Bin bin, int position) {
        // no need to update since Bin is reference in this impl
    }

    private String binsToString() {
        return bins.toString();
    }

	@Override
	public IContent shallowCopy() {
		// TODO Auto-generated method stub
		return new Cuboid(this);
	}

	@Override
	public void insert(IDataPoint dataPoint) {
		if(debug) {
			System.out.println("insert!");
		}
		// TODO Auto-generated method stub
		boolean exactMatchFound = false;
        int previousTimestampBinIndex = -1;
        for (int i = getBinsCount() - 1; i >= 0; i--) {
            Bin bin = getBinAtPosition(i);
            long binTimestamp = getBinTimestamp(bin);
            if (binTimestamp < dataPoint.getTime()) {
                previousTimestampBinIndex = i;
                break;

            } else if (binTimestamp == dataPoint.getTime()) {
                exactMatchFound = true;
            }
            incrementBinCount(bin);
            updateBin(bin, i);
        }

        if (!exactMatchFound) {
            long exactBinCount = previousTimestampBinIndex == -1 ? 0 : getBinCount(getBinAtPosition(previousTimestampBinIndex));
            Bin exactBin = createBin(dataPoint.getTime(), exactBinCount);
            incrementBinCount(exactBin);
            insertBin(previousTimestampBinIndex + 1, exactBin);
        }
	}
	
	private int findExactMatchOrPredecessor(long targetBinTimestamp, boolean acceptExactMatch) {
        int low = -1;
        int high = getBinsCount() - 1;

        while (low != high) {
            int sum = low + high;
            int addition = sum < 0 ? 0 : sum % 2;
            int midBinIndex = (sum / 2) + addition;
            long midBinTimestamp = getBinTimestamp(getBinAtPosition(midBinIndex));

            if (acceptExactMatch && (midBinTimestamp == targetBinTimestamp)) {
                return midBinIndex;
            } else if (midBinTimestamp >= targetBinTimestamp) {
                high = midBinIndex - 1;
            } else {
                low = midBinIndex;
            }
        }
        /* Now, low and high both point to the element in question. */
        return low;
    }

    public long queryTotalCount() {
        List<Bin> globalBin = queryCounts(Long.MIN_VALUE / 2, Long.MAX_VALUE, 1);
        return globalBin.get(0).getCount();
    }

    public List<Bin> queryCounts(long startTime, long bucketLength, long bucketCount) {
        if (bucketLength < 1) {
            throw new IllegalArgumentException("Bucket length cannot be smaller than 1, but is " + bucketLength);
        }
        if (bucketCount < 1) {
            throw new IllegalArgumentException("Bucket count cannot be smaller than 1, but is " + bucketCount);
        }

        List<Bin> result = new ArrayList<>();

        long previousEndBinEventCount = -1;

        // TODO: optimize to not search for out-of-range buckets ...
        for (int i = 0; i < bucketCount; i++) {
            long bucketStartTime = startTime + i * bucketLength;

            long startBinEventCount = (previousEndBinEventCount == -1) ? getBinEventCount(findExactMatchOrPredecessor(bucketStartTime, false)) : previousEndBinEventCount;

            long bucketEndTime = bucketStartTime + bucketLength - 1;
            int endBucketBinIndex = findExactMatchOrPredecessor(bucketEndTime, true);
            long endBinEventCount = getBinEventCount(endBucketBinIndex);
            previousEndBinEventCount = endBinEventCount;

            long bucketEventCount = endBinEventCount - startBinEventCount;
            if (bucketEventCount > 0) {
                result.add(new Bin(bucketStartTime, bucketEventCount));
            }
        }
        return result;
    }

    private long getBinEventCount(int binIndex) {
        if (binIndex == -1) {
            return 0;
        }
        return getBinCount(getBinAtPosition(binIndex));
    }
    
    @Override
    public String toString() {
        return binsToString();
    }

	@Override
	public ACuboid aggregateAdd(ACuboid cuboid) {
		this.addAllBins((Cuboid) cuboid);
		return cuboid;
	}

}
