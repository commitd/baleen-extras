package com.tenode.baleen.extra.annotators.relationships;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

import com.tenode.baleen.abta.common.stopwords.StopWordRemover;
import com.tenode.baleen.extra.annotators.relationships.data.PatternExtract;

import uk.gov.dstl.baleen.types.language.Pattern;
import uk.gov.dstl.baleen.types.language.Sentence;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

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
	@ConfigurationParameter(name = PARAM_WINDOW_SIZE, defaultValue = "5")
	private int windowSize;

	private final StopWordRemover stopWordRemover = new StopWordRemover();

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {

		for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {

			List<Entity> entities = JCasUtil.selectCovered(jCas, Entity.class, sentence);
			List<WordToken> words = JCasUtil.selectCovered(jCas, WordToken.class, sentence);

			// Remove words which are covered by an entity
			List<WordToken> nonEntityWords = words.stream().filter(w -> !entities.stream().anyMatch(e -> {
				return e.getBegin() <= w.getBegin() && w.getEnd() <= e.getEnd();
			})).collect(Collectors.toList());

			// Find entities within (windowSize) words of one another

			String text = jCas.getDocumentText();
			String lowerText = text.toLowerCase();
			List<PatternExtract> patterns = new ArrayList<PatternExtract>();
			for (int i = 0; i < entities.size(); i++) {
				for (int j = i + 1; j < entities.size(); j++) {

					Entity a = entities.get(i);
					Entity b = entities.get(j);

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
						int count = countWordsBetween(p, words);
						return count >= 0 && count < windowSize;
					})
					.filter(p -> !p.contains(lowerText, "no", "not", "neither"))
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

	private int countWordsBetween(PatternExtract p, List<WordToken> words) {
		int startWord = -1;
		int endWord = -1;

		int i = 0;
		for (WordToken w : words) {
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

	private Stream<WordToken> removeAdditionalWords(PatternExtract pe, Stream<WordToken> tokens) {
		return tokens
				.filter(t -> t.getBegin() >= pe.getStart() && t.getEnd() <= pe.getEnd())
				.filter(t -> !stopWordRemover.isStopWord(t.getCoveredText()));
	}

	private void outputPattern(JCas jCas, PatternExtract pattern) {
		Pattern a = new Pattern(jCas);
		a.setBegin(pattern.getStart());
		a.setEnd(pattern.getEnd());
		a.setSource(pattern.getFrom());
		a.setTarget(pattern.getTo());

		List<WordToken> tokens = pattern.getWordTokens();
		FSArray array = new FSArray(jCas, tokens.size());
		int i = 0;
		for (WordToken w : tokens) {
			array.set(i, w);
			i++;
		}
		a.setWords(array);
		addToJCasIndex(a);
	}

}
