package com.tenode.baleen.annotators.coreference;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import com.google.common.collect.Multimap;
import com.tenode.baleen.extras.common.annotators.SpanUtils;
import com.tenode.baleen.extras.common.jcas.ReferentUtils;

import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

public class ReferentToEntity extends BaleenAnnotator {

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {

		Multimap<ReferenceTarget, Entity> referentMap = ReferentUtils.createReferentMap(jCas, Entity.class);

		Map<ReferenceTarget, Entity> targets = ReferentUtils.filterToSingle(referentMap, this::getBestEntity);

		// Now look through the non-entities and create entities in their place.

		List<Entity> toAdd = ReferentUtils.streamReferent(jCas, targets)
				.map(a -> {
					ReferenceTarget referent = a.getReferent();
					Entity entity = targets.get(referent);
					if (entity != null) {
						return SpanUtils.copyEntity(jCas, a.getBegin(), a.getEnd(), entity);
					} else {
						return null;
					}
				}).filter(Objects::nonNull)
				.collect(Collectors.toList());

		addToJCasIndex(toAdd);

	}

	private Entity getBestEntity(Collection<Entity> list) {
		return list.stream().reduce((a, b) -> isBetterEntity(a, b) ? b : a).get();
	}

	private boolean isBetterEntity(Entity original, Entity challenger) {
		// Simple version, just look for the longest string
		// we could look at how complete the attributes are, etc
		return original.getValue().length() < challenger.getValue().length();
	}

}
