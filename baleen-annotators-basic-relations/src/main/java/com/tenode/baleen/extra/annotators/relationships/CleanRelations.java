package com.tenode.baleen.extra.annotators.relationships;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.Relation;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

/**
 * Removes multiple copies of the same relation within a document.
 *
 * This is a naive and simple approach which can hide many issues - it is effectively performing
 * relationship coreference and deduplication based solely at a relationship level. The algorithm
 * works by looking is the relationship types are the same, and if the enttiies are the same (here
 * as well is difficult, this is based on entities having the same type and value which may be
 * incorrect for muliple John Smiths).
 *
 * This only really useful if you want to ensure that from a single document you get only a single
 * relationship of the same type, subtype between the same two entities because you want to
 * (naively) push data into database and not have to consider this in future algorithms (focusing on
 * counting the same relations appearing in different documents).
 *
 */
public class CleanRelations extends BaleenAnnotator {

	@Override
	protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {
		final List<Relation> relations = new ArrayList<>(JCasUtil.select(jCas, Relation.class));

		final Set<Relation> toRemove = new HashSet<>();

		for (int i = 0; i < relations.size(); i++) {
			final Relation a = relations.get(i);

			if (!toRemove.contains(a)) {
				for (int j = i + 1; i < relations.size(); j++) {
					final Relation b = relations.get(j);

					if (isSame(a, b)) {
						toRemove.add(b);
					}

				}
			}

		}
	}

	private boolean isSame(final Relation a, final Relation b) {
		return isSame(a.getSource(), b.getSource()) && isSame(a.getTarget(), b.getTarget())
				&& isSame(a.getRelationshipType(), a.getRelationshipType())
				&& isSame(a.getRelationshipType(), a.getRelationshipType());
	}

	private boolean isSame(final Entity a, final Entity b) {
		// TODO: is the value test enough?
		return a.getType() == b.getType() && isSame(a.getValue(), b.getValue());
	}

	private boolean isSame(final String a, final String b) {
		if (a == b) {
			return true;
		} else if (a == null || b == null) {
			return false;
		} else {
			return a.equalsIgnoreCase(b);
		}
	}

}
