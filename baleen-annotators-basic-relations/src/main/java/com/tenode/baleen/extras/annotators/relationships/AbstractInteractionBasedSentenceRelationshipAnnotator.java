package com.tenode.baleen.extras.annotators.relationships;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.types.language.Interaction;
import uk.gov.dstl.baleen.types.language.Sentence;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.Relation;

public abstract class AbstractInteractionBasedSentenceRelationshipAnnotator
		extends AbstractInteractionBasedRelationshipAnnotator {

	@Override
	protected final void extract(JCas jCas) {

		Map<Sentence, Collection<Interaction>> sentenceToInteraction = JCasUtil.indexCovered(jCas, Sentence.class,
				Interaction.class);
		Map<Sentence, Collection<Entity>> sentenceToEntities = JCasUtil.indexCovered(jCas, Sentence.class,
				Entity.class);

		for (final Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {

			final Collection<Interaction> interactions = sentenceToInteraction.get(sentence);
			final Collection<Entity> entities = sentenceToEntities.get(sentence);

			// Check we have enough in the sentence to warrant further work
			if (!interactions.isEmpty() && entities.size() >= 2) {
				final Stream<Relation> relations = extract(jCas, sentence, interactions, entities);
				addRelationsToIndex(relations);
			}
		}
	}

	protected abstract Stream<Relation> extract(JCas jCas, Sentence sentence, Collection<Interaction> interactions,
			Collection<Entity> entities);

}
