package com.tenode.baleen.extras.readers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReuterReaderTest {

	private final static String SGML = "<!DOCTYPE lewis SYSTEM \"lewis.dtd\">\n" +
			"<REUTERS TOPICS=\"YES\" LEWISSPLIT=\"TRAIN\" CGISPLIT=\"TRAINING-SET\" OLDID=\"5544\" NEWID=\"1\">\n" +
			"<DATE>1-JAN-1990 10:11:12.13</DATE>\n" +
			"<TOPICS><D>topics</D></TOPICS>\n" +
			"<PLACES><D>uk</D><D>usa</D></PLACES>\n" +
			"<PEOPLE></PEOPLE>\n" +
			"<ORGS></ORGS>\n" +
			"<EXCHANGES></EXCHANGES>\n" +
			"<COMPANIES></COMPANIES>\n" +
			"<UNKNOWN> \n" +
			"&#5;&#5;&#5;C T\n" +
			"&#22;&#22;&#1;f0704&#31;reute\n" +
			"</UNKNOWN>\n" +
			"<TEXT>&#2;\n" +
			"<TITLE>TITLE</TITLE>\n" +
			"<DATELINE>   DATELINE </DATELINE><BODY> Some example\n" +
			"text. \n" +
			"Reuter\n" +
			"&#3;</BODY></TEXT>\n" +
			"</REUTERS>\n" +
			"<REUTERS TOPICS=\"NO\" LEWISSPLIT=\"TRAIN\" CGISPLIT=\"TRAINING-SET\" OLDID=\"2\" NEWID=\"2\">\n" +
			"<DATE>2-FEB-2002 20:21:22.00</DATE>\n" +
			"<TOPICS></TOPICS>\n" +
			"<PLACES><D>usa</D></PLACES>\n" +
			"<PEOPLE></PEOPLE>\n" +
			"<ORGS></ORGS>\n" +
			"<EXCHANGES></EXCHANGES>\n" +
			"<COMPANIES></COMPANIES>\n" +
			"<UNKNOWN>blah</UNKNOWN>\n" +
			"<TEXT>&#2;\n" +
			"<TITLE>TITLE 2</TITLE>\n" +
			"<DATELINE>    LOCATION, Date - </DATELINE><BODY>Another example\n" +
			" Reute\n" +
			"&#3;</BODY></TEXT>\n" +
			"</REUTERS>\n";

	private ReuterReader reader;

	private static Path tmpDir;

	@BeforeClass
	public static void beforeClass() throws IOException {
		tmpDir = Files.createTempDirectory("reuterstest");
		Files.write(tmpDir.resolve("file.sgm"), SGML.getBytes(StandardCharsets.UTF_8));

	}

	@AfterClass
	public static void afterClass() {
		tmpDir.toFile().delete();
	}

	@Before
	public void before() throws ResourceInitializationException {
		final String path = tmpDir.toAbsolutePath().toString();
		// reader = (ReuterReader) CollectionReaderFactory.createReader(ReuterReader.class,
		// ReuterReader.KEY_PATH,
		// path);

		reader = new ReuterReader();
		reader.setSgmPath(path);
		reader.doInitialize(null);
	}

	@After
	public void after() {
	}

	@Test
	public void test() throws IOException, UIMAException {
		final JCas jCas = JCasFactory.createJCas();
		assertTrue(reader.doHasNext());
		reader.doGetNext(jCas);

		assertEquals("Some example\ntext.", jCas.getDocumentText());

		jCas.reset();
		assertTrue(reader.doHasNext());
		reader.doGetNext(jCas);

		assertEquals("Another example", jCas.getDocumentText());

		jCas.reset();
		assertFalse(reader.doHasNext());
	}
}
