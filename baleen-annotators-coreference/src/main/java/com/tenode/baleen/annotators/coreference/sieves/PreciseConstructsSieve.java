package com.tenode.baleen.annotators.coreference.sieves;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.uima.jcas.JCas;

import com.tenode.baleen.annotators.coreference.data.Cluster;
import com.tenode.baleen.annotators.coreference.data.Mention;
import com.tenode.baleen.extras.common.grammar.ParseTree;
import com.tenode.baleen.extras.common.grammar.ParseTree.TreeNode;

import uk.gov.dstl.baleen.types.language.PhraseChunk;
import uk.gov.dstl.baleen.types.language.WordToken;

/**
 *
 * Our parser, OpenNlp, does not output (,) so we need to do a manual check for that.
 *
 *
 */
public class PreciseConstructsSieve extends AbstractCoreferenceSieve {

	private static final Predicate<WordToken> CONJUNCTION_FILTER = w -> w.getPartOfSpeech().equals("CC");

	private static final Pattern COMMA = Pattern.compile("\\s*,\\s*");

	private final ParseTree parseTree;

	public PreciseConstructsSieve(JCas jCas, ParseTree parseTree, List<Cluster> clusters, List<Mention> mentions) {
		super(jCas, clusters, mentions);
		this.parseTree = parseTree;
	}

	@Override
	public void sieve() {

		parseTree.traverseChildren(children -> {
			for (int i = 0; i < children.size() - 1; i++) {
				TreeNode a = children.get(i);
				TreeNode b = children.get(i + 1);

				// Appositive
				// Look for (NP , NP)

				if (a.getChunk().getChunkType().equals("NP") && b.getChunk().getChunkType().equals("NP")) {

					// Is there a comma between them, without AND/BUT/ETC
					String between = getJCas().getDocumentText().substring(a.getChunk().getEnd(),
							b.getChunk().getBegin());
					if (COMMA.matcher(between).matches() && !a.containsWord(CONJUNCTION_FILTER)
							&& !b.containsWord(CONJUNCTION_FILTER)) {
						addCoveredToCluster(a.getChunk(), b.getChunk());
					}

				}

				// Predicate nominative
				// (NP VP(is / was) ) then take the NP under VP as

				if (a.getChunk().getChunkType().equals("NP") && b.getChunk().getChunkType().equals("VP")) {
					Optional<TreeNode> np = b.getChildren().stream()
							.filter(n -> n.getChunk().getChunkType().equals("NP"))
							.findFirst();
					Optional<WordToken> is = b.getWords().stream()
							.filter(w -> w.getCoveredText().equalsIgnoreCase("is"))
							.findFirst();

					if (np.isPresent() && is.isPresent()) {
						addCoveredToCluster(a.getChunk(), b.getChunk());
					}

				}

			}
		});

	}

	protected void addCoveredToCluster(PhraseChunk a, PhraseChunk b) {
		List<Mention> aMentions = findMentionsBetween(a.getBegin(), a.getEnd());
		List<Mention> bMentions = findMentionsBetween(b.getBegin(), b.getEnd());

		addPairwiseToCluster(aMentions, bMentions);
	}

	private List<Mention> findMentionsBetween(int begin, int end) {
		return getMentions().stream()
				.filter(m -> begin <= m.getAnnotation().getBegin() && m.getAnnotation().getEnd() <= end)
				.collect(Collectors.toList());
	}

}
