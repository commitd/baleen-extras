package com.tenode.baleen.annotators.coreference.sieves;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.uima.jcas.JCas;

import com.tenode.baleen.annotators.coreference.data.Cluster;
import com.tenode.baleen.annotators.coreference.data.Mention;

public abstract class AbstractCoreferenceSieve implements CoreferenceSieve {

	private final JCas jCas;
	private final List<Cluster> clusters;
	private final List<Mention> mentions;

	protected AbstractCoreferenceSieve(JCas jCas, List<Cluster> clusters, List<Mention> mentions) {
		this.jCas = jCas;
		this.clusters = clusters;
		this.mentions = mentions;
	}

	public List<Cluster> getClusters() {
		return clusters;
	}

	public List<Mention> getMentions() {
		return mentions;
	}

	public JCas getJCas() {
		return jCas;
	}

	protected void addToCluster(Mention a, Mention b) {
		if (a.hasClusters()) {
			Cluster cluster = a.getAnyCluster();
			cluster.add(b);
		} else if (b.hasClusters()) {
			Cluster cluster = b.getAnyCluster();
			cluster.add(a);
		} else {
			clusters.add(new Cluster(a, b));
		}
	}

	protected void addPairwiseToCluster(Collection<Mention> a, Collection<Mention> b) {

		// Technically these will all need to end up in the same cluster

		Cluster cluster = a.stream().map(x -> x.getAnyCluster()).filter(Objects::nonNull).findAny()
				.orElseGet(() -> b.stream().map(x -> x.getAnyCluster()).filter(Objects::nonNull).findAny()
						.orElseGet(() -> {
							Cluster c = new Cluster();
							clusters.add(c);
							return c;
						}));

		cluster.addAll(a);
		cluster.addAll(b);

	}

}
