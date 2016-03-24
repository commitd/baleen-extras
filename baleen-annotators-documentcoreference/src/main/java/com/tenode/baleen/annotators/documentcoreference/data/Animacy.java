package com.tenode.baleen.annotators.documentcoreference.data;

/**
 * Animacy - is the item animate (alive) or not.
 */
public enum Animacy {
	ANIMATE, INANIMATE, UNKNOWN;

	/**
	 * Checks if is compatible.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return true, if is compatible
	 */
	public static boolean isCompatible(Animacy a, Animacy b) {
		return a == Animacy.UNKNOWN || b == Animacy.UNKNOWN || a == b;
	}
}
