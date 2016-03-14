package com.tenode.baleen.annotators.coreference.sieves;

/**
 * A sieve stage in the coreference pipeline.
 */
@FunctionalInterface
public interface CoreferenceSieve {

	/**
	 * Apply the sieve.
	 */
	void sieve();

}
