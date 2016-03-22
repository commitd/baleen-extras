package com.tenode.baleen.extras.readers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.elasticsearch.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.dstl.baleen.exceptions.BaleenException;
import uk.gov.dstl.baleen.uima.UimaSupport;

/**
 * Read the MUC-3 dataset.
 *
 * The text is all upper case, which Baleen performs poorly on, it also contains metadata (not just
 * the article text). We lower case the extra and remove excess metadata to create the jCas document
 * text.
 *
 * @baleen.javadoc
 */
public class MucReader extends AbstractStreamCollectionReader<MucEntry> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MucReader.class);

	// This should be \n\n\n but the TST-MUC-11 is different!
	private static final Splitter ARTICLE_SPLITTER = Splitter.on(Pattern.compile("\n\n\\s*\n")).trimResults()
			.omitEmptyStrings();

	/**
	 * Location of the directory containing the muc34 files.
	 *
	 * Note that only files which do not beign with key- will be used.
	 *
	 * @baleen.config path
	 */
	public static final String KEY_PATH = "path";
	@ConfigurationParameter(name = KEY_PATH, mandatory = true)
	private String mucPath;

	/**
	 * Sets the MUC file path - used for tests only.
	 *
	 * @param path
	 *            the new muc path
	 */
	public void setMucPath(String path) {
		this.mucPath = path;
	}

	@Override
	protected Stream<MucEntry> initializeStream(UimaContext context) throws BaleenException {
		final File[] files = new File(mucPath)
				.listFiles(f -> !f.getName().startsWith("key-") && f.isFile());

		Stream<MucEntry> map = Arrays.stream(files)
				.flatMap(f -> {
					try {
						final byte[] bytes = Files.readAllBytes(f.toPath());
						return StreamSupport.stream(ARTICLE_SPLITTER.split(new String(bytes, "UTF-8")).spliterator(),
								false);
					} catch (final Exception e) {
						LOGGER.warn("Discarding invalid content of {}", f, e);
						return Stream.empty();
					}
				}).map(text -> {

					final int nlIndex = text.indexOf("\n", 1);
					// Strip the first lines up to a the article start (signified by a --)
					final int textIndex = text.indexOf("--");
					if (nlIndex != -1 && textIndex != -1) {
						final String id = text.substring(0, nlIndex);
						final String content = text.substring(textIndex + 2).trim();
						return new MucEntry(id, content);
					} else {
						return null;
					}
				}).filter(Objects::nonNull)
				.map(e -> {
					String text = e.getText();
					// Make the paragraphs and text nicer
					text = text.replaceAll("\n", " ");

					// Strip out the clarification tags []
					text = text.replaceAll("(\\[.*?\\]\\s*)*", "");
					text = text.replaceAll("\\s{3,}", "\n\n");
					text = text.toLowerCase().trim();
					// Baleen bug? Lower case U.S. breaks the sentence splitter?
					text = text.replaceAll(Pattern.quote("u.s."), "us");
					e.setText(text);
					return e;
				});

		return map;
	}

	@Override
	protected void apply(MucEntry entry, JCas jCas) {
		jCas.setDocumentLanguage("en");
		jCas.setDocumentText(entry.getText());

		// This if is only reuqired for testing!
		final UimaSupport support = getSupport();
		if (support != null) {
			final DocumentAnnotation documentAnnotation = support.getDocumentAnnotation(jCas);
			documentAnnotation.setSourceUri(entry.getId());
		}
	}

	@Override
	protected void doClose() throws IOException {
		// Do nothing
	}

}
