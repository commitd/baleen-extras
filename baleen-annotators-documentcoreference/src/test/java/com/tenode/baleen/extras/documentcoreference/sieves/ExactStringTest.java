package com.tenode.baleen.extras.documentcoreference.sieves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import uk.gov.dstl.baleen.types.common.Person;
import uk.gov.dstl.baleen.types.semantic.Location;
import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;

public class ExactStringTest extends AbstractCoreferenceSieveTest {

	public ExactStringTest() {
		super(1);
	}

	@Test
	public void testExactStringMatch() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "Chris went to London and in London he saw Big Ben.";
		// london - london
		jCas.setDocumentText(text);

		Person chris = new Person(jCas);
		chris.setBegin(text.indexOf("Chris"));
		chris.setEnd(chris.getBegin() + "Chris".length());
		chris.addToIndexes();

		Location london = new Location(jCas);
		london.setBegin(text.indexOf("London"));
		london.setEnd(london.getBegin() + "London".length());
		london.addToIndexes();

		Location london2 = new Location(jCas);
		london2.setBegin(text.indexOf("London", london.getEnd()));
		london2.setEnd(london2.getBegin() + "London".length());
		london2.addToIndexes();

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		List<Location> location = new ArrayList<>(JCasUtil.select(jCas, Location.class));
		assertEquals(1, targets.size());
		assertSame(targets.get(0), location.get(0).getReferent());
		assertSame(targets.get(0), location.get(1).getReferent());
	}

}
