package com.tenode.baleen.extra.annotators.relationships;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import uk.gov.dstl.baleen.resources.SharedMongoResource;
import uk.gov.dstl.baleen.types.Base;
import uk.gov.dstl.baleen.types.language.Pattern;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

public class MongoPatternSaver extends BaleenAnnotator {

	/**
	 * Connection to Mongo
	 *
	 * @baleen.resource uk.gov.dstl.baleen.resources.SharedMongoResource
	 */
	public static final String KEY_MONGO = "mongo";
	@ExternalResource(key = KEY_MONGO)
	private SharedMongoResource mongo;

	/**
	 * The name of the Mongo collection to hold the patterns
	 *
	 * @baleen.config patterns
	 */
	public static final String KEY_COLLECTION = "collection";
	@ConfigurationParameter(name = KEY_COLLECTION, defaultValue = "patterns")
	private String collection;

	private DBCollection dbCollection;

	@Override
	public void doInitialize(UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);

		dbCollection = mongo.getDB().getCollection(collection);
	}

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {

		for (final Pattern pattern : JCasUtil.select(jCas, Pattern.class)) {
			final Base source = pattern.getSource();
			final Base target = pattern.getTarget();

			if (source instanceof Entity && target instanceof Entity) {
				final DBObject object = new BasicDBObject()
						.append("source", saveEntity((Entity) source))
						.append("target", saveEntity((Entity) target))
						.append("words", saveWords(pattern));

				dbCollection.save(object);
			}
		}
	}

	private DBObject saveWords(Pattern pattern) {
		final BasicDBList list = new BasicDBList();
		for (int i = 0; i < pattern.getWords().size(); i++) {
			final WordToken w = pattern.getWords(i);
			final BasicDBObject o = new BasicDBObject()
					.append("text", w.getCoveredText())
					.append("pos", w.getPartOfSpeech());

			if (w.getLemmas() != null && w.getLemmas().size() >= 1) {
				o.put("lemma", w.getLemmas(0).getLemmaForm());
			}

			list.add(o);
		}
		return list;
	}

	private DBObject saveEntity(Entity entity) {
		return new BasicDBObject()
				.append("text", entity.getCoveredText())
				.append("type", entity.getTypeName());
	}

}
