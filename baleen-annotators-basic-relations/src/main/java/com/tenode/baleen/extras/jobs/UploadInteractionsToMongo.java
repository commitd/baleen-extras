package com.tenode.baleen.extras.jobs;

import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;

import com.tenode.baleen.extras.jobs.io.CsvInteractionReader;
import com.tenode.baleen.extras.jobs.io.MongoInteractionWriter;

import uk.gov.dstl.baleen.resources.SharedMongoResource;
import uk.gov.dstl.baleen.uima.jobs.BaleenTask;
import uk.gov.dstl.baleen.uima.jobs.JobSettings;

public class UploadInteractionsToMongo extends BaleenTask {
	/**
	 * Connection to Mongo
	 *
	 * @baleen.resource uk.gov.dstl.baleen.resources.SharedMongoResource
	 */
	public static final String KEY_MONGO = "mongo";
	@ExternalResource(key = KEY_MONGO)
	private SharedMongoResource mongo;

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
	 * Save the data to csv, with filename prefixed by tje value.
	 *
	 * Leave this blank for no output.
	 *
	 * @baleen.config csv interactions.csv
	 */
	public static final String KEY_CSV_FILENAME = "input";
	@ConfigurationParameter(name = KEY_CSV_FILENAME, defaultValue = "interactions.csv")
	private String inputFilename;

	@Override
	protected void execute(JobSettings settings) throws AnalysisEngineProcessException {

		try (MongoInteractionWriter writer = new MongoInteractionWriter(mongo.getDB(), relationTypesCollection,
				interactionCollection)) {
			CsvInteractionReader reader = new CsvInteractionReader(inputFilename);
			reader.read((i, a) -> writer.write(i, a));
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

}
