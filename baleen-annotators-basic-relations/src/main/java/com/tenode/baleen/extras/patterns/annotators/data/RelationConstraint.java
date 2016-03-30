package com.tenode.baleen.extras.patterns.annotators.data;

import java.util.Collection;

import com.google.common.base.Strings;

import uk.gov.dstl.baleen.types.language.Interaction;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.Relation;

/**
 * A constraint which applies to a relation, constraining its type, source entity type, and target
 * entity type.
 */
public class RelationConstraint {
	private final String type;
	private final String source;
	private final String target;
	private final String pos;
	private final String subType;
	private final char posChar;

	/**
	 * Instantiates a new relation constraint.
	 *
	 * @param type
	 *            the type
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 */
	public RelationConstraint(final String type, final String subType, final String pos, final String source,
			final String target) {
		this.type = type;
		this.subType = subType;
		this.pos = pos;
		if (pos != null && pos.length() > 0) {
			this.posChar = Character.toLowerCase(pos.charAt(0));
		} else {
			this.posChar = '?';
		}
		this.source = source;
		this.target = target;

	}

	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Gets the target.
	 *
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * Gets the POS.
	 *
	 * @return the part of speech
	 */
	public String getPos() {
		return pos;
	}

	/**
	 * Gets the sub type.
	 *
	 * @return the sub type
	 */
	public String getSubType() {
		return subType;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Checks if this instance is valid.
	 *
	 * @return true, if is valid
	 */
	public boolean isValid() {
		return !Strings.isNullOrEmpty(type) && !Strings.isNullOrEmpty(subType) && !Strings.isNullOrEmpty(pos)
				&& !Strings.isNullOrEmpty(source) && !Strings.isNullOrEmpty(target);
	}

	/**
	 * Check is the relation provided matches this constraint.
	 *
	 * No inheritence and matches the full (not short) type name.
	 *
	 * @param relation
	 *            the relation
	 * @param symmetric
	 *            the symmetric (ie source and type can be swapped)
	 * @return true, if successful
	 */
	public boolean matches(final Relation relation, final boolean symmetric) {
		final Entity sourceEntity = relation.getSource();
		final Entity targetEntity = relation.getTarget();

		final String sourceType = sourceEntity.getTypeName();
		final String targetType = targetEntity.getTypeName();

		final boolean relationType = type.equalsIgnoreCase(relation.getRelationshipType())
				&& subType.equalsIgnoreCase(relation.getRelationSubType());

		if (!symmetric) {
			return relationType && sourceType.equalsIgnoreCase(source) && targetType.equalsIgnoreCase(target);
		} else {
			return relationType && (sourceType.equalsIgnoreCase(source) && targetType.equalsIgnoreCase(target)
					|| sourceType.equalsIgnoreCase(target) && targetType.equalsIgnoreCase(source));
		}

	}

	public boolean matches(Interaction interaction, Collection<WordToken> words) {
		String interactionType = interaction.getRelationshipType();
		String interactionSubType = interaction.getRelationSubType();

		boolean typeMatch = type.equalsIgnoreCase(interactionType) && subType.equalsIgnoreCase(interactionSubType);
		if (words == null || words.isEmpty()) {
			return typeMatch;
		} else {
			return typeMatch && words.stream()
					.anyMatch(w -> posChar == Character.toLowerCase(w.getPartOfSpeech().charAt(0)));
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (source == null ? 0 : source.hashCode());
		result = prime * result + (target == null ? 0 : target.hashCode());
		result = prime * result + (type == null ? 0 : type.hashCode());
		result = prime * result + (subType == null ? 0 : subType.hashCode());
		result = prime * result + (pos == null ? 0 : pos.hashCode());

		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
		final RelationConstraint other = (RelationConstraint) obj;
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
		if (subType == null) {
			if (other.subType != null) {
				return false;
			}
		} else if (!subType.equals(other.subType)) {
			return false;
		}
		if (pos == null) {
			if (other.pos != null) {
				return false;
			}
		} else if (!pos.equals(other.pos)) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.source + "-" + this.type + "-" + this.target;
	}

}