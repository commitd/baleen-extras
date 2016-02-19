package com.tenode.baleen.annotators.coreference.enhancers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.tenode.baleen.annotators.coreference.data.Mention;
import com.tenode.baleen.annotators.coreference.data.Multiplicity;
import com.tenode.baleen.resources.coreference.GenderMultiplicityResource;

import uk.gov.dstl.baleen.types.common.Organisation;
import uk.gov.dstl.baleen.types.language.WordToken;

public class MultiplicityEnhancer implements MentionEnhancer {

	private static final Map<String, Multiplicity> pronounMap = new HashMap<>();
	private final GenderMultiplicityResource multiplicityResource;

	static {
		// See Person

		Arrays.asList("i", "he", "her", "herself", "hers", "her", "him", "himself", "his", "it", "its", "itself", "me",
				"myself", "mine", "my", "one", "oneself", "one's", "she", "yourself").stream()
				.forEach(s -> pronounMap.put(s, Multiplicity.SINGULAR));
		Arrays.asList("ours", "our", "ourself", "ourselves", "their", "theirs", "them", "themself", "themselves",
				"they", "us", "we", "yourself", "yourselves").stream()
				.forEach(s -> pronounMap.put(s, Multiplicity.PLURAL));
	}

	public MultiplicityEnhancer(GenderMultiplicityResource multiplicityResource) {
		this.multiplicityResource = multiplicityResource;
	}

	@Override
	public void enhance(Mention mention) {
		switch (mention.getType()) {
		case PRONOUN:
			mention.setMultiplicity(pronounMap.getOrDefault(mention.getText().toLowerCase(), Multiplicity.UNKNOWN));
			return;
		case ENTITY:
			// Assumed singular, unless organisation
			if (mention.getAnnotation() instanceof Organisation) {
				mention.setMultiplicity(Multiplicity.UNKNOWN);
			} else {
				mention.setMultiplicity(Multiplicity.SINGULAR);
			}
			break;
		case NP:
			Multiplicity m = Multiplicity.UNKNOWN;
			WordToken head = mention.getHeadWordToken();
			if (head != null) {
				if (head.getPartOfSpeech().equals("NNS")
						|| mention.getHeadWordToken().getPartOfSpeech().equals("NPS")) {
					m = Multiplicity.PLURAL;
				} else {
					m = Multiplicity.SINGULAR;
				}
			}

			mention.setMultiplicity(m);
			break;
		}

		// TODO: Should we always check our resource and then override the multiplicity?

		if (mention.getMultiplicity() == Multiplicity.UNKNOWN) {
			Multiplicity assignedMultiplicity = multiplicityResource.lookupMultiplicity(mention.getText());
			mention.setMultiplicity(assignedMultiplicity);
		}
	}

}
