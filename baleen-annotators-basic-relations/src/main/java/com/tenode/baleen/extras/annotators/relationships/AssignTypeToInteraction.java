package com.tenode.baleen.extras.annotators.relationships;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mongodb.BasicDBList;
import com.mongodb.DBCollection;
import com.tenode.baleen.extras.common.jcas.SpanUtils;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;
import uk.gov.dstl.baleen.resources.SharedMongoResource;
import uk.gov.dstl.baleen.types.language.Interaction;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

public class AssignTypeToInteraction extends BaleenAnnotator {
	/**
	 * Connection to Mongo
	 *
	 * @baleen.resource uk.gov.dstl.baleen.resources.SharedMongoResource
	 */
	public static final String KEY_MONGO = "mongo";
	@ExternalResource(key = KEY_MONGO)
	private SharedMongoResource mongo;

	/**
	 * The name of the Mongo collection containing the relation types
	 *
	 * @baleen.config gazetteer
	 */
	public static final String PARAM_COLLECTION = "collection";
	@ConfigurationParameter(name = PARAM_COLLECTION, defaultValue = "relationTypes")
	private String collection;

	/**
	 * The name of the field in Mongo that contains the relation type
	 *
	 * @baleen.config type
	 */
	public static final String PARAM_TYPE_FIELD = "typeField";
	@ConfigurationParameter(name = PARAM_TYPE_FIELD, defaultValue = "type")
	private String typeField;

	/**
	 * The name of the field in Mongo that contains the relation sub type
	 *
	 * @baleen.config type
	 */
	public static final String PARAM_SUBTYPE_FIELD = "subTypeField";
	@ConfigurationParameter(name = PARAM_SUBTYPE_FIELD, defaultValue = "subType")
	private String subTypeField;

	/**
	 * The name of the field in Mongo that contains the relation source type
	 *
	 * @baleen.config source
	 */
	public static final String PARAM_SOURCE_FIELD = "sourceField";
	@ConfigurationParameter(name = PARAM_SOURCE_FIELD, defaultValue = "source")
	private String sourceField;

	/**
	 * The name of the field in Mongo that contains the relation source type
	 *
	 * @baleen.config target
	 */
	public static final String PARAM_TARGET_FIELD = "targetField";
	@ConfigurationParameter(name = PARAM_TARGET_FIELD, defaultValue = "target")
	private String targetField;

	/**
	 * The name of the field in Mongo that contains the relation pos
	 *
	 * @baleen.config posField pos
	 */
	public static final String PARAM_POS_FIELD = "posField";
	@ConfigurationParameter(name = PARAM_POS_FIELD, defaultValue = "pos")
	private String posField;

	/**
	 * The name of the field in Mongo that contains the relation values
	 *
	 * @baleen.config posField pos
	 */
	public static final String PARAM_VALUES_FIELD = "valueField";
	@ConfigurationParameter(name = PARAM_VALUES_FIELD, defaultValue = "value")
	private String valuesField;

	/**
	 * The stemming algorithm to use, as defined in OpenNLP's SnowballStemmer.ALGORITHM enum
	 *
	 * @baleen.config ENGLISH
	 */
	public static final String PARAM_ALGORITHM = "algorithm";
	@ConfigurationParameter(name = PARAM_ALGORITHM, defaultValue = "ENGLISH")
	protected String algorithm;

	/**
	 * Should the words be stemmed before processing?
	 *
	 * Set false if you want a very precise match against your values, effectively they must be the
	 * interaction values. Set to true for a more relaxed match but which might produce false
	 * positives.
	 *
	 * @baleen.config true
	 */
	public static final String PARAM_STEM = "stem";
	@ConfigurationParameter(name = PARAM_STEM, defaultValue = "true")
	protected boolean stem;

	private final Multimap<String, InteractionTypeDefinition> definitions = HashMultimap.create();
	private SnowballStemmer stemmer;

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.gov.dstl.baleen.uima.BaleenAnnotator#doInitialize(org.apache.uima.UimaContext)
	 */
	@Override
	public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);

		ALGORITHM algo = ALGORITHM.valueOf(algorithm);
		if (algo == null) {
			algo = ALGORITHM.ENGLISH;
		}
		stemmer = new SnowballStemmer(algo);

		final DBCollection dbCollection = mongo.getDB().getCollection(collection);

		dbCollection.find().forEach(o -> {
			String type = (String) o.get(typeField);
			String subType = (String) o.get(subTypeField);
			String pos = (String) o.get(posField);
			BasicDBList values = (BasicDBList) o.get(valuesField);

			InteractionTypeDefinition definition = new InteractionTypeDefinition(type, subType, pos);

			values.stream()
					.filter(s -> s instanceof String)
					.forEach(s -> {
						String key = toKey(definition.getPos(), (String) s);
						definitions.put(key, definition);
					});
		});
	}

	private String toKey(String pos, String word) {
		CharSequence normalised = word.toLowerCase().trim();
		if (stem) {
			normalised = stemmer.stem(normalised);
		}
		return String.format("%s:%s", Character.toLowerCase(pos.charAt(0)), normalised);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.gov.dstl.baleen.uima.BaleenAnnotator#doProcess(org.apache.uima.jcas.JCas)
	 */
	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {
		Map<Interaction, Collection<WordToken>> interactionToWords = JCasUtil.indexCovered(jCas, Interaction.class,
				WordToken.class);

		Collection<Interaction> allInteractions = new ArrayList<>(JCasUtil.select(jCas, Interaction.class));
		for (Interaction interaction : allInteractions) {
			String value = interaction.getCoveredText();
			Collection<WordToken> words = interactionToWords.get(interaction);

			if (words != null && !words.isEmpty() && value != null && !value.isEmpty()) {
				// So we have the covered words and the interaction value (ie the word covered by
				// the interact)

				// Look for a string match between the interaction value and the words then find all
				// the potential POS it could be

				Stream<String> keys = words.stream()
						.filter(p -> p.getCoveredText().equalsIgnoreCase(value))
						.map(w -> w.getPartOfSpeech())
						.distinct()
						.filter(Objects::nonNull)
						.map(p -> toKey(p, value));

				// For each interaction we create a new interaction which is has the right type info

				// This get does POS matching for us
				keys.map(definitions::get)
						.filter(l -> l != null && !l.isEmpty())
						.flatMap(Collection::stream)
						.forEach(d -> {
							Interaction i = SpanUtils.copyInteraction(jCas, interaction.getBegin(),
									interaction.getEnd(), interaction);

							i.setRelationshipType(d.getType());
							i.setRelationSubType(d.getSubType());

							addToJCasIndex(i);
						});
			}
		}

		// Delete the old interaction, its either been replaced or not
		removeFromJCasIndex(allInteractions);
	}

	public static class InteractionTypeDefinition {

		private final String type;
		private final String subType;
		private final String pos;

		public InteractionTypeDefinition(String type, String subType, String pos) {
			this.type = type;
			this.subType = subType;
			this.pos = pos;
		}

		public String getPos() {
			return pos;
		}

		public String getSubType() {
			return subType;
		}

		public String getType() {
			return type;
		}
	}
}
