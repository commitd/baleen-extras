package com.tenode.baleen.annotators.documentcoreference.sieves;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import com.tenode.baleen.annotators.documentcoreference.data.Cluster;
import com.tenode.baleen.annotators.documentcoreference.data.Mention;
import com.tenode.baleen.annotators.documentcoreference.data.MentionType;

/**
 * String matching which is more easily satisified that exact matching.
 */
public class RelaxedStringMatchSieve extends AbstractCoreferenceSieve {

	private static final Set<String> EXCLUDED = new HashSet<>(Arrays.asList("that", "there"));

	public RelaxedStringMatchSieve(JCas jCas, List<Cluster> clusters, List<Mention> mentions) {
		super(jCas, clusters, mentions);
	}

	@Override
	public void sieve() {

		// Text says nominal mention, we assume that to be mean Entity

		for (int i = 0; i < getMentions().size(); i++) {
			final Mention a = getMentions().get(i);

			final String aHead = a.getHead();
			if (aHead == null || aHead.isEmpty() || a.getType() != MentionType.ENTITY
					|| EXCLUDED.contains(aHead.toLowerCase())) {
				continue;
			}

			for (int j = i + 1; j < getMentions().size(); j++) {
				final Mention b = getMentions().get(j);
				final String bHead = b.getHead();
				if (bHead == null || bHead.isEmpty() || b.getType() != MentionType.ENTITY
						|| EXCLUDED.contains(aHead.toLowerCase())) {
					continue;
				}

				if (aHead.equalsIgnoreCase(bHead)) {
					addToCluster(a, b);
				}

			}
		}
	}

}
