package com.tenode.baleen.annotators.coreference.sieves;

import java.util.List;

import org.apache.uima.jcas.JCas;

import com.tenode.baleen.annotators.coreference.data.Cluster;
import com.tenode.baleen.annotators.coreference.data.Mention;
import com.tenode.baleen.annotators.coreference.data.MentionType;

public class RelaxedStringMatchSieve extends AbstractCoreferenceSieve {

	public RelaxedStringMatchSieve(JCas jCas, List<Cluster> clusters, List<Mention> mentions) {
		super(jCas, clusters, mentions);
	}

	@Override
	public void sieve() {

		// Text says nominal mention, we assume that to be mean Entity

		for (int i = 0; i < getMentions().size(); i++) {
			Mention a = getMentions().get(i);

			String aHead = a.getHead();
			if (aHead == null || aHead.isEmpty() || a.getType() != MentionType.ENTITY) {
				continue;
			}

			for (int j = i + 1; j < getMentions().size(); j++) {
				Mention b = getMentions().get(j);
				String bHead = b.getHead();
				if (bHead == null || bHead.isEmpty() || b.getType() != MentionType.ENTITY) {
					continue;
				}

				if (aHead.equalsIgnoreCase(bHead)) {
					addToCluster(a, b);
				}

			}
		}
	}

}
