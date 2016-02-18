package com.tenode.baleen.annotators.coreference.data;

public enum Person {

	FIRST, SECOND, THIRD, UNKNOWN;

	// FIRST Singular i, me, mine, my, myself
	// FIRST Plural we, us, our, ours, ourselves
	// Second singular yourself
	// Second plural yourselves
	// Second both you your yours
	// Third singular he him his she her hers himself herself one, one's
	// Third plural they them their theirs themselves
	// Third neuter it its itself

	// PLys: there, here
}
