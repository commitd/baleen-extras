package com.tenode.baleen.annotators.coreference.sieves;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.uima.jcas.JCas;

import com.google.common.collect.Sets;
import com.tenode.baleen.annotators.coreference.data.Cluster;
import com.tenode.baleen.annotators.coreference.data.Mention;
import com.tenode.baleen.extras.common.grammar.ParseTree;
import com.tenode.baleen.extras.common.grammar.data.ParseTreeNode;

import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.types.semantic.Location;

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
				ParseTreeNode a = children.get(i);
				ParseTreeNode b = children.get(i + 1);

				// Appositive
				// Look for (NP , NP)

				if (a.getChunk().getChunkType().equals("NP") && b.getChunk().getChunkType().equals("NP")) {

					// Is there a comma between them, without AND/BUT/ETC
					// Not in paper: Need to see if there's an AND in the larger noun phrase, eg
					// Police, Fire and Ambulance (will get police-fire at the moment)
					String between = getJCas().getDocumentText().substring(a.getChunk().getEnd(),
							b.getChunk().getBegin());
					ParseTreeNode parent = a.getParent();

					// Special case there if there's its a location "London, UK" will match
					// but we don't want it too. Probabl need both the a and b to have a location
					// before its wrong. Of course these depedends on the quality of the entity
					// extraction.

					if (COMMA.matcher(between).matches() && !parent.containsWord(CONJUNCTION_FILTER)
							&& (!coversLocation(a) || !coversLocation(b))) {
						addCoveredToCluster(a.getChunk(), b.getChunk());
					}

				}

				// Predicate nominative
				// (NP VP(is / was) ) then take the NP under VP as

				if (a.getChunk().getChunkType().equals("NP") && b.getChunk().getChunkType().equals("VP")) {
					Optional<ParseTreeNode> np = b.getChildren().stream()
							.filter(n -> n.getChunk().getChunkType().equals("NP"))
							.findFirst();
					Optional<WordToken> is = b.getWords().stream()
							.filter(w -> w.getCoveredText().equalsIgnoreCase("is"))
							.findFirst();

					if (np.isPresent() && is.isPresent()) {
						addCoveredToCluster(a.getChunk(), np.get().getChunk());
					}

				}

				// Relative pronoun

				if (a.getChunk().getChunkType().equals("NP") && b.getChunk().getChunkType().equals("WHNP")) {
					// The NP could be something that interests us, or it could a subpart of a large
					// NP.
					List<Mention> mention = findMentionsExactly(a.getChunk().getBegin(), a.getChunk().getEnd());
					List<Mention> pronoun = findMentionsExactly(b.getChunk().getBegin(), b.getChunk().getEnd());
					addPairwiseToCluster(mention, pronoun);
				}
			}
		});

		// TODO: Role appositive - slightly unclear how this is used. I guess its the "The actress
		// Rachel is in the show. The actress plays a single role"
		// Which is an the import anamorphic relation. However in that example "actress" is not
		// found as a NP / Entity in baleen. Perhaps we should create a
		// role annotation and then use that? (effective look for ROLE PERSON to fulfil this rule)

		// Acronym
		// The implement here depends on the acronym generator
		for (int i = 0; i < getMentions().size(); i++) {
			Mention a = getMentions().get(i);
			Set<String> aAcronyms = a.getAcronyms();

			for (int j = i + 1; j < getMentions().size(); j++) {
				Mention b = getMentions().get(j);
				Set<String> bAcronyms = b.getAcronyms();

				if (aAcronyms != null && bAcronyms != null && b.isAcronym() != a.isAcronym()
						&& !Sets.intersection(aAcronyms, bAcronyms).isEmpty()) {
					addToCluster(a, b);
				}

			}
		}

		// Denoymns: Nationality - Country
		// We are fortunate that we have Nationality and Location entities, and we alredy have the
		// existing
		// NationalityToLocation annotator, so this is not required.

	}

	private boolean coversLocation(ParseTreeNode a) {
		return findMentionsUnder(a.getChunk().getBegin(), a.getChunk().getEnd())
				.stream()
				.anyMatch(m -> m.getAnnotation() instanceof Location);
	}

}
