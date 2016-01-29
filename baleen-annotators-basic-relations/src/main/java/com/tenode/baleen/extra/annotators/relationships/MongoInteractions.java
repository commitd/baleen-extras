package com.tenode.baleen.extra.annotators.relationships;

import java.util.List;

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

	@Override
	public void doInitialize(UimaContext aContext) throws ResourceInitializationException {
		// Replace the default value for interaction.
		type = type.equals("Entity") ? "Interaction" : type;
		super.doInitialize(aContext);
	}

	@Override
	public void doProcess(JCas jCas) throws AnalysisEngineProcessException {
		try {
			JCas lemmaJCas = processLemmaForm(jCas);

			super.doProcess(lemmaJCas);

			moveAnnotations(lemmaJCas, jCas);

		} catch (UIMAException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private JCas processLemmaForm(JCas jCas) throws UIMAException {
		JCas lemmaJCas = JCasFactory.createJCas();

		StringBuilder lemmaText = new StringBuilder();
		for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
			List<WordToken> tokens = JCasUtil.selectCovered(jCas, WordToken.class, sentence);

			if (!tokens.isEmpty()) {
				// TODO: This could be improved for a general utility function, adding back
				// punctuation, etc but it does sufficient for here.
				tokens.forEach(t -> {
					int begin = lemmaText.length();
					lemmaText.append(t.getCoveredText());
					int end = lemmaText.length();
					lemmaText.append(" ");

					// We reference this the original word token from this annotation
					Pointer ptr = new Pointer(lemmaJCas);
					ptr.setBegin(begin);
					ptr.setEnd(end);
					ptr.setTarget(t);
					ptr.addToIndexes();
				});
				lemmaText.append(".");
			}

		}

		lemmaJCas.setDocumentText(lemmaText.toString());

		return lemmaJCas;
	}

	private void moveAnnotations(JCas lemmaJCas, JCas jCas) {
		for (Interaction interaction : JCasUtil.select(lemmaJCas, Interaction.class)) {
			Interaction coveringInteraction = JCasUtil.selectCovered(lemmaJCas, Pointer.class, interaction).stream()
					.map(p -> p.getTarget())
					.reduce(new Interaction(jCas), this::include, this::include);
			coveringInteraction.addToIndexes();
		}
	}

	private Interaction include(Interaction a, Annotation b) {
		if (a.getBegin() > b.getBegin()) {
			a.setBegin(b.getBegin());
		}

		if (a.getEnd() < b.getEnd()) {
			a.setEnd(b.getEnd());
		}

		return a;
	}
}
