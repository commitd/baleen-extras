package com.tenode.baleen.annotators.documentcoreference.enhancers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.tenode.baleen.annotators.documentcoreference.data.Animacy;
import com.tenode.baleen.annotators.documentcoreference.data.Mention;
import com.tenode.baleen.annotators.documentcoreference.data.MentionType;

import uk.gov.dstl.baleen.types.Base;
import uk.gov.dstl.baleen.types.common.CommsIdentifier;
import uk.gov.dstl.baleen.types.common.DocumentReference;
import uk.gov.dstl.baleen.types.common.Frequency;
import uk.gov.dstl.baleen.types.common.Money;
import uk.gov.dstl.baleen.types.common.Person;
import uk.gov.dstl.baleen.types.common.Url;
import uk.gov.dstl.baleen.types.common.Vehicle;
import uk.gov.dstl.baleen.types.geo.Coordinate;
import uk.gov.dstl.baleen.types.military.MilitaryPlatform;
import uk.gov.dstl.baleen.types.semantic.Location;
import uk.gov.dstl.baleen.types.semantic.Temporal;

/**
 * Adds animacy information to the mention.
 */
public class AnimacyEnhancer implements MentionEnhancer {

	private static final Map<String, Animacy> pronounMap = new HashMap<>();

	private static final Map<Class<? extends Base>, Animacy> semanticMap = new HashMap<>();

	static {
		Arrays.asList("i", "me", "mine", "my", "myself", "we", "us", "our", "ours", "ourselves", "yourself",
				"yourselves", "you", "your", "yours", "he", "him", "his", "she", "her", "hers", "himself", "herself",
				"one", "one's", "they", "them", "their", "theirs", "themselves", "who", "whose")
				.stream().forEach(s -> pronounMap.put(s, Animacy.ANIMATE));
		Arrays.asList("it", "its", "itself", "when", "where", "there", "here")
				.stream().forEach(s -> pronounMap.put(s, Animacy.INANIMATE));

		Arrays.asList(CommsIdentifier.class, DocumentReference.class, Frequency.class, Money.class, Url.class,
				Vehicle.class, Coordinate.class, MilitaryPlatform.class, Location.class, Temporal.class)
				.stream().forEach(s -> semanticMap.put(s, Animacy.INANIMATE));

		Arrays.asList(Person.class)
				.stream().forEach(s -> semanticMap.put(s, Animacy.ANIMATE));

		// Organisation.class and Nationality could be either
	}

	@Override
	public void enhance(Mention mention) {
		if (mention.getType() == MentionType.PRONOUN) {
			mention.setAnimacy(pronounMap.getOrDefault(mention.getText().toLowerCase(), Animacy.UNKNOWN));
		} else if (mention.getType() == MentionType.ENTITY) {
			final Class<? extends Base> entityClazz = mention.getAnnotation().getClass();
			for (final Class<? extends Base> clazz : semanticMap.keySet()) {
				if (clazz.isAssignableFrom(entityClazz)) {
					mention.setAnimacy(semanticMap.get(clazz));
					return;
				}
			}
			mention.setAnimacy(Animacy.UNKNOWN);
		} else {
			// TODO: Based on some database (if we can find or generate one)
			mention.setAnimacy(Animacy.UNKNOWN);
		}

	}

}
