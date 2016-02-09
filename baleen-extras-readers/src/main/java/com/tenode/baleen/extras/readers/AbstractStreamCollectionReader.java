package com.tenode.baleen.extras.readers;

import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.uima.UimaContext;

import uk.gov.dstl.baleen.exceptions.BaleenException;

public abstract class AbstractStreamCollectionReader<T> extends AbstractIteratatorCollectionReader<T> {

	@Override
	protected final Iterator<T> initializeIterator(UimaContext context) throws BaleenException {
		return initializeStream(context).iterator();
	}

	protected abstract Stream<T> initializeStream(UimaContext context) throws BaleenException;

}
