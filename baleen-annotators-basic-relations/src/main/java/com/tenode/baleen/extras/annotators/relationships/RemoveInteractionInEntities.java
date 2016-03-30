package com.tenode.baleen.extras.annotators.relationships;

import java.util.Collection;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.types.language.Interaction;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

/**
 * Removes all interactions pertaining to entities
 */
public class RemoveInteractionInEntities extends BaleenAnnotator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.gov.dstl.baleen.uima.BaleenAnnotator#doProcess(org.apache.uima.jcas.JCas)
	 */
	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {
		Map<Interaction, Collection<Entity>> covering = JCasUtil.indexCovering(jCas, Interaction.class, Entity.class);

		removeFromJCasIndex(covering.keySet());
	}

}
