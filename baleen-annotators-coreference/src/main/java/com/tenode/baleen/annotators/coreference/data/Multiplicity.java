package com.tenode.baleen.annotators.coreference.data;

/**
 * Multiplicity of a term - singular, plural.
 */
public enum Multiplicity {
	SINGULAR, PLURAL, UNKNOWN;

	/**
	 * Checks if is compatible.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return true, if is compatible
	 */
	public static boolean isCompatible(Multiplicity a, Multiplicity b) {
		return a == Multiplicity.UNKNOWN || b == Multiplicity.UNKNOWN || a == b;
	}
}
