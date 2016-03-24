package com.tenode.baleen.annotators.coreference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import com.tenode.baleen.annotators.documentcoreference.ReferentToEntity;

import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.types.common.Person;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.Location;
import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;

public class ReferentToEntityTest extends AbstractAnnotatorTest {

	public ReferentToEntityTest() {
		super(ReferentToEntity.class);
	}

	@Test
	public void test() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "John say that he would visit London";
		jCas.setDocumentText(text);

		ReferenceTarget rt = new ReferenceTarget(jCas);
		rt.addToIndexes();

		Person p = new Person(jCas);
		p.setBegin(0);
		p.setEnd(4);
		p.setReferent(rt);
		p.setValue("John");
		p.addToIndexes();

		WordToken he = new WordToken(jCas);
		he.setBegin(text.indexOf("he"));
		he.setEnd(he.getBegin() + "he".length());
		he.setReferent(rt);
		he.addToIndexes();

		Location l = new Location(jCas);
		l.setBegin(text.indexOf("London"));
		l.setEnd(l.getBegin() + "London".length());
		l.setValue("London");
		l.addToIndexes();

		processJCas();

		List<Entity> list = new ArrayList<>(JCasUtil.select(jCas, Entity.class));

		assertEquals(3, list.size());
		assertEquals("John", list.get(0).getValue());
		assertEquals("John", list.get(1).getValue());
		assertTrue(list.get(1) instanceof Person);
		assertEquals("London", list.get(2).getValue());
	}

}
