package com.tenode.baleen.extras.annotators.relationships;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import uk.gov.dstl.baleen.annotators.gazetteer.Mongo;
import uk.gov.dstl.baleen.types.Pointer;
import uk.gov.dstl.baleen.types.language.Interaction;
import uk.gov.dstl.baleen.types.language.Sentence;
import uk.gov.dstl.baleen.types.language.WordToken;

/**
 * Finds interaction words which are stored in a Mongo collection.
 *
 * This acts differently to a standard grammar as it is based on a lemma form of each word token not
 * the original text.
 *
 * Note you can not override the type. The type to be entity type will be Interaction.
 */
public class MongoInteractions extends Mongo {

	private final Map<Long, WordToken> wordTokenReference = new HashMap<>();

	@Override
	public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
		// Replace the default value for interaction.
		type = type.equals("Entity") ? "Interaction" : type;
		super.doInitialize(aContext);
	}

	@Override
	public void doProcess(final JCas jCas) throws AnalysisEngineProcessException {
		try {
			final JCas lemmaJCas = processLemmaForm(jCas);

			super.doProcess(lemmaJCas);

			moveAnnotations(lemmaJCas, jCas);

		} catch (final UIMAException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private JCas processLemmaForm(final JCas jCas) throws UIMAException {
		final JCas lemmaJCas = JCasFactory.createJCas();

		final StringBuilder lemmaText = new StringBuilder();
		for (final Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
			final List<WordToken> tokens = JCasUtil.selectCovered(jCas, WordToken.class, sentence);

			if (!tokens.isEmpty()) {
				// TODO: This could be improved for a general utility function, adding back
				// punctuation, etc but it does sufficient for here.
				tokens.forEach(t -> {
					wordTokenReference.put(t.getInternalId(), t);

					final int begin = lemmaText.length();
					if (t.getLemmas() != null && t.getLemmas().size() > 0) {
						lemmaText.append(t.getLemmas(0).getLemmaForm());
					} else {
						lemmaText.append(t.getCoveredText());
					}
					final int end = lemmaText.length();
					lemmaText.append(" ");

					// We reference this the original word token from this annotation
					final Pointer ptr = new Pointer(lemmaJCas);
					ptr.setBegin(begin);
					ptr.setEnd(end);
					ptr.setTargetId(t.getInternalId());
					ptr.addToIndexes();
				});
			}

		}

		lemmaJCas.setDocumentText(lemmaText.toString());

		return lemmaJCas;
	}

	private void moveAnnotations(final JCas lemmaJCas, final JCas jCas) {
		getMonitor().info("Found {}", JCasUtil.select(lemmaJCas, Interaction.class).size());

		for (final Interaction interaction : JCasUtil.select(lemmaJCas, Interaction.class)) {
			final Interaction coveringInteraction = JCasUtil.selectCovered(lemmaJCas, Pointer.class, interaction)
					.stream()
					.map(p -> wordTokenReference.get(p.getTargetId()))
					.reduce(new Interaction(jCas, interaction.getBegin(), interaction.getEnd()), this::include,
							this::include);
			coveringInteraction.setRelationshipType(interaction.getRelationshipType());
			coveringInteraction.setRelationSubType(interaction.getRelationSubType());
			coveringInteraction.setValue(interaction.getCoveredText());
			coveringInteraction.addToIndexes();
		}
	}

	private Interaction include(final Interaction a, final Annotation b) {
		if (a.getBegin() > b.getBegin()) {
			a.setBegin(b.getBegin());
		}

		if (a.getEnd() < b.getEnd()) {
			a.setEnd(b.getEnd());
		}

		return a;
	}
}
