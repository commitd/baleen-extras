package com.tenode.baleen.extras.annotators.relationships;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

import com.tenode.baleen.extras.annotators.relationships.data.PatternExtract;
import com.tenode.baleen.extras.common.language.StopWordRemover;

import uk.gov.dstl.baleen.types.language.Pattern;
import uk.gov.dstl.baleen.types.language.Sentence;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

/**
 * Find patterns in the document text.
 *
 * A pattern is a set of words between two entities. They are typically used to form a training set
 * for relationship extraction.
 *
 * As a result this annotator must be run after Entity and WordToken annotations have been added to
 * the JCas. That is post POS tagging (eg by OpenNlp) and after entity extraction (and ideally clean
 * up).
 *
 * The algorithm is basically described as follow. For each sentence we find entities which are less
 * than "windowSize" away from each other (measured in words). This are our candidate patterns. We
 * filter any patterns containing negatives (eg the words no or not). We then remove from the
 * pattern any stop words and any other entities which appear within the pattern text. We remove any
 * empty patterns and then create a new Pattern annotation. The Pattern annotation holds the
 * original range, plus the list of retained words (in the form of WordTokens).
 *
 * @baleen.javadoc
 */
public class PatternExtractor extends BaleenAnnotator {

	/**
	 * The max distance (in words) between two entites in a sentence before they are considered
	 * related by the verb between them.
	 *
	 * Use a small number to get a minimal set of high quality words.
	 *
	 * @baleen.config 5
	 */
	public static final String PARAM_WINDOW_SIZE = "windowSize";
	@ConfigurationParameter(name = PatternExtractor.PARAM_WINDOW_SIZE, defaultValue = "5")
	private int windowSize;

	private final StopWordRemover stopWordRemover = new StopWordRemover();

	private final java.util.regex.Pattern negationRegex = java.util.regex.Pattern.compile("\b((no)|(neither)|(not))\b");

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.gov.dstl.baleen.uima.BaleenAnnotator#doProcess(org.apache.uima.jcas.JCas)
	 */
	@Override
	protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {

		for (final Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {

			final List<Entity> entities = JCasUtil.selectCovered(jCas, Entity.class, sentence);
			final List<WordToken> words = JCasUtil.selectCovered(jCas, WordToken.class, sentence);

			// Remove words which are covered by an entity
			final List<WordToken> nonEntityWords = words.stream().filter(w -> !entities.stream().anyMatch(e -> {
				return e.getBegin() <= w.getBegin() && w.getEnd() <= e.getEnd();
			})).collect(Collectors.toList());

			// Find entities within (windowSize) words of one another

			final String text = jCas.getDocumentText();
			final String lowerText = text.toLowerCase();
			final List<PatternExtract> patterns = new ArrayList<PatternExtract>();
			for (int i = 0; i < entities.size(); i++) {
				for (int j = i + 1; j < entities.size(); j++) {

					final Entity a = entities.get(i);
					final Entity b = entities.get(j);

					if (a.getEnd() < b.getBegin()) {
						// A is before B
						patterns.add(new PatternExtract(a, b, a.getEnd(), b.getBegin()));
					} else if (a.getBegin() > b.getEnd()) {
						patterns.add(new PatternExtract(a, b, b.getEnd(), a.getBegin()));
					} else {
						// Overlapping entities ... ignore as no words between them
					}
				}
			}

			// Filter out patterns which are too far way
			// Filter out patterns which contain no, not or neither

			patterns.stream()
					.filter(p -> {
						final int count = countWordsBetween(p, words);
						return count >= 0 && count < windowSize;
					})
					.filter(p -> !negationRegex.matcher(p.getCoveredText(lowerText)).matches())
					.forEach(p -> {
						// Remove any other entities from the pattern
						// Remove stop words from the pattern

						// TODO: I question this in the paper. Whilst it is true we don't want stop
						// words I think we want
						// to extract a phrase. Their example is "play a role" which becomes
						// "play,role"
						p.setWordTokens(removeAdditionalWords(p, nonEntityWords.stream()).collect(Collectors.toList()));

						if (!p.isEmpty()) {
							outputPattern(jCas, p);
						}
					});

		}

	}

	/**
	 * Count words between the pattern and words.
	 *
	 * @param p
	 *            the p
	 * @param words
	 *            the words
	 * @return the int
	 */
	private int countWordsBetween(final PatternExtract p, final List<WordToken> words) {
		int startWord = -1;
		int endWord = -1;

		int i = 0;
		for (final WordToken w : words) {
			if (w.getBegin() <= p.getStart() && w.getEnd() >= p.getStart()) {
				startWord = i;
			}

			if (w.getBegin() <= p.getEnd() && w.getEnd() >= p.getEnd()) {
				endWord = i;
			}

			i++;
		}

		if (startWord == -1 || endWord == -1) {
			return -1;
		}

		return endWord - startWord;
	}

	/**
	 * Removes the additional words from the pattern extractor.
	 *
	 * Filters out stop words and words outside the pattern.
	 *
	 * @param pe
	 *            the pe
	 * @param tokens
	 *            the tokens
	 * @return the stream
	 */
	private Stream<WordToken> removeAdditionalWords(final PatternExtract pe, final Stream<WordToken> tokens) {
		return tokens
				.filter(t -> t.getBegin() >= pe.getStart() && t.getEnd() <= pe.getEnd())
				.filter(t -> !stopWordRemover.isStopWord(t.getCoveredText()))
				.filter(t -> t.getCoveredText().trim().length() > 1);
	}

	/**
	 * Output pattern (save to the jCas)
	 *
	 * @param jCas
	 *            the j cas
	 * @param pattern
	 *            the pattern
	 */
	private void outputPattern(final JCas jCas, final PatternExtract pattern) {
		final Pattern a = new Pattern(jCas);
		a.setBegin(pattern.getStart());
		a.setEnd(pattern.getEnd());
		a.setSource(pattern.getFrom());
		a.setTarget(pattern.getTo());

		final List<WordToken> tokens = pattern.getWordTokens();
		final FSArray array = new FSArray(jCas, tokens.size());
		int i = 0;
		for (final WordToken w : tokens) {
			array.set(i, w);
			i++;
		}
		a.setWords(array);
		addToJCasIndex(a);
	}

}
