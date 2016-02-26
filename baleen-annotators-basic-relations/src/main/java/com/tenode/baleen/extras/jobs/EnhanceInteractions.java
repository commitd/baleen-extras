package com.tenode.baleen.extras.jobs;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;

import com.tenode.baleen.extras.jobs.interactions.data.Word;
import com.tenode.baleen.extras.jobs.io.CsvInteractionReader;
import com.tenode.baleen.extras.jobs.io.CsvInteractionWriter;
import com.tenode.baleen.wordnet.resources.WordNetResource;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.dictionary.Dictionary;
import uk.gov.dstl.baleen.uima.jobs.BaleenTask;
import uk.gov.dstl.baleen.uima.jobs.JobSettings;

public class EnhanceInteractions extends BaleenTask {

	/**
	 * Connection to Wordnet
	 *
	 * @baleen.resource com.tenode.baleen.resources.wordnet.WordNetResource}
	 */
	public static final String KEY_WORDNET = "wordnet";
	@ExternalResource(key = KEY_WORDNET)
	private WordNetResource wordnet;

	/**
	 * Save the data to csv, with filename prefixed by tje value.
	 *
	 * Leave this blank for no output.
	 *
	 * @baleen.config csv interactions.csv
	 */
	public static final String KEY_CSV_INPUT = "input";
	@ConfigurationParameter(name = KEY_CSV_INPUT, defaultValue = "interactions.csv")
	private String inputFilename;

	/**
	 * Save the data to csv, with filename prefixed by tje value.
	 *
	 * Leave this blank for no output.
	 *
	 * @baleen.config csv interactions-enhanced.csv
	 */
	public static final String KEY_CSV_OUTPUT = "output";
	@ConfigurationParameter(name = KEY_CSV_OUTPUT, defaultValue = "interactions-enhanced.csv")
	private String outputFilename;

	private Dictionary dictionary;

	@Override
	protected void execute(JobSettings settings) throws AnalysisEngineProcessException {
		dictionary = wordnet.getDictionary();

		try (CsvInteractionWriter writer = new CsvInteractionWriter(outputFilename)) {
			CsvInteractionReader reader = new CsvInteractionReader(inputFilename);

			writer.initialise();

			reader.read((i, a) -> {

				Set<String> alternatives = getAlternativeWords(i.getWord())
						.map(s -> s.trim().toLowerCase())
						.filter(s -> s.length() > 2)
						.collect(Collectors.toSet());

				// Add in whatever the user provided
				alternatives.addAll(a);

				try {
					writer.write(i, alternatives);
				} catch (Exception e) {
					getMonitor().warn("Unable to write CSV row");
				}
			});
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}

	}

	public Stream<String> getAlternativeWords(Word word) {
		IndexWord indexWord = null;
		try {
			indexWord = dictionary.lookupIndexWord(word.getPos(), word.getLemma());
		} catch (JWNLException e) {
			// Ignore
		}

		if (indexWord == null) {
			return Stream.of(word.getLemma());
		}

		Stream<String> otherWords = indexWord.getSenses().stream()
				.flatMap(s -> s.getWords().stream())
				.map(x -> x.getLemma());

		return Stream.concat(Stream.of(word.getLemma()), otherWords).distinct();
	}
}
