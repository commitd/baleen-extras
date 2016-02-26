package com.tenode.baleen.extras.jobs.interactions.data;

public class InteractionRelation {
	private final String type;

	private final String subType;

	private final Word word;

	private final String source;

	private final String target;

	public InteractionRelation(String type, String subType, Word word, String source, String target) {
		this.type = type;
		this.subType = subType;
		this.word = word;
		this.source = source;
		this.target = target;
	}

	public String getType() {
		return type;
	}

	public String getSubType() {
		return subType;
	}

	public String getSource() {
		return source;
	}

	public String getTarget() {
		return target;
	}

	public Word getWord() {
		return word;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (source == null ? 0 : source.hashCode());
		result = prime * result + (subType == null ? 0 : subType.hashCode());
		result = prime * result + (target == null ? 0 : target.hashCode());
		result = prime * result + (type == null ? 0 : type.hashCode());
		result = prime * result + (word == null ? 0 : word.hashCode());
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
		InteractionRelation other = (InteractionRelation) obj;
		if (source == null) {
			if (other.source != null) {
				return false;
			}
		} else if (!source.equals(other.source)) {
			return false;
		}
		if (subType == null) {
			if (other.subType != null) {
				return false;
			}
		} else if (!subType.equals(other.subType)) {
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
		if (word == null) {
			if (other.word != null) {
				return false;
			}
		} else if (!word.equals(other.word)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return word.getLemma();
	}

}
