package com.tenode.baleen.annotators.documentcoreference;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import com.google.common.collect.Multimap;
import com.tenode.baleen.extras.common.jcas.ReferentUtils;
import com.tenode.baleen.extras.common.jcas.SpanUtils;

import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

/**
 * Convert non-entity annotations into entity annotations, in the case that an annotation has a
 * referent target that is shared with an entity.
 * <p>
 * This is useful for consumers that work specifically with entities but not with other types.
 *
 * @baleen.javadoc
 */
public class ReferentToEntity extends BaleenAnnotator {

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.gov.dstl.baleen.uima.BaleenAnnotator#doProcess(org.apache.uima.jcas.JCas)
	 */
	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {

		final Multimap<ReferenceTarget, Entity> referentMap = ReferentUtils.createReferentMap(jCas, Entity.class);

		final Collection<Entity> entities = new HashSet<>(JCasUtil.select(jCas, Entity.class));

		final Map<ReferenceTarget, Entity> targets = ReferentUtils.filterToSingle(referentMap, this::getBestEntity);

		// Now look through the non-entities and create entities in their place.

		final List<Entity> toAdd = ReferentUtils.streamReferent(jCas, targets)
				.map(a -> {
					final ReferenceTarget referent = a.getReferent();
					final Entity entity = targets.get(referent);
					if (entity != null && !entities.contains(a)) {
						return SpanUtils.copyEntity(jCas, a.getBegin(), a.getEnd(), entity);
					} else {
						return null;
					}
				}).filter(Objects::nonNull)
				.collect(Collectors.toList());

		addToJCasIndex(toAdd);

	}

	/**
	 * Gets the best entity from the list.
	 *
	 * @param list
	 *            the list
	 * @return the best entity
	 */
	private Entity getBestEntity(Collection<Entity> list) {
		return list.stream()
				.reduce((a, b) -> isBetterEntity(a, b) ? b : a)
				.get();
	}

	/**
	 * Checks if is better entity.
	 *
	 * @param original
	 *            the original
	 * @param challenger
	 *            the challenger
	 * @return true, if is better entity
	 */
	private boolean isBetterEntity(Entity original, Entity challenger) {
		// Simple version, just look for the longest string
		// we could look at how complete the attributes are, etc
		return original.getValue().length() < challenger.getValue().length();
	}

}
