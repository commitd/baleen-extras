package com.tenode.baleen.extras.common.annotators;

import java.util.Collection;

import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.types.Base;
import uk.gov.dstl.baleen.types.semantic.Entity;

public class SpanUtils {

	private SpanUtils() {
		// Singleton
	}

	public static Entity copyEntity(JCas jCas, int begin, int end, Entity entity) {
		// TODO: This could be better, but would suggest if better is need

		try {
			Entity instance = entity.getClass().getConstructor(JCas.class).newInstance(jCas);

			instance.setBegin(begin);
			instance.setEnd(end);
			instance.setReferent(entity.getReferent());
			instance.setValue(entity.getValue());
			return instance;
		} catch (Exception e) {
			return null;
		}

	}

	public static boolean existingEntity(Collection<Entity> entities, int begin, int end) {
		return entities.stream()
				.anyMatch(e -> e.getBegin() <= begin && end <= e.getEnd());
	}

	public static boolean existingEntity(Collection<Entity> entities, int begin, int end,
			Class<? extends Entity> clazz) {
		return entities.stream()
				.anyMatch(e -> e.getBegin() <= begin && end <= e.getEnd() && clazz.isInstance(e));
	}

	public static boolean overlaps(Base a, Base b) {
		return !(a.getEnd() < b.getBegin() || b.getEnd() < a.getBegin());
	}

}
