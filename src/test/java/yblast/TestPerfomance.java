package yblast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.SparseVector;

import org.apache.lucene.util.OpenBitSet;
import org.junit.Test;

public class TestPerfomance {

	private final static int size = 200;
	private final static int width = 8000;
	private final static int bits_set = 10;

	@Test
	// a naieve Java implementation
	public void testNaieve() {

		List<OpenBitSet> random1 = RandomArray.createBitSets(size, width,
				bits_set);
		List<OpenBitSet> random2 = RandomArray.createBitSets(size, width,
				bits_set);

		double totalRatio = 0.0;

		long start = System.currentTimeMillis();
		for (OpenBitSet e1 : random1) {
			long car1 = e1.cardinality();

			double max = 0.0;
			for (OpenBitSet e2 : random2) {
				long intersection = OpenBitSet.intersectionCount(e1, e2);
				if (intersection > 0) {
					long car2 = e2.cardinality();
					float ratio = (float) intersection / Math.max(car1, car2);
					if (ratio > max)
						max = ratio;
				}
			}

			totalRatio += max;
		}

		System.out.println("Naieve test: took : "
				+ (System.currentTimeMillis() - start) + "ms. (total ratio: "
				+ totalRatio + " )");
	}

	@Test
	public void testIntegerSets() {
		List<OpenBitSet> random1 = RandomArray.createBitSets(size, width,
				bits_set);
		List<OpenBitSet> random2 = RandomArray.createBitSets(size, width,
				bits_set);

		// convert random2 to sets of integers
		List<Set<Integer>> random2Ints = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			OpenBitSet bitset = random2.get(i);
			Set<Integer> intSet = new HashSet<>();
			for (int j = 0; j < width; j++) {
				boolean val = bitset.get(j);
				if (val)
					intSet.add(j);
			}
			random2Ints.add(intSet);
		}

		List<Set<Integer>> random1Ints = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			OpenBitSet bitset = random1.get(i);
			Set<Integer> intSet = new HashSet<>();
			for (int j = 0; j < width; j++) {
				boolean val = bitset.get(j);
				if (val)
					intSet.add(j);
			}
			random1Ints.add(intSet);
		}

		final long start = System.currentTimeMillis();
		// apply each vector to the whole matrix that is the other document...
		double totalRatio = 0.0;
		for (Set<Integer> random1Set : random1Ints) {
			final int car1 = random1Set.size();

			double max = 0.0;
			for (final Set<Integer> e2 : random2Ints) {
				int overlap = 0;

				for (final Integer e1 : random1Set) {
					if (e2.contains(e1))
						overlap += 1;
				}

				if (overlap > 0) {
					final float ratio = (float) overlap
							/ Math.max(car1, e2.size());
					if (ratio > max)
						max = ratio;
				}
			}

			totalRatio += max;
		}

		System.out.println("Integer sets test took: "
				+ (System.currentTimeMillis() - start) + "ms (totalRatio = "
				+ totalRatio + ")");

	}

	@Test
	public void testBlast() throws IOException {

		List<OpenBitSet> random1 = RandomArray.createBitSets(size, width,
				bits_set);
		List<OpenBitSet> random2 = RandomArray.createBitSets(size, width,
				bits_set);

		// convert the data in random2 to a matrix
		Matrix matrix = new BitMatrix(size, width);

		for (int i = 0; i < size; i++) {
			OpenBitSet bitset = random2.get(i);
			for (int j = 0; j < width; j++) {
				boolean val = bitset.get(j);
				if (val)
					matrix.set(i, j, 1.0);
			}
		}

		long start = System.currentTimeMillis();
		// apply each vector to the whole matrix that is the other document...
		double totalRatio = 0.0;
		for (OpenBitSet bitset : random1) {
			long car1 = bitset.cardinality();

			// convert bitset to vector
			SparseVector vec = new SparseVector(width);

			for (int i = 0; i < width; i++) {
				boolean val = bitset.get(i);
				if (val)
					vec.set(i, 1.0);
			}

			// do the interesting stuff
			SparseVector output = new SparseVector(size);
			matrix.mult(vec, output);

			double max = 0.0;
			for (VectorEntry entry : output) {
				if (entry.get() > 0.0) {
					double ratio = entry.get()
							/ (Math.max(car1, random2.get(entry.index())
									.cardinality()));
					if (ratio > max) {
						max = ratio;
					}
				}
			}
			totalRatio += max;
		}

		System.out.println("BLAS test took: "
				+ (System.currentTimeMillis() - start) + "ms (totalRatio = "
				+ totalRatio + ")");
	}

}
