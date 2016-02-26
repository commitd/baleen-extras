package com.tenode.baleen.extras.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.resource.ResourceInitializationException;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.tenode.baleen.extras.jobs.interactions.InteractionIdentifier;
import com.tenode.baleen.extras.jobs.interactions.data.InteractionWord;
import com.tenode.baleen.extras.jobs.interactions.data.PatternReference;
import com.tenode.baleen.extras.jobs.interactions.data.Word;
import com.tenode.baleen.extras.jobs.writers.CsvInteractionWriter;
import com.tenode.baleen.extras.jobs.writers.InteractionWordWriter;
import com.tenode.baleen.extras.jobs.writers.MongoInteractionWriter;
import com.tenode.baleen.extras.jobs.writers.MonitorInteractionWriter;
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
	 *
	 * The name of the Mongo collection to read
	 *
	 * @baleen.config patterns
	 */
	public static final String KEY_PATTERN_COLLECTION = "patternCollection";
	@ConfigurationParameter(name = KEY_PATTERN_COLLECTION, defaultValue = "patterns")
	private String patternCollection;

	/**
	 * The name of the Mongo collection to outputs type (source, target, type) constraints too
	 *
	 * @baleen.config patterns
	 */
	public static final String KEY_RELATIONSHIP_COLLECTION = "relationTypesCollection";
	@ConfigurationParameter(name = KEY_RELATIONSHIP_COLLECTION, defaultValue = "relationTypes")
	private String relationTypesCollection;

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
	@ConfigurationParameter(name = KEY_THRESHOLD, defaultValue = "0.2")
	private double threshold;

	/**
	 * Log the information on completion
	 *
	 * @baleen.config patterns false
	 */
	public static final String KEY_OUTPUT = "log";
	@ConfigurationParameter(name = KEY_OUTPUT, defaultValue = "false")
	private boolean outputToLog;

	/**
	 * Save the data to csv, with filename prefixed by tje value.
	 *
	 * Leave this blank for no output.
	 *
	 * @baleen.config csv interactions-
	 */
	public static final String KEY_CSV_FILENAME = "csv";
	@ConfigurationParameter(name = KEY_CSV_FILENAME, defaultValue = "interactions.csv")
	private String csvFilename;

	private Dictionary dictionary;

	private final List<InteractionWordWriter> interactionWriters = new ArrayList<>();

	@Override
	public void doInitialize(UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);

		if (outputToLog) {
			interactionWriters.add(new MonitorInteractionWriter(getMonitor()));
		}

		if (StringUtils.isNotEmpty(interactionCollection) && StringUtils.isNotEmpty(relationTypesCollection)) {
			interactionWriters
					.add(new MongoInteractionWriter(mongo.getDB(), relationTypesCollection, interactionCollection));
		}

		if (StringUtils.isNotEmpty(csvFilename)) {
			interactionWriters.add(new CsvInteractionWriter(getMonitor(), csvFilename));
		}
	}

	@Override
	protected void execute(JobSettings settings) throws AnalysisEngineProcessException {
		dictionary = wordnet.getDictionary();
		InteractionIdentifier identifier = new InteractionIdentifier(getMonitor(), minPatternsInCluster, threshold);
		getMonitor().info("Loading patterns from Mongo");
		List<PatternReference> patterns = readPatternsFromMongo();
		getMonitor().info("Found {} patterns", patterns.size());
		getMonitor().info("Extracting interaction words...");
		Stream<InteractionWord> words = identifier.process(patterns);
		getMonitor().info("Writing interaction words...");
		write(words);
		getMonitor().info("Interaction identification complete");

	}

	private List<PatternReference> readPatternsFromMongo() {
		// TODO: Ideally this would do something in a more streaming manner, as there are likely to
		// be lots of examples. Loading all patterns into memory might be prohibitive.

		DBCollection collection = mongo.getDB().getCollection(patternCollection);

		List<PatternReference> patterns = new ArrayList<>((int) collection.count());

		DBCursor cursor = collection.find();
		while (cursor.hasNext()) {
			DBObject o = cursor.next();

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

			PatternReference pattern = new PatternReference(o.get("_id").toString(), tokens);
			pattern.setSourceType(((BasicDBObject) o.get("source")).getString("type"));
			pattern.setTargetType(((BasicDBObject) o.get("target")).getString("type"));
			patterns.add(pattern);
		}

		return patterns;

	}

	private void write(Stream<InteractionWord> words) {

		interactionWriters.forEach(w -> w.initialise());

		words.forEach(interaction -> {
			List<String> alternatives = interaction.getAlternativeWords(dictionary)
					.map(s -> s.trim().toLowerCase())
					.filter(s -> s.length() > 2)
					.collect(Collectors.toList());
			String lemma = interaction.getWord().getLemma();

			// TODO: Find the best
			String relationshipType = wordnet.getSuperSenses(interaction.getWord().getPos(), lemma).findAny()
					.orElse(lemma);

			if (!alternatives.isEmpty()) {
				interactionWriters.forEach(writer -> writer.write(interaction, relationshipType, lemma, alternatives));
			}

		});

		interactionWriters.forEach(w -> w.destroy());
	}

}
