package com.tenode.baleen.annotators.coreference.sieves;

import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import com.tenode.baleen.annotators.coreference.data.Cluster;
import com.tenode.baleen.annotators.coreference.data.Mention;
import com.tenode.baleen.annotators.coreference.utils.StopWordExtractor;

public class StrictHeadMatchSieve extends AbstractCoreferenceSieve {

	private final boolean compatibleModifiers;
	private final boolean wordInclusion;

	public StrictHeadMatchSieve(JCas jCas, List<Cluster> clusters, List<Mention> mentions,
			boolean compatibleModifiers, boolean wordInclusion) {
		super(jCas, clusters, mentions);

		this.compatibleModifiers = compatibleModifiers;
		this.wordInclusion = wordInclusion;
	}

	@Override
	public void sieve() {

		// TODO: We really need to work over clusters for this to make sense!

		for (int i = 0; i < getMentions().size(); i++) {
			Mention a = getMentions().get(i);
			String aHead = a.getHead();
			if (aHead == null || aHead.isEmpty()) {
				continue;
			}
			aHead = aHead.toLowerCase();

			for (int j = i + 1; j < getMentions().size(); j++) {
				Mention b = getMentions().get(j);
				String bHead = b.getHead();
				if (bHead == null || bHead.isEmpty()) {
					continue;
				}
				bHead = bHead.toLowerCase();

				// Entity head match - does one head contain the others
				if (!aHead.contains(bHead) && !bHead.contains(aHead)) {
					continue;
				}

				// Word inclusion - stop words of the mention are in the cluster
				if (wordInclusion && !StopWordExtractor.hasSubsetOfNonStopWords(a, b)) {
					continue;
				}

				// Compatible modifiers only - do the two candidate mentions have the same adject /
				// nouns
				if (compatibleModifiers && !haveSubsetOfSameModifier(a, b)) {
					continue;
				}

				// Not i-within-i
				// NOTE: We just check for overlap here, not if a sub-NP, which is a cheap test and
				// can come first (but not in the cluster based case since, then we need to find the
				// mentions to test first.
				if (!a.overlaps(b)) {
					continue;
				}

				// Otherwise they are in the same cluster
				addToCluster(a, b);

			}
		}
	}

	private boolean haveSubsetOfSameModifier(Mention a, Mention b) {
		Set<String> aModifiers = getModifiers(a);
		Set<String> bModifiers = getModifiers(b);

		// NOTE: This is ordered, a is earlier than b and it is unusal to introduce more information
		// to an entity later in the document
		return aModifiers.containsAll(bModifiers);
	}

}
