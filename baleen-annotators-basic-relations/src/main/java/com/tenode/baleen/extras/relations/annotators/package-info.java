/**
 * This package contains annotators which help process interaction to relations.
 *
 * Assuming that interaction words have been identified they must first be annotated, @see
 * com.tenode.baleen.extras.interactions.annotators for details.
 *
 * Then we need to add one or more interaction based annotators. These may be derived from
 * {@link com.tenode.baleen.extras.relations.annotators.AbstractInteractionBasedRelationshipAnnotator}
 * or
 * {@link com.tenode.baleen.extras.relations.annotators.AbstractInteractionBasedSentenceRelationshipAnnotator}
 * . For this example we will use the
 * {@link com.tenode.baleen.extras.relations.annotators.SimpleInteractionRelationship} which is a
 * toy example and should not be used in a production pipeline!
 *
 * Relationship extraction associates entities and as such should occur after Entity extraction
 *
 * <pre>
 * annotators:
 * - # Entity extraction and cleaners
 * - # Interaction markup
 * - com.tenode.baleen.extras.relations.annotators.SimpleInteractionRelationship
 * - com.tenode.baleen.extras.relations.annotators.CleanRelations
 * - com.tenode.baleen.extras.relations.annotators.RelationTypeFilter
 * </pre>
 *
 * Interaction based relationship extraction may generate a lot of erroneous relations. The relation
 * may be valid, but not between the two particularly entry types. For this reason most pipelines
 * will wish to include the CleanRelations (to remove duplicate relations) and the
 * FilterRelationType (to remove based relations where they link entities of invalid types).
 *
 * Note that to use the RelationTypeFilter you require a Mongo resource.
 *
 * Finally you may want to output relations, see
 * {@link com.tenode.baleen.extras.common.print.Relations} to print to console. Or the Mongo
 * consumer will also saves relation information.
 *
 */
package com.tenode.baleen.extras.relations.annotators;