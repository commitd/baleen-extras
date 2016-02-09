package com.tenode.baleen.extras.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.tenode.baleen.extras.consumers.relationships.MongoPatternSaver;
import com.tenode.baleen.extras.jobs.interactions.InteractionIdentifier;
import com.tenode.baleen.extras.jobs.interactions.data.PatternReference;
import com.tenode.baleen.extras.jobs.interactions.data.Word;
import com.tenode.baleen.wordnet.WordNetUtils;
import com.tenode.baleen.wordnet.resources.WordNetResource;

import net.sf.extjwnl.dictionary.Dictionary;
import uk.gov.dstl.baleen.resources.SharedMongoResource;
import uk.gov.dstl.baleen.uima.jobs.BaleenTask;
import uk.gov.dstl.baleen.uima.jobs.JobSettings;

/**
 * Identifity Interaction words based on a mongo collection of patterns.
 *
 * This requires a wordnet dictionary and a mongo resource. The mongo collection should hold
 * patterns which have been extracted by a pipeline @see MongoPatternSaver containing.
 */
public class IdentifyInteractions extends BaleenTask {

	/**
	 * Connection to Wordnet
	 *
	 * @baleen.resource com.tenode.baleen.resources.wordnet.WordNetResource}
	 */
	public static final String KEY_WORDNET = "wordnet";
	@ExternalResource(key = KEY_WORDNET)
	private WordNetResource wordnet;

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
	public static final String KEY_PATTERN_COLLECTION = "patternCollection";
	@ConfigurationParameter(name = KEY_PATTERN_COLLECTION, defaultValue = "patterns")
	private String patternCollection;

	/**
	 * The name of the Mongo collection to output the words to
	 *
	 * @baleen.config patterns
	 */
	public static final String KEY_INTERACTION_COLLECTION = "interactionCollection";
	@ConfigurationParameter(name = KEY_INTERACTION_COLLECTION, defaultValue = "interactions")
	private String interactionCollection;

	/**
	 * The name of the Mongo collection to hold the patterns
	 *
	 * @baleen.config minPatterns 2
	 */
	public static final String KEY_MIN_PATTERNS_IN_CLUSTER = "minPatterns";
	@ConfigurationParameter(name = KEY_MIN_PATTERNS_IN_CLUSTER, defaultValue = "2")
	private int minPatternsInCluster;

	/**
	 * The similarity threshold between two patterns (before they are consider the same)
	 *
	 * @baleen.config patterns 0.2
	 */
	public static final String KEY_THRESHOLD = "threshold";
	@ConfigurationParameter(name = MongoPatternSaver.KEY_COLLECTION, defaultValue = "0.2")
	private double threshold;

	@Override
	protected void execute(JobSettings settings) throws AnalysisEngineProcessException {

		Dictionary dictionary = wordnet.getDictionary();
		InteractionIdentifier identifier = new InteractionIdentifier(minPatternsInCluster, threshold, dictionary);
		List<PatternReference> patterns = readPatternsFromMongo();
		Stream<String> words = identifier.process(patterns);
		writeWordsToMongo(words);

	}

	private List<PatternReference> readPatternsFromMongo() {
		// TODO: Ideally this would do something in a more streaming manner, as there are likely to
		// be lots of examples

		DBCollection collection = mongo.getDB().getCollection(patternCollection);

		List<PatternReference> patterns = new ArrayList<>((int) collection.count());

		DBCursor cursor = collection.find();
		while (cursor.hasNext()) {
			DBObject o = cursor.next();
			// TODO: We don't currently use types (entity source target) but we could do
			// at least to output something to the relationship type filter

			BasicDBList list = (BasicDBList) o.get("words");
			List<Word> tokens = list.stream().map(l -> {
				BasicDBObject dbo = (BasicDBObject) l;
				String pos = dbo.getString("pos");
				String lemma = dbo.getString("lemma");

				// Fall back to actual text if no lemma
				if (lemma == null) {
					lemma = dbo.getString("text");
				}

				return new Word(lemma.trim().toLowerCase(), WordNetUtils.toPos(pos));
			}).filter(w -> w.getPos() != null)
					.collect(Collectors.toList());

			patterns.add(new PatternReference(o.get("_id").toString(), tokens));
		}

		return patterns;

	}

	private void writeWordsToMongo(Stream<String> words) {
		DBCollection collection = mongo.getDB().getCollection(interactionCollection);
		words.map(w -> {
			w = w.toLowerCase().trim();
			BasicDBObject dbo = new BasicDBObject("value", w);
			// Add in relationship type info so these are passed through to the interaction after
			// annotation
			dbo.put("relationshipType", w);
			dbo.put("relationSubType", w);
			return dbo;
		}).forEach(collection::save);
	}

}
