package com.tenode.baleen.extras.jobs;

import java.io.IOException;
import java.util.HashSet;
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

import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;
import uk.gov.dstl.baleen.uima.jobs.BaleenTask;
import uk.gov.dstl.baleen.uima.jobs.JobSettings;

/**
 * Enhance and extend the list of interaction words through WordNet.
 *
 * This is useful for increasing the range of a words which are considered for interaction gazetteer
 * matching without increasing the manual effort. It is likely that the user will will to review the
 * words after running this, to ensure the words truely have the same meaning at the relationship
 * requires.
 *
 * The CSV file, see {@link CsvInteractionReader} and {@link CsvInteractionWriter} for format
 * information, is read. This lemma and POS are used to find additional dictionary words which have
 * the same meaning.
 *
 * The output is saved back in the same format.
 *
 *
 * @baleen.javadoc
 *
 */
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

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * uk.gov.dstl.baleen.uima.jobs.BaleenTask#execute(uk.gov.dstl.baleen.uima.jobs.JobSettings)
	 */
	@Override
	protected void execute(JobSettings settings) throws AnalysisEngineProcessException {
		dictionary = wordnet.getDictionary();

		try (CsvInteractionWriter writer = new CsvInteractionWriter(outputFilename)) {
			final CsvInteractionReader reader = new CsvInteractionReader(inputFilename);

			writer.initialise();

			reader.read((i, a) -> {

				final Set<String> alternatives = getAlternativeWords(i.getWord())
						.map(s -> s.trim().toLowerCase())
						.filter(s -> s.length() > 2)
						.collect(Collectors.toSet());

				// Add in whatever the user provided
				alternatives.addAll(a);

				try {
					writer.write(i, alternatives);
				} catch (final Exception e) {
					getMonitor().warn("Unable to write CSV row", e);
				}
			});

			getMonitor().info("Interaction enhacement complete and written to {}", outputFilename);
		} catch (final IOException e) {
			throw new AnalysisEngineProcessException(e);
		}

	}

	/**
	 * Gets the alternative words from the dictionary.
	 *
	 * @param word
	 *            the word
	 * @return the alternative words (non null and always contains the word itself)
	 */
	private Stream<String> getAlternativeWords(Word word) {
		IndexWord indexWord = null;
		try {
			indexWord = dictionary.lookupIndexWord(word.getPos(), word.getLemma());
		} catch (final Exception e) {
			// Ignore - problems with wordnet
		}

		if (indexWord == null) {
			return Stream.of(word.getLemma());
		}

		Set<String> set = new HashSet<String>();
		set.add(word.getLemma());
		for (Synset synset : indexWord.getSenses()) {
			for (net.sf.extjwnl.data.Word w : synset.getWords()) {
				set.add(w.getLemma());
			}
		}

		return set.stream();
	}
}
