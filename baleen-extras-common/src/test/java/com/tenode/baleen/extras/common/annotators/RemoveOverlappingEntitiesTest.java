package com.tenode.baleen.extras.common.annotators;

import static org.junit.Assert.assertEquals;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.annotators.testing.Annotations;
import uk.gov.dstl.baleen.types.common.Person;
import uk.gov.dstl.baleen.types.semantic.Location;
import uk.gov.dstl.baleen.types.temporal.DateType;

public class RemoveOverlappingEntitiesTest extends AbstractAnnotatorTest {

	public RemoveOverlappingEntitiesTest() {
		super(RemoveOverlappingEntities.class);
	}

	@Test
	public void test() throws Exception {
		populateJCas(jCas);

		processJCas();

		assertEquals(1, JCasUtil.select(jCas, Person.class).size());
		assertEquals(1, JCasUtil.select(jCas, DateType.class).size());
		assertEquals(1, JCasUtil.select(jCas, Location.class).size());

		DateType dt = JCasUtil.selectByIndex(jCas, DateType.class, 0);
		assertEquals("December 1972", dt.getCoveredText());

		Location l = JCasUtil.selectByIndex(jCas, Location.class, 0);
		assertEquals("Oxford", l.getCoveredText());
	}

	private void populateJCas(JCas jCas) {
		jCas.setDocumentText("Eliza was born in December 1972 in Oxford");

		Annotations.createPerson(jCas, 0, 5, "Eliza");
		Annotations.createPerson(jCas, 1, 5, "liza");
		Annotations.createDateType(jCas, 18, 31, "December 1972");
		Annotations.createDateType(jCas, 18, 26, "December");
		Annotations.createLocation(jCas, 35, 37, "OX", null);
		Annotations.createLocation(jCas, 35, 41, "Oxford", null);
		Annotations.createLocation(jCas, 36, 41, "xford", null);
	}

}
