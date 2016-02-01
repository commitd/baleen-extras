package com.tenode.baleen.extras.readers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.gov.dstl.baleen.uima.BaleenCollectionReader;

/**
 * A collection reader which loads SGM files from the Reuters21578 archive.
 *
 * Available for download at http://www.daviddlewis.com/resources/testcollections/reuters21578/
 *
 * Extract the data (use 'tar xvf reuters21579.tar.gz' or 7zip on Windows).
 */
public class ReuterReader extends BaleenCollectionReader {

	public ReuterReader() {
		// Do nothing
	}

	/**
	 * Location of the directory containing the sgm files.
	 *
	 * @baleen.resource String
	 */
	public static final String KEY_PATH = "path";
	@ExternalResource(key = KEY_PATH, mandatory = false)
	String sgmPath;

	private Iterator<String> documents;

	public void setSgmPath(final String sgmPath) {
		this.sgmPath = sgmPath;
	}

	@Override
	protected void doInitialize(final UimaContext context) throws ResourceInitializationException {
		final File[] files = new File(sgmPath)
				.listFiles(f -> f.getName().endsWith(".sgm"));

		DocumentBuilder documentBuilder;
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			documentBuilder = factory.newDocumentBuilder();
		} catch (final Exception e) {
			throw new ResourceInitializationException(e);
		}

		documents = Arrays.stream(files)
				.flatMap(sgmlFile -> {

					try {
						final byte[] bytes = Files.readAllBytes(sgmlFile.toPath());
						final String sgml = new String(bytes, "UTF-8");

						// Remove the <!DOCTYPE lewis SYSTEM "lewis.dtd">
						// Then add a root element
						String xml = "<root>" + sgml.substring("<!DOCTYPE lewis SYSTEM \"lewis.dtd\">".length())
								+ "</root>";

						// Remove the
						xml = xml.replaceAll("&#\\d+;", "");

						final ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes("UTF-8"));
						final Document doc = documentBuilder.parse(input);
						final NodeList reutersDocument = doc.getElementsByTagName("REUTERS");
						return nodeListToElements(reutersDocument);
					} catch (final Exception e) {
						getMonitor().warn("Unable to process SGML file {}", sgmlFile.getAbsolutePath(), e);
					}

					return Stream.<Element> empty();

				}).flatMap(e -> {
					return nodeListToText(e.getElementsByTagName("BODY"));
				}).filter(s -> {
					return !s.isEmpty();
				}).iterator();
	}

	private Stream<String> nodeListToText(final NodeList list) {
		final List<String> elements = new ArrayList<>(list.getLength());

		for (int i = 0; i < list.getLength(); i++) {
			final Node n = list.item(i);
			String text = n.getTextContent();
			text = text.replaceAll("Reuter?\\s*$", "");
			elements.add(text.trim());
		}

		return elements.stream();
	}

	private Stream<Element> nodeListToElements(final NodeList list) {
		final List<Element> elements = new ArrayList<>(list.getLength());

		for (int i = 0; i < list.getLength(); i++) {
			final Node n = list.item(i);
			if (n.getNodeType() == Element.ELEMENT_NODE) {
				elements.add((Element) n);
			}
		}

		return elements.stream();
	}

	@Override
	public boolean doHasNext() throws IOException, CollectionException {
		return documents.hasNext();
	}

	@Override
	public void doGetNext(final JCas jCas) throws IOException, CollectionException {
		final String text = documents.next();
		jCas.setDocumentLanguage("en");
		jCas.setDocumentText(text);
	}

	@Override
	protected void doClose() throws IOException {
		// Do nothing
	}
}
