package yblast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.lucene.util.OpenBitSet;

public class RandomArray {

	/**
	 * Get an array of randomly filled bitsets
	 * @param size
	 * @param maxBitsSet
	 * @return
	 */
	
	public static List<OpenBitSet> createBitSets(int size, int width, int maxBitsSet)
	{
		Random random = new Random();
		
		List<OpenBitSet> bitsets = new ArrayList<>();
		for(int i=0; i < size; i++)
		{
			OpenBitSet bitset = new OpenBitSet(width);
			for(int r=0; r < maxBitsSet; r++)
			{
				bitset.set(random.nextInt(width));
			}
		
			bitsets.add(bitset);
		}
	
		return bitsets;
	}
	
}
