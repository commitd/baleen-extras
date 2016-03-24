package com.tenode.baleen.extras.common.consumers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import uk.gov.dstl.baleen.uima.BaleenConsumer;

/**
 * Base class for outputting CSV files.
 */
public abstract class AbstractCsvConsumer extends BaleenConsumer {

	private static final Pattern NORMALIZE_PATTERN = Pattern.compile("\\s+");

	private static final String KEY_PREFIX = "filename";
	@ConfigurationParameter(name = KEY_PREFIX, defaultValue = "evaluation-relations.csv")
	private String filename;

	private CSVPrinter writer;

	/**
	 * Instantiates a new abstract csv consumer.
	 */
	protected AbstractCsvConsumer() {
		super();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.gov.dstl.baleen.uima.BaleenAnnotator#doInitialize(org.apache.uima.UimaContext)
	 */
	@Override
	public void doInitialize(UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);

		try {

			writer = new CSVPrinter(
					new OutputStreamWriter(new FileOutputStream(filename, false), StandardCharsets.UTF_8),
					CSVFormat.TDF);
		} catch (final IOException e) {
			throw new ResourceInitializationException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.gov.dstl.baleen.uima.BaleenAnnotator#doProcess(org.apache.uima.jcas.JCas)
	 */
	@Override
	protected final void doProcess(JCas jCas) throws AnalysisEngineProcessException {
		write(jCas);

		try {
			writer.flush();
		} catch (final IOException e) {
			getMonitor().warn("Unable to flush file", e);
		}

	}

	/**
	 * Write the JCas to CSV.
	 *
	 * @param jCas
	 *            the j cas
	 */
	protected abstract void write(JCas jCas);

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.gov.dstl.baleen.uima.BaleenAnnotator#doDestroy()
	 */
	@Override
	protected void doDestroy() {

		try {
			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (final IOException e) {
					getMonitor().warn("Failed to close csv writer", e);
				}
			}
		} finally {
			writer = null;
		}

		super.doDestroy();
	}

	/**
	 * Called by implementors to write a row.
	 *
	 * @param row
	 *            the row
	 */
	protected void write(Object... row) {
		try {
			writer.printRecord(row);
		} catch (final IOException e) {
			getMonitor().warn("Failed to write line to csv", e);

		}
	}

	/**
	 * Normalize the text (called by implementors).
	 *
	 * @param text
	 *            the text
	 * @return the string
	 */
	protected String normalize(String text) {
		return NORMALIZE_PATTERN.matcher(text).replaceAll(" ").trim();
	}
}