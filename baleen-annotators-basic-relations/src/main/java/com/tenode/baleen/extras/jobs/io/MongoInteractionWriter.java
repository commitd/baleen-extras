package com.tenode.baleen.extras.jobs.io;

import java.util.Collection;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.tenode.baleen.extras.jobs.interactions.data.InteractionRelation;

public class MongoInteractionWriter implements InteractionWriter {

	private final DBCollection relationTypes;
	private final DBCollection interactions;

	public MongoInteractionWriter(DB db, String relationTypesCollection, String interactionCollection) {
		interactions = db.getCollection(interactionCollection);
		relationTypes = db.getCollection(relationTypesCollection);
	}

	@Override
	public void write(InteractionRelation interaction,
			Collection<String> alternatives) {
		// Write to the interactions collection
		// ADd in relationshiptype and subtype (which can be manually changed later)
		BasicDBObject interactionObject = new BasicDBObject("value", alternatives);
		interactionObject.put("relationshipType", interaction.getType());
		interactionObject.put("relationSubType", interaction.getSubType());
		interactions.save(interactionObject);

		// Write out to the relationship constraints
		BasicDBObject relationTypeObject = new BasicDBObject()
				.append("source", interaction.getSource())
				.append("target", interaction.getTarget())
				.append("type", interaction.getType());
		relationTypes.save(relationTypeObject);

	}

}
