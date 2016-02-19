package com.tenode.baleen.annotators.coreference.data;

public enum Multiplicity {
	SINGULAR, PLURAL, UNKNOWN;

	public static boolean isCompatible(Multiplicity a, Multiplicity b) {
		return a == Multiplicity.UNKNOWN || b == Multiplicity.UNKNOWN || a == b;
	}
}
