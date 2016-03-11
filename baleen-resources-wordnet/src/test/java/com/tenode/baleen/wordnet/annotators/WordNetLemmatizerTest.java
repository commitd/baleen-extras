package com.tenode.baleen.wordnet.annotators;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import com.tenode.baleen.wordnet.resources.WordNetResource;

import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.types.language.WordLemma;
import uk.gov.dstl.baleen.types.language.WordToken;

public class WordNetLemmatizerTest extends AbstractAnnotatorTest {

	private final ExternalResourceDescription wordnetErd;

	public WordNetLemmatizerTest() {
		super(WordNetLemmatizer.class);
		wordnetErd = ExternalResourceFactory.createExternalResourceDescription("wordnet", WordNetResource.class);

	}

	@Test
	public void test() throws UIMAException, ResourceInitializationException {
		jCas.setDocumentText("Is this working?");

		final WordToken t = new WordToken(jCas);
		t.setBegin(jCas.getDocumentText().indexOf("working"));
		t.setEnd(t.getBegin() + "working".length());
		t.setPartOfSpeech("VERB");
		t.addToIndexes();

		// Add an another with an lemma already
		final WordToken s = new WordToken(jCas);
		s.setBegin(jCas.getDocumentText().indexOf("working"));
		s.setEnd(t.getBegin() + "working".length());
		s.setPartOfSpeech("VERB");
		s.setLemmas(new FSArray(jCas, 1));
		final WordLemma existingLemma = new WordLemma(jCas);
		existingLemma.setPartOfSpeech("existing");
		existingLemma.setLemmaForm("existing");
		s.setLemmas(0, existingLemma);
		s.addToIndexes();

		processJCas("wordnet", wordnetErd);

		final List<WordToken> out = new ArrayList<>(JCasUtil.select(jCas, WordToken.class));

		assertEquals("work", out.get(0).getLemmas(0).getLemmaForm());
		assertEquals(existingLemma, out.get(1).getLemmas(0));
	}

}
