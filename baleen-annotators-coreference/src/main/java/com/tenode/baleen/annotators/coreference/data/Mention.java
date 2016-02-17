package com.tenode.baleen.annotators.coreference.data;

import java.util.HashSet;
import java.util.Set;

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

	private String head;

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

	public String getHead() {
		return head;
	}

	public void setHead(String head) {
		this.head = head;
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

}
