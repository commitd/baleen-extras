package com.tenode.baleen.extras.documentcoreference.annotators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import com.tenode.baleen.extras.common.grammar.DependencyGraph;
import com.tenode.baleen.extras.common.grammar.ParseTree;
import com.tenode.baleen.extras.documentcoreference.data.Cluster;
import com.tenode.baleen.extras.documentcoreference.data.Mention;
import com.tenode.baleen.extras.documentcoreference.detector.MentionDetector;
import com.tenode.baleen.extras.documentcoreference.enhancers.AcronymEnhancer;
import com.tenode.baleen.extras.documentcoreference.enhancers.AnimacyEnhancer;
import com.tenode.baleen.extras.documentcoreference.enhancers.GenderEnhancer;
import com.tenode.baleen.extras.documentcoreference.enhancers.MentionEnhancer;
import com.tenode.baleen.extras.documentcoreference.enhancers.MultiplicityEnhancer;
import com.tenode.baleen.extras.documentcoreference.enhancers.PersonEnhancer;
import com.tenode.baleen.extras.documentcoreference.resources.GenderMultiplicityResource;
import com.tenode.baleen.extras.documentcoreference.sieves.CoreferenceSieve;
import com.tenode.baleen.extras.documentcoreference.sieves.ExactStringMatchSieve;
import com.tenode.baleen.extras.documentcoreference.sieves.ExtractReferenceTargets;
import com.tenode.baleen.extras.documentcoreference.sieves.InSentencePronounSieve;
import com.tenode.baleen.extras.documentcoreference.sieves.PreciseConstructsSieve;
import com.tenode.baleen.extras.documentcoreference.sieves.PronounResolutionSieve;
import com.tenode.baleen.extras.documentcoreference.sieves.ProperHeadMatchSieve;
import com.tenode.baleen.extras.documentcoreference.sieves.RelaxedHeadMatchSieve;
import com.tenode.baleen.extras.documentcoreference.sieves.RelaxedStringMatchSieve;
import com.tenode.baleen.extras.documentcoreference.sieves.StrictHeadMatchSieve;

import uk.gov.dstl.baleen.types.Base;
import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

/**
 * Resolves coreferent entities.
 * <p>
 * In effect the Stanford approach is a set of 10+ passes which address the different types of
 * coreference. At each stage mentions are related, each related mention is added to a cluster (a
 * set of mentions which are related). At the end of the process the clusters are joined
 * transitively and all mentions inside a cluster are considered coreferent.
 * <p>
 * A mention is a NP, entity or pronoun. In Stanford the largest NP is taken, within Baleen we felt
 * that entities are more important, therefore we take the largest NP which does not contain a NP.
 * <p>
 * TODO: Review mention extraction
 * <p>
 * This is a partial implementation at present, and so will not perform as well as the
 * StanfordCoreNlp coreference. This is partially due to time constraints.
 * <p>
 * The following implementation details to date:
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
 * <p>
 * We discard any algorithms which are for a specific corpus (eg OntoNotes).
 * <p>
 * This is very much unoptimised. Each sieve will calculate over all entities, even though many will
 * already in the same cluster.
 * <p>
 * TODO: At the moment we don't do the clustering properly. We need just perform pairwise operations
 * repeated.
 *
 * For more information see the various supporting papers.
 * <ul>
 * <li>http://nlp.stanford.edu/software/dcoref.shtml
 * <li>http://www.mitpressjournals.org/doi/pdf/10.1162/COLI_a_00152
 * <li>http://nlp.stanford.edu/pubs/discourse-referent-lifespans.pdf
 * <li>http://nlp.stanford.edu/pubs/conllst2011-coref.pdf
 * <li>http://nlp.stanford.edu/pubs/coreference-emnlp10.pdf
 * </ul>
 *
 * TODO: To really improve further, we need an analysis of what is missing higher up Baleen. For
 * example we don't have roles or the animacy information so "a doctor" is just a noun phrase and
 * hence could be mapped to it. If we had "person role" entity marker we would mark this an ANIMATE.
 *
 * @baleen.javadoc
 */
public class Coreference extends BaleenAnnotator {

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

