package com.tenode.baleen.extras.common.consumers;

import java.util.Collection;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import uk.gov.dstl.baleen.types.language.Sentence;
import uk.gov.dstl.baleen.types.semantic.Relation;

public class CsvRelationEvalationConsumer extends AbstractCsvConsumer {

	@Override
	public void doInitialize(UimaContext aContext) throws ResourceInitializationException {

		super.doInitialize(aContext);

		write("file", "sentence", "type", "subtype", "source", "target", "sourceText", "targetText",
				"sourceType", "targetType");
	}

	@Override
	protected void write(JCas jCas) {
		String source = getDocumentAnnotation(jCas).getSourceUri();

		Map<Relation, Collection<Sentence>> coveringSentence = JCasUtil.indexCovering(jCas, Relation.class,
				Sentence.class);

		JCasUtil.select(jCas, Relation.class).stream().map(r -> {

			String sentence = "";
			Collection<Sentence> sentences = coveringSentence.get(r);
			if (!sentences.isEmpty()) {
				sentence = sentences.iterator().next().getCoveredText();
			}

			return new String[] {
					source,
					sentence,
					r.getRelationshipType(),
					r.getRelationSubType(),
					normalize(r.getSource().getValue()),
					normalize(r.getTarget().getValue()),
					normalize(r.getSource().getCoveredText()),
					normalize(r.getTarget().getCoveredText()),
					r.getSource().getType().getShortName(),
					r.getTarget().getType().getShortName()

			};
		}).forEach(this::write);

	}

}
