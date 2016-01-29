package com.tenode.baleen.extra.annotators.relationships;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import com.tenode.baleen.extra.annotators.relationships.SimpleInteractionRelationship;

import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.annotators.testing.Annotations;
import uk.gov.dstl.baleen.types.common.Person;
import uk.gov.dstl.baleen.types.language.Interaction;
import uk.gov.dstl.baleen.types.language.Sentence;
import uk.gov.dstl.baleen.types.semantic.Location;
import uk.gov.dstl.baleen.types.semantic.Relation;

public class SimpleInteractionRelationshipTest extends AbstractAnnotatorTest {

	public SimpleInteractionRelationshipTest() {
		super(SimpleInteractionRelationship.class);
	}

	@Test
	public void testDoProcess() throws AnalysisEngineProcessException, ResourceInitializationException {

		jCas.setDocumentText("Jon visits London.");

		Sentence s = new Sentence(jCas);
		s.setBegin(0);
		s.setEnd(jCas.size());
		s.addToIndexes();

		Person person = Annotations.createPerson(jCas, 0, 3, "Jon");
		Location location = Annotations.createLocation(jCas, 12, 18, "London", "");

		Interaction interaction = new Interaction(jCas);
		interaction.setBegin(5);
		interaction.setBegin(11);
		interaction.setRelationshipType("visit");
		interaction.setValue("visit");
		interaction.addToIndexes();

		processJCas();

		List<Relation> relations = new ArrayList<>(JCasUtil.select(jCas, Relation.class));
		assertEquals(1, relations.size());
		Relation r = relations.get(0);

		assertEquals(person, r.getSource());
		assertEquals(location, r.getTarget());
		assertEquals("visit", r.getRelationshipType());

	}

}
