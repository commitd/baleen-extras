package com.tenode.baleen.extras.jobs;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;
import com.mongodb.DBCollection;

import uk.gov.dstl.baleen.resources.SharedFongoResource;
import uk.gov.dstl.baleen.uima.jobs.AbstractBaleenTaskTest;

public class UploadInteractionsToMongoTest extends AbstractBaleenTaskTest {

	private ExternalResourceDescription fongoErd;

	@Before
	public void before() {
		fongoErd = ExternalResourceFactory.createExternalResourceDescription("mongo", SharedFongoResource.class,
				"fongo.collection", "na", "fongo.data",
				"[  ]");

	}

	@Test
	public void test() throws ResourceInitializationException, AnalysisEngineProcessException, IOException,
			ResourceAccessException {
		final File file = File.createTempFile("test", "uimt");
		Files.write("MOVEMENT,went,source,target,went,VERB,gone", file, StandardCharsets.UTF_8);

		final AnalysisEngine ae = create(UploadInteractionsToMongo.class, "mongo", fongoErd, "input", file);
		execute(ae);

		final SharedFongoResource sfr = (SharedFongoResource) ae.getUimaContext().getResourceObject("mongo");
		final DBCollection relationTypes = sfr.getDB().getCollection("relationTypes");
		assertTrue(relationTypes.count() > 0);
		final DBCollection interactions = sfr.getDB().getCollection("interactions");
		assertTrue(interactions.count() > 0);

	}

}
