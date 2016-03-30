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

/**
 * Write events to CSV for evaluation purposes.
 * <p>
 * Format is:
 * <ul>
 * <li>source
 * <li>sentence
 * <li>type
 * <li>Words
 * <li>Entities
 * <li>Arguments...
 *
 */
public class CsvEventEvalationConsumer extends AbstractCsvConsumer {

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

		write("source", "sentence", "type", "words", "Entities then arguments...");

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

		final Map<ComplexEvent, Collection<Sentence>> coveringSentence = JCasUtil.indexCovering(jCas,
				ComplexEvent.class,
				Sentence.class);

		JCasUtil.select(jCas, ComplexEvent.class).stream()
				.map(e -> extracted(source, coveringSentence, e))
				.filter(Objects::nonNull)
				.forEach(this::write);

	}

	private String[] extracted(final String source, final Map<ComplexEvent, Collection<Sentence>> coveringSentence,
			ComplexEvent e) {
		String sentence = "";
		final Collection<Sentence> sentences = coveringSentence.get(e);
		if (!sentences.isEmpty()) {
			sentence = sentences.iterator().next().getCoveredText();
		} else {
			// This shouldn't be empty, unless you have no sentence annotation
			return null;
		}

		final List<String> list = new ArrayList<>();
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
						final Entity t = (Entity) x;
						list.add(normalize(t.getCoveredText()));
					});
		}

		if (e.getArguments() != null && e.getArguments().size() > 0) {
			Arrays.stream(e.getArguments().toArray())
					.map(this::normalize)
					.forEach(list::add);
		}

		return list.toArray(new String[list.size()]);
	}

}
