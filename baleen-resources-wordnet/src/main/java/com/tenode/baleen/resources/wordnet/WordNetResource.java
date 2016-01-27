package com.tenode.baleen.resources.wordnet;

import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import uk.gov.dstl.baleen.uima.BaleenResource;

public class WordNetResource extends BaleenResource {

	private Dictionary dictionary;

	@Override
	protected boolean doInitialize(ResourceSpecifier specifier, Map<String, Object> additionalParams)
			throws ResourceInitializationException {

		try {
			dictionary = Dictionary.getDefaultResourceInstance();
		} catch (JWNLException e) {
			throw new ResourceInitializationException(e);
		}

		return super.doInitialize(specifier, additionalParams);
	}

	public Dictionary getDictionary() {
		return dictionary;
	}

	public IndexWord getWord(POS pos, String lemma) throws JWNLException {
		return dictionary.lookupIndexWord(pos, lemma);
	}

}
