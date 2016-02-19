package com.tenode.baleen.annotators.coreference.data;

public enum Animacy {
	ANIMATE, INANIMATE, UNKNOWN;

	public static boolean isCompatible(Animacy a, Animacy b) {
		return a == Animacy.UNKNOWN || b == Animacy.UNKNOWN || a == b;
	}
}
