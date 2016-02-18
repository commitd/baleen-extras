package com.tenode.baleen.annotators.coreference.sieves;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.uima.jcas.JCas;

import com.google.common.primitives.Doubles;
import com.tenode.baleen.annotators.coreference.data.Cluster;
import com.tenode.baleen.annotators.coreference.data.Mention;

public class ProperHeadMatchSieve extends AbstractCoreferenceSieve {

	public ProperHeadMatchSieve(JCas jCas, List<Cluster> clusters, List<Mention> mentions) {
		super(jCas, clusters, mentions);
	}

	@Override
	public void sieve() {
		// Note: Head must be proper nouns, but ours are by construction

		for (int i = 0; i < getMentions().size(); i++) {
			Mention a = getMentions().get(i);

			String aHead = a.getHead();
			if (aHead == null || aHead.isEmpty()) {
				continue;
			}
			aHead = aHead.toLowerCase();

			for (int j = i + 1; j < getMentions().size(); j++) {
				Mention b = getMentions().get(j);
				String bHead = b.getHead();
				if (bHead == null || bHead.isEmpty()) {
					continue;
				}
				bHead = bHead.toLowerCase();

				if (aHead.equals(bHead)) {

					// Not i-within-i
					if (!a.overlaps(b)) {
						continue;
					}

					// No modifier
					if (!hasSameModifiers(a, b)) {
						continue;
					}

					// No numerical mismatches
					List<Double> aNumbers = extractNumbers(a.getText());
					List<Double> bNumbers = extractNumbers(b.getText());

					if (!hasSameNumbers(aNumbers, bNumbers)) {
						continue;
					}

					addToCluster(a, b);

				}
			}
		}
	}

	// TODO: Find a complete list of these
	private final Set<String> spatialModifiers = new HashSet<String>(
			Arrays.asList("northern", "southern", "western", "eastern", "south", "east", "north", "west"));

	private boolean hasSameModifiers(Mention a, Mention b) {
		// TODO: The paper says location named entities, other proper nouns or other spatial
		// modifiers but since locations should be other proper nouns we ignore that clause. We
		// could look for Locations covered by the annotation.

		Set<String> aModifiers = getSpatialAndPNModifier(a);
		Set<String> bModifiers = getSpatialAndPNModifier(b);

		return aModifiers.size() == bModifiers.size() && aModifiers.containsAll(bModifiers);
	}

	private Set<String> getSpatialAndPNModifier(Mention a) {
		return a.getWords().stream()
				.filter(w -> w.getPartOfSpeech().startsWith("NP") || spatialModifiers.contains(w.getCoveredText()))
				.map(w -> w.getCoveredText().toLowerCase())
				.collect(Collectors.toSet());
	}

	// TODO: Does this cover reasonable one (we could have 200k for example)
	private static final Pattern NUMBER = Pattern.compile("-?\\d+(,\\d+)*(\\.\\d+)?");

	// Assymetric
	private List<Double> extractNumbers(String text) {
		List<Double> list = new LinkedList<>();
		Matcher matcher = NUMBER.matcher(text);
		while (matcher.find()) {
			Double d = Doubles.tryParse(matcher.group().replaceAll(",", ""));
			if (d != null) {
				list.add(d);
			}
		}
		return list;
	}

	// Assymetric
	private boolean hasSameNumbers(Collection<Double> aNumbers, Collection<Double> bNumbers) {

		for (double b : bNumbers) {
			boolean found = false;
			for (double a : aNumbers) {
				// 'Fuzzy match' the numbers
				if (Math.abs(a - b) < 0.01 * Math.max(Math.abs(a), Math.abs(a))) {
					found = true;
					break;
				}
			}

			if (!found) {
				return false;
			}
		}

		return true;
	}

}
