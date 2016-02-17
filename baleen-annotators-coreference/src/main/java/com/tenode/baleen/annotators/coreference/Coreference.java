package com.tenode.baleen.annotators.coreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tenode.baleen.annotators.coreference.data.Cluster;
import com.tenode.baleen.annotators.coreference.data.Mention;
import com.tenode.baleen.annotators.coreference.sieves.CoreferenceSieve;
import com.tenode.baleen.annotators.coreference.sieves.ExactStringMatchSieve;
import com.tenode.baleen.annotators.coreference.sieves.ExtractReferenceTargets;
import com.tenode.baleen.annotators.coreference.sieves.PreciseConstructsSieve;
import com.tenode.baleen.annotators.coreference.sieves.RelaxedStringMatchSieve;
import com.tenode.baleen.extras.common.grammar.DependencyGraph;
import com.tenode.baleen.extras.common.grammar.ParseTree;

import uk.gov.dstl.baleen.types.Base;
import uk.gov.dstl.baleen.types.language.PhraseChunk;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

/**
 * Resolves coreferent entities.
 *
 * In effect the Standford approach is a set of 10+ passes which address the different types of
 * coreference. At each stage mentions are related and when related the are added to a cluster (a
 * set of mentions which are related). At the end of the process the clusters are joined
 * transitively and all mentions inside are considered coreferent.
 *
 * A mention is a NP, entity or pronoun. In Standford the largest NP is taken, within Baleen we felt
 * that entities are more important, therefore we take the largest NP which does not contain a NP.
 *
 * This is a partial implementation at present, and so will not perform as well as the
 * StandfordCoreNlp coreference. It is partial due to time constraints.
 *
 * The following details implementation to date:
 * <ul>
 * <li>Mention detection: Done
 * <li>Pass 1 Speaker Identification:
 * <li>Pass 2 Exact String Match: Done
 * <li>Pass 3 Relaxed String Match: Done
 * <li>Pass 4 Precise Constructs: appositive. Not: predicate, role appositive, relative pronoun,
 * acronym, demonym
 * <li>Pass 5-7 Strict Head Match:
 * <li>Pass 8 Proper Head Noun Match:
 * <li>Pass 9 Relaxed Head Match:
 * <li>Pass 10 Pronoun Resolution:
 * <li>Post process: Done
 * <li>Output: Done
 * </ul>
 *
 * We discard any the algorithm which are for a specific corpus (eg OntoNotes).
 *
 * This is very much unoptimised. Each sieve will calculate over all entities, even though many will
 * already in the same cluster.
 *
 * For more information see the various supporting papers.
 * <ul>
 * <li>http://nlp.stanford.edu/software/dcoref.shtm
 * <li>http://www.mitpressjournals.org/doi/pdf/10.1162/COLI_a_00152
 * <li>http://nlp.stanford.edu/pubs/discourse-referent-lifespans.pdf
 * <li>http://nlp.stanford.edu/pubs/conllst2011-coref.pdf
 * <li>http://nlp.stanford.edu/pubs/coreference-emnlp10.pdf
 * </ul>
 *
 * @baleen.javadoc
 */
public class Coreference extends BaleenAnnotator {
	private static final Predicate<String> NP_FILTER = s -> s.startsWith("N");

	private static final Logger LOGGER = LoggerFactory.getLogger(Coreference.class);

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {

		DependencyGraph dependencyGraph = DependencyGraph.build(jCas);
		ParseTree parseTree = ParseTree.build(jCas);

		// Detect mentions
		List<Mention> mentions = detectMentions(jCas);

		// Extract head words and other aspects needed for later, determine acronyms, denonym,
		// gender, etc
		enhanceMention(jCas, parseTree, mentions);

		mentions.forEach(System.out::println);

		// Perform the sieve
		List<Cluster> clusters = sieve(jCas, parseTree, mentions);

		// Post processing
		postProcess(clusters);

		// Output to reference targets
		outputReferenceTargets(jCas, clusters);
	}

	private List<Mention> detectMentions(JCas jCas) {
		// TODO: We could use parsetree rather than npCoveringNp

		List<WordToken> pronouns = JCasUtil.select(jCas, WordToken.class).stream()
				.filter(w -> w.getPartOfSpeech().startsWith("PP") || w.getPartOfSpeech().startsWith("WP"))
				.collect(Collectors.toList());
		Collection<Entity> entities = JCasUtil.select(jCas, Entity.class);
		List<PhraseChunk> phrases = new ArrayList<>(JCasUtil.select(jCas, PhraseChunk.class));

		// Remove any noun phrases which cover entities
		List<PhraseChunk> npCoveringEntities = JCasUtil.indexCovering(jCas, Entity.class, PhraseChunk.class).values()
				.stream()
				.flatMap(e -> e.stream())
				.collect(Collectors.toList());

		Map<PhraseChunk, Collection<PhraseChunk>> npCoveringNp = new HashMap<>(
				JCasUtil.indexCovering(jCas, PhraseChunk.class,
						PhraseChunk.class));

		phrases.removeAll(npCoveringEntities);

		// Get all noun phrases (discarding other phrase types)
		Iterator<PhraseChunk> phraseIterator = phrases.iterator();
		while (phraseIterator.hasNext()) {
			PhraseChunk phraseChunk = phraseIterator.next();
			if (!phraseChunk.getChunkType().startsWith("N")) {
				phraseIterator.remove();
				npCoveringNp.remove(phraseChunk);
			}
		}

		// Now repeat for the collection removing from the values of the collect
		Iterator<Entry<PhraseChunk, Collection<PhraseChunk>>> npIterator = npCoveringNp.entrySet().iterator();
		while (npIterator.hasNext()) {
			Entry<PhraseChunk, Collection<PhraseChunk>> e = npIterator.next();

			e.getValue().removeAll(npCoveringEntities);
			e.getValue().removeIf(p -> !p.getChunkType().startsWith("N"));
		}

		// Remove all phrases which are covered by another phrase

		phrases.removeIf(p -> {
			Collection<PhraseChunk> covered = npCoveringNp.get(p);
			return covered != null && !covered.isEmpty();
		});

		// TODO: Remove all pronouns which are covered by the phrases?

		// Do we have all the noun phrases, or just the ones which cover another noun phrase

		List<Mention> mentions = new ArrayList<>();

		entities.forEach(e -> mentions.add(new Mention(e)));
		phrases.forEach(e -> mentions.add(new Mention(e)));
		pronouns.forEach(e -> mentions.add(new Mention(e)));

		return mentions;
	}

