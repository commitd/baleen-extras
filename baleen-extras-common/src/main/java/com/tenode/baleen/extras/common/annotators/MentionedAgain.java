package com.tenode.baleen.extras.common.annotators;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

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
		String text = jCas.getDocumentText();

		Collection<Entity> list = JCasUtil.select(jCas, Entity.class);

		Set<Span> spans = new HashSet<>(list.size());

		list.stream()
				.forEach(e -> {
					Pattern pattern = Pattern.compile("\\b" + Pattern.quote(e.getCoveredText()) + "\\b");
					Matcher matcher = pattern.matcher(text);
					while (matcher.find()) {
						if (!SpanUtils.existingEntity(list, matcher.start(), matcher.end(), e.getClass())) {
							spans.add(new Span(e, matcher.start(), matcher.end()));
						}
					}
				});

		spans.stream().forEach(s -> {
			Entity newEntity = SpanUtils.copyEntity(jCas, s.getBegin(), s.getEnd(), s.getEntity());

			if (s.getEntity().getReferent() == null) {
				// Make them the same
				ReferenceTarget rt = new ReferenceTarget(jCas);
				addToJCasIndex(rt);

				s.getEntity().setReferent(rt);
			}

			newEntity.setReferent(s.getEntity().getReferent());

			addToJCasIndex(newEntity);
		});
	}

	// NOTE: Entity is specifically excluded from the equals / hashcode so that we get uniqueness
	// based on span and type alone.
	private static class Span {

		private final int begin;

		private final int end;

		private final Class<? extends Entity> clazz;

		private final Entity entity;

		public Span(Entity entity, int begin, int end) {
			this.entity = entity;
			this.clazz = entity.getClass();
			this.begin = begin;
			this.end = end;
		}

		public int getBegin() {
			return begin;
		}

		public int getEnd() {
			return end;
		}

		public Class<?> getClazz() {
			return clazz;
		}

		public Entity getEntity() {
			return entity;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + begin;
			result = prime * result + (clazz == null ? 0 : clazz.hashCode());
			result = prime * result + end;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Span other = (Span) obj;
			if (begin != other.begin) {
				return false;
			}
			if (clazz == null) {
				if (other.clazz != null) {
					return false;
				}
			} else if (!clazz.equals(other.clazz)) {
				return false;
			}
			if (end != other.end) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return String.format("%s[%d,%d]", getClass().getSimpleName(), begin, end);
		}

	}
}
