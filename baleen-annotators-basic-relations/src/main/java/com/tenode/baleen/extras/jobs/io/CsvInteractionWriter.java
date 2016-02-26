package com.tenode.baleen.extras.jobs.writers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.tenode.baleen.extras.jobs.interactions.data.InteractionWord;

import uk.gov.dstl.baleen.uima.UimaMonitor;

public class CsvInteractionWriter implements InteractionWordWriter {

	private final String csvFilename;
	private CSVPrinter writer;
	private final UimaMonitor monitor;

	public CsvInteractionWriter(UimaMonitor monitor, String csvFilename) {
		this.monitor = monitor;
		this.csvFilename = csvFilename;
	}

	@Override
	public void initialise() {
		try {
			writer = new CSVPrinter(new FileWriter(csvFilename), CSVFormat.RFC4180);
		} catch (IOException e) {
			monitor.error("UNable to create writer", e);
		}
	}

	@Override
	public void write(InteractionWord word, String relationshipType, String lemma, List<String> alternatives) {
		if (writer != null) {
			word.getPairs().forEach(p -> {

				try {
					Object[] record = new Object[4 + alternatives.size()];
					record[0] = relationshipType;
					record[1] = lemma;
					record[2] = p.getSource();
					record[3] = p.getTarget();
					for (int i = 0; i < alternatives.size(); i++) {
						record[4 + i] = alternatives.get(i);
					}
					writer.printRecord(record);
				} catch (Exception e) {
					monitor.warn("Unable to write interaction csv row", e);
				}
			});
		} else {
			monitor.warn("Ignoring interaction csv row, no writer");
		}
	}

	@Override
	public void destroy() {
		try {
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		} catch (Exception e) {
			writer = null;
		}
	}

}
