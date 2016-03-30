package com.tenode.baleen.extras.documentcoreference.enhancers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.tenode.baleen.extras.documentcoreference.data.Gender;
import com.tenode.baleen.extras.documentcoreference.data.Mention;
import com.tenode.baleen.extras.documentcoreference.data.MentionType;
import com.tenode.baleen.extras.documentcoreference.resources.GenderMultiplicityResource;

import uk.gov.dstl.baleen.types.Base;
import uk.gov.dstl.baleen.types.common.Nationality;
import uk.gov.dstl.baleen.types.common.Person;

/**
 * Adds gender information to a mention.
 */
public class GenderEnhancer implements MentionEnhancer {
	private static final Map<String, Gender> pronounMap = new HashMap<>();
	private final GenderMultiplicityResource genderResource;

	static {
		Arrays.asList("he", "him", "his", "himself")
				.stream().forEach(s -> pronounMap.put(s, Gender.M));
		Arrays.asList("she", "her", "hers", "herself")
				.stream().forEach(s -> pronounMap.put(s, Gender.F));
		Arrays.asList("it", "its", "itself", "when", "where", "there", "here")
				.stream().forEach(s -> pronounMap.put(s, Gender.N));
	}

	public GenderEnhancer(GenderMultiplicityResource genderResource) {
		this.genderResource = genderResource;
	}

	@Override
	public void enhance(Mention mention) {
		if (mention.getType() == MentionType.PRONOUN) {
			mention.setGender(pronounMap.getOrDefault(mention.getText().toLowerCase(), Gender.UNKNOWN));
		} else if (mention.getType() == MentionType.ENTITY) {
			final Base annotation = mention.getAnnotation();

			if (annotation instanceof Person) {
				final Person p = (Person) annotation;

				Gender gender = getGenderFromTitle(p.getTitle());
				if (gender == Gender.UNKNOWN) {
					gender = genderResource.lookupGender(mention.getText());
				}

				mention.setGender(gender);
			} else if (annotation instanceof Nationality) {
				mention.setGender(Gender.UNKNOWN);
			} else {
				mention.setGender(Gender.N);
			}
		} else {
			final Gender gender = genderResource.lookupGender(mention.getText());
			mention.setGender(gender);
		}
	}

	private Gender getGenderFromTitle(String title) {
		if (title == null || title.isEmpty()) {
			return Gender.UNKNOWN;
		}

		// TODO: Not exhaustive

		switch (title.toLowerCase()) {
		case "mr":
		case "sir":
		case "lord":
		case "duke":
		case "prince":
			return Gender.M;

		case "mrs":
		case "miss":
		case "ms":
		case "dame":
		case "lady":
		case "duchess":
		case "princess":
			return Gender.F;

		default:
			return Gender.UNKNOWN;
		}
	}

}
