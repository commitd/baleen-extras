package com.tenode.baleen.annotators.coreference.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

import uk.gov.dstl.baleen.types.Base;
import uk.gov.dstl.baleen.types.language.PhraseChunk;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.types.semantic.Entity;

/**
 * A Mention which can be coreference.
 *
 */
public class Mention {

	/** The annotation. */
	private final Base annotation;

	/** The type. */
	private final MentionType type;

	/** The clusters. */
	private final Set<Cluster> clusters = new HashSet<>();

	private Set<String> acronyms;

	private WordToken headWordToken;

	private List<WordToken> words;

	private Person person = Person.UNKNOWN;

	private Animacy animacy = Animacy.UNKNOWN;

	private Gender gender = Gender.UNKNOWN;

	private Multiplicity multiplicity = Multiplicity.UNKNOWN;

	private Mention(Base annotation, MentionType type) {
		this.annotation = annotation;
		this.type = type;
	}

	/**
	 * Instantiates a new mention.
	 *
	 * @param annotation
	 *            the annotation
	 */
	public Mention(WordToken annotation) {
		this(annotation, MentionType.PRONOUN);
	}

	public Mention(Entity annotation) {
		this(annotation, MentionType.ENTITY);
	}

	public Mention(PhraseChunk annotation) {
		this(annotation, MentionType.NP);
	}

	/**
	 * Gets the annotation.
	 *
	 * @return the annotation
	 */
	public Base getAnnotation() {
		return annotation;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public MentionType getType() {
		return type;
	}

	public Set<Cluster> getClusters() {
		return clusters;
	}

	public boolean hasClusters() {
		return !clusters.isEmpty();
	}

	public Cluster getAnyCluster() {
		if (!clusters.isEmpty()) {
			return clusters.iterator().next();
		} else {
			return null;
		}
	}

	/**
	 * Adds the to cluster - use cluster.add(mention) as this will not update the cluster.
	 *
	 * @param cluster
	 *            the cluster
	 */
	public void addToCluster(Cluster cluster) {
		clusters.add(cluster);
	}

	/**
	 * Clear clusters - should not be used outside Coreference (will not remove from the cluster)
	 */
	public void clearClusters() {
		clusters.clear();
	}

	public String getText() {
		return annotation.getCoveredText();
	}

	public void setHeadWordToken(WordToken headWordToken) {
		this.headWordToken = headWordToken;
	}

	public WordToken getHeadWordToken() {
		return headWordToken;
	}

	public String getHead() {
		return getHeadWordToken() != null ? getHeadWordToken().getCoveredText() : null;
	}

	public boolean isAcronym() {
		return !StringUtils.containsWhitespace(getText())
				&& org.apache.commons.lang3.StringUtils.isAllUpperCase(getText());
	}

	public void setAcronym(Set<String> acronyms) {
		this.acronyms = acronyms;
	}

	public Set<String> getAcronyms() {
		return acronyms;
	}

	public boolean overlaps(Mention mention) {
		Base a = mention.getAnnotation();
		Base b = mention.getAnnotation();
		return !(a.getEnd() < b.getBegin() || b.getEnd() < a.getBegin());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getText() + "[" + type + "]";
	}

	public void setMultiplicity(Multiplicity multiplicity) {
		this.multiplicity = multiplicity;
	}

	public Multiplicity getMultiplicity() {
		return multiplicity;
	}

	public void setWords(List<WordToken> words) {
		this.words = words;
	}

	public List<WordToken> getWords() {
		return words;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Person getPerson() {
		return person;
	}

	public void setAnimacy(Animacy animacy) {
		this.animacy = animacy;
	}

	public Animacy getAnimacy() {
		return animacy;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}
}
