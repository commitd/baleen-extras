package com.tenode.baleen.extras.common.annotators;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import com.tenode.baleen.extras.common.jcas.Span;
import com.tenode.baleen.extras.common.jcas.SpanUtils;

import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

/**
 * Creates entity annotations for each piece of text which is the same as the covered text.
 *
 * This is useful when a model is used (rather than a regex) and it only find a subset of the
 * mentions in a document.
 *
 * It an annotation of the same type already exists on the covering text then another is not added.
 *
 */
public class MentionedAgain extends BaleenAnnotator {

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {
		final String text = jCas.getDocumentText();

		final Collection<Entity> list = JCasUtil.select(jCas, Entity.class);

		final Set<Span> spans = new HashSet<>(list.size());

		list.stream()
				.forEach(e -> {
					final Pattern pattern = Pattern.compile("\\b" + Pattern.quote(e.getCoveredText()) + "\\b");
					final Matcher matcher = pattern.matcher(text);
					while (matcher.find()) {
						if (!SpanUtils.existingEntity(list, matcher.start(), matcher.end(), e.getClass())) {
							spans.add(new Span(e, matcher.start(), matcher.end()));
						}
					}
				});

		spans.stream().forEach(s -> {
			final Entity newEntity = SpanUtils.copyEntity(jCas, s.getBegin(), s.getEnd(), s.getEntity());

			if (s.getEntity().getReferent() == null) {
				// Make them the same
				final ReferenceTarget rt = new ReferenceTarget(jCas);
				addToJCasIndex(rt);

				s.getEntity().setReferent(rt);
			}

			newEntity.setReferent(s.getEntity().getReferent());

			addToJCasIndex(newEntity);
		});
	}

}
