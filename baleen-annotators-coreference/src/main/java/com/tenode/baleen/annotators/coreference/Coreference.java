package com.tenode.baleen.annotators.coreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import com.tenode.baleen.annotators.coreference.data.Cluster;
import com.tenode.baleen.annotators.coreference.data.Mention;
import com.tenode.baleen.annotators.coreference.detector.MentionDetector;
import com.tenode.baleen.annotators.coreference.enhancers.AcronymEnhancer;
import com.tenode.baleen.annotators.coreference.enhancers.AnimacyEnhancer;
import com.tenode.baleen.annotators.coreference.enhancers.GenderEnhancer;
import com.tenode.baleen.annotators.coreference.enhancers.MentionEnhancer;
import com.tenode.baleen.annotators.coreference.enhancers.MultiplicityEnhancer;
import com.tenode.baleen.annotators.coreference.enhancers.PersonEnhancer;
import com.tenode.baleen.annotators.coreference.sieves.CoreferenceSieve;
import com.tenode.baleen.annotators.coreference.sieves.ExactStringMatchSieve;
import com.tenode.baleen.annotators.coreference.sieves.ExtractReferenceTargets;
import com.tenode.baleen.annotators.coreference.sieves.InSentencePronounSieve;
import com.tenode.baleen.annotators.coreference.sieves.PreciseConstructsSieve;
import com.tenode.baleen.annotators.coreference.sieves.PronounResolutionSieve;
import com.tenode.baleen.annotators.coreference.sieves.ProperHeadMatchSieve;
import com.tenode.baleen.annotators.coreference.sieves.RelaxedHeadMatchSieve;
import com.tenode.baleen.annotators.coreference.sieves.RelaxedStringMatchSieve;
import com.tenode.baleen.annotators.coreference.sieves.StrictHeadMatchSieve;
import com.tenode.baleen.extras.common.grammar.DependencyGraph;
import com.tenode.baleen.extras.common.grammar.ParseTree;
import com.tenode.baleen.resources.coreference.GenderMultiplicityResource;

