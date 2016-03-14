/*
 *
 */
package com.tenode.baleen.extras.jobs.io;

import java.util.Collection;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.tenode.baleen.extras.annotators.relationships.RelationTypeFilter;
import com.tenode.baleen.extras.jobs.UploadInteractionsToMongo;
import com.tenode.baleen.extras.jobs.interactions.data.InteractionDefinition;

import uk.gov.dstl.baleen.annotators.gazetteer.Mongo;

/**
 * Write interaction data to Mongo database.
 *
 * This will output interaction data into two different collection for different uses by other
 * annotators.
 *
 * It first outputs interaction words into Mongo gazetteer format (see {@link Mongo} for more
 * details). This allows the standard Baleen Mongo gazetteer annotators to mark up Interaction
 * words.
 *
 *
 * Secondly it saves information about relationship type constraints to the relationTypeCollection.
 * This is used by the {@link RelationTypeFilter} in order to remove any invalid relationships
 * between types. The relation (DateTime, said, Location) is likely to be invalid for example. Valid
 * options is derived directly from the
 *
 * See {@link UploadInteractionsToMongo} for more details.
 *
 */
public class MongoInteractionWriter implements InteractionWriter {

	/** The relation types. */
	private final DBCollection relationTypes;

	/** The interactions. */
	private final DBCollection interactions;

	/**
	 * Instantiates a new instance.
	 *
	 * @param db
	 *            the db the write to
	 * @param relationTypesCollection
	 *            the relation types collection name
	 * @param interactionCollection
	 *            the interaction collection name
	 */
	public MongoInteractionWriter(DB db, String relationTypesCollection, String interactionCollection) {
		interactions = db.getCollection(interactionCollection);
		relationTypes = db.getCollection(relationTypesCollection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tenode.baleen.extras.jobs.io.InteractionWriter#write(com.tenode.baleen.extras.jobs.
	 * interactions.data.InteractionDefinition, java.util.Collection)
	 */
	@Override
	public void write(InteractionDefinition interaction,
			Collection<String> alternatives) {
		// Write to the interactions collection
		// ADd in relationshiptype and subtype (which can be manually changed later)
		final BasicDBObject interactionObject = new BasicDBObject("value", alternatives);
		interactionObject.put("relationshipType", interaction.getType());
		interactionObject.put("relationSubType", interaction.getSubType());
		interactions.save(interactionObject);

		// Write out to the relationship constraints
		final BasicDBObject relationTypeObject = new BasicDBObject()
				.append("source", interaction.getSource())
				.append("target", interaction.getTarget())
				.append("type", interaction.getType());
		relationTypes.save(relationTypeObject);

	}

	/**
	 * Clear all collections.
	 */
	public void clear() {
		interactions.remove(new BasicDBObject());
		relationTypes.remove(new BasicDBObject());
	}

}
