package com.tenode.baleen.annotators.documentcoreference.enhancers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.tenode.baleen.annotators.documentcoreference.data.Mention;
import com.tenode.baleen.annotators.documentcoreference.data.MentionType;
import com.tenode.baleen.annotators.documentcoreference.data.Person;

/**
 * Add person information to mentions.
 */
public class PersonEnhancer implements MentionEnhancer {

	private static final Map<String, Person> map = new HashMap<>();

	static {
		// See Person

		Arrays.asList("i", "me", "mine", "my", "myself", "we", "us", "our", "ours", "ourselves")
				.stream().forEach(s -> map.put(s, Person.FIRST));
		Arrays.asList("yourself", "yourselves", "you", "your", "yours")
				.stream().forEach(s -> map.put(s, Person.SECOND));
		Arrays.asList("he", "him", "his", "she", "her", "hers", "himself", "herself",
				"they", "them", "their", "theirs", "themselves", "it", "its", "itself", "one", "one's", "oneself")
				.stream().forEach(s -> map.put(s, Person.THIRD));
	}

	@Override
	public void enhance(Mention mention) {

		if (mention.getType() == MentionType.PRONOUN) {
			mention.setPerson(map.getOrDefault(mention.getText().toLowerCase(), Person.UNKNOWN));
		} else {
			mention.setPerson(Person.UNKNOWN);
		}

	}

}
