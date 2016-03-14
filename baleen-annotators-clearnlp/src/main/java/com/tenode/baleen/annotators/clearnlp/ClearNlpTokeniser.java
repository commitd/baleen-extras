package com.tenode.baleen.annotators.clearnlp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;

import edu.emory.clir.clearnlp.component.mode.morph.AbstractMPAnalyzer;
import edu.emory.clir.clearnlp.component.mode.pos.AbstractPOSTagger;
import edu.emory.clir.clearnlp.component.utils.NLPUtils;
import edu.emory.clir.clearnlp.dependency.DEPLib;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.tokenization.AbstractTokenizer;
import edu.emory.clir.clearnlp.util.lang.TLanguage;
import uk.gov.dstl.baleen.types.language.Sentence;
import uk.gov.dstl.baleen.types.language.WordLemma;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

/**
 * ClearNlp tokeniser performing Sentence segmentation, POS tagging and word analysis.
 *
 * This annotator perform functions similar to the OpenNlp annotator - outputting full complete
 * Sentence and WordToken annotations.
 *
 * This annotator might be preferable to the OpenNlp equivalent if the pipeline uses other ClearNlp
 * functions.
 *
 * NOTE: Unlike ClearNlp itself this annotator supports only English (is hard coded to it).
 *
 * @baleen.javadoc
 */
public class ClearNlpTokeniser extends BaleenAnnotator {

	private AbstractPOSTagger posTagger;
	private AbstractMPAnalyzer mpAnalyser;
	private AbstractTokenizer tokeniser;

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.gov.dstl.baleen.uima.BaleenAnnotator#doInitialize(org.apache.uima.UimaContext)
	 */
	@Override
	public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);

		final TLanguage language = TLanguage.ENGLISH;

		tokeniser = NLPUtils.getTokenizer(language);
		mpAnalyser = NLPUtils.getMPAnalyzer(language);
		posTagger = NLPUtils.getPOSTagger(language, "general-en-pos.xz");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.gov.dstl.baleen.uima.BaleenAnnotator#doProcess(org.apache.uima.jcas.JCas)
	 */
	@Override
	protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {
		final String text = jCas.getDocumentText();

		final InputStream stream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
		final List<List<String>> sentences = tokeniser.segmentize(stream);

		int sentenceStart = 0;

		for (final List<String> sentence : sentences) {

			final DEPTree tree = new DEPTree(sentence);

			posTagger.process(tree);
			mpAnalyser.process(tree);

			// This is a little painful. The DepNode's a just strings, so we need to find them in
			// the text so we get the offsets.
			int tokenStart = 0;
			int tokenEnd = sentenceStart;

			for (int i = 0; i < tree.size(); i++) {

				final String word = tree.get(i).getWordForm();
				final String lemma = tree.get(i).getLemma();
				final String pos = tree.get(i).getPOSTag();

				tokenStart = text.indexOf(word, tokenEnd);

				if (tokenStart >= 0) {
					// Add a word token if we have found our word
					tokenEnd = tokenStart + word.length();

					final WordToken wordToken = new WordToken(jCas);
					wordToken.setBegin(tokenStart);
					wordToken.setEnd(tokenEnd);
					wordToken.setPartOfSpeech(pos);

					final WordLemma wordLemma = new WordLemma(jCas);
					wordLemma.setBegin(tokenStart);
					wordLemma.setEnd(tokenEnd);
					wordLemma.setLemmaForm(lemma);

					final FSArray lemmaArray = new FSArray(jCas, 1);
					lemmaArray.set(0, wordLemma);
					wordToken.setLemmas(lemmaArray);

					addToJCasIndex(wordToken);
				} else if (word.equals(DEPLib.ROOT_TAG)) {
					// Ignore tags
				} else {
					getLogger().warn("Not found a tokenised word in document text: " + word);
				}

			}

			// Do we have a non-zero length sentence?
			// If so create a setence
			if (sentenceStart != tokenEnd) {
				final Sentence s = new Sentence(jCas);
				s.setBegin(sentenceStart);
				s.setEnd(tokenEnd);
				addToJCasIndex(s);
			}

			sentenceStart = tokenEnd;

		}
	}

}
