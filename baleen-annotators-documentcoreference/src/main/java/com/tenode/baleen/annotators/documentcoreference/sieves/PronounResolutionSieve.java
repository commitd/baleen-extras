package com.tenode.baleen.annotators.documentcoreference.sieves;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.uima.jcas.JCas;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.tenode.baleen.annotators.documentcoreference.data.Animacy;
import com.tenode.baleen.annotators.documentcoreference.data.Cluster;
import com.tenode.baleen.annotators.documentcoreference.data.Gender;
import com.tenode.baleen.annotators.documentcoreference.data.Mention;
import com.tenode.baleen.annotators.documentcoreference.data.MentionType;
import com.tenode.baleen.annotators.documentcoreference.data.Person;

/**
 * Attempts to connect pronouns to an entity.
 *
 * This is very difficult problems which will likely fail in the current implementation.
 */
public class PronounResolutionSieve extends AbstractCoreferenceSieve {

	private static final int MAX_SENTENCE_DISTANCE = 3;

	public PronounResolutionSieve(JCas jCas, List<Cluster> clusters, List<Mention> mentions) {
		super(jCas, clusters, mentions);
	}

	@Override
	public void sieve() {

		final Multimap<Mention, Mention> potential = HashMultimap.create();

		for (int i = 0; i < getMentions().size(); i++) {
			final Mention a = getMentions().get(i);

			for (int j = i + 1; j < getMentions().size(); j++) {
				final Mention b = getMentions().get(j);

				// We are coreferencing pronouns only
				if (a.getType() != MentionType.PRONOUN && b.getType() != MentionType.PRONOUN) {
					continue;
				}

				// Don't coreference a pronoun to a pronoun
				if (a.getType() == MentionType.PRONOUN && b.getType() == MentionType.PRONOUN) {
					continue;
				}

				if (a.getSentenceIndex() < 0 || b.getSentenceIndex() < 0) {
					continue;
				}

				final Mention pronoun = a.getType() == MentionType.PRONOUN ? a : b;
				final Mention other = a.getType() == MentionType.PRONOUN ? b : a;

				// Not in paper: If the pronoun is before the other that's odd, (He said Hello. John
				// did.)
				if (pronoun.getAnnotation().getEnd() < other.getAnnotation().getBegin()) {
					continue;
				}

				// Not in paper: We found poor results for "it" really because it never refers to
				// something that Baleen annotates
				// it would be good for say money oy maybe speak but currently we'll just drop it
				if (pronoun.getText().toLowerCase().startsWith("it")) {
					continue;
				}

				// If pronouns then we can have either way around, otherwise we need the entity/np
				// first.
				if (a.getType() == MentionType.PRONOUN && b.getType() == MentionType.PRONOUN) {
					// Paper: Only consider within three
					final int sentenceDistance = pronoun.getSentenceIndex() - other.getSentenceIndex();
					if (Math.abs(sentenceDistance) > MAX_SENTENCE_DISTANCE) {
						continue;
					}

				} else {

					// Paper: Only consider within three
					// Not in paper: And the pronoun must be after the mention
					final int sentenceDistance = pronoun.getSentenceIndex() - other.getSentenceIndex();
					if (sentenceDistance < 0 && sentenceDistance > MAX_SENTENCE_DISTANCE) {
						continue;
					}

					// Not in paper: If the same sentence the pronoun should be after
					if (sentenceDistance == 0 && pronoun.getAnnotation().getEnd() <= other.getAnnotation().getBegin()) {
						continue;
					}
				}

				// Are the attributes compatible (gender=gender, etc)
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

		potential.asMap().entrySet().stream().forEach(e -> addBestAsMatch(e.getKey(), e.getValue()));
	}

	private void addBestAsMatch(Mention key, Collection<Mention> potentialMatches) {

		final Collection<Mention> matched;
		if (potentialMatches.size() > 1) {
			List<Mention> list = new ArrayList<Mention>(potentialMatches);
			Collections.sort(list, (a, b) -> {
				if (a.overlaps(b)) {
					return 0;
				}

				// Use in-sentence word distance
				if (a.getAnnotation().getEnd() <= b.getAnnotation().getBegin()) {
					return b.getAnnotation().getBegin() - a.getAnnotation().getEnd();
				} else {
					return b.getAnnotation().getEnd() - a.getAnnotation().getBegin();
				}
			});

			matched = list;
		} else {
			// Either empty or just one...
			matched = potentialMatches;
		}

		// Get the first (nearest) which doesn't overlap
		Optional<Mention> match = matched.stream()
				.filter(m -> !key.overlaps(m))
				.findFirst();

		if (match.isPresent()) {
			addToCluster(key, match.get());
		}
	}
}
