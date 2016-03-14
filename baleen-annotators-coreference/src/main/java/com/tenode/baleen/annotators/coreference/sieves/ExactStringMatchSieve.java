package com.tenode.baleen.annotators.coreference.sieves;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import com.tenode.baleen.annotators.coreference.data.Cluster;
import com.tenode.baleen.annotators.coreference.data.Mention;
import com.tenode.baleen.annotators.coreference.data.MentionType;

/**
 * Coreference based on exact matching.
 *
 * See 3.3.3 Pass 3.
 */
public class ExactStringMatchSieve extends AbstractCoreferenceSieve {
	private static final Set<String> EXCLUDED = new HashSet<>(Arrays.asList("that", "there"));

	/**
	 * Instantiates a new insatnce
	 *
	 * @param jCas
	 *            the jcas
	 */
	public ExactStringMatchSieve(JCas jCas, List<Cluster> clusters, List<Mention> mentions) {
		super(jCas, clusters, mentions);
	}

	@Override
	public void sieve() {
		for (int i = 0; i < getMentions().size(); i++) {
			final Mention a = getMentions().get(i);
			final String aText = a.getText();

			if (a.getType() == MentionType.PRONOUN || EXCLUDED.contains(aText.toLowerCase())) {
				continue;
			}

			for (int j = i + 1; j < getMentions().size(); j++) {
				final Mention b = getMentions().get(j);
				final String bText = b.getText();

				if (b.getType() == MentionType.PRONOUN || EXCLUDED.contains(bText.toLowerCase())) {
					continue;
				}

				if (aText.equalsIgnoreCase(bText)) {
					addToCluster(a, b);
				}

			}
		}
	}

}
