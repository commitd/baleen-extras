package com.tenode.baleen.extras.common.consumers;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.uima.BaleenConsumer;

/**
 * Logs when a document is done.
 *
 * Useful for development, when you need to set a breakpoint at the end of a documents processing.
 *
 * @baleen.javadoc
 */
public class DoneConsumer extends BaleenConsumer {

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {
		getMonitor().info("Document processed");

	}

}