import uk.gov.dstl.baleen.types.Base;
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
 * TODO; Review the mention extraction its not quite right
 *
 * This is a partial implementation at present, and so will not perform as well as the
 * StandfordCoreNlp coreference. It is partial due to time constraints, and largely unavailabled
 *
 * The following details implementation to date:
 * <ul>
 * <li>Mention detection: Done
 * <li>Pass 1 Speaker Identification: TODO
 * <li>Pass 2 Exact String Match: Done
 * <li>Pass 3 Relaxed String Match: Done
 * <li>Pass X: We added a pronoun match within the same sentence.
 * <li>Pass 4 Precise Constructs: Done - appositive, predicate. relative pronoun, acronym. Not done
 * - role appositive (since Baleen doens't have a role entity to mark up). Done elsewhere - demonym
 * are covered in the NationalityToLocation annotator.
 * <li>Pass 5-7 Strict Head Match: Done
 * <li>Pass 8 Proper Head Noun Match: Done
 * <li>Pass 9 Relaxed Head Match: Done
 * <li>Pass 10 Pronoun Resolution: Done
 * <li>Post process: Done
 * <li>Output: Done
 * </ul>
 *
 * Attributes of mentions (gender, animacy, number) are included, but for animacy we could not get
 * the data (Ji and Lin, 2009) and it says for research use only anyway. As such we ignore the
 * dictionary lookup.
 *
 * We discard any the algorithm which are for a specific corpus (eg OntoNotes).
 *
 * This is very much unoptimised. Each sieve will calculate over all entities, even though many will
 * already in the same cluster.
 *
 * TODO: At the moment we don't do the clustering properly. We need just perform pairwise operations
 * repeated.
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

	/**
	 * GenderMultiplicityResource to provide information on gender and multiplicity from a
	 * dictionary.
	 *
	 * @baleen.resource com.tenode.baleen.resources.coreference.GenderMultiplicityResource
	 */
	public static final String PARAM_GENDER_MULTIPLICITY = "genderMultiplicity";
	@ExternalResource(key = PARAM_GENDER_MULTIPLICITY)
	private GenderMultiplicityResource genderMultiplicityResource;

	/**
	 * Perform only a single pass (of the provided index)
	 *
	 * Only useful for unit testing.
	 *
	 * -1 means all
	 *
	 * @baleen.resource -1
	 */
	public static final String PARAM_SINGLE_PASS = "pass";
	@ConfigurationParameter(name = PARAM_SINGLE_PASS, defaultValue = "-1")
	private int singlePass;

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {

		DependencyGraph dependencyGraph = DependencyGraph.build(jCas);
		ParseTree parseTree = ParseTree.build(jCas);

		// Detect mentions
		List<Mention> mentions = new MentionDetector(jCas, dependencyGraph, parseTree).detect();

		mentions.forEach(System.out::println);

		// Extract head words and other aspects needed for later, determine acronyms, denonym,
		// gender, etc
		enhanceMention(jCas, dependencyGraph, parseTree, mentions);

		List<Cluster> clusters = sieve(jCas, parseTree, mentions);

		// Post processing
		postProcess(clusters);

		// Output to reference targets
		outputReferenceTargets(jCas, clusters);
	}

	private void enhanceMention(JCas jCas, DependencyGraph dependencyGraph, ParseTree parseTree,
			List<Mention> mentions) {

		MentionEnhancer[] enhancers = new MentionEnhancer[] {
				new AcronymEnhancer(),
				new PersonEnhancer(),
				new MultiplicityEnhancer(genderMultiplicityResource),
				new GenderEnhancer(genderMultiplicityResource),
				new AnimacyEnhancer()
		};

		for (Mention mention : mentions) {

			for (MentionEnhancer enhancer : enhancers) {
				enhancer.enhance(mention);
			}
		}
	}

	// NOTE: This is unused because the IDE doesn't know that singlePass will be injected.
	@SuppressWarnings("unused")
	private List<Cluster> sieve(JCas jCas, ParseTree parseTree, List<Mention> mentions) {

		List<Cluster> clusters = new ArrayList<>();

		CoreferenceSieve[] sieves = new CoreferenceSieve[] {
				new ExtractReferenceTargets(jCas, clusters, mentions),
				// TODO: new SpeakerIdentificationSieve(jCas, clusters, mentions),
				new ExactStringMatchSieve(jCas, clusters, mentions),
				new RelaxedStringMatchSieve(jCas, clusters, mentions),
				new InSentencePronounSieve(jCas, clusters, mentions),
				new PreciseConstructsSieve(jCas, parseTree, clusters, mentions),
				// Pass A-C are all strict head with different params
				new StrictHeadMatchSieve(jCas, clusters, mentions, true, true),
				new StrictHeadMatchSieve(jCas, clusters, mentions, true, false),
				new StrictHeadMatchSieve(jCas, clusters, mentions, false, true),
				new ProperHeadMatchSieve(jCas, clusters, mentions),
				new RelaxedHeadMatchSieve(jCas, clusters, mentions),
				new PronounResolutionSieve(jCas, clusters, mentions)
		};

		if (singlePass >= 0 && sieves.length > singlePass) {
			sieves = new CoreferenceSieve[] {
					sieves[singlePass]
			};
			getMonitor().info("Single pass mode {}: {}", singlePass, sieves[0].getClass().getSimpleName());
		}

		Arrays.stream(sieves).forEach(s -> {
			s.sieve();
			getMonitor().debug("Cluster size after {} is {}", s.getClass().getSimpleName(), clusters.size());
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

			getMonitor().info("Cluster:\n");

			for (Mention m : c.getMentions()) {
				// TODO: We overwrite the referent target here, not sure what we'd do if there was
				// already one.
				// (since it would need to be consistent over the whole cluster)
				// Perhaps we should build initial clusters from the existing RTs?

				Base annotation = m.getAnnotation();
				annotation.setReferent(target);
				addToJCasIndex(annotation);

				getMonitor().info("\t{}\n", m.getAnnotation().getCoveredText());
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
