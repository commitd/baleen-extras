package com.tenode.baleen.annotators.coreference;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.types.Base;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

public class ReferentToEntity extends BaleenAnnotator {

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {

		Collection<Base> potentialReferences = JCasUtil.select(jCas, Base.class);

		Map<ReferenceTarget, Entity> targets = new HashMap<>();
		// First we create a map of all the targets

		potentialReferences.stream()
				.filter(p -> (p instanceof Entity))
				.filter(p -> p.getReferent() != null)
				.forEach(e -> {
					Entity entity = (Entity) e;
					ReferenceTarget referent = e.getReferent();
					Entity existing = targets.get(referent);
					if (existing == null || isBetterEntity(existing, entity)) {
						targets.put(referent, entity);
					}
				});

		// Now look through the non-entities and create entities in their place.

		List<Entity> toAdd = potentialReferences.stream()
				.filter(p -> !(p instanceof Entity))
				.filter(p -> p.getReferent() != null)
				.map(a -> {
					System.out.println(a.getCoveredText());
					ReferenceTarget referent = a.getReferent();
					Entity entity = targets.get(referent);
					if (entity != null) {
						System.out.println(entity.getCoveredText());
						return copyEntity(jCas, a.getBegin(), a.getEnd(), entity);
					} else {
						return null;
					}
				}).filter(Objects::nonNull)
				.collect(Collectors.toList());

		addToJCasIndex(toAdd);

	}

	private Entity copyEntity(JCas jCas, int begin, int end, Entity entity) {
		// TODO: This could be better, but would suggest if better is need

		try {
			Entity instance = entity.getClass().getConstructor(JCas.class).newInstance(jCas);

			instance.setBegin(begin);
			instance.setEnd(end);
			instance.setReferent(entity.getReferent());
			instance.setValue(entity.getValue());
			return instance;
		} catch (Exception e) {
			getMonitor().warn("Unable to create an entity of type {}", entity.getTypeName());
			return null;
		}

	}

	private boolean isBetterEntity(Entity original, Entity challenger) {
		// Simple version, just look for the longest string
		// we could look at how complete the attributes are, etc
		return original.getValue().length() < challenger.getValue().length();
	}

}
