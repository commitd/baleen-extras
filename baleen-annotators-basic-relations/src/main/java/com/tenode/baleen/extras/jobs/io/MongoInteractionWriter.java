package com.tenode.baleen.extras.jobs.writers;

import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.tenode.baleen.extras.jobs.interactions.data.InteractionWord;

public class MongoInteractionWriter implements InteractionWordWriter {

	private final DBCollection relationTypes;
	private final DBCollection interactions;

	public MongoInteractionWriter(DB db, String relationTypesCollection, String interactionCollection) {
		interactions = db.getCollection(interactionCollection);
		relationTypes = db.getCollection(relationTypesCollection);
	}

	@Override
	public void write(InteractionWord interaction, String relationshipType, String lemma, List<String> alternatives) {
		// Write to the interactions collection
		// ADd in relationshiptype and subtype (which can be manually changed later)
		BasicDBObject interactionObject = new BasicDBObject("value", alternatives);
		interactionObject.put("relationshipType", relationshipType);
		interactionObject.put("relationSubType", lemma);
		interactions.save(interactionObject);

		// Write out to the relationship constraints
		interaction.getPairs().stream().forEach(p -> {
			BasicDBObject relationTypeObject = new BasicDBObject()
					.append("source", p.getSource())
					.append("target", p.getTarget())
					.append("type", relationshipType);
			relationTypes.save(relationTypeObject);
		});
	}

}
