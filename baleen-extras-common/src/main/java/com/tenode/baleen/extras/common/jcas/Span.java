package com.tenode.baleen.extras.common.jcas;

import uk.gov.dstl.baleen.types.semantic.Entity;

// NOTE: Entity is specifically excluded from the equals / hashcode so that we get uniqueness
// based on span and type alone.
public class Span {

	private final int begin;

	private final int end;

	private final Class<? extends Entity> clazz;

	private final Entity entity;

	public Span(Entity entity, int begin, int end) {
		this.entity = entity;
		this.clazz = entity.getClass();
		this.begin = begin;
		this.end = end;
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public Entity getEntity() {
		return entity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + begin;
		result = prime * result + (clazz == null ? 0 : clazz.hashCode());
		result = prime * result + end;
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
		final Span other = (Span) obj;
		if (begin != other.begin) {
			return false;
		}
		if (clazz == null) {
			if (other.clazz != null) {
				return false;
			}
		} else if (!clazz.equals(other.clazz)) {
			return false;
		}
		if (end != other.end) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s[%d,%d]", getClass().getSimpleName(), begin, end);
	}

}