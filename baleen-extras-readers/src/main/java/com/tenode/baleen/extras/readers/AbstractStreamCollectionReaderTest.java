package com.tenode.baleen.extras.readers;

import java.io.IOException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import uk.gov.dstl.baleen.exceptions.BaleenException;

public class AbstractStreamCollectionReaderTest {

	@Test
	public void testSkip() {

	}

	@Test
	public void testMax() {

	}

	@Test
	public void testSkipAndMax() {

	}

	public class FakeStreamCollectionReader extends AbstractStreamCollectionReader<Integer> {

		@Override
		public void setSkipDocuments(int skipDocuments) {
			super.setSkipDocuments(skipDocuments);
		}

		@Override
		protected void setMaxDocuments(Integer max) {
			super.setMaxDocuments(max);
		}

		@Override
		protected Stream<Integer> initializeStream(UimaContext context) throws BaleenException {
			return IntStream.range(0, 10).boxed();
		}

		@Override
		protected void apply(Integer next, JCas jCas) {
			// Do nothing
		}

		@Override
		protected void doClose() throws IOException {
			// Do nothing
		}

	}

}
