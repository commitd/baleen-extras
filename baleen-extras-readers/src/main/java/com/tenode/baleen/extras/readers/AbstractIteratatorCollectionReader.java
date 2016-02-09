package com.tenode.baleen.extras.readers;

import java.io.IOException;
import java.util.Iterator;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import uk.gov.dstl.baleen.exceptions.BaleenException;
import uk.gov.dstl.baleen.uima.BaleenCollectionReader;

public abstract class AbstractIteratatorCollectionReader<T> extends BaleenCollectionReader {

	private Iterator<T> iterator;

	@Override
	protected final void doInitialize(UimaContext context) throws ResourceInitializationException {
		try {
			iterator = initializeIterator(context);
		} catch (BaleenException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	protected final void doGetNext(JCas jCas) throws IOException, CollectionException {
		T next = iterator.next();
		apply(next, jCas);
	}

	@Override
	public final boolean doHasNext() throws IOException, CollectionException {
		return iterator.hasNext();
	}

	protected abstract Iterator<T> initializeIterator(UimaContext context) throws BaleenException;

	protected abstract void apply(T next, JCas jCas);

}
