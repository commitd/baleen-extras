package com.tenode.baleen.extras.annotators.relationships;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.types.language.Interaction;
import uk.gov.dstl.baleen.types.language.Sentence;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.Relation;

/**
 * Extract relationships based on the pattern [entity]-[interaction]-[entity].
 * <p>
 * Any other words in the sentence are ignored, this is purely based on the ordering. The
 * interaction text is used as the relationship. So additional parsing information is used.
 * <p>
 * This is likely to produce a lot of noise but could be useful for simple sentences. For example,
 * where animals are entities, 'jumps' and 'capital' are considered interactions - so that London is
 * the capital of UK:
 * <ul>
 * <li>The fox jumps the dog - Correct (fox-jumps-dog)</li>
 * <li>The fox jumps the dog and the cat- Correct (fox-jumps-dog,fox-jumps-cat)</li>
 * <li>The London is the capital of the UK - Correct (london-capital-UK)</li>
 * <li>The fox jumps the dog and the cat in London - Incorrect fox jumps London
 * (fox-jumps-dog,fox-jumps-cat,fox-jumps-london)</li>
 * <li>The fox jumps the dog in capital city, London. - Incorrect
 * (fox-jumps-dog,fox-jumps-london,dog-capital-london)</li>
 * <li>The fox was jumped by the dog - Incorrect, as the subject is the wrong way around
 * (fox-jumps-dog)</li>
 * </ul>
 *
 * You can clean up some of these if your relations have source-target type information. See
 * RelationTypeFilter.
 */
public class SimpleInteractionRelationship extends AbstractInteractionBasedSentenceRelationshipAnnotator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tenode.baleen.extras.annotators.relationships.
	 * AbstractInteractionBasedSentenceRelationshipAnnotator#extract(org.apache.uima.jcas.JCas,
	 * uk.gov.dstl.baleen.types.language.Sentence, java.util.Collection, java.util.Collection)
	 */
	@Override
	protected Stream<Relation> extract(final JCas jCas, final Sentence sentence,
			final Collection<Interaction> interactions,
			final Collection<Entity> entities) {

		return interactions.stream().flatMap(i -> {

			final List<Entity> leftOfInteraction = entities.stream().filter(e -> e.getEnd() < i.getBegin())
					.collect(Collectors.toList());
			final List<Entity> rightOfInteraction = entities.stream().filter(e -> e.getBegin() > i.getEnd())
					.collect(Collectors.toList());

			return createPairwiseRelations(jCas, i, leftOfInteraction, rightOfInteraction);

		});
	}

}
