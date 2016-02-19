package com.tenode.baleen.annotators.coreference.sieves;

import java.util.List;

import org.apache.uima.jcas.JCas;

import com.tenode.baleen.annotators.coreference.data.Cluster;
import com.tenode.baleen.annotators.coreference.data.Mention;
import com.tenode.baleen.annotators.coreference.data.MentionType;

/**
 * Mentions are
 *
 * See 3.3.3 Pass 3.
 */
public class ExactStringMatchSieve extends AbstractCoreferenceSieve {

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
			Mention a = getMentions().get(i);
			String aText = a.getText();

			if (a.getType() == MentionType.PRONOUN) {
				continue;
			}

			for (int j = i + 1; j < getMentions().size(); j++) {
				Mention b = getMentions().get(j);
				String bText = b.getText();

				if (b.getType() == MentionType.PRONOUN) {
					continue;
				}

				if (aText.equalsIgnoreCase(bText)) {
					addToCluster(a, b);
				}

			}
		}
	}

}
