package com.tenode.baleen.extras.annotators.relationships;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import com.google.common.base.Objects;
import com.tenode.baleen.extras.common.annotators.SpanUtils;

import uk.gov.dstl.baleen.types.language.Interaction;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.Relation;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

public abstract class AbstractInteractionBasedRelationshipAnnotator extends BaleenAnnotator {

	@Override
	protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {

		try {
			preExtract(jCas);

			extract(jCas);

		} finally {
			postExtract(jCas);
		}

	}

	protected abstract void extract(JCas jCas);

	protected void preExtract(final JCas jCas) {
		// Do nothing
	}

	protected void postExtract(final JCas jCas) {
		// Do nothing
	}

	protected void addRelationsToIndex(final Stream<Relation> relations) {
		if (relations != null) {
			relations
					// Only add events aren't in the same
					// Prevents overlapping spans since that makes no sense
					.filter(r -> r.getSource().getInternalId() != r.getTarget().getInternalId()
							&& !SpanUtils.overlaps(r.getSource(), r.getTarget()))
					// Discard anything which has no relationship type
					// TODO: Is this sensible, these are direct connection between A and
					// B for the dependency graph (you can't be more connected than
					// that) but then you have no relationship text to work with.
					.filter(r -> r.getRelationshipType() != null
							|| !StringUtils.isBlank(r.getRelationshipType()))
					.forEach(this::addToJCasIndex);
		}
	}

	protected Relation createRelation(final JCas jCas, final Interaction interaction, final Entity source,
			final Entity target) {
		final Relation r = new Relation(jCas);
		r.setBegin(interaction.getBegin());
		r.setEnd(interaction.getEnd());
		r.setRelationshipType(interaction.getRelationshipType());
		r.setRelationSubType(interaction.getRelationSubType());
		r.setSource(source);
		r.setTarget(target);
		r.setValue(interaction.getValue());
		return r;
	}

	protected Stream<Relation> createPairwiseRelations(final JCas jCas, final Interaction interaction,
			final List<Entity> sources,
			final List<Entity> targets) {
		return sources.stream().flatMap(l -> {
			return targets.stream().map(r -> {
				return createRelation(jCas, interaction, l, r);
			});
		});
	}

	protected Stream<Relation> createMeshedRelations(final JCas jCas, final Interaction interaction,
			final Collection<Entity> collection) {

		final List<Relation> relations = new LinkedList<>();

		List<Entity> entities;
		if (collection instanceof List) {
			entities = (List<Entity>) collection;
		} else {
			entities = new ArrayList<>(collection);
		}

		ListIterator<Entity> outer = entities.listIterator();
		while (outer.hasNext()) {
			final Entity source = outer.next();

			ListIterator<Entity> inner = entities.listIterator(outer.nextIndex());
			while (inner.hasNext()) {
				final Entity target = inner.next();

				relations.add(createRelation(jCas, interaction, source, target));
			}
		}

		return relations.stream();
	}

	protected Stream<Relation> distinct(final Stream<Relation> stream) {
		return stream.map(RelationWrapper::new)
				.distinct()
				.map(RelationWrapper::getRelation);
	}

	private static class RelationWrapper {
		private final Relation relation;

		public RelationWrapper(final Relation relation) {
			this.relation = relation;
		}

		public Relation getRelation() {
			return relation;
		}

		// We specifically equals / hashcode on the specific aspects of Relation

		// TODO: We don't mind about the begin/end value as long as the relation specific info is
		// the same?

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof RelationWrapper)) {
				return false;
			} else {
				final Relation or = ((RelationWrapper) other).getRelation();
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
