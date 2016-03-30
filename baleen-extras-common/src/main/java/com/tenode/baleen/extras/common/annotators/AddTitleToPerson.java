package com.tenode.baleen.extras.common.annotators;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.annotators.grammatical.NPTitleEntity;
import uk.gov.dstl.baleen.types.common.Person;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

/**
 * Add title (mr, president, etc) information to previously found people.
 * <p>
 * Often with NLP models we find a person, e.g. John Smith but omit the title information, e.g.
 * General John Smith, General Sir John Smith. This annotator adds that information back onto the
 * entity, thus improving the quality of person extraction and reducing the number of unannotated
 * words in a document.
 *
 * @baleen.javadoc
 */
public class AddTitleToPerson extends BaleenAnnotator {

	/** The titles. */
	private static Set<String> TITLES = new HashSet<>(Arrays.asList(NPTitleEntity.TITLES));

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.gov.dstl.baleen.uima.BaleenAnnotator#doProcess(org.apache.uima.jcas.JCas)
	 */
	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {
		// We copy this array as we'll modify people as we go
		Collection<Person> people = JCasUtil.select(jCas, Person.class);

		for (Person p : people) {

			while (true) {
				List<WordToken> preceding = JCasUtil.selectPreceding(WordToken.class, p, 1);
				if (preceding.isEmpty()) {
					// Stop if we run out of words
					break;
				}

				WordToken token = preceding.get(0);
				String text = token.getCoveredText().toLowerCase();

				if (TITLES.contains(text)) {
					// If we match modify the entity
					p.setBegin(token.getBegin());
					p.setTitle(extendTitle(token.getCoveredText(), p.getTitle()));
					// Not sure if I should do this... it might replace something better?
					p.setValue(p.getCoveredText());
				} else {
					// Stop if we don't find a title
					break;
				}
			}
		}
	}

	/**
	 * Add the prefix to the existing title.
	 *
	 * @param prefix
	 *            the prefix
	 * @param title
	 *            the title
	 * @return the string
	 */
	private String extendTitle(String prefix, String title) {
		if (title == null || title.isEmpty()) {
			return prefix;
		} else {
			return prefix + " " + title;
		}
	}

}
