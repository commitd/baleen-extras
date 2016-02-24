package com.tenode.baleen.extras.common.consumers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import uk.gov.dstl.baleen.types.language.Sentence;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.types.semantic.ComplexEvent;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.uima.utils.UimaTypesUtils;

public class CsvEventEvalationConsumer extends AbstractCsvConsumer {

	@Override
	public void doInitialize(UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);

		write("file", "sentence", "type", "words", "Entities then arguments...");

	}

	@Override
	protected void write(JCas jCas) {
		String source = getDocumentAnnotation(jCas).getSourceUri();

		Map<ComplexEvent, Collection<Sentence>> coveringSentence = JCasUtil.indexCovering(jCas, ComplexEvent.class,
				Sentence.class);

		JCasUtil.select(jCas, ComplexEvent.class).stream().map(e -> {

			String sentence = "";
			Collection<Sentence> sentences = coveringSentence.get(e);
			if (!sentences.isEmpty()) {
				sentence = sentences.iterator().next().getCoveredText();
			} else {
				// TODO: How could this be null?
				return null;
			}

			List<String> list = new ArrayList<>();
			list.add(source);
			list.add(sentence);

			if (e.getEventType() != null) {
				list.add(Arrays.stream(UimaTypesUtils.toArray(e.getEventType()))
						.collect(Collectors.joining(",")));
			} else {
				list.add("");
			}

			if (e.getTokens() != null) {
				list.add(Arrays.stream(e.getTokens().toArray())
						.map(w -> ((WordToken) w).getCoveredText())
						.map(this::normalize)
						.collect(Collectors.joining(" ")));
			} else {
				list.add("");
			}

			if (e.getEntities() != null && e.getEntities().size() > 0) {
				Arrays.stream(e.getEntities().toArray())
						.forEach(x -> {
					Entity t = (Entity) x;
					list.add(normalize(t.getCoveredText()));
				});
			}

			if (e.getArguments() != null && e.getArguments().size() > 0) {
				Arrays.stream(e.getArguments().toArray())
						.map(this::normalize)
						.forEach(list::add);
			}

			return list.toArray(new String[list.size()]);
		}).filter(Objects::nonNull).forEach(this::write);

	}

}
