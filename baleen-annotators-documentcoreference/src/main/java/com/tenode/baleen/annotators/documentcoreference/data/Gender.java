package com.tenode.baleen.annotators.documentcoreference.data;

/**
 * The gender of term.
 */
public enum Gender {
	M, F, N, UNKNOWN;

	/**
	 * Checks if is compatible.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return true, if is compatible
	 */
	public static boolean isCompatible(Gender a, Gender b) {
		return a == Gender.UNKNOWN || b == Gender.UNKNOWN || a == b;
	}
}
