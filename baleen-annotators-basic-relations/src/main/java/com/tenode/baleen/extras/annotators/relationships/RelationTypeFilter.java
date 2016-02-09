package com.tenode.baleen.extras.annotators.relationships;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Strings;
import com.mongodb.DBCollection;

import uk.gov.dstl.baleen.resources.SharedMongoResource;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.Relation;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

public class RelationTypeFilter extends BaleenAnnotator {

	/**
	 * Connection to Mongo
	 *
	 * @baleen.resource uk.gov.dstl.baleen.resources.SharedMongoResource
	 */
	public static final String KEY_MONGO = "mongo";
	@ExternalResource(key = KEY_MONGO)
	private SharedMongoResource mongo;

	/**
	 * The name of the Mongo collection containing the relation types
	 *
	 * @baleen.config gazetteer
	 */
	public static final String PARAM_COLLECTION = "collection";
	@ConfigurationParameter(name = PARAM_COLLECTION, defaultValue = "relationTypes")
	private String collection;

	/**
	 * The name of the field in Mongo that contains the relation type
	 *
	 * @baleen.config type
	 */
	public static final String PARAM_TYPE_FIELD = "typeField";
	@ConfigurationParameter(name = PARAM_TYPE_FIELD, defaultValue = "type")
	private String typeField;

	/**
	 * The name of the field in Mongo that contains the relation source type
	 *
	 * @baleen.config source
	 */
	public static final String PARAM_SOURCE_FIELD = "typeField";
	@ConfigurationParameter(name = PARAM_SOURCE_FIELD, defaultValue = "source")
	private String sourceField;

	/**
	 * The name of the field in Mongo that contains the relation source type
	 *
	 * @baleen.config target
	 */
	public static final String PARAM_TARGET_FIELD = "typeField";
	@ConfigurationParameter(name = PARAM_TARGET_FIELD, defaultValue = "target")
	private String targetField;

	/**
	 * Determines strictness of filtering.
	 *
	 * In strict mode the relationsship type must be defined and the source and target type the same
	 * in order to pass the filter. In non-strict mode, if the relationship type has no constraints
	 * then the relationship will pass. If the relationship type has constraints then these must be
	 * adhered too.
	 *
	 * @baleen.config false
	 */
	public static final String PARAM_STRICT = "strict";
	@ConfigurationParameter(name = RelationTypeFilter.PARAM_STRICT, defaultValue = "false")
	private boolean strict;

	/**
	 * Determines if relations can be considered symmetric (source and target swapped)
	 *
	 * @baleen.config true
	 */
	public static final String PARAM_SYMMETRIC = "symmetric";
	@ConfigurationParameter(name = RelationTypeFilter.PARAM_SYMMETRIC, defaultValue = "true")
	private boolean symetric;

	private final Map<String, Set<RelationConstraint>> constraints = new HashMap<>();

	@Override
	public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);

		final DBCollection dbCollection = mongo.getDB().getCollection(collection);

		dbCollection.find().forEach(o -> {
			final RelationConstraint constraint = new RelationConstraint((String) o.get(typeField),
					(String) o.get(sourceField),
					(String) o.get(targetField));

			if (constraint.isValid()) {
				Set<RelationConstraint> set = constraints.get(constraint.getType());
				if (set == null) {
					set = new HashSet<>();
					constraints.put(constraint.getType(), set);
				}
				set.add(constraint);
			}

		});
	}

	@Override
	protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {
		for (final Relation relation : JCasUtil.select(jCas, Relation.class)) {

			final Set<RelationConstraint> rcs = constraints.get(relation.getRelationshipType());

			boolean remove;
			if (rcs == null || rcs.isEmpty()) {

				// In strict mode we remove
				if (strict) {
					remove = true;
				} else {
					remove = false;
				}

			} else {
				remove = check(rcs, relation);
			}

			if (remove) {
				removeFromJCasIndex(relation);
			}
		}
	}

	private boolean check(final Set<RelationConstraint> rcs, final Relation relation) {
		return rcs.stream().anyMatch(p -> {
			return p.matches(relation, symetric);
		});
	}

	private static class RelationConstraint {
		private final String type;
		private final String source;
		private final String target;

		private RelationConstraint(final String type, final String source, final String target) {
			this.type = type;
			this.source = source;
			this.target = target;

		}

		public String getType() {
			return type;
		}

		public boolean isValid() {
			return !Strings.isNullOrEmpty(type) && !Strings.isNullOrEmpty(source) && !Strings.isNullOrEmpty(target);
		}

		public boolean matches(final Relation relation, final boolean symmetric) {
			final Entity sourceEntity = relation.getSource();
			final Entity targetEntity = relation.getTarget();

			// TODO: Allow inheritence here?
			// TODO: Use the full name (and/or allow short type for baleen types only)

			final String sourceType = sourceEntity.getTypeName();
			final String targetType = targetEntity.getTypeName();

			if (!symmetric) {
				return sourceType.equalsIgnoreCase(source) && targetType.equalsIgnoreCase(target);
			} else {
				return sourceType.equalsIgnoreCase(source) && targetType.equalsIgnoreCase(target)
						|| sourceType.equalsIgnoreCase(target) && targetType.equalsIgnoreCase(source);
			}

		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (source == null ? 0 : source.hashCode());
			result = prime * result + (target == null ? 0 : target.hashCode());
			result = prime * result + (type == null ? 0 : type.hashCode());
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
			RelationConstraint other = (RelationConstraint) obj;
			if (source == null) {
				if (other.source != null) {
					return false;
				}
			} else if (!source.equals(other.source)) {
				return false;
			}
			if (target == null) {
				if (other.target != null) {
					return false;
				}
			} else if (!target.equals(other.target)) {
				return false;
			}
			if (type == null) {
				if (other.type != null) {
					return false;
				}
			} else if (!type.equals(other.type)) {
				return false;
			}
			return true;
		}

	}
}
