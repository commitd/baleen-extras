package com.tenode.baleen.extras.patterns.jobs.interactions.data;

import net.sf.extjwnl.data.POS;

/**
 * A simple representation of a word (as Lemma and Part of Speech).
 * <p>
 * Has sensible hashcode, equals and toString implementations.
 */
public class Word {

	private final String lemma;

	private final POS pos;

	/**
	 * Instantiates a new word.
	 *
	 * @param lemma
	 *            the lemma
	 * @param pos
	 *            the pos
	 */
	public Word(String lemma, POS pos) {
		this.lemma = lemma;
		this.pos = pos;
	}

	/**
	 * Gets the lemma.
	 *
	 * @return the lemma
	 */
	public String getLemma() {
		return lemma;
	}

	/**
	 * Gets the pos.
	 *
	 * @return the pos
	 */
	public POS getPos() {
		return pos;
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
		result = prime * result + (lemma == null ? 0 : lemma.hashCode());
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
		final Word other = (Word) obj;
		if (lemma == null) {
			if (other.lemma != null) {
				return false;
			}
		} else if (!lemma.equals(other.lemma)) {
			return false;
		}
		if (pos != other.pos) {
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
		return String.format("%s [%s]", lemma, pos);
	}

}