	/**
	 * Should the prononiam (John - he) be performed.
	 *
	 * This is the worst performing seive in that is must 'guess' without any real rules what entity
	 * the pronoun is referring to. We currently have little data about animacy etc which will help
	 * (They - BBC ok, He - BBC not ok).
	 *
	 * Currently a closest entity of the same type is used, but that won't perform well in many
	 * cases.
	 *
	 * @baleen.resource pronomial false
	 */
	public static final String PARAM_INCLUDE_PRONOMIAL = "pronomial";
	@ConfigurationParameter(name = PARAM_INCLUDE_PRONOMIAL, defaultValue = "false")
	private boolean includePronomial;

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {

		DependencyGraph dependencyGraph = DependencyGraph.build(jCas);
		ParseTree parseTree = ParseTree.build(jCas);

		// Detect mentions
		List<Mention> mentions = new MentionDetector(jCas, dependencyGraph, parseTree).detect();

		// Extract head words and other aspects needed for later, determine acronyms, denonym,
		// gender, etc
		enhanceMention(dependencyGraph, parseTree, mentions);

		List<Cluster> clusters = sieve(jCas, parseTree, mentions);

		// Post processing
		postProcess(clusters);

		// Output to reference targets
		outputReferenceTargets(jCas, clusters);
	}

	private void enhanceMention(DependencyGraph dependencyGraph, ParseTree parseTree,
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

	private List<Cluster> sieve(JCas jCas, ParseTree parseTree, List<Mention> mentions) {

		List<Cluster> clusters = new ArrayList<>();

		CoreferenceSieve[] sieves = new CoreferenceSieve[] {
				new ExtractReferenceTargets(jCas, clusters, mentions), // Good
				// TODO: SpeakerIdentificationSieve not implemented
				new ExactStringMatchSieve(jCas, clusters, mentions), // Good
				new RelaxedStringMatchSieve(jCas, clusters, mentions), // Good
				new InSentencePronounSieve(jCas, clusters, mentions), // Good
				new PreciseConstructsSieve(jCas, parseTree, clusters, mentions), // Good
				// Pass A-C are all strict head with different params
				new StrictHeadMatchSieve(jCas, clusters, mentions, true, true), // Good
				new StrictHeadMatchSieve(jCas, clusters, mentions, true, false), // Good
				new StrictHeadMatchSieve(jCas, clusters, mentions, false, true), // Good
				new ProperHeadMatchSieve(jCas, clusters, mentions), // Good
				new RelaxedHeadMatchSieve(jCas, clusters, mentions), // Good
				includePronomial ? new PronounResolutionSieve(jCas, clusters, mentions) : null
				// Questionable - Needs more help from
				// Baleen entities yet and more data from animacy if its to work well.
		};

		if (singlePass >= 0 && sieves.length > singlePass) {
			sieves = new CoreferenceSieve[] {
					sieves[singlePass]
			};
			getMonitor().info("Single pass mode {}: {}", singlePass, sieves[0].getClass().getSimpleName());
		}

		Arrays.stream(sieves)
				.filter(Objects::nonNull)
				.forEach(CoreferenceSieve::sieve);

		return clusters;
	}

	private void logClusters(List<Cluster> clusters) {

		clusters.forEach(c -> {
			getMonitor().info("Cluster:\n");
			c.getMentions().stream()
					.forEach(a -> getMonitor().info("\t" + a.getAnnotation().getCoveredText() + " : " + a.getType()));
		});

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

		ArrayList<ReferenceTarget> toRemove = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		removeFromJCasIndex(toRemove);

		// Save clusters a referent targets

		merged.forEach(c -> {
			ReferenceTarget target = new ReferenceTarget(jCas);

			for (Mention m : c.getMentions()) {
				// We overwrite the referent target here, given that we used the initial target to
				// bootstrap our work
				// TODO: Could add an option not to override here.

				Base annotation = m.getAnnotation();
				annotation.setReferent(target);
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

			if (!overlap) {
				merged.add(cluster);
			}
		}

		return merged;
	}

	private void updateMentionClusters(List<Cluster> clusters, List<Mention> mentions) {
		mentions.forEach(Mention::clearClusters);

		clusters.forEach(c -> c.getMentions().forEach(m -> m.addToCluster(c)));
	}
}