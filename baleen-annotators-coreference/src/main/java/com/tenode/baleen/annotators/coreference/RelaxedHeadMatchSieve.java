package com.tenode.baleen.annotators.coreference;

import java.util.List;

import org.apache.uima.jcas.JCas;

import com.tenode.baleen.annotators.coreference.data.Cluster;
import com.tenode.baleen.annotators.coreference.data.Mention;
import com.tenode.baleen.annotators.coreference.sieves.AbstractCoreferenceSieve;

public class RelaxedHeadMatchSieve extends AbstractCoreferenceSieve {

	protected RelaxedHeadMatchSieve(JCas jCas, List<Cluster> clusters, List<Mention> mentions) {
		super(jCas, clusters, mentions);
	}

	@Override
	public void sieve() {
		// TODO Auto-generated method stub

	}

}
