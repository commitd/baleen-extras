package com.tenode.baleen.annotators.documentcoreference.enhancers;

import com.tenode.baleen.annotators.documentcoreference.data.Mention;

/**
 * interface for enhancers which add additional information to the mentions.
 */
@FunctionalInterface
public interface MentionEnhancer {

	/**
	 * Enhance the mention.
	 *
	 * @param mention
	 *            the mention
	 */
	void enhance(Mention mention);

}
