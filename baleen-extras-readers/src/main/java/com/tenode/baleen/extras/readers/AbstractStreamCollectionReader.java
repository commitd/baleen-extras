package com.tenode.baleen.extras.readers;

import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.uima.UimaContext;

import uk.gov.dstl.baleen.exceptions.BaleenException;

/**
 * A collection reader which is generated as a stream..
 *
 * @param <T>
 *            the generic type
 */
public abstract class AbstractStreamCollectionReader<T> extends AbstractIteratatorCollectionReader<T> {

	@Override
	protected final Iterator<T> initializeIterator(UimaContext context) throws BaleenException {
		return initializeStream(context).iterator();
	}

	/**
	 * Initialize the stream.
	 *
	 * @param context
	 *            the context
	 * @return the stream
	 * @throws BaleenException
	 *             the baleen exception
	 */
	protected abstract Stream<T> initializeStream(UimaContext context) throws BaleenException;

}
