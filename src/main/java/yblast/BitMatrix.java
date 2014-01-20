package yblast;

import no.uib.cipr.matrix.sparse.LinkedSparseMatrix;

public class BitMatrix extends LinkedSparseMatrix {

	protected BitMatrix(int numRows, int numColumns) {
		super(numRows, numColumns);
	}
}
