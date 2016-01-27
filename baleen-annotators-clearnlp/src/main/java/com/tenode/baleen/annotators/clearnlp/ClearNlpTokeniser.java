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
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.tokenization.AbstractTokenizer;
import edu.emory.clir.clearnlp.util.lang.TLanguage;
import uk.gov.dstl.baleen.types.language.Sentence;
import uk.gov.dstl.baleen.types.language.WordLemma;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

public class ClearNlpTokeniser extends BaleenAnnotator {

	private AbstractPOSTagger posTagger;
	private AbstractMPAnalyzer mpAnalyser;
	private AbstractTokenizer tokeniser;

	@Override
	public void doInitialize(UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);

		TLanguage language = TLanguage.ENGLISH;

		tokeniser = NLPUtils.getTokenizer(language);
		mpAnalyser = NLPUtils.getMPAnalyzer(language);
		posTagger = NLPUtils.getPOSTagger(language, "general-en-pos.xz");

	}

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {
		String text = jCas.getDocumentText();

		InputStream stream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
		List<List<String>> sentences = tokeniser.segmentize(stream);

		int sentenceStart = 0;

		for (List<String> sentence : sentences) {

			DEPTree tree = new DEPTree(sentence);

			posTagger.process(tree);
			mpAnalyser.process(tree);

			// This is a little painful. The DepNode's a just strings, so we need to find them in
			// the text so we get the offsets.
			int tokenStart = 0;
			int tokenEnd = sentenceStart;

			for (int i = 0; i < tree.size(); i++) {

				String word = tree.get(i).getWordForm();
				String lemma = tree.get(i).getLemma();
				String pos = tree.get(i).getPOSTag();

				tokenStart = text.indexOf(word, tokenEnd);

				if (tokenStart >= 0) {
					// Add a word token if we have found our word
					tokenEnd = tokenStart + word.length();

					WordToken wordToken = new WordToken(jCas);
					wordToken.setBegin(tokenStart);
					wordToken.setEnd(tokenEnd);
					wordToken.setPartOfSpeech(pos);

					WordLemma wordLemma = new WordLemma(jCas);
					wordLemma.setBegin(tokenStart);
					wordLemma.setEnd(tokenEnd);
					wordLemma.setLemmaForm(lemma);

					FSArray lemmaArray = new FSArray(jCas, 1);
					lemmaArray.set(0, wordLemma);
					wordToken.setLemmas(lemmaArray);

					addToJCasIndex(wordToken);
				} else {
					getLogger().warn("Not found a tokenised word in document text: " + word);
				}

			}

			// Do we have a non-zero length sentence?
			// If so create a setence
			if (sentenceStart != tokenEnd) {
				Sentence s = new Sentence(jCas);
				s.setBegin(sentenceStart);
				s.setEnd(tokenEnd);
				addToJCasIndex(s);
			}

			sentenceStart = tokenEnd;

		}
	}

}
