package com.tenode.baleen.extras.common.jcas;

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
			final Entity instance = entity.getClass().getConstructor(JCas.class).newInstance(jCas);

			instance.setBegin(begin);
			instance.setEnd(end);
			instance.setReferent(entity.getReferent());
			instance.setValue(entity.getValue());
			return instance;
		} catch (final Exception e) {
			return null;
		}

	}

	/**
	 * Are any entities in the entities collection covering the range begin to end?
	 *
	 * @param entities
	 *            the entities
	 * @param begin
	 *            the begin
	 * @param end
	 *            the end
	 * @return true, if successful
	 */
	public static boolean existingEntity(Collection<Entity> entities, int begin, int end) {
		return entities.stream()
				.anyMatch(e -> e.getBegin() <= begin && end <= e.getEnd());
	}

	/**
	 * Does the entities collection contain an entity coveringthe range begin-end of the correct
	 * class?
	 *
	 * @param entities
	 *            the entities
	 * @param begin
	 *            the begin
	 * @param end
	 *            the end
	 * @param clazz
	 *            the clazz
	 * @return true, if successful
	 */
	public static boolean existingEntity(Collection<Entity> entities, int begin, int end,
			Class<? extends Entity> clazz) {
		return entities.stream()
				.anyMatch(e -> e.getBegin() <= begin && end <= e.getEnd() && clazz.isInstance(e));
	}

	public static boolean overlaps(Base a, Base b) {
		return !(a.getEnd() < b.getBegin() || b.getEnd() < a.getBegin());
	}

}
