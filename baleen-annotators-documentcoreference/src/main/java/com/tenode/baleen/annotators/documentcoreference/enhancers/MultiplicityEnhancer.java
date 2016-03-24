package com.tenode.baleen.annotators.documentcoreference.enhancers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.tenode.baleen.annotators.documentcoreference.data.Mention;
import com.tenode.baleen.annotators.documentcoreference.data.Multiplicity;
import com.tenode.baleen.resources.documentcoreference.GenderMultiplicityResource;

import uk.gov.dstl.baleen.types.common.Organisation;
import uk.gov.dstl.baleen.types.language.WordToken;

/**
 * Add multiplicity information to mention.
 */
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
			final WordToken head = mention.getHeadWordToken();
			if (head != null) {
				if ("NNS".equalsIgnoreCase(head.getPartOfSpeech())
						|| "NPS".equalsIgnoreCase(mention.getHeadWordToken().getPartOfSpeech())) {
					m = Multiplicity.PLURAL;
				} else {
					m = Multiplicity.SINGULAR;
				}
			}

			mention.setMultiplicity(m);
			break;
		default:
			return;
		}

		// TODO: Should we always check our resource and then override the multiplicity?

		if (mention.getMultiplicity() == Multiplicity.UNKNOWN) {
			final Multiplicity assignedMultiplicity = multiplicityResource.lookupMultiplicity(mention.getText());
			mention.setMultiplicity(assignedMultiplicity);
		}
	}

}
