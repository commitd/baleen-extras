package com.tenode.baleen.annotators.coreference.data;

public enum Gender {
	M, F, N, UNKNOWN;

	public static boolean isCompatible(Gender a, Gender b) {
		return a == Gender.UNKNOWN || b == Gender.UNKNOWN || a == b;
	}
}
