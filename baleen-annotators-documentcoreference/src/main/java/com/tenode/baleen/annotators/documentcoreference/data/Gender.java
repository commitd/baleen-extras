package com.tenode.baleen.annotators.documentcoreference.data;

/**
 * The gender of term.
 */
public enum Gender {
	M, F, N, UNKNOWN;

	/**
	 * Checks if is compatible, so they must be the same.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return true, if is compatible
	 */
	public static boolean strictEquals(Gender a, Gender b) {
		return a == b;
	}

	/**
	 * Checks if is compatible, which allows for unknown to match.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return true, if is compatible
	 */
	public static boolean lenientEquals(Gender a, Gender b) {
		return a == Gender.UNKNOWN || b == Gender.UNKNOWN || a == b;
	}
}
