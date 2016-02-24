package com.tenode.baleen.extras.common.consumers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import au.com.bytecode.opencsv.CSVWriter;
import uk.gov.dstl.baleen.uima.BaleenConsumer;

public abstract class AbstractCsvConsumer extends BaleenConsumer {

	private final static Pattern NORMALIZE = Pattern.compile("\\s+");

	private static final String KEY_PREFIX = "filename";
	@ConfigurationParameter(name = KEY_PREFIX, defaultValue = "evaluation-relations.csv")
	private String filename;

	private CSVWriter writer;

	public AbstractCsvConsumer() {
		super();
	}

	@Override
	public void doInitialize(UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);

		try {
			writer = new CSVWriter(new FileWriter(filename, false), '\t');
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}

	}

	@Override
	protected final void doProcess(JCas jCas) throws AnalysisEngineProcessException {
		write(jCas);

		try {
			writer.flush();
		} catch (IOException e) {
			getMonitor().warn("Unable to flush file", e);
		}

	}

	protected abstract void write(JCas jCas);

	@Override
	protected void doDestroy() {

		try {
			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					getMonitor().warn("Failed to close relation writer", e);
				}
			}
		} finally {
			writer = null;
		}

		super.doDestroy();
	}

	protected void write(String... row) {
		writer.writeNext(row);
	}

	protected String normalize(String text) {
		return NORMALIZE.matcher(text).replaceAll(" ").trim();
	}
}