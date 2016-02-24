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

public class CsvCoreferenceConsumer extends AbstractCsvConsumer {

	private final StopWordRemover stopWordRemover = new StopWordRemover();

	@Override
	public void doInitialize(UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);
		write("source", "id", "reference", "type", "text", "value",
				"EntityCount then Entities... then WordCount then words...");
	}

	@Override
	protected void write(JCas jCas) {

		String source = getDocumentAnnotation(jCas).getSourceUri();

		// For each entity we need to find all the other sentences they are contained in
		// TODO: use coreference param , actually two flags
		// - would be both should I joint entities based on doc coref "John - John Smith
		// - and then use non-entities too "He - John Smith"
		// - Or just output the coreferent map?

		// This should be all entities and sentences
		Map<Entity, Collection<Sentence>> coveringSentence = JCasUtil.indexCovering(jCas, Entity.class, Sentence.class);
		Map<Sentence, Collection<Entity>> coveredEntities = JCasUtil.indexCovered(jCas, Sentence.class, Entity.class);
		Map<Sentence, Collection<WordToken>> coveredTokens = JCasUtil.indexCovered(jCas, Sentence.class,
				WordToken.class);
		Map<WordToken, Collection<Entity>> coveringEntity = JCasUtil.indexCovering(jCas, WordToken.class,
				Entity.class);

		JCasUtil.select(jCas, Entity.class).stream().map(e -> {
			List<String> list = new ArrayList<>();

			Sentence sentence = null;
			Collection<Sentence> sentences = coveringSentence.get(e);
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
			list.add(normalize(e.getCoveredText()));
			list.add(normalize(e.getValue()));

			Collection<Entity> entities = coveredEntities.get(sentence);

			int entityCountIndex = list.size();
			int entityCount = 0;
			list.add("0");

			// Entities
			for (Entity x : entities) {
				if (x.getInternalId() != e.getInternalId()) {
					list.add(normalize(x.getValue()));
					list.add(x.getType().getShortName());
					entityCount++;
				}
			}
			list.set(entityCountIndex, Integer.toString(entityCount));

			// Add words which aren't entities

			// Add a placeholder for the word count
			int wordCountIndex = list.size();
			list.add("0");

			int wordCount = 0;
			for (WordToken t : coveredTokens.get(sentence)) {
				// Filter out entities
				Collection<Entity> collection = coveringEntity.get(t);
				String word = t.getCoveredText();
				if ((collection == null || collection.isEmpty()) && !stopWordRemover.isStopWord(word)) {
					list.add(normalize(word));
					wordCount++;
				}
			}
			list.set(wordCountIndex, Integer.toString(wordCount));

			// TODO: Add related entities too? they'd likely just be in the same sentence though

			return list.toArray(new String[list.size()]);
		}).filter(Objects::nonNull)
				.forEach(this::write);
	}

}
