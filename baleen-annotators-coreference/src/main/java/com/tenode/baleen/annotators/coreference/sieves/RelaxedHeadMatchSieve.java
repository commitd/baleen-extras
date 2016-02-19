package com.tenode.baleen.annotators.coreference.sieves;

import java.util.List;

import org.apache.uima.jcas.JCas;

import com.tenode.baleen.annotators.coreference.data.Cluster;
import com.tenode.baleen.annotators.coreference.data.Mention;
import com.tenode.baleen.annotators.coreference.data.MentionType;
import com.tenode.baleen.annotators.coreference.utils.StopWordExtractor;

import uk.gov.dstl.baleen.types.Base;

public class RelaxedHeadMatchSieve extends AbstractCoreferenceSieve {

	public RelaxedHeadMatchSieve(JCas jCas, List<Cluster> clusters, List<Mention> mentions) {
		super(jCas, clusters, mentions);
	}

	@Override
	public void sieve() {
		for (int i = 0; i < getMentions().size(); i++) {
			Mention a = getMentions().get(i);

			if (a.getType() != MentionType.ENTITY) {
				continue;
			}
			String aText = a.getText();
			Class<? extends Base> aClazz = a.getAnnotation().getClass();

			for (int j = i + 1; j < getMentions().size(); j++) {
				Mention b = getMentions().get(j);
				String bHead = b.getHead();
				if (bHead == null || bHead.isEmpty() || b.getType() != MentionType.ENTITY) {
					continue;
				}
				Class<? extends Base> bClazz = b.getAnnotation().getClass();

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
