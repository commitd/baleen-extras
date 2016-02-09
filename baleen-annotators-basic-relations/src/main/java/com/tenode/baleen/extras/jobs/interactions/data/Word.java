package com.tenode.baleen.extras.jobs.interactions.data;

import net.sf.extjwnl.data.POS;

public class Word {

	private final String lemma;
	private final POS pos;

	public Word(String lemma, POS pos) {
		this.lemma = lemma;
		this.pos = pos;
	}

	public String getLemma() {
		return lemma;
	}

	public POS getPos() {
		return pos;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (lemma == null ? 0 : lemma.hashCode());
		result = prime * result + (pos == null ? 0 : pos.hashCode());
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
		Word other = (Word) obj;
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

	@Override
	public String toString() {
		return pos + ":" + lemma;
	}

}
