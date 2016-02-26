package com.tenode.baleen.extras.jobs.io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.tenode.baleen.extras.jobs.interactions.data.InteractionRelation;

public class CsvInteractionWriter implements InteractionWriter {

	private final String csvFilename;
	private CSVPrinter writer;

	public CsvInteractionWriter(String csvFilename) {
		this.csvFilename = csvFilename;
	}

	@Override
	public void initialise() throws IOException {
		writer = new CSVPrinter(new FileWriter(csvFilename), CSVFormat.RFC4180);
	}

	@Override
	public void write(InteractionRelation interaction, Collection<String> alternatives) throws IOException {
		if (writer != null) {
			Object[] record = new Object[6 + alternatives.size()];
			record[0] = interaction.getType();
			record[1] = interaction.getSubType();
			record[2] = interaction.getSource();
			record[3] = interaction.getTarget();
			record[4] = interaction.getWord().getLemma();
			record[5] = interaction.getWord().getPos().getLabel();
			Iterator<String> iterator = alternatives.iterator();
			for (int i = 0; i < alternatives.size(); i++) {
				record[6 + i] = iterator.next();
			}
			writer.printRecord(record);

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
