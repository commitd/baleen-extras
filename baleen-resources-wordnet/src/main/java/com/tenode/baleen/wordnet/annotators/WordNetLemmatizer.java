package com.tenode.baleen.wordnet.annotators;

import java.util.Optional;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

import com.tenode.baleen.wordnet.WordNetUtils;
import com.tenode.baleen.wordnet.resources.WordNetResource;

import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import uk.gov.dstl.baleen.types.language.WordLemma;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

public class WordNetLemmatizer extends BaleenAnnotator {

	/**
	 * Connection to Wordnet
	 *
	 * @baleen.resource com.tenode.baleen.resources.wordnet.WordNetResource
	 */
	public static final String KEY_WORDNET = "wordnet";
	@ExternalResource(key = KEY_WORDNET)
	private WordNetResource wordnet;

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {
		for (WordToken t : JCasUtil.select(jCas, WordToken.class)) {
			if (t.getLemmas() == null || t.getLemmas().size() > 0) {
				String text = t.getCoveredText();
				POS pos = WordNetUtils.toPos(t.getPartOfSpeech());
				if (pos != null) {
					Optional<IndexWord> lookupWord = wordnet.lookupWord(pos, text);
					if (lookupWord.isPresent()) {
						t.setLemmas(new FSArray(jCas, 1));
						WordLemma wordLemma = new WordLemma(jCas);
						wordLemma.setLemmaForm(lookupWord.get().getLemma());
						t.setLemmas(0, wordLemma);
					}
				}
			}
		}
	}

}
