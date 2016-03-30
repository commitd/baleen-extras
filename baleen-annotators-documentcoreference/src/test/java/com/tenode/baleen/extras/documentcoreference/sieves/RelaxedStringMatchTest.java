package com.tenode.baleen.extras.documentcoreference.sieves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import uk.gov.dstl.baleen.types.common.Organisation;
import uk.gov.dstl.baleen.types.common.Person;
import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;

public class RelaxedStringMatchTest extends AbstractCoreferenceSieveTest {

	public RelaxedStringMatchTest() {
		super(2);
	}

	@Test
	public void testRelaxStringMatch() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "The University of Warwick is near Coventry and that was the University at which Chris studied.";
		// university of warwick - university
		jCas.setDocumentText(text);

		Person chris = new Person(jCas);
		chris.setBegin(text.indexOf("Chris"));
		chris.setEnd(chris.getBegin() + "Chris".length());
		chris.addToIndexes();

		Organisation uow = new Organisation(jCas);
		uow.setBegin(text.indexOf("University of Warwick"));
		uow.setEnd(uow.getBegin() + "University of Warwick".length());
		uow.addToIndexes();

		Organisation u = new Organisation(jCas);
		u.setBegin(text.indexOf("University", uow.getEnd()));
		u.setEnd(u.getBegin() + "University".length());
		u.addToIndexes();

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		List<Organisation> location = new ArrayList<>(JCasUtil.select(jCas, Organisation.class));
		assertEquals(1, targets.size());
		assertSame(targets.get(0), location.get(0).getReferent());
		assertSame(targets.get(0), location.get(1).getReferent());
	}
}
