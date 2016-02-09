package com.tenode.baleen.extras.readers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.elasticsearch.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.dstl.baleen.exceptions.BaleenException;

public class MucReader extends AbstractStreamCollectionReader<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MucReader.class);

	private static final Splitter ARTICLE_SPLITTER = Splitter.on("\n\n\n").trimResults().omitEmptyStrings();

	/**
	 * Location of the directory containing the muc34 files.
	 *
	 * Note that only files which do not beign with key- will be used.
	 *
	 * @baleen.resource String
	 */
	public static final String KEY_PATH = "path";
	@ConfigurationParameter(name = KEY_PATH, mandatory = true)
	private String mucPath;

	@Override
	protected Stream<String> initializeStream(UimaContext context) throws BaleenException {
		final File[] files = new File(mucPath)
				.listFiles(f -> !f.getName().startsWith("key-") && f.isFile());

		return Arrays.stream(files)
				.flatMap(f -> {
					try {
						byte[] bytes = Files.readAllBytes(f.toPath());
						return StreamSupport.stream(ARTICLE_SPLITTER.split(new String(bytes, "UTF-8")).spliterator(),
								false);
					} catch (Exception e) {
						LOGGER.warn("Discarding invalid content of {}", f, e);
						return Stream.empty();
					}
				}).map(text -> {
					// Strip the first lines up to a the article start (signified by a --)
					int index = text.indexOf("--");
					if (index != -1) {
						return text.substring(index + 2).trim();
					} else {
						return text;
					}
				}).map(text -> {
					// Strip out the clarification tags []
					return text.replaceAll("\\[.*?\\]", "");
				});
	}

	@Override
	protected void apply(String text, JCas jCas) {
		jCas.setDocumentLanguage("en");
		jCas.setDocumentText(text);
	}

	@Override
	protected void doClose() throws IOException {
		// Do nothing
	}

}
