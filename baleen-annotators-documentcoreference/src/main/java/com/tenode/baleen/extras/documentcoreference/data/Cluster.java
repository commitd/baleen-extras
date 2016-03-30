package com.tenode.baleen.extras.documentcoreference.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A cluster of mentions.
 * <p>
 * All mentions in a cluster are considered to be coreferent.
 */
public class Cluster {

	// We require a set which maintains order, earlier entries (from earler passes) are likely more
	// accurate than others
	private final Set<Mention> mentions = new LinkedHashSet<>();

	public Cluster() {
		// Do nothing
	}

	public Cluster(Mention m) {
		mentions.add(m);
	}

	public Cluster(Mention... array) {
		addAll(Arrays.asList(array));
	}

	public boolean contains(Mention mention) {
		return mentions.contains(mention);
	}

	public Set<Mention> getMentions() {
		return mentions;
	}

	public void add(Mention mention) {
		mentions.add(mention);
		mention.addToCluster(this);
	}

	public void addAll(Collection<Mention> collection) {
		mentions.addAll(collection);
		collection.forEach(m -> m.addToCluster(this));
	}

	public int getSize() {
		return mentions.size();
	}

	public void add(Cluster cluster) {
		mentions.addAll(cluster.getMentions());
	}

	public boolean intersects(Cluster cluster) {
		return mentions.stream()
				.anyMatch(cluster::contains);
	}

}
