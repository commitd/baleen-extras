
package com.tenode.baleen.extras.jobs.io;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import com.tenode.baleen.extras.jobs.interactions.data.InteractionDefinition;
import com.tenode.baleen.extras.jobs.interactions.data.Word;

import net.sf.extjwnl.data.POS;

/**
 * Read interactions from CSV.
 *
 * This reads interaction data as written by the {@link CsvInteractionWriter}.
 *
 */
public class CsvInteractionReader {

	private final String inputFilename;

	/**
	 * Instantiates a new CSV interaction reader.
	 *
	 * @param inputFilename
	 *            the input filename
	 */
	public CsvInteractionReader(String inputFilename) {
		this.inputFilename = inputFilename;
	}

	/**
	 * Read the CSV file and send interactions to the consumer
	 *
	 * @param consumer
	 *            the consumer (first param is the InteractionRelation and the second the list of
	 *            alternative words)
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void read(BiConsumer<InteractionDefinition, Collection<String>> consumer) throws IOException {

		try (CSVParser parser = new CSVParser(new FileReader(inputFilename), CSVFormat.RFC4180)) {
			StreamSupport.stream(parser.spliterator(), false)
					.forEach(r -> {
						String type = r.get(0);
						String subType = r.get(1);
						String source = r.get(2);
						String target = r.get(3);
						String lemma = r.get(4);
						POS pos = POS.getPOSForLabel(r.get(5));

						InteractionDefinition i = new InteractionDefinition(type, subType, new Word(lemma, pos), source,
								target);

						List<String> alternatives = new ArrayList<>(r.size() - 6);
						for (int j = 0; j < r.size(); j++) {
							alternatives.add(r.get(j));
						}

						consumer.accept(i, alternatives);
					});

		}
	}

}
