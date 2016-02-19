package com.tenode.baleen.annotators.coreference.enhancers;

import com.tenode.baleen.annotators.coreference.data.Mention;

public interface MentionEnhancer {

	void enhance(Mention mention);

}
