package com.tenode.baleen.extras.jobs.interactions.data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.resource.ResourceInitializationException;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.tenode.baleen.extras.jobs.interactions.InteractionIdentifier;
import com.tenode.baleen.resources.wordnet.WordNetResource;

import net.sf.extjwnl.data.POS;
import uk.gov.dstl.baleen.resources.SharedMongoResource;
import uk.gov.dstl.baleen.uima.jobs.BaleenTask;
import uk.gov.dstl.baleen.uima.jobs.JobSettings;

public class ClusterTask extends BaleenTask {

	/**
	 * Connection to Mongo
	 *
	 * @baleen.resource uk.gov.dstl.baleen.resources.SharedMongoResource
	 */
	public static final String KEY_MONGO = "mongo";
	@ExternalResource(key = KEY_MONGO)
	private SharedMongoResource mongo;

	/**
	 * WordNet
	 *
	 * @baleen.resource com.tenode.resources.WordNetResource
	 */
	public static final String KEY_WORDNET = "wordnet";
	@ExternalResource(key = KEY_WORDNET)
	private WordNetResource wordNet;

	/**
	 * The name of the Mongo collection to hold the patterns.
	 *
	 * These are as created by the MongoPatternSaver.
	 *
	 * @baleen.config patterns
	 */
	public static final String KEY_PATTERN_COLLECTION = "patternCollection";
	@ConfigurationParameter(name = KEY_PATTERN_COLLECTION, defaultValue = "patterns")
	private String patternCollectionName;

	/**
	 * The name of the Mongo collection to hold the output patterns.
	 *
	 * These are in a format readable by the Mongo Gazetteer Annotator. However not that the values
	 * are lemmas (not all the words)
	 *
	 * @baleen.config interactionWords
	 */
	// TODO: Should we try to build all forms into the gazetteer?
	public static final String KEY_INTERACTION_COLLECTION = "gazetterCollection";
	@ConfigurationParameter(name = KEY_INTERACTION_COLLECTION, defaultValue = "interactionWords")
	private String interactionCollectionName;

	/**
	 * The similarity threshold (0,1] (based on the inner product / cosine distance)
	 *
	 * The smaller threshold the more pattern clusters will be created.
	 *
	 * @baleen.config 0.4
	 */
	public static final String KEY_THRESHOLD = "threshold";
	@ConfigurationParameter(name = KEY_THRESHOLD, defaultValue = "0.4")
	private double threshold;

	/**
	 * The minimum number of patterns to be in a cluster before that cluster is considered valid.
	 *
	 * The lowest value is 1. Higher values will produce few, better defined clusters. With a value
	 * of 1 there will be a potential noise in the interaction words.
	 *
	 *
	 * @baleen.config 2
	 */
	public static final String KEY_MIN_PATTERNS = "minPatterns";
	@ConfigurationParameter(name = KEY_MIN_PATTERNS, defaultValue = "2")
	private int minPatternsInCluster;

	private DBCollection patternCollection;

	private DBCollection interactionCollection;

	@Override
	public void doInitialize(UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);

		patternCollection = mongo.getDB().getCollection(patternCollectionName);

		interactionCollection = mongo.getDB().getCollection(interactionCollectionName);
	}

	@Override
	protected void execute(JobSettings settings) throws AnalysisEngineProcessException {
		final List<PatternReference> patterns = loadPatternsFromMongo().collect(Collectors.toList());

		InteractionIdentifier identifier = new InteractionIdentifier(minPatternsInCluster, threshold,
				wordNet.getDictionary());
		Stream<String> interactionWords = identifier.process(patterns);

		// Replace the database
		interactionCollection.drop();
		interactionWords.forEach(w -> {
			BasicDBObject o = new BasicDBObject(1)
					.append("value", w);

			interactionCollection.save(o);
		});

	}

	private Stream<PatternReference> loadPatternsFromMongo() {
		return StreamSupport.stream(patternCollection.find().spliterator(), false)
				.map(o -> {
					final BasicDBList words = (BasicDBList) o.get("words");
					final List<Word> tokens = words.stream().map(w -> {
						// NOTE: The paper seems to suggest you use the word, not the lemma
						// and then normalise after the fact.
						// However doing that must reduce the chance of phrases being matched as the
						// same relation therefore we do it upfront as we want "Jack saw Jill, Jack
						// sees Jill, etc to be same)
						// Obviously the quality of this approach will depend on the quality of the
						// lemma outputs.

						DBObject object = (DBObject) w;
						String text = (String) object.get("text");
						String pos = (String) object.get("pos");
						String lemma = (String) object.get("lemma");

						if (lemma == null || lemma.isEmpty()) {
							// If we don't have lemma information, we just have to use the actual
							// word
							// TODO: Should be lemma here, using wordnet? Or trust the user wanted
							// this.
							lemma = text;
						}

						return new Word(lemma, posFromString(pos));
					}).collect(Collectors.toList());
					return new PatternReference(o.get("_id").toString(), tokens);
				});

	}

	private POS posFromString(String pos) {
		if (pos == null || pos.isEmpty()) {
			return null;
		}

		if (pos.charAt(0) == 'N') {
			return POS.NOUN;
		} else if (pos.charAt(0) == 'V') {
			return POS.VERB;
		} else {
			return null;
		}
	}

}
