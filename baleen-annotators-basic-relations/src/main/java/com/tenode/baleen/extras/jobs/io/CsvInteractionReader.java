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

import com.tenode.baleen.extras.jobs.interactions.data.InteractionRelation;
import com.tenode.baleen.extras.jobs.interactions.data.Word;

import net.sf.extjwnl.data.POS;

public class CsvInteractionReader {

	private final String inputFilename;

	public CsvInteractionReader(String inputFilename) {
		this.inputFilename = inputFilename;
	}

	public void read(BiConsumer<InteractionRelation, Collection<String>> consumer) throws IOException {

		try (CSVParser parser = new CSVParser(new FileReader(inputFilename), CSVFormat.RFC4180)) {
			StreamSupport.stream(parser.spliterator(), false)
					.forEach(r -> {
						String type = r.get(0);
						String subType = r.get(1);
						String source = r.get(2);
						String target = r.get(3);
						String lemma = r.get(4);
						POS pos = POS.getPOSForLabel(r.get(5));

						InteractionRelation i = new InteractionRelation(type, subType, new Word(lemma, pos), source,
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
