package com.tenode.baleen.annotators.coreference.sieves;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.uima.jcas.JCas;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.tenode.baleen.annotators.coreference.data.Animacy;
import com.tenode.baleen.annotators.coreference.data.Cluster;
import com.tenode.baleen.annotators.coreference.data.Gender;
import com.tenode.baleen.annotators.coreference.data.Mention;
import com.tenode.baleen.annotators.coreference.data.MentionType;
import com.tenode.baleen.annotators.coreference.data.Person;

public class PronounResolutionSieve extends AbstractCoreferenceSieve {

	private static final int MAX_SENTENCE_DISTANCE = 3;

	public PronounResolutionSieve(JCas jCas, List<Cluster> clusters, List<Mention> mentions) {
		super(jCas, clusters, mentions);
	}

	@Override
	public void sieve() {

		Multimap<Mention, Mention> potential = HashMultimap.create();

		for (int i = 0; i < getMentions().size(); i++) {
			Mention a = getMentions().get(i);

			for (int j = i + 1; j < getMentions().size(); j++) {
				Mention b = getMentions().get(j);

				// We are coreferencing pronouns only
				if (a.getType() != MentionType.PRONOUN && b.getType() != MentionType.PRONOUN) {
					continue;
				}

				if (a.getType() == MentionType.PRONOUN && b.getType() == MentionType.PRONOUN) {
					continue;
				}

				if (a.getSentenceIndex() < 0 || b.getSentenceIndex() < 0) {
					continue;
				}

				Mention pronoun = a.getType() == MentionType.PRONOUN ? a : b;
				Mention other = a.getType() == MentionType.PRONOUN ? b : a;

				// If pronouns then we can have either way around, otherwise we need the entity/np
				// first.
				if (a.getType() == MentionType.PRONOUN && b.getType() == MentionType.PRONOUN) {
					// Paper: Only consider within three
					int sentenceDistance = pronoun.getSentenceIndex() - other.getSentenceIndex();
					if (Math.abs(sentenceDistance) > MAX_SENTENCE_DISTANCE) {
						continue;
					}

				} else {

					// Paper: Only consider within three
					// Not in paper: And the pronoun must be after the mention
					int sentenceDistance = pronoun.getSentenceIndex() - other.getSentenceIndex();
					if (sentenceDistance < 0 && sentenceDistance > MAX_SENTENCE_DISTANCE) {
						continue;
					}

					// Not in paper: If the same sentence the pronoun should be after
					if (sentenceDistance == 0 && pronoun.getAnnotation().getEnd() <= other.getAnnotation().getBegin()) {
						continue;
					}
				}

				// Are the attributes compatible (gender=gender)
				if (!a.isAttributeCompatible(b)) {
					continue;
				}

				// Not in paper: No overlap it makes little sense
				if (a.overlaps(b)) {
					continue;
				}

				if (pronoun.getPerson() == Person.FIRST || pronoun.getPerson() == Person.SECOND
						|| pronoun.getPerson() == Person.THIRD && (pronoun.getGender() == Gender.M
								|| pronoun.getGender() == Gender.F)
						|| pronoun.getAnimacy() == Animacy.ANIMATE) {
					// This pronoun is for a person (in all likelihood)
					// we trust in Baleen NER and must have a person entity at the other
					// TODO: Or nationality?

					if (!(other.getAnnotation() instanceof uk.gov.dstl.baleen.types.common.Person
							|| other.getAnnotation() instanceof uk.gov.dstl.baleen.types.common.Nationality)) {
						continue;
					}
				}

				// Similarly to avoid if we have a neutral we can't link to a person
				if (pronoun.getGender() == Gender.N
						&& other.getAnnotation() instanceof uk.gov.dstl.baleen.types.common.Person) {
					continue;
				}

				// TODO: There might be many more of these simple constraints on our semantic
				// types...

				potential.put(pronoun, other);

			}
		}

		// For each of the matches we need to select the best one

		potential.asMap().entrySet().stream().forEach(e -> {
			Mention key = e.getKey();
			Collection<Mention> collection = e.getValue();

			Mention match = null;
			if (collection.size() > 1) {
				List<Mention> list = new ArrayList<Mention>(collection);
				Collections.sort(list, (a, b) -> {
					int sentenceIndex = Integer.compare(a.getSentenceIndex(), b.getSentenceIndex());
					if (sentenceIndex != 0) {
						return sentenceIndex;
					} else {
						// NOTE: WE'd like to find the minimum distance here

						if (a.overlaps(b)) {
							return 0;
						}

						// Use in-sentence word distance
						if (a.getAnnotation().getEnd() <= b.getAnnotation().getBegin()) {
							return b.getAnnotation().getBegin() - a.getAnnotation().getEnd();
						} else {
							return b.getAnnotation().getEnd() - a.getAnnotation().getBegin();
						}

					}
				});

				// Take the closest match
				// TODO: Need more heuristics about this to determine if its the best!
				match = list.get(0);
			} else {
				match = collection.iterator().next();
			}

			addToCluster(key, match);
		});
	}
}