	private void enhanceMention(JCas jCas, ParseTree parseTree, List<Mention> mentions) {
		for (Mention mention : mentions) {

			String head = getHeadWords(jCas, parseTree, mention);
			if (head == null || head.isEmpty()) {
				mention.setHead(head);
			}

		}
	}

	private String getHeadWords(JCas jCas, ParseTree parseTree, Mention mention) {
		Collection<WordToken> words;
		switch (mention.getType()) {
		default:
		case PRONOUN:
			return null;
		case ENTITY:
			words = JCasUtil.selectCovered(jCas, WordToken.class, mention.getAnnotation());
			break;
		case NP:
			PhraseChunk chunk = (PhraseChunk) mention.getAnnotation();
			words = parseTree.getChildWords(chunk, NP_FILTER).collect(Collectors.toList());
			break;
		}

		boolean foundNN = false;
		StringBuilder sb = new StringBuilder();
		for (WordToken w : words) {
			if (w.getPartOfSpeech().startsWith("N")) {
				foundNN = true;
				sb.append(w.getCoveredText());
				sb.append(" ");
			} else if (foundNN) {
				// Left our head words
				break;
			} else {
				// Not found a NN yet, so... carry on
			}
		}

		return sb.toString().trim();
	}

	private List<Cluster> sieve(JCas jCas, ParseTree parseTree, List<Mention> mentions) {

		List<Cluster> clusters = new ArrayList<>();

		CoreferenceSieve[] sieves = new CoreferenceSieve[] {
				new ExtractReferenceTargets(jCas, clusters, mentions),
				// new SpeakerIdentificationSieve(jCas, clusters, mentions),
				new ExactStringMatchSieve(jCas, clusters, mentions),
				new RelaxedStringMatchSieve(jCas, clusters, mentions),
				new PreciseConstructsSieve(jCas, parseTree, clusters, mentions),
				// Pass A-C are all strict head with different params
				// new StrictHeadMatchSieve(jCas, true, true, clusters, mentions),
				// new StrictHeadMatchSieve(jCas, true, false, clusters, mentions),
				// new ProperHeadMatchSieve(jCas, false, true, clusters, mentions),
				// new RelaxedHeadMatchSieve(jCas, clusters, mentions),
				// new PronounResolutionSieve(jCas, clusters, mentions)
		};

		Arrays.stream(sieves).forEach(s -> {
			s.sieve();
		});

		return clusters;
	}

	private void postProcess(List<Cluster> clusters) {

		// NOTE: The paper says the two rules are *only* used in OntoNotes:
		// 1. Remove singleton clusters
		// 2. Short mentions of appositive patterns
		// We implement 1, as it makes sense genreally and leave 2 as an OntoNotes specific
		// optimisation.

		Iterator<Cluster> iterator = clusters.iterator();
		while (iterator.hasNext()) {
			Cluster cluster = iterator.next();
			if (cluster.getSize() <= 1) {
				iterator.remove();
			}
		}

	}

	private void outputReferenceTargets(JCas jCas, List<Cluster> clusters) {

		// Merge the clusters together

		List<Cluster> merged = mergeClusters(clusters);

		// Remove all the previous reference targets as we've included them in our process

		removeFromJCasIndex(JCasUtil.select(jCas, ReferenceTarget.class));

		// Save clusters a referent targets

		merged.forEach(c -> {
			ReferenceTarget target = new ReferenceTarget(jCas);

			LOGGER.debug("Cluster:\n");

			for (Mention m : c.getMentions()) {
				// TODO: We overwrite the referent target here, not sure what we'd do if there was
				// already one.
				// (since it would need to be consistent over the whole cluster)
				// Perhaps we should build initial clusters from the existing RTs?

				Base annotation = m.getAnnotation();
				annotation.setReferent(target);
				addToJCasIndex(annotation);

				LOGGER.debug("\t{}\n", m.getAnnotation().getCoveredText());
			}

			addToJCasIndex(target);
		});
	}

	private List<Cluster> mergeClustersAndUpdate(List<Cluster> clusters, List<Mention> mentions) {
		List<Cluster> merged = mergeClusters(clusters);
		updateMentionClusters(merged, mentions);
		return merged;
	}

	private List<Cluster> mergeClusters(List<Cluster> clusters) {
		List<Cluster> merged = new ArrayList<>(clusters.size());

		for (Cluster cluster : clusters) {

			boolean overlap = false;
			for (Cluster mergedCluster : merged) {
				if (mergedCluster.intersects(cluster)) {
					mergedCluster.add(cluster);
					overlap = true;
					break;
				}
			}

			if (overlap == false) {
				merged.add(cluster);
			}
		}

		return merged;
	}

	private void updateMentionClusters(List<Cluster> clusters, List<Mention> mentions) {
		mentions.forEach(Mention::clearClusters);

		clusters.forEach(c -> {
			c.getMentions().forEach(m -> m.addToCluster(c));
		});
	}
}
