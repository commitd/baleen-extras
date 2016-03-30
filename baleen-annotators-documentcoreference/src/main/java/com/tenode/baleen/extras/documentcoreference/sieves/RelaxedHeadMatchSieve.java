package com.tenode.baleen.extras.documentcoreference.sieves;

import java.util.List;

import org.apache.uima.jcas.JCas;

import com.tenode.baleen.extras.documentcoreference.data.Cluster;
import com.tenode.baleen.extras.documentcoreference.data.Mention;
import com.tenode.baleen.extras.documentcoreference.data.MentionType;
import com.tenode.baleen.extras.documentcoreference.utils.StopWordExtractor;

import uk.gov.dstl.baleen.types.Base;

/**
 * Sieve based on looser matching of head terms.
 */
public class RelaxedHeadMatchSieve extends AbstractCoreferenceSieve {

	public RelaxedHeadMatchSieve(JCas jCas, List<Cluster> clusters, List<Mention> mentions) {
		super(jCas, clusters, mentions);
	}

	@Override
	public void sieve() {
		for (int i = 0; i < getMentions().size(); i++) {
			final Mention a = getMentions().get(i);

			if (a.getType() != MentionType.ENTITY) {
				continue;
			}
			final String aText = a.getText();
			final Class<? extends Base> aClazz = a.getAnnotation().getClass();

			for (int j = i + 1; j < getMentions().size(); j++) {
				final Mention b = getMentions().get(j);
				final String bHead = b.getHead();
				if (bHead == null || bHead.isEmpty() || b.getType() != MentionType.ENTITY) {
					continue;
				}
				final Class<? extends Base> bClazz = b.getAnnotation().getClass();

				// Not i-within-i
				if (a.overlaps(b)) {
					continue;
				}

				// We have the same or at least semantically same type of entity
				if (!aClazz.isAssignableFrom(bClazz) && !bClazz.isAssignableFrom(aClazz)) {
					continue;
				}

				// Word inclusion
				if (!StopWordExtractor.hasSubsetOfNonStopWords(a, b)) {
					continue;
				}

				// Do we contain the head word?
				if (!aText.contains(bHead)) {
					continue;
				}

				addToCluster(a, b);
			}
		}
	}

}
