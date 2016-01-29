package com.tenode.baleen.extra.annotators.relationships;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import com.google.common.base.Objects;

import uk.gov.dstl.baleen.types.language.Interaction;
import uk.gov.dstl.baleen.types.language.Sentence;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.Relation;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

public abstract class AbstractInteractionBasedRelationshipAnnotator extends BaleenAnnotator {

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {

		try {
			preExtract(jCas);

			for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {

				List<Interaction> interactions = JCasUtil.selectCovered(jCas, Interaction.class, sentence);
				List<Entity> entities = JCasUtil.selectCovered(jCas, Entity.class, sentence);

				// Check we have enough in the sentence to warrant further work
				if (!interactions.isEmpty() && entities.size() >= 2) {
					Stream<Relation> relations = extract(jCas, sentence, interactions, entities);

					if (relations != null) {
						relations.forEach(r -> {
							addToJCasIndex(r);
						});
					}
				}
			}
		} finally {
			postExtract(jCas);
		}
	}

	protected void preExtract(JCas jCas) {
		// Do nothing
	}

	protected void postExtract(JCas jCas) {
		// Do nothing
	}

	protected Relation createRelation(JCas jCas, Interaction interaction, Entity source, Entity target) {
		Relation r = new Relation(jCas);
		r.setBegin(interaction.getBegin());
		r.setEnd(interaction.getEnd());
		r.setRelationshipType(interaction.getRelationshipType());
		r.setRelationSubType(interaction.getRelationSubType());
		r.setSource(source);
		r.setTarget(target);
		return r;
	}

	protected Stream<Relation> createPairwiseRelations(JCas jCas, Interaction interaction, List<Entity> sources,
			List<Entity> targets) {
		return sources.stream().flatMap(l -> {
			return targets.stream().map(r -> {
				return createRelation(jCas, interaction, l, r);
			});
		});
	}

	protected Stream<Relation> createMeshedRelations(JCas jCas, Interaction interaction, List<Entity> entities) {

		List<Relation> relations = new LinkedList<>();

		for (int i = 0; i < entities.size(); i++) {
			for (int j = i + 1; j < entities.size(); j++) {

				Entity source = entities.get(i);
				Entity target = entities.get(j);

				relations.add(createRelation(jCas, interaction, source, target));
			}
		}

		return relations.stream();
	}

	protected abstract Stream<Relation> extract(JCas jCas, Sentence sentence, List<Interaction> interactions,
			List<Entity> entities);

	protected Stream<Relation> distinct(Stream<Relation> stream) {
		return stream.map(RelationWrapper::new)
				.distinct()
				.map(RelationWrapper::getRelation);
	}

	private static class RelationWrapper {
		private final Relation relation;

		public RelationWrapper(Relation relation) {
			this.relation = relation;
		}

		public Relation getRelation() {
			return relation;
		}

		// We specifically equals / hashcode on the specific aspects of Relation

		// TODO: We don't mind about the begin/end value as long as the relation specific info is
		// the same?

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof RelationWrapper)) {
				return false;
			} else {
				Relation or = ((RelationWrapper) other).getRelation();
				if (relation.getBegin() != or.getBegin()) {
					return false;
				} else if (relation.getEnd() != or.getEnd()) {
					return false;
				} else if (relation.getRelationshipType() != or.getRelationshipType()) {
					return false;
				} else if (relation.getRelationSubType() != or.getRelationSubType()) {
					return false;
				} else if (relation.getSource() != or.getSource()) {
					return false;
				} else if (relation.getTarget() != or.getTarget()) {
					return false;
				} else {
					return true;
				}
			}
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(relation.getBegin(), relation.getEnd(), relation.getRelationshipType(),
					relation.getRelationSubType(), relation.getSource(), relation.getTarget());
		}

	}
}
