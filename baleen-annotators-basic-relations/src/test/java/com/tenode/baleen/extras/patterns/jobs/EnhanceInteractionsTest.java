package com.tenode.baleen.extras.patterns.jobs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;
import com.tenode.baleen.extras.patterns.jobs.EnhanceInteractions;
import com.tenode.baleen.extras.wordnet.resources.WordNetResource;

import uk.gov.dstl.baleen.uima.jobs.AbstractBaleenTaskTest;

public class EnhanceInteractionsTest extends AbstractBaleenTaskTest {

	private ExternalResourceDescription wordnetErd;

	@Before
	public void before() {
		wordnetErd = ExternalResourceFactory.createExternalResourceDescription("wordnet", WordNetResource.class);
	}

	@Test
	public void test() throws UIMAException, IOException {
		final File input = File.createTempFile("test", "in");
		input.deleteOnExit();
		final File output = File.createTempFile("test", "out");
		output.deleteOnExit();

		Files.write("MOVEMENT,went,person,location,went,VERB", input, StandardCharsets.UTF_8);

		final AnalysisEngine ae = create(EnhanceInteractions.class, "wordnet", wordnetErd, "input",
				input.getAbsolutePath(), "output", output.getAbsolutePath());
		execute(ae);

		final List<String> lines = Files.readLines(output, StandardCharsets.UTF_8);
		assertFalse(lines.isEmpty());
		// Additional alternatives added
		assertTrue(lines.get(0).split(",").length > 6);

	}

}
