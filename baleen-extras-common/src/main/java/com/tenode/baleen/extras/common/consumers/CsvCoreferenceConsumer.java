package com.tenode.baleen.extras.common.consumers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.tenode.baleen.extras.common.language.StopWordRemover;

import uk.gov.dstl.baleen.types.language.Sentence;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.types.semantic.Entity;

/**
 * WRite coreference information to a CSV.
 *
 * The format is as follows:
 * <ul>
 * <li>source
 * <li>id
 * <li>reference
 * <li>type
 * <li>text
 * <li>value
 * <li>EntityCount
 * <li>then EntityCount * Entities (value, type)
 * <li>nonEntityNonStopWordsCount
 * <li>nonEntityNonStopWordsCount * nonEntityNonStopWords ( (format word then pos)
 * <li>NonStopWordsNotCoveredByEntitiesCount
 * <li>then NonStopWordsNotCoveredByEntitiesCount * NonStopWordsNotCoveredByEntities (format word
 * then pos)
 * </ul>
 *
 * @baleen.javadoc
 */
public class CsvCoreferenceConsumer extends AbstractCsvConsumer {

	private final StopWordRemover stopWordRemover = new StopWordRemover();

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.tenode.baleen.extras.common.consumers.AbstractCsvConsumer#doInitialize(org.apache.uima.
	 * UimaContext)
	 */
	@Override
	public void doInitialize(UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);
		write("source", "id", "reference", "type", "value",
				"EntityCount then Entities... then nonEntityNonStopWords (format word then pos) then NonStopWordsNotCoveredByEntitiesCount then (format word then pos)...");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.tenode.baleen.extras.common.consumers.AbstractCsvConsumer#write(org.apache.uima.jcas.
	 * JCas)
	 */
	@Override
	protected void write(JCas jCas) {

		final String source = getDocumentAnnotation(jCas).getSourceUri();

		// For each entity we need to find all the other sentences they are contained in
		// TODO: use coreference param , actually two flags
		// - would be both should I joint entities based on doc coref "John - John Smith
		// - and then use non-entities too "He - John Smith"
		// - Or just output the coreferent map?

		// This should be all entities and sentences
		final Map<Entity, Collection<Sentence>> coveringSentence = JCasUtil.indexCovering(jCas, Entity.class,
				Sentence.class);
		final Map<Sentence, Collection<Entity>> coveredEntities = JCasUtil.indexCovered(jCas, Sentence.class,
				Entity.class);
		final Map<Sentence, Collection<WordToken>> coveredTokens = JCasUtil.indexCovered(jCas, Sentence.class,
				WordToken.class);
		final Map<WordToken, Collection<Entity>> coveringEntity = JCasUtil.indexCovering(jCas, WordToken.class,
				Entity.class);

		JCasUtil.select(jCas, Entity.class).stream().map(e -> {
			final List<String> list = new ArrayList<>();

			Sentence sentence = null;
			final Collection<Sentence> sentences = coveringSentence.get(e);
			if (!sentences.isEmpty()) {
				sentence = sentences.iterator().next();
			} else {
				getMonitor().error("Entity without sentence {}", e.getCoveredText());
				return null;
			}

			list.add(source);
			list.add(e.getExternalId());

			if (e.getReferent() != null) {
				list.add(Long.toString(e.getReferent().getInternalId()));
			} else {
				list.add("");
			}

			list.add(e.getType().getShortName());
			list.add(normalize(e.getValue()));

			final Collection<Entity> entities = coveredEntities.get(sentence);

			// Entities
			final int entityCountIndex = list.size();
			int entityCount = 0;
			list.add("0");

			for (final Entity x : entities) {
				if (x.getInternalId() != e.getInternalId()) {
					list.add(normalize(x.getValue()));
					list.add(x.getType().getShortName());
					entityCount++;
				}
			}
			list.set(entityCountIndex, Integer.toString(entityCount));

			// Add (non-stop) words - separate out the entities from the other words

			final List<WordToken> entityNonStopWords = new ArrayList<>();
			final List<WordToken> nonEntityNonStopWords = new ArrayList<>();

			for (final WordToken t : coveredTokens.get(sentence)) {
				// Filter out entities
				final String word = t.getCoveredText();
				if (!stopWordRemover.isStopWord(word)) {

					final Collection<Entity> collection = coveringEntity.get(t);
					if (collection == null || collection.isEmpty()) {
						nonEntityNonStopWords.add(t);
					} else if (!collection.stream().anyMatch(x -> e.getInternalId() == x.getInternalId())) {
						// Output any entity other than the one we are processing
						entityNonStopWords.add(t);
					}
				}
			}

			// We output

			list.add(Integer.toString(entityNonStopWords.size()));
			entityNonStopWords.forEach(t -> {
				list.add(normalize(t.getCoveredText()));
				list.add(t.getPartOfSpeech());
			});

			list.add(Integer.toString(nonEntityNonStopWords.size()));
			nonEntityNonStopWords.forEach(t -> {
				list.add(normalize(t.getCoveredText()));
				list.add(t.getPartOfSpeech());
			});

			// TODO: Add related entities too? they'd likely just be in the same sentence and hence
			// are already included. They could at least be given a higher priority when clustering.

			return list.toArray(new String[list.size()]);
		}).filter(Objects::nonNull).forEach(this::write);
	}

}
